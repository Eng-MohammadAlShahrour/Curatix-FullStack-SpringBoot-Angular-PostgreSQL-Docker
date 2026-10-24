package com.curatix.museumloan.loan;

import com.curatix.museumloan.artifact.Artifact;
import com.curatix.museumloan.artifact.ArtifactRepository;
import com.curatix.museumloan.museum.MuseumRepository;
import com.curatix.museumloan.user.User;
import com.curatix.museumloan.user.UserRepository;
import com.curatix.museumloan.user.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LoanService {

    private final LoanRequestRepository loanRepository;
    private final ArtifactRepository artifactRepository;
    private final MuseumRepository museumRepository;
    private final UserRepository userRepository;

    public List<LoanDto> listAll() {
        return loanRepository.findAll().stream().map(LoanDto::from).toList();
    }

    public List<LoanDto> listByStatus(LoanStatus status) {
        return loanRepository.findByStatusOrderByCreatedAtDesc(status).stream().map(LoanDto::from).toList();
    }

    public LoanDto get(Long id) {
        return LoanDto.from(find(id));
    }

    // ---- State machine ----

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
        return LoanDto.from(loan);
    }

    @Transactional
    public LoanDto approve(Long loanId, Long reviewerId) {
        LoanRequest loan = find(loanId);
        requireStatus(loan, LoanStatus.PENDING);
        loan.setStatus(LoanStatus.APPROVED);
        // Mark artifact as RESERVED
        Artifact a = loan.getArtifact();
        a.setStatus("RESERVED");
        artifactRepository.save(a);
        return LoanDto.from(loanRepository.save(loan));
    }

    @Transactional
    public LoanDto reject(Long loanId, Long reviewerId) {
        LoanRequest loan = find(loanId);
        requireStatus(loan, LoanStatus.PENDING);
        loan.setStatus(LoanStatus.REJECTED);
        return LoanDto.from(loanRepository.save(loan));
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
        return LoanDto.from(loanRepository.save(loan));
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
        return LoanDto.from(loanRepository.save(loan));
    }

    // ---- Helpers ----

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
}