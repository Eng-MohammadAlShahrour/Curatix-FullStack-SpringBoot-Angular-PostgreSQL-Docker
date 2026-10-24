package com.curatix.museumloan.loan;

import com.curatix.museumloan.artifact.Artifact;
import com.curatix.museumloan.artifact.ArtifactRepository;
import com.curatix.museumloan.museum.Museum;
import com.curatix.museumloan.museum.MuseumRepository;
import com.curatix.museumloan.user.User;
import com.curatix.museumloan.user.UserRepository;
import com.curatix.museumloan.user.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class LoanServiceTest {

    @Autowired LoanService loanService;
    @Autowired LoanRequestRepository loanRepository;
    @Autowired ArtifactRepository artifactRepository;
    @Autowired MuseumRepository museumRepository;
    @Autowired UserRepository userRepository;

    private User lender;
    private User borrower;
    private User reviewer;
    private Museum lenderMuseum;
    private Museum borrowerMuseum;
    private Artifact artifact;

    @BeforeEach
    void setup() {
        lenderMuseum = museumRepository.save(
            Museum.builder().name("Louvre").country("France").city("Paris").build());
        borrowerMuseum = museumRepository.save(
            Museum.builder().name("British").country("UK").city("London").build());

        lender = userRepository.save(User.builder()
            .email("lender@test.com").passwordHash("x").fullName("L")
            .role(UserRole.MUSEUM).museumId(lenderMuseum.getId()).build());
        borrower = userRepository.save(User.builder()
            .email("borrower@test.com").passwordHash("x").fullName("B")
            .role(UserRole.MUSEUM).museumId(borrowerMuseum.getId()).build());
        reviewer = userRepository.save(User.builder()
            .email("reviewer@test.com").passwordHash("x").fullName("R")
            .role(UserRole.REVIEWER).build());

        artifact = artifactRepository.save(Artifact.builder()
            .museum(lenderMuseum).name("Mona Lisa").status("AVAILABLE").build());
    }

    @Test
    void create_setsPendingStatus() {
        var loan = loanService.create(new LoanCreateRequest(artifact.getId(), "notes"), borrower.getId());
        assertEquals("PENDING", loan.status());
        assertEquals("Mona Lisa", loan.artifactName());
        assertEquals("Louvre", loan.fromMuseumName());
        assertEquals("British", loan.toMuseumName());
    }

    @Test
    void create_throwsIfSameMuseumRequests() {
        assertThrows(IllegalArgumentException.class, () ->
            loanService.create(new LoanCreateRequest(artifact.getId(), null), lender.getId())
        );
    }

    @Test
    void create_throwsIfArtifactNotAvailable() {
        artifact.setStatus("ON_LOAN");
        artifactRepository.save(artifact);
        assertThrows(IllegalArgumentException.class, () ->
            loanService.create(new LoanCreateRequest(artifact.getId(), null), borrower.getId())
        );
    }

    @Test
    void approve_changesToApprovedAndReservesArtifact() {
        var loan = loanService.create(new LoanCreateRequest(artifact.getId(), null), borrower.getId());
        var approved = loanService.approve(loan.id(), reviewer.getId());
        assertEquals("APPROVED", approved.status());
        assertEquals("RESERVED", artifactRepository.findById(artifact.getId()).orElseThrow().getStatus());
    }

    @Test
    void approve_throwsIfNotPending() {
        var loan = loanService.create(new LoanCreateRequest(artifact.getId(), null), borrower.getId());
        loanService.approve(loan.id(), reviewer.getId());
        assertThrows(IllegalArgumentException.class, () ->
            loanService.approve(loan.id(), reviewer.getId())
        );
    }

    @Test
    void ship_changesToShippedAndSetsOnLoan() {
        var loan = loanService.create(new LoanCreateRequest(artifact.getId(), null), borrower.getId());
        loanService.approve(loan.id(), reviewer.getId());
        var shipped = loanService.ship(loan.id(), lender.getId());
        assertEquals("SHIPPED", shipped.status());
        assertEquals("ON_LOAN", artifactRepository.findById(artifact.getId()).orElseThrow().getStatus());
    }

    @Test
    void ship_throwsIfNotApproved() {
        var loan = loanService.create(new LoanCreateRequest(artifact.getId(), null), borrower.getId());
        assertThrows(IllegalArgumentException.class, () ->
            loanService.ship(loan.id(), lender.getId())
        );
    }

    @Test
    void ship_throwsIfNotLenderMuseum() {
        var loan = loanService.create(new LoanCreateRequest(artifact.getId(), null), borrower.getId());
        loanService.approve(loan.id(), reviewer.getId());
        assertThrows(IllegalArgumentException.class, () ->
            loanService.ship(loan.id(), borrower.getId())
        );
    }

    @Test
    void returnArtifact_changesToReturnedAndSetsAvailable() {
        var loan = loanService.create(new LoanCreateRequest(artifact.getId(), null), borrower.getId());
        loanService.approve(loan.id(), reviewer.getId());
        loanService.ship(loan.id(), lender.getId());
        var returned = loanService.returnArtifact(loan.id(), borrower.getId());
        assertEquals("RETURNED", returned.status());
        assertEquals("AVAILABLE", artifactRepository.findById(artifact.getId()).orElseThrow().getStatus());
    }

    @Test
    void returnArtifact_throwsIfNotShipped() {
        var loan = loanService.create(new LoanCreateRequest(artifact.getId(), null), borrower.getId());
        loanService.approve(loan.id(), reviewer.getId());
        assertThrows(IllegalArgumentException.class, () ->
            loanService.returnArtifact(loan.id(), borrower.getId())
        );
    }

    @Test
    void returnArtifact_throwsIfNotBorrower() {
        var loan = loanService.create(new LoanCreateRequest(artifact.getId(), null), borrower.getId());
        loanService.approve(loan.id(), reviewer.getId());
        loanService.ship(loan.id(), lender.getId());
        assertThrows(IllegalArgumentException.class, () ->
            loanService.returnArtifact(loan.id(), lender.getId())
        );
    }

    @Test
    void reject_changesToRejected() {
        var loan = loanService.create(new LoanCreateRequest(artifact.getId(), null), borrower.getId());
        var rejected = loanService.reject(loan.id(), reviewer.getId());
        assertEquals("REJECTED", rejected.status());
    }

    @Test
    void reject_throwsIfNotPending() {
        var loan = loanService.create(new LoanCreateRequest(artifact.getId(), null), borrower.getId());
        loanService.approve(loan.id(), reviewer.getId());
        assertThrows(IllegalArgumentException.class, () ->
            loanService.reject(loan.id(), reviewer.getId())
        );
    }
}