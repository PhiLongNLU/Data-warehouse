package org.example;

import org.example.Connector.JDBIConnector;
import org.example.model.FactTrip;
import org.jdbi.v3.core.Handle;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;


import static org.example.service.ETLTransform.*;

public class ETLProcess {
    private static void processETL() {
        // Đọc trạng thái từ bảng log (control_db)
        String controlLogSQL = "SELECT status FROM control_db.logs WHERE status = 'EXTRACT_SUCCESS' LIMIT 1";

        try (Handle handle = JDBIConnector.getStagingJdbi().open()) {
            String status = handle.createQuery(controlLogSQL).mapTo(String.class).findFirst().orElse("");

            if ("EXTRACT_SUCCESS".equals(status)) {
                // Nếu trạng thái là EXTRACT_SUCCESS, bắt đầu ETL
                String selectStagingSQL = "SELECT * FROM stagingdb.staging";
                List<String[]> stagingData = handle.createQuery(selectStagingSQL).map((rs, ctx) -> new String[]{
                        rs.getString("transit_time"),
                        rs.getString("start_city"),
                        rs.getString("end_city"),
                        rs.getString("start_point"),
                        rs.getString("end_point"),
                        rs.getString("departure_date"),
                        rs.getString("departure_time"),
                        rs.getString("arrival_time"),
                        rs.getString("arrival_date"),
                        rs.getString("ticket_price"),
                        rs.getString("bus_type"),
                        rs.getString("total_available_seat"),
                        rs.getString("date_get_data"),
                        rs.getString("time_get_data"),
                        rs.getString("location_get_data")
                }).list();

                // Xử lý từng dòng dữ liệu trong staging
                for (String[] row : stagingData) {
                    // Transform dữ liệu
                    String transitTime = convertToTimeFormat(row[0]);
                    String startCity = row[1];
                    String endCity = row[2];
                    String startPoint = row[3];
                    String endPoint = row[4];
                    LocalDate departureDate = parseDate(row[5]);
                    LocalTime departureTime = parseTime(row[6]);
                    LocalTime arrivalTime = parseTime(row[7]);
                    LocalDate arrivalDate = parseDate(row[8]);
                    double ticketPrice = parsePrice(row[9]);
                    String busType = row[10];
                    int totalAvailableSeat = parseSeat(row[11]);
                    LocalDate dateGetData = parseDate(row[12]);
                    LocalTime timeGetData = parseTime(row[13]);

                    // insert vao lay khoa
                    int startCityKey = getOrInsertCity(handle, startCity);
                    int endCityKey = getOrInsertCity(handle, endCity);
                    int startPointKey = getOrInsertPoint(handle, startPoint);
                    int endPointKey = getOrInsertPoint(handle, endPoint);
                    int departureDateKey = getOrInsertDate(handle, departureDate);
                    int arrivalDateKey = getOrInsertDate(handle, arrivalDate);
                    int departureTimeKey = getOrInsertTime(handle, departureTime);
                    int arrivalTimeKey = getOrInsertTime(handle, arrivalTime);
                    int busKey = getOrInsertBus(handle, busType);
                    int transitKey = getOrInsertTransit(handle, startCityKey, endCityKey, startPointKey, endPointKey, transitTime,departureDateKey,arrivalDateKey,departureTimeKey,arrivalTimeKey,busKey);

                    // Tạo đối tượng FactTrip từ dữ liệu đã transform
                    FactTrip factTrip = new FactTrip();
                    factTrip.setTransitKey(transitKey);
                    factTrip.setTicketPrice(ticketPrice);
                    factTrip.setTotalAvailableSeat(totalAvailableSeat);
                    // Kiểm tra xem có đối tượng của bảng factTrip có cùng id của dim_transit không va insert
                    insertOrUpdateFactTrip(handle,factTrip);
                }
                System.out.println("ETL Process completed successfully.");

            } else {
                System.out.println("No EXTRACT_SUCCESS status found, ETL not started.");
            }
        }
    }
    public static void main(String[] args) {
        processETL();
    }
}
