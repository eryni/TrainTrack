package com.traintrack.app.model;

import jakarta.persistence.*;

@Entity
@Table(name = "ridership_data")
public class RidershipData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String year;
    private String month;
    private String time;

    private Integer entryCount;
    private Integer exitCount;

    @ManyToOne
    @JoinColumn(name = "station_id", nullable = false)
    private Station station;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getYear() { return year; }
    public void setYear(String year) { this.year = year; }

    public String getMonth() { return month; }
    public void setMonth(String month) { this.month = month; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public Integer getEntryCount() { return entryCount; }
    public void setEntryCount(Integer entryCount) { this.entryCount = entryCount; }

    public Integer getExitCount() { return exitCount; }
    public void setExitCount(Integer exitCount) { this.exitCount = exitCount; }

    public Station getStation() { return station; }
    public void setStation(Station station) { this.station = station; }
}
