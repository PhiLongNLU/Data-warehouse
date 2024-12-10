package org.example;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import org.example.Connector.JDBIConnector;
import org.jdbi.v3.core.Handle;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.List;

public class ExtractDataToStaging {
    private static void saveToStaging() {
        // Lấy đường dẫn từ cột source_file trong bảng configs
        String path = getSourceFilePath();
        if (path == null || path.isEmpty()) {
            System.err.println("Không tìm thấy đường dẫn file từ bảng configs.");
            updateLog("EXTRACT_FAILED", "Không tìm thấy đường dẫn file từ bảng configs.", 0, null);
            return;
        }

        File file = new File(path);

        // Cập nhật log: bắt đầu trích xuất dữ liệu
        updateLog("EXTRACTING", "Bắt đầu quá trình trích xuất dữ liệu.", 0, null);

        if (!file.exists()) {
            System.err.println("File không tồn tại: " + path);
            updateLog("EXTRACT_FAILED", "Không tìm thấy file: " + path, 0, null);
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file));
             CSVReader csvReader = new CSVReader(reader)) {

            // Đọc tất cả các dòng từ file CSV
            List<String[]> allLines = csvReader.readAll();

            // Kiểm tra nếu file rỗng hoặc không đủ dữ liệu
            if (allLines.isEmpty()) {
                System.err.println("File CSV trống.");
                updateLog("EXTRACT_FAILED", "File CSV trống.", 0, null);
                return;
            }

            // Loại bỏ dòng đầu tiên (thường là header)
            allLines.remove(0);

            // Bắt đầu một phiên giao dịch để xóa dữ liệu cũ trước khi chèn dữ liệu mới
            String truncateSql = "TRUNCATE TABLE staging";  // Sử dụng TRUNCATE để xóa toàn bộ dữ liệu
            String insertSql = "INSERT INTO staging (transit_time, start_city, end_city, start_point, end_point, " +
                               "departure_date, departure_time, arrival_time, arrival_date, ticket_price, bus_type, " +
                               "total_available_seat, date_get_data, time_get_data, location_get_data) " +
                               "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            try (Handle handle = JDBIConnector.getStagingJdbi().open()) {
                handle.useTransaction(h -> {
                    // Xóa toàn bộ dữ liệu trong bảng staging
                    h.createUpdate(truncateSql).execute();
                    System.out.println("Dữ liệu cũ đã bị xóa khỏi bảng staging.");

                    // Thực hiện batch insert dữ liệu mới từ CSV
                    for (String[] line : allLines) {
                        if (line.length != 15) {
                            System.err.println("Dòng không hợp lệ: " + String.join(",", line));
                            continue;
                        }

                        h.createUpdate(insertSql)
                         .bind(0, line[0])  // transit_time
                         .bind(1, line[1])  // start_city
                         .bind(2, line[2])  // end_city
                         .bind(3, line[3])  // start_point
                         .bind(4, line[4])  // end_point
                         .bind(5, line[5])  // departure_date
                         .bind(6, line[6])  // departure_time
                         .bind(7, line[7])  // arrival_time
                         .bind(8, line[8])  // arrival_date
                         .bind(9, line[9]) // ticket_price
                         .bind(10, line[10]) // bus_type
                         .bind(11, line[11]) // total_available_seat
                         .bind(12, line[12]) // date_get_data
                         .bind(13, line[13]) // time_get_data
                         .bind(14, line[14]) // location_get_data
                         .execute();
                    }
                });
            }

            // Cập nhật log: quá trình trích xuất thành công
            updateLog("EXTRACT_SUCCESS", "Dữ liệu đã được lưu thành công vào bảng staging.", allLines.size(), null);
        } catch (FileNotFoundException e) {
            System.err.println("Không tìm thấy file: " + e.getMessage());
            updateLog("EXTRACT_FAILED", "Không tìm thấy file: " + e.getMessage(), 0, e.getMessage());
        } catch (IOException | CsvException e) {
            System.err.println("Lỗi khi đọc file: " + e.getMessage());
            updateLog("EXTRACT_FAILED", "Lỗi khi đọc file: " + e.getMessage(), 0, e.getMessage());
        } catch (Exception e) {
            System.err.println("Lỗi không xác định: " + e.getMessage());
            updateLog("EXTRACT_FAILED", "Lỗi không xác định: " + e.getMessage(), 0, e.getMessage());
        }
    }

    // Phương thức để lấy đường dẫn file từ bảng configs
    private static String getSourceFilePath() {
        String selectSql = "SELECT source_file FROM control_db.configs WHERE id = ?";

        try (Handle handle = JDBIConnector.getControlJdbi().open()) {
            // Giả sử bạn lấy config_id = 1
            return handle.createQuery(selectSql)
                         .bind(0, 1) // config_id = 1
                         .mapTo(String.class)
                         .findOnly();
        } catch (Exception e) {
            System.err.println("Lỗi khi truy vấn bảng configs: " + e.getMessage());
            return null;
        }
    }

    // Phương thức ghi log vào bảng logs trong control_db
    private static void updateLog(String status, String message, int count, String errorMessage) {
        String insertLogSql = "INSERT INTO control_db.logs (configs_id, count, status, date_update, date_get_data, error_message, create_by) " +
                              "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Handle handle = JDBIConnector.getControlJdbi().open()) {
            handle.createUpdate(insertLogSql)
                  .bind(0, 1)
                  .bind(1, count)
                  .bind(2, status)
                  .bind(3, LocalDateTime.now())
                  .bind(4, LocalDateTime.now())
                  .bind(5, errorMessage)
                  .bind(6, "NGÂN")
                  .execute();
            System.out.println("Log đã được ghi: " + status);
        } catch (Exception e) {
            System.err.println("Lỗi khi ghi log: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        saveToStaging();
    }
}
