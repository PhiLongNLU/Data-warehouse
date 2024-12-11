package org.example.service;

import org.example.model.FactTrip;
import org.jdbi.v3.core.Handle;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class ETLTransform {
    public ETLTransform() {
    }

    public static String convertToTimeFormat(String timeString) {
        if (timeString != null && timeString.contains("giờ")) {
            String hours = timeString.split(" ")[0];
            return String.format("%02d:00:00", Integer.parseInt(hours));
        }
        return "00:00:00";
    }

    public static LocalDate parseDate(String date) {
        return LocalDate.parse(date, DateTimeFormatter.ofPattern("dd-MM-yyyy"));
    }

    public static LocalTime parseTime(String time) {
        return LocalTime.parse(time, DateTimeFormatter.ofPattern("HH:mm"));
    }

    public static double parsePrice(String price) {
        return Double.parseDouble(price.replace(".", "").replace("đ", "").trim());
    }

    public static int parseSeat(String seat) {
        return Integer.parseInt(seat.replace("chỗ trống", "").trim());
    }

    public static int getOrInsertCity(Handle handle, String cityName) {
        String selectCitySQL = "SELECT City_Key FROM data_warehouse.Dim_City WHERE City_Name = ?";
        String insertCitySQL = "INSERT INTO data_warehouse.Dim_City (City_Name) VALUES (?)";
        return handle.createQuery(selectCitySQL).bind(0, cityName).mapTo(Integer.class).findFirst()
                .orElseGet(() -> handle.createUpdate(insertCitySQL).bind(0, cityName).executeAndReturnGeneratedKeys("City_Key").mapTo(Integer.class).one());
    }

    public static int getOrInsertPoint(Handle handle, String pointName) {
        String selectPointSQL = "SELECT Point_Key FROM data_warehouse.Dim_Point WHERE Point_Name = ?";
        String insertPointSQL = "INSERT INTO data_warehouse.Dim_Point (Point_Name) VALUES (?)";
        return handle.createQuery(selectPointSQL).bind(0, pointName).mapTo(Integer.class).findFirst()
                .orElseGet(() -> handle.createUpdate(insertPointSQL).bind(0, pointName).executeAndReturnGeneratedKeys("Point_Key").mapTo(Integer.class).one());
    }

    public static int getOrInsertDate(Handle handle, LocalDate date) {
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

    public static int getOrInsertTime(Handle handle, LocalTime time) {
        String selectTimeSQL = "SELECT Time_Key FROM data_warehouse.Dim_TimeOfDay WHERE Time = ?";
        String insertTimeSQL = "INSERT INTO data_warehouse.Dim_TimeOfDay (Time) VALUES (?)";
        return handle.createQuery(selectTimeSQL).bind(0, time).mapTo(Integer.class).findFirst()
                .orElseGet(() -> handle.createUpdate(insertTimeSQL).bind(0, time).executeAndReturnGeneratedKeys("Time_Key").mapTo(Integer.class).one());
    }

    public static int getOrInsertBus(Handle handle, String busType) {
        String selectBusSQL = "SELECT Bus_Key FROM data_warehouse.Dim_Bus WHERE BusType = ?";
        String insertBusSQL = "INSERT INTO data_warehouse.Dim_Bus (BusType) VALUES (?)";
        return handle.createQuery(selectBusSQL).bind(0, busType).mapTo(Integer.class).findFirst()
                .orElseGet(() -> handle.createUpdate(insertBusSQL).bind(0, busType).executeAndReturnGeneratedKeys("Bus_Key").mapTo(Integer.class).one());
    }

    public static int getOrInsertTransit(Handle handle, int startCityKey, int endCityKey, int startPointKey, int endPointKey, String transitTime, int departureDateKey, int arrivalDateKey, int departureTimeKey, int arrivalTimeKey, int busKey) {
        // SQL để kiểm tra sự tồn tại của bản ghi Dim_Transit
        String selectTransitSQL = "SELECT Transit_Key FROM data_warehouse.Dim_Transit WHERE StartCity_Key = ? AND EndCity_Key = ? AND StartPoint_Key = ? AND EndPoint_Key = ? AND TransitTime = ? AND DepartureDate_Key = ? AND ArrivalDate_Key = ? AND DepartureTime_Key = ? AND ArrivalTime_Key = ? AND Bus_Key = ?";

        // SQL để chèn bản ghi mới vào Dim_Transit
        String insertTransitSQL = "INSERT INTO data_warehouse.Dim_Transit (StartCity_Key, EndCity_Key, StartPoint_Key, EndPoint_Key, TransitTime, DepartureDate_Key, ArrivalDate_Key, DepartureTime_Key, ArrivalTime_Key, Bus_Key) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        // Thực thi câu lệnh truy vấn
        return handle.createQuery(selectTransitSQL)
                .bind(0, startCityKey)
                .bind(1, endCityKey)
                .bind(2, startPointKey)
                .bind(3, endPointKey)
                .bind(4, transitTime)
                .bind(5, departureDateKey)
                .bind(6, arrivalDateKey)
                .bind(7, departureTimeKey)
                .bind(8, arrivalTimeKey)
                .bind(9, busKey)
                .mapTo(Integer.class)
                .findFirst()
                .orElseGet(() -> handle.createUpdate(insertTransitSQL)
                        .bind(0, startCityKey)
                        .bind(1, endCityKey)
                        .bind(2, startPointKey)
                        .bind(3, endPointKey)
                        .bind(4, transitTime)
                        .bind(5, departureDateKey)
                        .bind(6, arrivalDateKey)
                        .bind(7, departureTimeKey)
                        .bind(8, arrivalTimeKey)
                        .bind(9, busKey)
                        .executeAndReturnGeneratedKeys("Transit_Key")
                        .mapTo(Integer.class)
                        .one());
    }

    public static FactTrip getFactTripByTransitKey(Handle handle, int transitKey) {
        // Câu lệnh SQL để tìm FactTrip dựa trên transitKey
        String selectFactSQL = "SELECT Transit_Key, DateExpiredData, TicketPrice, TotalAvailableSeat " +
                "FROM data_warehouse.Fact_Trip WHERE Transit_Key = ? and DateExpiredData = '9999-12-30' ";

        // Truy vấn và map kết quả vào đối tượng FactTrip
        return handle.createQuery(selectFactSQL)
                .bind(0, transitKey) // Bind transitKey vào câu lệnh SQL
                .map((rs, ctx) -> {
                    int transitKeyResult = rs.getInt("Transit_Key");
                    LocalDate dateExpiredData = rs.getObject("DateExpiredData", LocalDate.class);
                    double ticketPrice = rs.getDouble("TicketPrice");
                    int totalAvailableSeat = rs.getInt("TotalAvailableSeat");

                    // Tạo và trả về đối tượng FactTrip
                    return new FactTrip(transitKeyResult, dateExpiredData, ticketPrice, totalAvailableSeat);
                })
                .findFirst()
                .orElse(null); // Nếu không tìm thấy, trả về null
    }

    public static void insertOrUpdateFactTrip(Handle handle, FactTrip factTrip) {
        // Lấy FactTrip cũ từ database theo transitKey
        FactTrip existingFactTrip = getFactTripByTransitKey(handle, factTrip.getTransitKey());
        // Đặt ngày hết hạn mới là 30-12-9999
        LocalDate newExpirationDate = LocalDate.of(9999, 12, 30);
        if (existingFactTrip != null) {
            // Kiểm tra xem dữ liệu có thay đổi không
            boolean isSameData = factTrip.equals(existingFactTrip);
      
            if (!isSameData) {
                // Nếu dữ liệu khác, cập nhật dateExpiredData của dữ liệu cũ
                String updateSQL = "UPDATE data_warehouse.Fact_Trip " +
                        "SET DateExpiredData = ? " +
                        "WHERE Transit_Key = ?";

                handle.createUpdate(updateSQL)
                        .bind(0, new Date())
                        .bind(1, factTrip.getTransitKey())
                        .execute();

                // Thêm bản ghi mới với dữ liệu khác
                String insertSQL = "INSERT INTO data_warehouse.Fact_Trip (Transit_Key, DateExpiredData, TicketPrice, TotalAvailableSeat) " +
                        "VALUES (?, ?, ?, ?)";


                handle.createUpdate(insertSQL)
                        .bind(0, factTrip.getTransitKey())
                        .bind(1, newExpirationDate)
                        .bind(2, factTrip.getTicketPrice())
                        .bind(3, factTrip.getTotalAvailableSeat())
                        .execute();

                System.out.println("Updated existing FactTrip and inserted new FactTrip with Transit_Key: " + factTrip.getTransitKey());
            } else {
                // Nếu dữ liệu không thay đổi, không làm gì cả
//                System.out.println("No changes detected for Transit_Key: " + factTrip.getTransitKey());
            }
        } else {
            // Nếu FactTrip không tồn tại, chèn bản ghi mới
            String insertSQL = "INSERT INTO data_warehouse.Fact_Trip (Transit_Key, DateExpiredData, TicketPrice, TotalAvailableSeat) " +
                    "VALUES (?, ?, ?, ?)";

            handle.createUpdate(insertSQL)
                    .bind(0, factTrip.getTransitKey())
                    .bind(1, newExpirationDate)
                    .bind(2, factTrip.getTicketPrice())
                    .bind(3, factTrip.getTotalAvailableSeat())
                    .execute();

            System.out.println("Inserted new FactTrip with Transit_Key: " + factTrip.getTransitKey());
        }
    }


}
