package com.traintrack.app.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/powerbi")
public class PowerBIConfigController {

    @Value("${powerbi.historical.trends.url:}")
    private String historicalTrendsUrl;

    @Value("${powerbi.peak.hours.url:}")
    private String peakHoursUrl;

    @Value("${powerbi.station.comparison.url:}")
    private String stationComparisonUrl;

    @GetMapping("/config")
    public Map<String, String> getPowerBIConfig() {
        Map<String, String> config = new HashMap<>();
        config.put("historicalTrendsUrl", historicalTrendsUrl);
        config.put("peakHoursUrl", peakHoursUrl);
        config.put("stationComparisonUrl", stationComparisonUrl);
        return config;
    }
}