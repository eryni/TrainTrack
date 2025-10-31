package com.traintrack.app.controller;

import com.traintrack.app.model.ReportedIssue;
import com.traintrack.app.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/reports")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @PostMapping
    public ResponseEntity<Map<String, Object>> submitReport(@RequestBody ReportedIssue report) {
        Map<String, Object> response = new HashMap<>();

        try {
            if (report.getTitle() == null || report.getTitle().trim().isEmpty()) {
                response.put("error", "Title is required");
                return ResponseEntity.badRequest().body(response);
            }

            if (report.getDescription() == null || report.getDescription().trim().isEmpty()) {
                response.put("error", "Description is required");
                return ResponseEntity.badRequest().body(response);
            }

            if (report.getUserId() == null) {
                response.put("error", "User ID is required");
                return ResponseEntity.badRequest().body(response);
            }

            ReportedIssue savedReport = reportService.createReport(report);
            response.put("message", "Report submitted successfully");
            response.put("report", savedReport);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("Failed to submit report: " + e.getMessage());
            e.printStackTrace();
            response.put("error", "Failed to submit report: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ReportedIssue>> getUserReports(@PathVariable Long userId) {
        return ResponseEntity.ok(reportService.getReportsByUserId(userId));
    }

    @GetMapping
    public ResponseEntity<List<ReportedIssue>> getAllReports() {
        return ResponseEntity.ok(reportService.getAllReports());
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Map<String, Object>> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();

        try {
            String status = request.get("status");
            ReportedIssue updated = reportService.updateReportStatus(id, status);
            response.put("message", "Status updated successfully");
            response.put("report", updated);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", "Failed to update status: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}