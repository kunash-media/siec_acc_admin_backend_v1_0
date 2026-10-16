package com.siec_acc.repository;

import com.siec_acc.dto.response.ProjectDocumentDto;
import com.siec_acc.entity.ProjectDocumentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectDocumentRepository extends JpaRepository<ProjectDocumentEntity, Long> {

    Optional<ProjectDocumentEntity> findByPdocStrId(String pdocStrId);

    // Projection - does NOT load the file blob.
    @Query("select new com.siec_acc.dto.response.ProjectDocumentDto(d.pdocStrId, d.documentType, d.fileName, d.fileSize, d.uploadedBy, d.createdAt) " +
            "from ProjectDocumentEntity d where d.projectStrId = :pid order by d.createdAt desc")
    List<ProjectDocumentDto> findListByProject(@Param("pid") String projectStrId);

    void deleteByProjectStrId(String projectStrId);
}