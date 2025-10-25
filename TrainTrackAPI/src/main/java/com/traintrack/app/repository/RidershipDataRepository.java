package com.traintrack.app.repository;

import com.traintrack.app.model.RidershipData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RidershipDataRepository extends JpaRepository<RidershipData, Long> {

    // ✅ Correct way to access a field of a related entity
    List<RidershipData> findByStation_Id(Long stationId);

    List<RidershipData> findByStation_IdAndYear(Long stationId, String year);

    List<RidershipData> findByStation_IdAndMonth(Long stationId, String month);
}
