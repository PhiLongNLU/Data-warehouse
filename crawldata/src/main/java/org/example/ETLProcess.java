package org.example;

import org.example.Connector.JDBIConnector;
import org.jdbi.v3.core.Handle;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ETLProcess {

    private static void saveToDimAndFact() {
        String selectStagingSQL = "SELECT * FROM staging";
        try (Handle handle = JDBIConnector.getStagingJdbi().open()) {
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

                // Insert vào Dim và lấy khóa
                int startCityKey = getOrInsertCity(handle, startCity);
                int endCityKey = getOrInsertCity(handle, endCity);
                int startPointKey = getOrInsertPoint(handle, startPoint);
                int endPointKey = getOrInsertPoint(handle, endPoint);
                int transitKey = getOrInsertTransit(handle, startCityKey, endCityKey, startPointKey, endPointKey, transitTime);
                int departureDateKey = getOrInsertDate(handle, departureDate);
                int arrivalDateKey = getOrInsertDate(handle, arrivalDate);
                int departureTimeKey = getOrInsertTime(handle, departureTime);
                int arrivalTimeKey = getOrInsertTime(handle, arrivalTime);
                int busKey = getOrInsertBus(handle, busType);

                // Insert vào Fact
                insertFactTrip(handle, transitKey, departureDateKey, departureTimeKey, arrivalDateKey, arrivalTimeKey,
                        dateGetData, dateGetData.plusMonths(1), busKey, ticketPrice, totalAvailableSeat);
            }
            System.out.println("ETL Process completed successfully.");
        }
    }

    private static String convertToTimeFormat(String timeString) {
        if (timeString.contains("giờ")) {
            String hours = timeString.split(" ")[0];
            return String.format("%02d:00:00", Integer.parseInt(hours));
        }
        return "00:00:00";
    }

    private static LocalDate parseDate(String date) {
        return LocalDate.parse(date, DateTimeFormatter.ofPattern("dd-MM-yyyy"));
    }

    private static LocalTime parseTime(String time) {
        return LocalTime.parse(time, DateTimeFormatter.ofPattern("HH:mm"));
    }

    private static double parsePrice(String price) {
        return Double.parseDouble(price.replace(".", "").replace("đ", "").trim());
    }

    private static int parseSeat(String seat) {
        return Integer.parseInt(seat.replace("chỗ trống", "").trim());
    }

    private static int getOrInsertCity(Handle handle, String cityName) {
        String selectCitySQL = "SELECT City_Key FROM data_warehouse.Dim_city WHERE City_Name = ?";
        String insertCitySQL = "INSERT INTO data_warehouse.Dim_city (City_Name) VALUES (?)";

        return handle.createQuery(selectCitySQL).bind(0, cityName).mapTo(Integer.class).findFirst()
                .orElseGet(() -> handle.createUpdate(insertCitySQL).bind(0, cityName).executeAndReturnGeneratedKeys("City_Key").mapTo(Integer.class).one());
    }

    private static int getOrInsertPoint(Handle handle, String pointName) {
        String selectPointSQL = "SELECT Point_Key FROM data_warehouse.Dim_Point WHERE Point_Name = ?";
        String insertPointSQL = "INSERT INTO data_warehouse.Dim_Point (Point_Name) VALUES (?)";

        return handle.createQuery(selectPointSQL).bind(0, pointName).mapTo(Integer.class).findFirst()
                .orElseGet(() -> handle.createUpdate(insertPointSQL).bind(0, pointName).executeAndReturnGeneratedKeys("Point_Key").mapTo(Integer.class).one());
    }

    private static int getOrInsertTransit(Handle handle, int startCityKey, int endCityKey, int startPointKey, int endPointKey, String transitTime) {
        String selectTransitSQL = "SELECT Transit_Key FROM data_warehouse.Dim_Transit WHERE StartCity_Key = ? AND EndCity_Key = ? AND StartPoint_Key = ? AND EndPoint_Key = ? AND TransitTime = ?";
        String insertTransitSQL = "INSERT INTO data_warehouse.Dim_Transit (StartCity_Key, EndCity_Key, StartPoint_Key, EndPoint_Key, TransitTime) VALUES (?, ?, ?, ?, ?)";

        return handle.createQuery(selectTransitSQL)
                .bind(0, startCityKey)
                .bind(1, endCityKey)
                .bind(2, startPointKey)
                .bind(3, endPointKey)
                .bind(4, transitTime)
                .mapTo(Integer.class).findFirst()
                .orElseGet(() -> handle.createUpdate(insertTransitSQL)
                        .bind(0, startCityKey)
                        .bind(1, endCityKey)
                        .bind(2, startPointKey)
                        .bind(3, endPointKey)
                        .bind(4, transitTime)
                        .executeAndReturnGeneratedKeys("Transit_Key")
                        .mapTo(Integer.class).one());
    }

    private static int getOrInsertDate(Handle handle, LocalDate date) {
        String selectDateSQL = "SELECT Date_Key FROM data_warehouse.Dim_Date WHERE Date = ?";
        String insertDateSQL = "INSERT INTO data_warehouse.Dim_Date (Date, Year, Month, Day) VALUES (?, ?, ?, ?)";

        return handle.createQuery(selectDateSQL).bind(0, date).mapTo(Integer.class).findFirst()
                .orElseGet(() -> handle.createUpdate(insertDateSQL)
                        .bind(0, date)
                        .bind(1, date.getYear())
                        .bind(2, date.getMonthValue())
                        .bind(3, date.getDayOfMonth())
                        .executeAndReturnGeneratedKeys("Date_Key").mapTo(Integer.class).one());
    }

    private static int getOrInsertTime(Handle handle, LocalTime time) {
        String selectTimeSQL = "SELECT Time_Key FROM data_warehouse.Dim_TimeOfDay WHERE Time = ?";
        String insertTimeSQL = "INSERT INTO data_warehouse.Dim_TimeOfDay (Time) VALUES (?)";

        return handle.createQuery(selectTimeSQL).bind(0, time).mapTo(Integer.class).findFirst()
                .orElseGet(() -> handle.createUpdate(insertTimeSQL).bind(0, time).executeAndReturnGeneratedKeys("Time_Key").mapTo(Integer.class).one());
    }

    private static int getOrInsertBus(Handle handle, String busType) {
        String selectBusSQL = "SELECT Bus_Key FROM data_warehouse.Dim_Bus WHERE BusType = ?";
        String insertBusSQL = "INSERT INTO data_warehouse.Dim_Bus (BusType) VALUES (?)";

        return handle.createQuery(selectBusSQL).bind(0, busType).mapTo(Integer.class).findFirst()
                .orElseGet(() -> handle.createUpdate(insertBusSQL).bind(0, busType).executeAndReturnGeneratedKeys("Bus_Key").mapTo(Integer.class).one());
    }

    private static void insertFactTrip(Handle handle, int transitKey, int departureDateKey, int departureTimeKey,
                                       int arrivalDateKey, int arrivalTimeKey, LocalDate dateGetData, LocalDate dateExpiredData,
                                       int busKey, double ticketPrice, int totalAvailableSeat) {
        String insertFactSQL = "INSERT INTO data_warehouse.Fact_Trip (Transit_Key, DepartureDate_Key, DepartureTime_Key, ArrivalDate_Key, " +
                "ArrivalTime_Key, DateGetData, DateExpiredData, Bus_Key, TicketPrice, TotalAvailableSeat) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        handle.createUpdate(insertFactSQL)
                .bind(0, transitKey)
                .bind(1, departureDateKey)
                .bind(2, departureTimeKey)
                .bind(3, arrivalDateKey)
                .bind(4, arrivalTimeKey)
                .bind(5, dateGetData)
                .bind(6, dateExpiredData)
                .bind(7, busKey)
                .bind(8, ticketPrice)
                .bind(9, totalAvailableSeat)
                .execute();
    }

    public static void main(String[] args) {
        saveToDimAndFact();
    }
}
