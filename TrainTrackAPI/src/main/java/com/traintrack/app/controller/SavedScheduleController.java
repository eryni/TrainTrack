package com.traintrack.app.controller;

import com.traintrack.app.model.SavedSchedule;
import com.traintrack.app.model.Station;
import com.traintrack.app.repository.SavedScheduleRepository;
import com.traintrack.app.repository.StationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/schedules")
@CrossOrigin(origins = "http://localhost:4200")
public class SavedScheduleController {

    @Autowired
    private SavedScheduleRepository savedScheduleRepository;

    @Autowired
    private StationRepository stationRepository;

    @PostMapping
    public SavedSchedule saveSchedule(@RequestBody SavedSchedule schedule) {
        Station station = stationRepository.findById(schedule.getStation().getId())
                .orElseThrow(() -> new RuntimeException("Station not found"));
        schedule.setStation(station);
        return savedScheduleRepository.save(schedule);
    }

    @GetMapping("/user/{userId}")
    public List<SavedSchedule> getUserSchedules(@PathVariable Long userId) {
        return savedScheduleRepository.findByUserId(userId);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSchedule(@PathVariable Long id) {
        if (!savedScheduleRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        savedScheduleRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}