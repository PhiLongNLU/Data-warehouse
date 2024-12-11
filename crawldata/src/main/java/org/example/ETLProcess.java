package org.example;

import org.example.Connector.JDBIConnector;
import org.example.model.FactTrip;
import org.jdbi.v3.core.Handle;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;


import static org.example.service.ETLTransform.*;

public class ETLProcess {
    private static void processETL() {

        // 1. Kiểm tra trạng thái log trong ngày hiện tại
        LocalDate currentDate = LocalDate.now();
        Date sqlDate = Date.valueOf(currentDate);
        String controlLogSQL = "SELECT status FROM control_db.logs WHERE date_get_data = :date AND status = 'EXTRACT_SUCCESS'";
        //2. Kết nối đến ControlDb và lấy ra status
        try (Handle handle = JDBIConnector.getStagingJdbi().open()) {
            // Truy vấn và lấy kết quả đầu tiên, nếu không có thì trả về chuỗi rỗng
            String status = handle.createQuery(controlLogSQL)
                    .bind("date", sqlDate)  // Gắn giá trị của ngày vào câu lệnh SQL
                    .mapTo(String.class)
                    .findFirst()
                    .orElse("");
            //3. Kiểm tra trạng thái nếu là EXTRACT_SUCCESS thì bắt đầu ETL
            if ("EXTRACT_SUCCESS".equals(status)) {
                // Nếu trạng thái là EXTRACT_SUCCESS, bắt đầu ETL
                //4. Ghi vào log status = TRANSFORMING
                updateLog("TRANSFORMING",  0, null);
                //5 Bắt đầu trích xuất dữ liệu
                //5.1 Kết nối đến stagingDB và lấy dữ liệu
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
                int count =0;
                //7. transform dữ liệu về đúng kiểu của fact,dim
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

                    //8. Thêm vào bảng Dim hoặc lấy các Dim_id (nếu đã có)
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

                    //9. Thêm dòng vào bảng fact từ các dim và dữ liệu của staging
                    // tạo đối tượng FactTrip
                    FactTrip factTrip = new FactTrip();
                    factTrip.setTransitKey(transitKey);
                    factTrip.setTicketPrice(ticketPrice);
                    factTrip.setTotalAvailableSeat(totalAvailableSeat);
                    // Thêm hoặc cập nhật FactTrip, trả về so dong anh huong
                    count = count + insertOrUpdateFactTrip(handle,factTrip);
                }
                System.out.println("ETL Process completed successfully.");
               // 10. Ghi vào log status TRANSFORMED
                updateLog("TRANSFORM_SUCCESS", count, null);
            } else {
                //3.1 Trích xuất dữ liệu thất bại hoặc đang trích xuất
                System.out.println("No EXTRACT_SUCCESS status found, ETL not started.");
                updateLog("TRANSFORM_FAILED", 0, "No EXTRACT_SUCCESS status found, ETL not started.");
            }
        }catch (Exception e) {
            // bất kì lỗi nào xảy ra trong quá trình ETL đều được ghi vào log với status TRANSFORM_FAILED
            System.err.println("ETL Process failed: " + e.getMessage());
            updateLog("TRANSFORM_FAILED", 0, e.getMessage());
        }
    }
    private static void updateLog(String status, int count, String errorMessage) {
        String insertLogSql = "INSERT INTO control_db.logs (configs_id, count, status, date_update, date_get_data, error_message, create_by) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Handle handle = JDBIConnector.getControlJdbi().open()) {
            handle.createUpdate(insertLogSql)
                    .bind(0, 1)
                    .bind(1, count)
                    .bind(2, status)
                    .bind(3, LocalDate.now())
                    .bind(4, LocalDate.now())
                    .bind(5, errorMessage)
                    .bind(6, "Hải")
                    .execute();
            System.out.println("Log đã được ghi: " + status);
        } catch (Exception e) {
            System.err.println("Lỗi khi ghi log: " + e.getMessage());
        }
    }
    public static void main(String[] args) {
        processETL();
    }
}
