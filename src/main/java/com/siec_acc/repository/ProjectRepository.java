package com.siec_acc.repository;

import com.siec_acc.dto.response.ProjectSummaryResponseDto;
import com.siec_acc.entity.ProjectEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<ProjectEntity, Long>, JpaSpecificationExecutor<ProjectEntity> {

    Optional<ProjectEntity> findByProjectStrId(String projectStrId);

    // Highest existing code for a prefix like "26LW" - used to generate the next project code.
    Optional<ProjectEntity> findTopByProjectCodeStartingWithOrderByProjectCodeDesc(String prefix);

    /** Use this in ClientServiceImpl.deleteClient to block deleting a client that has projects. */
    long countByClientStrId(String clientStrId);


    @Query(value = "select new com.siec_acc.dto.response.ProjectSummaryResponseDto(" +
            "p.projectPrimeId, p.projectStrId, p.projectCode, p.projectName, p.status) " +
            "from ProjectEntity p where " +
            "(:status is null or p.status = :status) and " +
            "(:search is null or lower(p.projectStrId) like lower(concat('%', :search, '%')) " +
            "  or lower(p.projectCode) like lower(concat('%', :search, '%')) " +
            "  or lower(p.projectName) like lower(concat('%', :search, '%'))) " +
            "order by p.projectPrimeId desc",
            countQuery = "select count(p) from ProjectEntity p where " +
                    "(:status is null or p.status = :status) and " +
                    "(:search is null or lower(p.projectStrId) like lower(concat('%', :search, '%')) " +
                    "  or lower(p.projectCode) like lower(concat('%', :search, '%')) " +
                    "  or lower(p.projectName) like lower(concat('%', :search, '%')))")
    Page<ProjectSummaryResponseDto> findProjectSummaries(@Param("status") String status,
                                                         @Param("search") String search,
                                                         Pageable pageable);
}