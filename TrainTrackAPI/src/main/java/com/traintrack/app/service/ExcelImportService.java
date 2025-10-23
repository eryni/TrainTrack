package com.traintrack.app.service;

import com.traintrack.app.model.RidershipData;
import com.traintrack.app.model.Station;
import com.traintrack.app.repository.RidershipDataRepository;
import com.traintrack.app.repository.StationRepository;
import jakarta.annotation.PostConstruct;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ExcelImportService {

    @Autowired
    private RidershipDataRepository ridershipDataRepository;

    @Autowired
    private StationRepository stationRepository;

    private final List<String> stationNames = Arrays.asList(
            "North Ave", "Quezon Ave", "GMA Kamuning", "Cubao",
            "Santolan", "Ortigas", "Shaw Blvd", "Boni Ave",
            "Guadalupe", "Buendia", "Ayala Ave", "Magallanes", "Taft"
    );

    private static final Pattern YEAR_PATTERN = Pattern.compile("(20\\d{2})");
    private static final Pattern MONTH_PATTERN = Pattern.compile("(?i)(January|February|March|April|May|June|July|August|September|October|November|December)");

    //@PostConstruct
    public void init() {
        System.out.println("📊 Starting Excel import...");
        importData();
        System.out.println("🚆 Backend is up and ready to serve requests!");
    }

    @Transactional
    public void importData() {
        try (InputStream inputStream = new ClassPathResource("Data-Request-FOI.xlsx").getInputStream();
             Workbook workbook = WorkbookFactory.create(inputStream)) {

            for (Sheet sheet : workbook) {
                String sheetName = sheet.getSheetName().trim();
                String year = extractYear(sheetName);

                System.out.println("\n📄 Importing sheet: '" + sheetName + "' → Year detected: " + year);

                String currentMonth = "Unknown";

                for (int i = 0; i <= sheet.getLastRowNum(); i++) {
                    Row row = sheet.getRow(i);
                    if (row == null) continue;

                    // 🔍 Detect a new month section (like "January 2020")
                    for (int c = 0; c < row.getLastCellNum(); c++) {
                        Cell cell = row.getCell(c);
                        if (cell != null && cell.getCellType() == CellType.STRING) {
                            String text = cell.getStringCellValue().trim();
                            Matcher m = MONTH_PATTERN.matcher(text);
                            if (m.find()) {
                                currentMonth = capitalize(m.group(1));
                                System.out.println("📆 Now importing month: " + currentMonth + " (" + year + ")");
                                // skip processing this row — it’s a header marker
                                continue;
                            }
                        }
                    }

                    // Detect data rows (usually start with time like "05:00")
                    Cell timeCell = row.getCell(0);
                    if (timeCell == null || timeCell.getCellType() != CellType.STRING) continue;

                    String time = timeCell.getStringCellValue().trim();
                    if (time.isEmpty() || time.equalsIgnoreCase("Time")) continue;

                    // Read entry/exit counts per station
                    for (int s = 0; s < stationNames.size(); s++) {
                        String stationName = stationNames.get(s);
                        int entryCol = 1 + (s * 2);
                        int exitCol = 2 + (s * 2);

                        Double entry = getNumericValue(row.getCell(entryCol));
                        Double exit = getNumericValue(row.getCell(exitCol));

                        if (entry == null && exit == null) continue;

                        Station station = stationRepository.findByName(stationName)
                                .orElseThrow(() -> new RuntimeException("Station not found: " + stationName));

                        RidershipData data = new RidershipData();
                        data.setYear(year);
                        data.setMonth(currentMonth);
                        data.setTime(time);
                        data.setStation(station);
                        data.setEntryCount(entry != null ? entry.intValue() : 0);
                        data.setExitCount(exit != null ? exit.intValue() : 0);

                        ridershipDataRepository.save(data);
                    }
                }
            }

            System.out.println("\n✅ Excel import completed successfully!");

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error importing Excel file: " + e.getMessage());
        }
    }

    /** Extract year like "2020" from sheet name "HOURLY 2020" */
    private String extractYear(String sheetName) {
        String cleaned = sheetName.replaceAll("(?i)hourly", "").trim();
        Matcher matcher = YEAR_PATTERN.matcher(cleaned);
        if (matcher.find()) return matcher.group(1);
        return "Unknown";
    }

    private Double getNumericValue(Cell cell) {
        if (cell == null) return null;
        return switch (cell.getCellType()) {
            case NUMERIC -> cell.getNumericCellValue();
            case STRING -> {
                try {
                    yield Double.parseDouble(cell.getStringCellValue());
                } catch (NumberFormatException e) {
                    yield null;
                }
            }
            default -> null;
        };
    }

    private String capitalize(String word) {
        return word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase();
    }
}
