package com.traintrack.app.repository;

import com.traintrack.app.model.ReportedIssue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportedIssueRepository extends JpaRepository<ReportedIssue, Long> {
    List<ReportedIssue> findByUserId(Long userId);
    List<ReportedIssue> findByStatus(String status);
}