package com.curatix.museumloan.loan;

import com.curatix.museumloan.artifact.Artifact;
import com.curatix.museumloan.artifact.ArtifactRepository;
import com.curatix.museumloan.museum.MuseumRepository;
import com.curatix.museumloan.notification.LoanEvent;
import com.curatix.museumloan.notification.LoanEventPublisher;
import com.curatix.museumloan.notification.LoanEventType;
import com.curatix.museumloan.metrics.LoanMetrics;
import com.curatix.museumloan.user.User;
import com.curatix.museumloan.user.UserRepository;
import com.curatix.museumloan.user.UserRole;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LoanService {

    private static final Logger log = LoggerFactory.getLogger(LoanService.class);

    private final LoanRequestRepository loanRepository;
    private final ArtifactRepository artifactRepository;
    private final MuseumRepository museumRepository;
    private final UserRepository userRepository;
    private final LoanEventPublisher eventPublisher;
    private final LoanMetrics loanMetrics;

    @Transactional(readOnly = true)
    public List<LoanDto> listAll() {
        return loanRepository.findAll().stream().map(LoanDto::from).toList();
    }

    @Transactional(readOnly = true)
    public List<LoanDto> listByStatus(LoanStatus status) {
        return loanRepository.findByStatusOrderByCreatedAtDesc(status).stream().map(LoanDto::from).toList();
    }

    @Transactional(readOnly = true)
    public LoanDto get(Long id) {
        return LoanDto.from(find(id));
    }

    @Transactional
    public LoanDto create(LoanCreateRequest req, Long userId) {
        User u = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (u.getRole() != UserRole.MUSEUM || u.getMuseumId() == null) {
            throw new IllegalArgumentException("Only MUSEUM users with an assigned museum can request loans");
        }
        Artifact a = artifactRepository.findById(req.artifactId())
                .orElseThrow(() -> new IllegalArgumentException("Artifact not found"));

        if (a.getMuseum().getId().equals(u.getMuseumId())) {
            throw new IllegalArgumentException("Cannot request a loan from your own museum");
        }
        if (!"AVAILABLE".equals(a.getStatus())) {
            throw new IllegalArgumentException("Artifact is not available for loan");
        }

        LoanRequest loan = LoanRequest.builder()
                .artifact(a)
                .fromMuseum(a.getMuseum())
                .toMuseum(museumRepository.getReferenceById(u.getMuseumId()))
                .requestedBy(u)
                .status(LoanStatus.PENDING)
                .notes(req.notes())
                .build();
        loanRepository.save(loan);
        loanMetrics.recordLoanCreated();
        publishLoanEvent(LoanEventType.LOAN_CREATED, loan);
        return LoanDto.from(loan);
    }

    @Transactional
    public LoanDto approve(Long loanId, Long reviewerId) {
        LoanRequest loan = find(loanId);
        requireStatus(loan, LoanStatus.PENDING);
        loan.setStatus(LoanStatus.APPROVED);
        Artifact a = loan.getArtifact();
        a.setStatus("RESERVED");
        artifactRepository.save(a);
        LoanRequest saved = loanRepository.save(loan);
        loanMetrics.recordTransition(LoanStatus.APPROVED);
        publishLoanEvent(LoanEventType.LOAN_APPROVED, saved);
        return LoanDto.from(saved);
    }

    @Transactional
    public LoanDto reject(Long loanId, Long reviewerId) {
        LoanRequest loan = find(loanId);
        requireStatus(loan, LoanStatus.PENDING);
        loan.setStatus(LoanStatus.REJECTED);
        LoanRequest saved = loanRepository.save(loan);
        loanMetrics.recordTransition(LoanStatus.REJECTED);
        publishLoanEvent(LoanEventType.LOAN_REJECTED, saved);
        return LoanDto.from(saved);
    }

    @Transactional
    public LoanDto ship(Long loanId, Long userId) {
        LoanRequest loan = find(loanId);
        requireStatus(loan, LoanStatus.APPROVED);
        requireMuseumUser(userId, loan.getFromMuseum().getId(), "Only the lending museum can ship");
        loan.setStatus(LoanStatus.SHIPPED);
        Artifact a = loan.getArtifact();
        a.setStatus("ON_LOAN");
        artifactRepository.save(a);
        LoanRequest saved = loanRepository.save(loan);
        loanMetrics.recordTransition(LoanStatus.SHIPPED);
        publishLoanEvent(LoanEventType.LOAN_SHIPPED, saved);
        return LoanDto.from(saved);
    }

    @Transactional
    public LoanDto returnArtifact(Long loanId, Long userId) {
        LoanRequest loan = find(loanId);
        requireStatus(loan, LoanStatus.SHIPPED);
        requireMuseumUser(userId, loan.getToMuseum().getId(), "Only the borrowing museum can return");
        loan.setStatus(LoanStatus.RETURNED);
        Artifact a = loan.getArtifact();
        a.setStatus("AVAILABLE");
        artifactRepository.save(a);
        LoanRequest saved = loanRepository.save(loan);
        loanMetrics.recordTransition(LoanStatus.RETURNED);
        publishLoanEvent(LoanEventType.LOAN_RETURNED, saved);
        return LoanDto.from(saved);
    }

    private LoanRequest find(Long id) {
        return loanRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Loan request not found: " + id));
    }

    private void requireStatus(LoanRequest loan, LoanStatus expected) {
        if (loan.getStatus() != expected) {
            throw new IllegalArgumentException(
                    "Invalid transition: current status is " + loan.getStatus() + ", expected " + expected);
        }
    }

    private void requireMuseumUser(Long userId, Long expectedMuseumId, String msg) {
        User u = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (u.getRole() != UserRole.MUSEUM || u.getMuseumId() == null
                || !u.getMuseumId().equals(expectedMuseumId)) {
            throw new IllegalArgumentException(msg);
        }
    }

    private void publishLoanEvent(LoanEventType type, LoanRequest loan) {
        try {
            User borrower = loan.getRequestedBy();
            eventPublisher.publish(new LoanEvent(
                    type,
                    loan.getId(),
                    loan.getArtifact().getName(),
                    loan.getFromMuseum().getName(),
                    loan.getToMuseum().getName(),
                    borrower.getEmail(),
                    borrower.getFullName()
            ));
        } catch (Exception ex) {
            log.warn("Failed to publish loan event {} for #{}: {}", type, loan.getId(), ex.getMessage());
        }
    }
}