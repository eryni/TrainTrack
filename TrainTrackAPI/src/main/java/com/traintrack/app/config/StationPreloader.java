package com.traintrack.app.config;

import com.traintrack.app.service.StationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class StationPreloader implements CommandLineRunner {

    @Autowired
    private StationService stationService;

    @Override
    public void run(String... args) {
        stationService.preloadStations();
    }
}
