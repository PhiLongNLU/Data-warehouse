package org.example;

import org.example.Connector.JDBIConnector;
import org.jdbi.v3.core.Jdbi;

import java.util.List;

public class DataMartGetter {
    public static void main(String[] args) {
        // Kết nối đến cơ sở dữ liệu
        Jdbi jdbi = JDBIConnector.getDataWarehouseJdbi();

        // Truy vấn view
        String sql = "SELECT * FROM data_mart.vw_Total_Revenue_Per_City_Pair"; // Thay 'my_view' bằng tên view của bạn

        // Thực thi truy vấn và lấy kết quả dưới dạng mảng String[]
        List<String[]> results = jdbi.withHandle(handle ->
                handle.createQuery(sql)
                        .map((rs, ctx) -> {
                            // Chuyển mỗi dòng thành mảng String
                            int columnCount = rs.getMetaData().getColumnCount();
                            String[] row = new String[columnCount];
                            for (int i = 0; i < columnCount; i++) {
                                row[i] = rs.getString(i + 1); // Lấy dữ liệu từ từng cột
                            }
                            return row; // Trả về mảng cho mỗi dòng
                        })
                        .list()
        );

        // In kết quả
        for (String[] row : results) {
            for (String column : row) {
                System.out.print(column + "\t"); // In ra các giá trị trong mỗi dòng
            }
            System.out.println();
        }
    }
}
