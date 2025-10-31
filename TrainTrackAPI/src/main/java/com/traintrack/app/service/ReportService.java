package com.traintrack.app.service;

import com.traintrack.app.model.ReportedIssue;
import com.traintrack.app.repository.ReportedIssueRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ReportService {

    @Autowired
    private ReportedIssueRepository reportRepository;

    public ReportedIssue createReport(ReportedIssue report) {
        return reportRepository.save(report);
    }

    public List<ReportedIssue> getReportsByUserId(Long userId) {
        return reportRepository.findByUserId(userId);
    }

    public List<ReportedIssue> getAllReports() {
        return reportRepository.findAll();
    }

    public Optional<ReportedIssue> getReportById(Long id) {
        return reportRepository.findById(id);
    }

    public ReportedIssue updateReportStatus(Long id, String status) {
        ReportedIssue report = reportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Report not found"));
        report.setStatus(status);
        return reportRepository.save(report);
    }
}