package com.traintrack.app.service;

import com.traintrack.app.model.RidershipData;
import com.traintrack.app.repository.RidershipDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class AIPredictionService {

    @Autowired
    private RidershipDataRepository ridershipDataRepository;

    private static final Pattern TIME_PATTERN = Pattern.compile("^(\\d{1,2})");
    private static final ZoneId MANILA_ZONE = ZoneId.of("Asia/Manila");

    public Map<String, Object> predict(Long stationId, int minutesAhead) {
        List<RidershipData> data = ridershipDataRepository.findByStation_Id(stationId);

        if (data.isEmpty()) {
            return Map.of(
                    "stationId", stationId,
                    "predictedRidership", 0,
                    "congestionLevel", "Unknown",
                    "confidence", 0.0,
                    "timestamp", LocalTime.now(MANILA_ZONE).toString(),
                    "lowDensityPeriods", Collections.emptyList()
            );
        }

        Map<Integer, Double> hourlyTotals = data.stream()
                .map(d -> Map.entry(extractHour(d.getTime()),
                        Optional.ofNullable(d.getEntryCount()).orElse(0) +
                                Optional.ofNullable(d.getExitCount()).orElse(0)))
                .filter(e -> e.getKey() >= 0 && e.getKey() <= 23)
                .collect(Collectors.groupingBy(
                        Map.Entry::getKey,
                        Collectors.averagingDouble(Map.Entry::getValue)
                ));

        hourlyTotals.remove(-1);
        if (hourlyTotals.isEmpty()) {
            return Map.of(
                    "stationId", stationId,
                    "predictedRidership", 0,
                    "congestionLevel", "Unknown",
                    "confidence", 0.0,
                    "timestamp", LocalTime.now(MANILA_ZONE).toString(),
                    "lowDensityPeriods", Collections.emptyList()
            );
        }

        System.out.println("[DEBUG] Station " + stationId + " hour keys: " + hourlyTotals.keySet());

        ZonedDateTime nowManila = ZonedDateTime.now(MANILA_ZONE).plusMinutes(minutesAhead);
        int predictedHour = nowManila.getHour();

        int lookupHour = hourlyTotals.containsKey(predictedHour)
                ? predictedHour
                : hourlyTotals.keySet().stream()
                .filter(h -> h <= predictedHour)
                .max(Integer::compare)
                .orElse(hourlyTotals.keySet().stream().min(Integer::compare).orElse(0));

        System.out.println("[DEBUG] Selected hour key: " + lookupHour);

        double base = hourlyTotals.getOrDefault(lookupHour, 0.0);
        double localMax = hourlyTotals.values().stream()
                .mapToDouble(Double::doubleValue)
                .max()
                .orElse(1);

        double ratio = Math.min(1.0, base / localMax);
        double randomFactor = 0.85 + (new Random(stationId + lookupHour).nextDouble() * 0.3);
        double scaled = base * randomFactor;

        System.out.println("Hourly totals for station " + stationId + ":");
        hourlyTotals.forEach((hour, total) -> System.out.println(hour + " => " + total));
        System.out.println("Base value: " + base + ", Local max: " + localMax);
        System.out.println("Computed ratio: " + ratio);

        String level;
        if (ratio < 0.25) level = "Light";
        else if (ratio < 0.5) level = "Moderate";
        else if (ratio < 0.75) level = "Heavy";
        else level = "Very Heavy";

        double confidence = 0.7 + (0.3 * Math.random());
        String predictedTimeRange = String.format("%02d:00 – %02d:59", lookupHour, lookupHour);

        return Map.of(
                "stationId", stationId,
                "predictedRidership", Math.round(scaled),
                "congestionLevel", level,
                "confidence", confidence,
                "timestamp", predictedTimeRange,
                "lowDensityPeriods", calculateLowDensityPeriods(hourlyTotals)
        );
    }

    private List<String> calculateLowDensityPeriods(Map<Integer, Double> hourlyTotals) {
        if (hourlyTotals.isEmpty()) {
            return Collections.emptyList();
        }

        double average = hourlyTotals.values().stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0);

        double threshold = average * 0.4;

        List<Integer> lowDensityHours = hourlyTotals.entrySet().stream()
                .filter(e -> e.getValue() <= threshold)
                .sorted(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .limit(6)
                .sorted()
                .collect(Collectors.toList());

        List<String> periods = new ArrayList<>();
        int i = 0;
        while (i < lowDensityHours.size()) {
            int startHour = lowDensityHours.get(i);
            int endHour = startHour;

            while (i + 1 < lowDensityHours.size() &&
                    lowDensityHours.get(i + 1) == endHour + 1) {
                endHour = lowDensityHours.get(i + 1);
                i++;
            }

            if (endHour == startHour) {
                periods.add(String.format("%02d:00 - %02d:59", startHour, startHour));
            } else {
                periods.add(String.format("%02d:00 - %02d:59", startHour, endHour));
            }

            i++;

            if (periods.size() >= 3) {
                break;
            }
        }

        return periods;
    }

    private int extractHour(String timeString) {
        if (timeString == null || timeString.isBlank()) return -1;
        String normalized = timeString.replace("–", "-").replace("—", "-").trim();

        String[] parts = normalized.split("-");
        if (parts.length == 0) return -1;

        String firstPart = parts[0].trim();
        Matcher m = TIME_PATTERN.matcher(firstPart);
        if (m.find()) {
            try {
                int hour = Integer.parseInt(m.group(1));
                return hour % 24;
            } catch (NumberFormatException ignored) {}
        }
        return -1;
    }
}