package org.example.service;

import org.example.assets.CrawlProcessStatus;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class CSVExporter {
    public static void exportToCSV(List<FutaTable> buses, String filePath) throws IOException {
        File file = new File(filePath);

        //Execute old data in file before add new data to file
        if (file.exists()) {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains("12-31-+999999999")) {
                    LocalDate currentDate = LocalDate.now();
                    String currentDateFormat = DateTimeFormatter.ofPattern("MM-dd-yyyy").format(currentDate);
                    line = line.replace("12-31-+999999999", currentDateFormat);
                }
                sb.append(line).append("\n");
            }
            br.close();

            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8));
            bw.write(sb.toString());
            bw.close();
        } else {
            BufferedWriter bw = new BufferedWriter(new FileWriter(filePath));
            String header = "transit_time,start_city,end_city,start_point,end_point,departure_date,departure_time," +
                    "arrival_time,arrival_date,ticket_price,bus_type,total_available_seat,date_get_data,time_get_data,location_get_data\n";
            bw.write(header);
            bw.close();
        }

        BufferedWriter bw = writeDataToFileCSV(buses, file);
        bw.close();
        System.out.println("Complete to get data!");
    }

    private static BufferedWriter writeDataToFileCSV(List<FutaTable> buses, File file) throws IOException {
        DateTimeFormatter formatterDate = DateTimeFormatter.ofPattern("MM-dd-yyyy");
        DateTimeFormatter formatterTime = DateTimeFormatter.ofPattern("hh:mm");

        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true), StandardCharsets.UTF_8));
        for (var bus : buses) {
            String dataLine = bus.transitTime + "," + bus.startCity + "," + bus.endCity + "," + bus.startPoint + "," + bus.endPoint + ","
                    + bus.departureDate + "," + bus.departureTime + "," + bus.arrivalTime + "," + bus.arrivalDate + "," + bus.ticketPrice + ","
                    + bus.busType + "," + bus.totalAvailableSeat + "," + bus.dateGetData + "," + LocalTime.now().format(formatterTime) + "," + CrawlProcessStatus.LOCATION_GET_DATA + "\n";
            bw.write(dataLine);
        }
        return bw;
    }

    public static void exportToCSV2(List<FutaTable> rows, String filePath) throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(filePath));
        String header = "transit_time,start_city,end_city,start_point,end_point,departure_date,departure_time," +
                "arrival_time,arrival_date,ticket_price,bus_type,total_available_seat,date_get_data,time_get_data,location_get_data\n";
        bw.write(header);
        bw.close();

        bw = writeDataToFileCSV(rows, new File(filePath));
        bw.close();
        System.out.println("Complete to get data!");
    }
}