package com.mess.app.repository;

import com.mess.app.entity.Candidate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CandidateRepository extends JpaRepository<Candidate, String> {  // ✅ String

    Optional<Candidate> findByCandidateId(String candidateId);

    List<Candidate> findByStatus(Candidate.Status status);

    List<Candidate> findByStatusOrderByCandidateIdAsc(Candidate.Status status);

    @Query("SELECT c FROM Candidate c ORDER BY c.candidateId DESC LIMIT 1")
    Optional<Candidate> findTopByOrderByCandidateIdDesc();

    long countByStatus(Candidate.Status status);

    boolean existsByCandidateId(String candidateId);

    @Query("SELECT EXTRACT(MONTH FROM c.joiningDate), COUNT(c) FROM Candidate c " +
            "WHERE EXTRACT(YEAR FROM c.joiningDate) = :year " +
            "GROUP BY EXTRACT(MONTH FROM c.joiningDate) ORDER BY EXTRACT(MONTH FROM c.joiningDate)")
    List<Object[]> getMonthlyJoining(@Param("year") int year);

    @Query("SELECT EXTRACT(MONTH FROM c.leavingDate), COUNT(c) FROM Candidate c " +
            "WHERE EXTRACT(YEAR FROM c.leavingDate) = :year " +
            "AND c.leavingDate IS NOT NULL " +
            "GROUP BY EXTRACT(MONTH FROM c.leavingDate) ORDER BY EXTRACT(MONTH FROM c.leavingDate)")
    List<Object[]> getMonthlyLeaving(@Param("year") int year);

    List<Candidate> findByJoiningDateBetween(LocalDate startDate, LocalDate endDate);

    List<Candidate> findByLeavingDateBetween(LocalDate startDate, LocalDate endDate);

    /** Expected mess fees for a month: sum of monthlyRate of ACTIVE candidates. */
    @Query("SELECT SUM(c.monthlyRate) FROM Candidate c WHERE c.status = 'ACTIVE'")
    java.math.BigDecimal getTotalActiveMonthlyRate();
}
