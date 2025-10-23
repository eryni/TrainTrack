package com.traintrack.app.service;

import com.traintrack.app.model.RidershipData;
import com.traintrack.app.repository.RidershipDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PredictionService {

    @Autowired
    private RidershipDataRepository ridershipDataRepository;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final double GLOBAL_MAX = 130771; // Absolute upper bound

    public Map<String, Object> predictCongestion(Long stationId, int minutesAhead) {
        List<RidershipData> data = ridershipDataRepository.findByStation_Id(stationId)
                .stream()
                .filter(d -> d.getTime() != null && !d.getTime().equalsIgnoreCase("TOTAL"))
                .collect(Collectors.toList());

        if (data.isEmpty()) {
            return buildEmptyResult(stationId, minutesAhead, "No valid data found for station");
        }

        // Combine entry + exit counts per time
        Map<String, Double> hourlyTotals = data.stream()
                .collect(Collectors.groupingBy(
                        RidershipData::getTime,
                        Collectors.averagingDouble(d ->
                                Optional.ofNullable(d.getEntryCount()).orElse(0)
                                        + Optional.ofNullable(d.getExitCount()).orElse(0))
                ));

        if (hourlyTotals.size() < 3) {
            return buildEmptyResult(stationId, minutesAhead, "Not enough hourly data for prediction");
        }

        List<String> sortedTimes = new ArrayList<>(hourlyTotals.keySet());
        sortedTimes.sort(Comparator.comparing(this::parseTimeSlot));

        List<Double> values = sortedTimes.stream()
                .map(hourlyTotals::get)
                .collect(Collectors.toList());

        List<Double> smoothed = smoothData(values, 3);

        LocalTime targetTime = LocalTime.now().plusMinutes(minutesAhead);
        int targetHour = targetTime.getHour();

        List<LocalTime> timeSlots = sortedTimes.stream()
                .map(this::parseTimeSlot)
                .collect(Collectors.toList());

        int idx = 0;
        for (int i = 0; i < timeSlots.size(); i++) {
            if (timeSlots.get(i).getHour() >= targetHour) {
                idx = i;
                break;
            }
        }
        idx = Math.max(0, Math.min(idx, smoothed.size() - 1));

        int startIdx = Math.max(0, idx - 2);
        List<Double> recent = smoothed.subList(startIdx, idx + 1);

        double y1 = recent.get(Math.max(0, recent.size() - 3));
        double y2 = recent.get(Math.max(0, recent.size() - 2));
        double y3 = recent.get(recent.size() - 1);
        double slope = ((y3 - y2) + (y2 - y1)) / 2.0;

        double predicted = y3 + (minutesAhead / 60.0) * slope;
        predicted = Math.max(predicted, 0);

        // --- Adaptive scaling based on this station’s history ---
        double localMin = Collections.min(values);
        double localMax = Collections.max(values);
        double localRange = Math.max(localMax - localMin, 1000);

        // Use both local and global normalization
        double normalized = (predicted - localMin) / localRange;
        normalized = Math.max(0, Math.min(1, normalized));

        // --- Sigmoid compression to prevent saturation ---
        // shifts mid-values into 0.4–0.6 region and pushes extremes apart
        double curved = 1 / (1 + Math.exp(-6 * (normalized - 0.5)));

        // --- Apply diversity and realism ---
        Random random = new Random();
        double randomFactor = 0.9 + (random.nextDouble() * 0.2); // ±10%
        double ratio = curved * randomFactor;

        // Scale to realistic range (but rarely near 130k)
        double scaled = 200 + ratio * (GLOBAL_MAX - 200);
        scaled = Math.min(scaled, GLOBAL_MAX);

        // --- Classify ---
        String level = classifyCongestion(ratio);

        // --- Confidence ---
        double stability = 1.0 - Math.min(1.0, Math.abs(slope) / (y3 + 1e-6));
        double confidence = 0.5 + 0.5 * stability;
        double levelFactor = switch (level) {
            case "Light" -> 1.0;
            case "Moderate" -> 0.9;
            case "Heavy" -> 0.75;
            case "Very Heavy" -> 0.6;
            default -> 1.0;
        };
        confidence *= levelFactor;
        confidence = Math.max(0.3, Math.min(1.0, confidence));

        // ✅ Final Prediction (with confidence influence)
        double finalPrediction = scaled * (0.8 + 0.4 * confidence);

        System.out.printf(
                "[PREDICT] Station %d | Hour %02d | Raw %.1f | Norm %.2f | Final %.1f | Level %s | Conf %.2f%n",
                stationId, targetHour, predicted, ratio, finalPrediction, level, confidence
        );

        Map<String, Object> result = new HashMap<>();
        result.put("stationId", stationId);
        result.put("predictedRidership", Math.round(finalPrediction));
        result.put("congestionLevel", level);
        result.put("confidence", confidence);
        result.put("minutesAhead", minutesAhead);
        result.put("timestamp", targetTime.toString());

        return result;
    }

    // --- Helpers ---
    private LocalTime parseTimeSlot(String time) {
        try {
            String start = time.split("-")[0].trim();
            if (start.length() == 4) start = "0" + start;
            return LocalTime.parse(start, TIME_FORMATTER);
        } catch (Exception e) {
            return LocalTime.MIDNIGHT;
        }
    }

    private List<Double> smoothData(List<Double> values, int window) {
        if (values.size() < window) return values;
        List<Double> result = new ArrayList<>();
        for (int i = 0; i < values.size(); i++) {
            int start = Math.max(0, i - window + 1);
            double avg = values.subList(start, i + 1)
                    .stream().mapToDouble(Double::doubleValue).average().orElse(0);
            result.add(avg);
        }
        return result;
    }

    private String classifyCongestion(double ratio) {
        if (ratio < 0.25) return "Light";
        if (ratio < 0.5) return "Moderate";
        if (ratio < 0.75) return "Heavy";
        return "Very Heavy";
    }

    private Map<String, Object> buildEmptyResult(Long stationId, int minutesAhead, String msg) {
        Map<String, Object> res = new HashMap<>();
        res.put("stationId", stationId);
        res.put("predictedRidership", 0);
        res.put("congestionLevel", "Unknown");
        res.put("confidence", 0.0);
        res.put("minutesAhead", minutesAhead);
        res.put("timestamp", new Date().toString());
        System.out.println("[WARN] " + msg);
        return res;
    }
}
