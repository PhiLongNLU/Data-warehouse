package org.example.service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class CSVExporter {
    public static void exportToCSV(List<FutaTable> buses, String filePath) {
        try {
            File file = new File(filePath);
            boolean fileExists = file.exists();

            //Execute old data in file before add new data to file
            if (fileExists) {
                BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    if (line.contains("12-31-+9999")) {
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
            }
            else{
                BufferedWriter bw = new BufferedWriter(new FileWriter(filePath));
                String header = "Transit Time,Start City,End City,Start Point,End Point,Departure Date,Departure Time," +
                        "Arrival Time,Arrival Date,Ticket Price,Bus Type,Total Available Seat,Date Get Data,date Expired Data\n";
                bw.write(header);
                bw.close();
            }

            try(BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true), StandardCharsets.UTF_8))){
                for (var bus : buses) {
                    String dataLine = bus.transitTime + "," + bus.startCity + "," + bus.endCity + "," + bus.startPoint + "," + bus.endPoint + ","
                            + bus.departureDate + "," + bus.departureTime + "," + bus.arrivalTime + "," + bus.arrivalDate + "," + bus.ticketPrice + ","
                            + bus.busType + "," + bus.totalAvailableSeat + "," + bus.dateGetData + "," + bus.dateExpiredData + "\n";
                    bw.write(dataLine);
                }
                System.out.println("Complete to get data!");
            }
            catch (Exception e){
                System.out.println(e.getMessage());
            }

        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}