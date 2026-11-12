# Ngọc Rồng NewGen — Full Server, Database (`nro_newgen`) & Client

Kho mã nguồn chính thức của dự án **Ngọc Rồng NewGen**, bao gồm cơ sở dữ liệu độc lập **`nro_newgen`**, Máy chủ Game Java tùy biến, Dự án Unity Client 2 Tab - Hành Trang Lưới (`NgocRongNewGen_Unity`) và bộ cài **Client PC (`NgocRongNewGen_Client`)**.

## Cấu trúc thư mục
- `Database_NgocRongNewGen/` : **Database độc lập chính thức của Ngọc Rồng NewGen**:
  - `nro_newgen.sql` : File SQL chính thức (chuẩn `UTF-8`, đã làm sạch 100% dữ liệu rác của bản mẫu, không chứa dữ liệu nhân vật cá nhân).
  - `data_cau_hinh_shop_item.sql` & `CapNhat_Shop_Item_Tu_Git.bat` : Bộ dữ liệu **27 bảng cấu hình tĩnh** (Shop, Vật phẩm, Map, Quái, NPC, Hào quang...) giúp đồng bộ cập nhật Store/Item sang máy khác mà **giữ nguyên 100% dữ liệu nhân vật**.
  - `Xuat_SQL_NewGen.bat` : Công cụ 1-click xuất lại cấu hình Shop/Item từ MariaDB ra `data_cau_hinh_shop_item.sql` và `nro_newgen.sql` (an toàn không xuất dữ liệu `player`/`account`).
  - `NHAT_KY_CAP_NHAT_SHOP_ITEM_VA_TINH_NANG.md` : **Tài liệu chi tiết toàn bộ Vật phẩm mới, bảng giá/chỉ số Store (Shop Chi Chi, Trọng Tài VT6, Bill, Whis) và các tính năng Giai đoạn 1 & 2 đã triển khai**.
  - `SO_TAY_TRA_CUU_2_BAN_MAU.md` : Sổ tay quản trị & bảng tra cứu vật phẩm, giới hạn sức mạnh và mẫu SQL tham khảo từ 2 bản mẫu (`Teamobi2026` & `SRC NRO NEW 01`).
- `NgocRongNewGen_Server/` : Mã nguồn Server Java đang vận hành của **Ngọc Rồng NewGen** (`Config.properties` kết nối trực tiếp tới `database.name=nro_newgen`, bản build `dist/NgocRongNewGen.jar`, full `21.043` icon x4).
- `NgocRongNewGen_Unity/` : Mã nguồn Unity Client 2 Tab - Hành Trang Lưới (tự đọc IP từ `server_ip.txt`, hỗ trợ `Tàn sát tất cả`, menu `NRO Admin -> Build Client PC Ngoc Rong NewGen`, build bằng `5_Build_Client_NgocRongNewGen_Unity.bat`).
- `NgocRongNewGen_Client/` : Bộ Client PC `NgocRongNewGen.exe` cho người chơi tải về (cấu hình tên & IP máy chủ tại `server_ip.txt`).
- `Mau_ThamKhao/` : **Chỉ chứa ảnh & data vật phẩm** lấy từ các bản mẫu để tham khảo khi thêm item mới (mã nguồn / bản build của bản mẫu đã được gỡ bỏ, không dùng để chạy):
  - `Hoisinhngocrong2025/` : `icons_x4/` (~14.430 icon) + `nr_part.json`.
  - `SRC_NRO_NEW_01/` : `resources/` (icon, ảnh, map...), `Eff/`, `mob/`, `item template.xlsx`, `ngocrong.sql` (data item mẫu).
  - `Teamobi2026/` : `item.xlsx`, `database team2026.sql` (data item mẫu), `archive_removed_events_backup.sql` (NPC/shop sự kiện cũ), `Lệnh admin.docx`.

## Hướng dẫn chạy nhanh
0. **Cách nhanh nhất:** chạy `0_KhoiDong_Server_Va_AdminPanel.bat`. Script sẽ bật MariaDB, nạp DB nếu thiếu, mở Game Server trong cửa sổ riêng, rồi tự mở **Web Admin Panel** tại http://localhost:8686.
   - Mật khẩu panel: `admin.panel.password` trong `NgocRongNewGen_Server/Config.properties` (**mặc định `NewGen@2026`, nhớ đổi**).
   - Chức năng của panel:
     - Xem người chơi online, trạng thái server (RAM, luồng, DB).
     - Gửi vật phẩm (có option), cộng/trừ vàng, ngọc, hồng ngọc, gửi Thỏi vàng khóa/thường.
     - Kick, Ban/Mở ban tài khoản, gửi thông báo toàn server.
     - Tạo/xóa Giftcode.
     - Lưu toàn bộ dữ liệu, bảo trì đếm ngược, Lưu & Tắt server an toàn.
   - Giao diện là file `NgocRongNewGen_Server/admin_panel/index.html` (sửa trực tiếp, không cần build lại). Nhật ký thao tác admin ghi tại `NgocRongNewGen_Server/log/admin_panel.log`.
   - Muốn vào panel từ máy khác: đặt `admin.panel.host=0.0.0.0` và dùng mật khẩu mạnh.
1. Chạy `1_Chay_Database_Va_Server.bat` để tự động bật MariaDB (cổng `3306`, tự nạp `Database_NgocRongNewGen/nro_newgen.sql` nếu máy mới chưa có DB `nro_newgen`) và khởi động Game Server **Ngọc Rồng NewGen** (cổng `14445`) ngay trong cửa sổ hiện tại.
2. Mở `3_Mo_Client_Test_Game.bat` để vào game:
   - **Tài khoản Admin duy nhất mặc định:** `admin` / `123456`
   - Người chơi mới có thể đăng ký/đăng nhập tự động khi `custom.autoregister=true`.
3. Khi chỉnh sửa code Java Server, chạy `2_Build_Lai_Server_Java.bat` để cập nhật `dist/NgocRongNewGen.jar`.
4. Khi mở Online LAN / Radmin VPN / VPS: đổi `127.0.0.1` thành IP máy chủ trong `NgocRongNewGen_Server/Config.properties` và `NgocRongNewGen_Client/server_ip.txt`, sau đó chạy `4_Dong_Goi_Client_Zip_Cho_Nguoi_Choi.bat` để xuất file `NgocRongNewGen_Client_Full.zip`.
