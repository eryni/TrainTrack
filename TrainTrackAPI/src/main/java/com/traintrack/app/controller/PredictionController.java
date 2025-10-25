package com.traintrack.app.controller;

import com.traintrack.app.service.PredictionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@CrossOrigin(origins = "http://localhost:4200") // ✅ Allow Angular dev server
@RestController
@RequestMapping("/api")
public class PredictionController {

    @Autowired
    private PredictionService predictionService;

    @GetMapping("/predict")
    public Map<String, Object> predict(
            @RequestParam Long stationId,
            @RequestParam(defaultValue = "15") int minutesAhead
    ) {
        return predictionService.predictCongestion(stationId, minutesAhead);
    }
}
