package org.example;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;

public class CSVExporter {
    public static void exportToCSV(List<FutaTable> buses, String filePath) {
        // Current date to replace the expired date
        String currentDate = LocalDate.now().toString(); // Format is YYYY-MM-DD
        File file = new File(filePath);

        try {
            if(!file.exists()) file.createNewFile();
        }
        catch (IOException ioException){
            System.out.println(ioException.getMessage());
        }
        try {
            // Load all lines from the CSV file
            List<String> lines = Files.readAllLines(Paths.get(filePath));

            // If the file is not empty, update the "Date Expired" column
            if (!lines.isEmpty()) {
                // Assume the first line is the header
                String header = lines.get(0);

                // Find the index of the "Date Expired" column
                String[] headers = header.split(",");
                int dateExpiredIndex = -1;
                for (int i = 0; i < headers.length; i++) {
                    if (headers[i].equals("Date Expired")) {
                        dateExpiredIndex = i;
                        break;
                    }
                }

                // If the "Date Expired" column exists, proceed with updating the data
                if (dateExpiredIndex != -1) {
                    // Iterate over the remaining lines (data rows)
                    for (int i = 1; i < lines.size(); i++) {
                        String line = lines.get(i);
                        String[] columns = line.split(",");

                        // Check if "Date Expired" is set to the old value
                        if (columns[dateExpiredIndex].equals("12-31-+999999999")) {
                            columns[dateExpiredIndex] = currentDate; // Replace with the current date
                        }

                        // Rebuild the updated line
                        lines.set(i, String.join(",", columns));
                    }
                }
            }

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, StandardCharsets.UTF_8))) {
                // If file is empty, write headers
                if (Files.size(Paths.get(filePath)) == 0) {
                    String[] headers = {"Transit Time", "Start City", "End City", "Start Point", "End Point",
                            "Departure Date", "Departure Time", "Arrival Time", "Arrival Date",
                            "Ticket Price", "Bus Type", "Total Available Seat", "Date Get Data", "Date Expired"};
                    writer.write(String.join(",", headers) + "\n");
                }

                for (FutaTable bus : buses) {
                    StringBuilder row = new StringBuilder();
                    row.append(bus.getTransitTime()).append(",");
                    row.append(bus.getStartCity()).append(",");
                    row.append(bus.getEndCity()).append(",");
                    row.append(bus.getStartPoint()).append(",");
                    row.append(bus.getEndPoint()).append(",");
                    row.append(bus.getDepartureDate()).append(",");
                    row.append(bus.getDepartureTime()).append(",");
                    row.append(bus.getArrivalTime()).append(",");
                    row.append(bus.getArrivalDate()).append(",");
                    row.append(bus.getTicketPrice()).append(",");
                    row.append(bus.getBusType()).append(",");
                    row.append(bus.getTotalAvailableSeat()).append(",");
                    row.append(bus.getDateGetData()).append(",");
                    row.append(bus.getDateExpiredData());

                    writer.write(row.toString());
                    writer.newLine();
                }

                System.out.println("CSV file updated successfully at " + filePath);

                Files.write(Paths.get(filePath), lines);
                System.out.println("CSV file updated with new 'Date Expired' values");
            } catch (IOException e) {
                System.err.println("Failed to append new data to CSV: " + e.getMessage());
            }
        } catch (IOException e) {
            System.err.println("Failed to read the CSV file: " + e.getMessage());
        }
    }
}