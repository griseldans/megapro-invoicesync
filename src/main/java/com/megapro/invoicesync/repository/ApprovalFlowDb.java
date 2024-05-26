package com.megapro.invoicesync.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.megapro.invoicesync.model.ApprovalFlow;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ApprovalFlowDb extends JpaRepository<ApprovalFlow, Integer> {
    @Query("SELECT af FROM ApprovalFlow af WHERE af.nominalRange <= :amount ORDER BY af.nominalRange DESC")
    Optional<ApprovalFlow> findTopByNominalRangeLessThanEqualOrderByNominalRangeDesc(BigDecimal amount);
    List<ApprovalFlow> findAllByOrderByNominalRangeDesc();
    List<ApprovalFlow> findAllByOrderByNominalRangeAsc();
    List<ApprovalFlow> findByOrderByNominalRangeAsc();


}

