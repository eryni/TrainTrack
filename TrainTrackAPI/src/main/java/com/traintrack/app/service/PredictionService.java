package com.traintrack.app.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class PredictionService {

    @Autowired
    private AIPredictionService aiPredictionService;

    public Map<String, Object> predictCongestion(Long stationId, int minutesAhead) {
        return aiPredictionService.predict(stationId, minutesAhead);
    }
}