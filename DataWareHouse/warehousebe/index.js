const express = require("express");
const mysql = require('mysql');
const cors = require("cors");

const app = express();
const port = 5000;

// Cấu hình kết nối SQL Server
const poolConfig  = {
  user: "root",
  password: "LeLong@123",
  server: "localhost",
  database: "data_mart",
  port: 3310,
  options: {
    encrypt: true, // nếu sử dụng Azure
    trustServerCertificate: true, // cho server cục bộ
  },
};

// Middleware
app.use(cors());
app.use(express.json());

// Endpoint lấy danh sách sản phẩm
app.get("/api/products", async (req, res) => {
  try {
    const pool = await mysql.createConnection(poolConfig);
    console.log(pool);
    const result = await pool
    .query("SELECT * FROM data_mart.vw_Total_Revenue_Per_City_Pair",
      (err, result) => {

        if (err) {
          console.error(err);
          res.status(500).json({ message: 'Lỗi truy vấn cơ sở dữ liệu' });
        } else {
          res.json(result);
        }
      }
    )

      
  } catch (error) {
    res.status(500).send("Error retrieving products: " + error.message);
  }
});

// Chạy server
app.listen(port, () => {
  console.log(`Server is running on http://localhost:${port}`);
});
