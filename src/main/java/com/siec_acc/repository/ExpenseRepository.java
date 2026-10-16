package com.siec_acc.repository;

import com.siec_acc.entity.ExpenseEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExpenseRepository extends JpaRepository<ExpenseEntity, Long> {

    Optional<ExpenseEntity> findByExpenseStrId(String expenseStrId);

    boolean existsByExpenseStrId(String expenseStrId);

    @Query("SELECT COALESCE(MAX(e.expensePrimeId), 0) FROM ExpenseEntity e")
    Long findMaxPrimeId();

    @Query("""
        SELECT e FROM ExpenseEntity e
        WHERE (:status    IS NULL OR e.expenseStatus    = :status)
          AND (:category  IS NULL OR e.expenseCategory  = :category)
          AND (:projectId IS NULL OR e.expenseProjectId = :projectId)
          AND (:dateFrom  IS NULL OR e.expenseDate     >= :dateFrom)
          AND (:dateTo    IS NULL OR e.expenseDate     <= :dateTo)
          AND (:q         IS NULL
               OR LOWER(e.expenseDescription) LIKE LOWER(CONCAT('%', :q, '%'))
               OR LOWER(e.expenseCategory)    LIKE LOWER(CONCAT('%', :q, '%'))
               OR LOWER(e.expenseVendor)      LIKE LOWER(CONCAT('%', :q, '%')))
    """)
    Page<ExpenseEntity> search(
            @Param("status")    String status,
            @Param("category")  String category,
            @Param("projectId") String projectId,
            @Param("dateFrom")  LocalDate dateFrom,
            @Param("dateTo")    LocalDate dateTo,
            @Param("q")         String q,
            Pageable pageable
    );

    @Query("""
        SELECT
          COUNT(e),
          COALESCE(SUM(e.expenseAmount), 0),
          SUM(CASE WHEN e.expenseStatus = 'pending'  THEN 1 ELSE 0 END),
          SUM(CASE WHEN e.expenseStatus = 'approved' THEN 1 ELSE 0 END),
          SUM(CASE WHEN e.expenseStatus = 'rejected' THEN 1 ELSE 0 END)
        FROM ExpenseEntity e
        WHERE (:status    IS NULL OR e.expenseStatus    = :status)
          AND (:category  IS NULL OR e.expenseCategory  = :category)
          AND (:projectId IS NULL OR e.expenseProjectId = :projectId)
          AND (:dateFrom  IS NULL OR e.expenseDate     >= :dateFrom)
          AND (:dateTo    IS NULL OR e.expenseDate     <= :dateTo)
          AND (:q         IS NULL
               OR LOWER(e.expenseDescription) LIKE LOWER(CONCAT('%', :q, '%'))
               OR LOWER(e.expenseCategory)    LIKE LOWER(CONCAT('%', :q, '%'))
               OR LOWER(e.expenseVendor)      LIKE LOWER(CONCAT('%', :q, '%')))
    """)
    Object[] aggregate(
            @Param("status")    String status,
            @Param("category")  String category,
            @Param("projectId") String projectId,
            @Param("dateFrom")  LocalDate dateFrom,
            @Param("dateTo")    LocalDate dateTo,
            @Param("q")         String q
    );

    // Used by the Projects module (expenseProjectId holds the project's projectStrId).
    List<ExpenseEntity> findByExpenseProjectIdOrderByExpenseDateDesc(String expenseProjectId);

    boolean existsByExpenseProjectId(String expenseProjectId);

}