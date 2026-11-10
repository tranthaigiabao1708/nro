# Ngọc Rồng NewGen — Full Server, Database (`nro_newgen`) & Client

Kho mã nguồn chính thức của dự án **Ngọc Rồng NewGen**, bao gồm cơ sở dữ liệu độc lập **`nro_newgen`**, Máy chủ Game Java tùy biến, Dự án Unity Client 2 Tab - Hành Trang Lưới (`PRJ_2Tab_550K`) và bộ cài **Client PC (`Client_ChoNguoiChoiTaiVe`)**.

## Cấu trúc thư mục
- `Database_NgocRongNewGen/` : **Database độc lập chính thức của Ngọc Rồng NewGen**:
  - `nro_newgen.sql` : File SQL chính thức (chuẩn `UTF-8`, đã làm sạch 100% dữ liệu rác của bản mẫu, giữ nguyên các cơ chế riêng của Ngọc Rồng NewGen).
  - `Xuat_SQL_NewGen.bat` : Công cụ 1-click xuất lại `nro_newgen` từ MariaDB ra file `nro_newgen.sql` sau khi chỉnh sửa trong game/Navicat.
  - `SO_TAY_TRA_CUU_2_BAN_MAU.md` : Sổ tay quản trị & bảng tra cứu vật phẩm, giới hạn sức mạnh và mẫu SQL tham khảo từ 2 bản mẫu (`Teamobi2026` & `SRC NRO NEW 01`).
- `Teamobi2026/SRC/` : Mã nguồn Server Java đang vận hành của **Ngọc Rồng NewGen** (`Config.properties` kết nối trực tiếp tới `database.name=nro_newgen`, bản build `dist/NgocRongOnline.jar`, full `21.043` icon x4).
- `PRJ_2Tab_550K/` : Mã nguồn Unity Client 2 Tab - Hành Trang Lưới (tự đọc IP từ `server_ip.txt`, hỗ trợ `Tàn sát tất cả`, menu `NRO Admin -> Build Client PC 2 Tab`).
- `Client_ChoNguoiChoiTaiVe/` : Bộ Client PC cho người chơi tải về (cấu hình tên & IP máy chủ tại `server_ip.txt`).
- `Hoisinhngocrong2025/` & `SRC NRO NEW 01/` : Kho tài nguyên Icon/Part và bản mẫu tham khảo đối chiếu.

## Hướng dẫn chạy nhanh
1. Chạy `1_Chay_Database_Va_Server.bat` để tự động bật MariaDB (cổng `3306`, tự nạp `Database_NgocRongNewGen/nro_newgen.sql` nếu máy mới chưa có DB `nro_newgen`) và khởi động Game Server **Ngọc Rồng NewGen** (cổng `14445`).
2. Mở `3_Mo_Client_Test_Game.bat` để vào game:
   - **Tài khoản Admin duy nhất mặc định:** `admin` / `123456`
   - Người chơi mới có thể đăng ký/đăng nhập tự động khi `custom.autoregister=true`.
3. Khi chỉnh sửa code Java Server, chạy `2_Build_Lai_Server_Java.bat` để cập nhật `dist/NgocRongOnline.jar`.
4. Khi mở Online LAN / Radmin VPN / VPS: đổi `127.0.0.1` thành IP máy chủ trong `Teamobi2026/SRC/Config.properties` và `Client_ChoNguoiChoiTaiVe/server_ip.txt`, sau đó chạy `4_Dong_Goi_Client_Zip_Cho_Nguoi_Choi.bat` để xuất file `NgocRongNewGen_Client_Full.zip`.
