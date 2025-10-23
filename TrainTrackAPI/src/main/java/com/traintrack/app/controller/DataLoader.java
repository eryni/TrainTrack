package com.traintrack.app.controller;

import com.traintrack.app.service.ExcelImportService;
import com.traintrack.app.service.StationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataLoader implements CommandLineRunner {

    @Autowired
    private StationService stationService;

    @Autowired
    private ExcelImportService excelImportService;

    @Override
    public void run(String... args) {
        stationService.preloadStations();
        excelImportService.importData();
    }
}
