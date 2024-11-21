package service;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import org.example.Connector.JDBIConnector;
import org.jdbi.v3.core.Handle;

import java.io.*;
import java.util.List;

public class ExtractDataToStaging {
    private static void saveToStaging(String path) {
        String csvFilePath = path;
        File file = new File(csvFilePath);

        if (!file.exists()) {
            System.err.println("File không tồn tại: " + csvFilePath);
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file));
             CSVReader csvReader = new CSVReader(reader)) {

            // Đọc tất cả các dòng từ file CSV
            List<String[]> allLines = csvReader.readAll();

            // Kiểm tra nếu file rỗng hoặc không đủ dữ liệu
            if (allLines.isEmpty()) {
                System.err.println("File CSV trống.");
                return;
            }

            // Loại bỏ dòng đầu tiên (thường là header)
            allLines.remove(0);

            // Chuẩn bị câu lệnh SQL cho batch insert
            String sql = "INSERT INTO staging (transit_time, start_city, end_city, start_point, end_point, " +
                         "departure_date, departure_time, arrival_time, arrival_date, ticket_price, bus_type, " +
                         "total_available_seat, date_get_data, date_expired_data) " +
                         "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            // Duyệt qua từng dòng và thực hiện batch insert
            try (Handle handle = JDBIConnector.getStagingJdbi().open()) {
                handle.useTransaction(h -> {
                    for (String[] line : allLines) {
                        if (line.length != 14) {
                            System.err.println("Dòng không hợp lệ: " + String.join(",", line));
                            continue;
                        }
                        // Thực hiện batch insert
                        h.createUpdate(sql)
                                .bind(0, line[0])
                                .bind(1, line[1])
                                .bind(2, line[2])
                                .bind(3, line[3])
                                .bind(4, line[4])
                                .bind(5, line[5])
                                .bind(6, line[6])
                                .bind(7, line[7])
                                .bind(8, line[8])
                                .bind(9, line[9])
                                .bind(10, line[10])
                                .bind(11, line[11])
                                .bind(12, line[12])
                                .bind(13, line[13])
                                .execute();
                    }
                });
            }
            System.out.println("Dữ liệu đã được lưu thành công vào bảng staging.");
        } catch (FileNotFoundException e) {
            System.err.println("Không tìm thấy file: " + e.getMessage());
        } catch (IOException | CsvException e) {
            System.err.println("Lỗi khi đọc file: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Lỗi không xác định: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        String path = "D:\\N4\\HK1\\DataWareHouse\\futabusFile\\futabus1.csv";
        saveToStaging(path);
    }
}
