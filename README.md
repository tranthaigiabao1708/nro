# Ngọc Rồng Online - Teamobi 2026 Full Source & 2-Tab Client

Kho mã nguồn đầy đủ gồm Máy chủ Java **Teamobi 2026**, Cơ sở dữ liệu SQL đã cấu hình sẵn, Kho Resource Icon **Hồi Sinh Ngọc Rồng 2025**, Dự án Unity **Client 2 Tab - Hành Trang Lưới (`PRJ_2Tab_550K`)** và bộ **Client PC (`Client_ChoNguoiChoiTaiVe`)**.

## Cấu trúc thư mục
- `Teamobi2026/` : Mã nguồn Server Java chính (`SRC/`), bản build sẵn `SRC/dist/NgocRongOnline.jar`, full `21.043` icon x4 và file SQL đầy đủ `database_team2026_full_ready.sql`.
- `PRJ_2Tab_550K/` : Mã nguồn Unity Client 2 Tab - Hành Trang Lưới (đã tích hợp tự đọc IP từ `server_ip.txt` và menu `NRO Admin -> Build Client PC 2 Tab`).
- `Client_ChoNguoiChoiTaiVe/` : Bộ Client PC cho người chơi tải về (sửa IP máy chủ tại `server_ip.txt`).
- `Hoisinhngocrong2025/` : Kho Resource Icon & Part mở rộng.
- `SRC NRO NEW 01/` : Bộ mã nguồn Server dự phòng (`Server` + `Login`).

## Hướng dẫn chạy nhanh
1. Nạp `Teamobi2026/database_team2026_full_ready.sql` vào MySQL/MariaDB cổng `3306` (user `root`, password rỗng, database `team2026`) hoặc dùng thư mục `runtime` cục bộ thông qua `1_Chay_Database_Va_Server.bat`.
2. Chạy `1_Chay_Database_Va_Server.bat` để bật Database & Game Server (cổng `14445`).
3. Mở `3_Mo_Client_Test_Game.bat` để vào game (Tài khoản Admin: `1` / `1` hoặc `admin` / `123456`).
4. Khi mở Online LAN / Radmin VPN / VPS: đổi `127.0.0.1` thành IP máy chủ trong `Teamobi2026/SRC/Config.properties` và `Client_ChoNguoiChoiTaiVe/server_ip.txt`, sau đó chạy `4_Dong_Goi_Client_Zip_Cho_Nguoi_Choi.bat` gửi cho người chơi tải về.

