package com.curatix.museumloan.loan;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LoanRequestRepository extends JpaRepository<LoanRequest, Long> {
    List<LoanRequest> findByStatusOrderByCreatedAtDesc(LoanStatus status);
    List<LoanRequest> findByRequestedByIdOrderByCreatedAtDesc(Long userId);
}