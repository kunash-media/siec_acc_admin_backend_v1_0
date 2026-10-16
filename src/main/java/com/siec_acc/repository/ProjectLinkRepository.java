package com.siec_acc.repository;

import com.siec_acc.entity.ProjectLinkEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectLinkRepository extends JpaRepository<ProjectLinkEntity, Long> {

    List<ProjectLinkEntity> findByProjectStrIdAndLinkType(String projectStrId, String linkType);

    Optional<ProjectLinkEntity> findByLinkTypeAndRefStrId(String linkType, String refStrId);

    boolean existsByProjectStrId(String projectStrId);

    long deleteByProjectStrIdAndLinkTypeAndRefStrId(String projectStrId, String linkType, String refStrId);
}