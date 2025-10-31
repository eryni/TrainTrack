package com.traintrack.app.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "saved_schedule")
public class SavedSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String congestionLevel;
    private double predictedRidership;
    private double confidence;
    private String timestamp;

    @ManyToOne
    @JoinColumn(name = "station_id", nullable = false)
    private Station station;

    // (Optional) link to a user if you have authentication
    private Long userId;

    public SavedSchedule() {}

    public SavedSchedule(String congestionLevel, double predictedRidership, double confidence, String timestamp, Station station, Long userId) {
        this.congestionLevel = congestionLevel;
        this.predictedRidership = predictedRidership;
        this.confidence = confidence;
        this.timestamp = timestamp;
        this.station = station;
        this.userId = userId;
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCongestionLevel() { return congestionLevel; }
    public void setCongestionLevel(String congestionLevel) { this.congestionLevel = congestionLevel; }

    public double getPredictedRidership() { return predictedRidership; }
    public void setPredictedRidership(double predictedRidership) { this.predictedRidership = predictedRidership; }

    public double getConfidence() { return confidence; }
    public void setConfidence(double confidence) { this.confidence = confidence; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public Station getStation() { return station; }
    public void setStation(Station station) { this.station = station; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
}
