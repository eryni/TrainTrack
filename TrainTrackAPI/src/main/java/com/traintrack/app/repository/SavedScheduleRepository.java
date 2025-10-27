package com.traintrack.app.repository;

import com.traintrack.app.model.SavedSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SavedScheduleRepository extends JpaRepository<SavedSchedule, Long> {
    List<SavedSchedule> findByUserId(Long userId);
}
