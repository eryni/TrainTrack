package com.traintrack.app.service;

import com.traintrack.app.model.Station;
import com.traintrack.app.repository.StationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class StationService {

    @Autowired
    private StationRepository stationRepository;

    private final List<String> STATION_NAMES = Arrays.asList(
            "North Ave", "Quezon Ave", "GMA Kamuning", "Cubao", "Santolan",
            "Ortigas", "Shaw Blvd", "Boni Ave", "Guadalupe", "Buendia",
            "Ayala Ave", "Magallanes", "Taft"
    );

    public void preloadStations() {
        if (stationRepository.count() == 0) {
            for (String name : STATION_NAMES) {
                Station station = new Station(name);
                stationRepository.save(station);
            }
            System.out.println("✅ Stations preloaded successfully!");
        } else {
            System.out.println("ℹ️ Stations already exist — skipping preload.");
        }
    }
}
