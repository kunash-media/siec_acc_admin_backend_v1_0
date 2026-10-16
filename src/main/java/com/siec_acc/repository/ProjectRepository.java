package com.siec_acc.repository;

import com.siec_acc.entity.ProjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<ProjectEntity, Long>, JpaSpecificationExecutor<ProjectEntity> {

    Optional<ProjectEntity> findByProjectStrId(String projectStrId);

    // Highest existing code for a prefix like "26LW" - used to generate the next project code.
    Optional<ProjectEntity> findTopByProjectCodeStartingWithOrderByProjectCodeDesc(String prefix);

    /** Use this in ClientServiceImpl.deleteClient to block deleting a client that has projects. */
    long countByClientStrId(String clientStrId);
}