# Sổ Tay Quản Trị & Tra Cứu Database — Ngọc Rồng NewGen (`nro_newgen`)

Tài liệu này tổng hợp cấu trúc cơ sở dữ liệu chính thức **`nro_newgen`** của riêng game **Ngọc Rồng NewGen**, cùng danh mục tra cứu vật phẩm & cơ chế trích xuất từ 2 bản mẫu tham khảo (`Teamobi2026` và `SRC NRO NEW 01`) để bạn dễ dàng mở rộng về sau.

---

## 1. Thông tin Database chính thức của Ngọc Rồng NewGen
- **Tên Database (Schema):** `nro_newgen` (database mẫu `team2026` đã được xóa khỏi MariaDB; file SQL mẫu chỉ còn lưu để tra cứu tại `Mau_ThamKhao/Teamobi2026/database team2026.sql` và `Mau_ThamKhao/SRC_NRO_NEW_01/ngocrong.sql`)
- **File SQL chính thức:** `Database_NgocRongNewGen/nro_newgen.sql` (mã hóa chuẩn `UTF-8`)
- **Công cụ xuất nhanh sau khi chỉnh sửa DB:** Chạy `Database_NgocRongNewGen/Xuat_SQL_NewGen.bat`
- **Tài khoản Admin mặc định duy nhất:**
  - Tài khoản: `admin`
  - Mật khẩu: `123456`
  - Quyền hạn: Quản trị viên tối cao (`is_admin = 1`, `admin = 1`)
- **Giftcode mặc định của Ngọc Rồng NewGen:**
  - `newgen` : 50 Thỏi vàng (khóa) + Bộ item hỗ trợ Cuồng nộ / Bổ huyết / Bổ khí / Giáp xên / Ẩn danh x10 (khóa)
  - `tanthu` : 20 Thỏi vàng (khóa) + 5 Cỏ bốn lá (+20% SKH) + 2 Vé khu up SKH

---

## 2. Phân nhóm 41 Bảng trong `nro_newgen`
1. **Nhóm Người chơi & Vận hành (đã làm sạch 100% rác bản mẫu):**
   - `account` (tài khoản), `player` (nhân vật), `clan` (bang hội), `super_rank` (BXH Siêu Hạng), `shop_ky_gui` (chợ ký gửi), `giftcode` (mã quà tặng), `history_transaction` (lịch sử giao dịch).
2. **Nhóm Vật phẩm & Cửa hàng:**
   - `item_template` (`0..1999` — danh sách vật phẩm gốc + tùy chỉnh), `item_option_template` (`0..250` — dòng chỉ số trang bị), `shop` (gắn với NPC), `tab_shop` (các tab trong cửa hàng), `item_shop` & `item_shop_option` (vật phẩm bán và chỉ số đi kèm).
3. **Nhóm Bản đồ, Quái & NPC:**
   - `map_template` (169 map, gồm Map `164` = **Khu up Sét Kích Hoạt**), `mob_template` (119 loại quái), `npc_template` (93 NPC).
4. **Nhóm Kỹ năng, Nhiệm vụ, Nội tại & Ngoại hình:**
   - `skill_template`, `intrinsic`, `task_main_template`, `task_sub_template`, `side_task_template`, `clan_task_template`, `achievement_template`, `data_badges`, `task_badges_template`, `radar`, `part`, `head_avatar`, `flag_bag`, `array_head_2_frames`.

---

## 3. Các Cơ chế Riêng đã Tích hợp trong Ngọc Rồng NewGen
- **Sét Kích Hoạt (SKH):** Rơi tại 3 map đầu mỗi hành tinh tỉ lệ cơ bản `1%` (`100/10.000`), vào **Khu up Sét Kích Hoạt** (`Map 164` qua **Vé khu up SKH** `ID 1787`) cộng thêm `+20%` (`21%`), dùng thêm **Cỏ bốn lá** (`ID 1635` bán tại Santa) cộng thêm `+20%` (`41%`), kết hợp thêm nội tại/trang bị tăng may mắn, Set Gohan và ước Rồng Thần (`+20%` may mắn). Đồ SKH rơi ra có thể giao dịch tự do.
- **Ngọc Rồng 1–7 Sao (`ID 14..20`):** Ngọc Rồng 1s/2s/3s cộng `+9% / +6% / +3%` sức đánh; ước bộ 1 sao có tùy chọn nhận buff may mắn.
- **Thỏi vàng (`ID 457`):** Phân biệt rõ Thỏi vàng khóa (rơi từ farm quái / giftcode — có `Option 30` Không thể giao dịch) và Thỏi vàng thường.
- **Tinh gọn Sự kiện:** Đã gỡ toàn bộ vật phẩm sự kiện rác, Boss sự kiện và NPC sự kiện cũ (bản lưu đối chiếu nằm tại `Mau_ThamKhao/Teamobi2026/archive_removed_events_backup.sql`).

---

## 4. Danh mục Vật phẩm Tham khảo từ Bản mẫu 2 (`SRC NRO NEW 01`) để Mở rộng dần
> **Lưu ý kỹ thuật khi thêm item mới vào `nro_newgen`:**
> `ItemData.java` truyền danh sách `item_template` tuần tự theo chỉ số `0 .. N-1`. Khi thêm item mới, hãy đặt `id` nối tiếp liên tục ngay sau ID cuối cùng hiện tại (`2000, 2001, 2002...` không nhảy cóc ID), đồng thời tăng `vsItem` thêm `+1` trong `DataGame.java` để Client tự tải lại danh mục item mới.

| ID gốc (Mẫu 2) | Type | Tên vật phẩm | Icon ID (Trạng thái Icon x4) | Part | Head/Body/Leg | Mô tả tham khảo |
| :---: | :---: | :--- | :--- | :---: | :---: | :--- |
| `2000` | `5` | Cải trang Vegeta Hakai | `15009` (Có sẵn) | `-1` | `-1/-1/-1` | Cải trang thành Vegeta Hakai |
| `2001` | `50` | Vé đổi áo hủy diệt | `15012` (Có sẵn) | `-1` | `-1/-1/-1` | Tới gặp Bill để sử dụng |
| `2002` | `50` | Vé đổi quần hủy diệt | `15013` (Có sẵn) | `-1` | `-1/-1/-1` | Tới gặp Bill để sử dụng |
| `2003` | `50` | Vé đổi găng hủy diệt | `15014` (Có sẵn) | `-1` | `-1/-1/-1` | Tới gặp Bill để sử dụng |
| `2004` | `50` | Vé đổi giày hủy diệt | `15015` (Có sẵn) | `-1` | `1279/1280/1281` | Tới gặp Bill để sử dụng |
| `2005` | `50` | Vé đổi nhẫn hủy diệt | `15016` (Có sẵn) | `-1` | `1282/1283/1284` | Tới gặp Bill để sử dụng |
| `2006` | `27` | Phiếu cải trang hải tặc | `5721` (Có sẵn) | `-1` | `-1/-1/-1` | Nhận được cải trang hải tặc ngẫu nhiên 1-7 ngày |
| `2007` | `27` | Phiếu cải trang hải tặc | `5721` (Có sẵn) | `-1` | `-1/-1/-1` | Nhận được cải trang hải tặc ngẫu nhiên 7-30 ngày |
| `2008` | `27` | Phiếu cải trang hải tặc | `5721` (Có sẵn) | `-1` | `-1/-1/-1` | Nhận được cải trang hải tặc ngẫu nhiên |
| `2009` | `27` | Mảnh kích hoạt | `15017` (Có sẵn) | `-1` | `-1/-1/-1` | Dùng để nâng cấp cấp đồ kích hoạt |
| `2010` | `27` | Bùa bảo vệ Vé | `15018` (Có sẵn) | `-1` | `-1/-1/-1` | Tăng tỉ lệ nâng cấp thành công vé kích hoạt là 100% |
| `2011` | `27` | Thỏi vàng | `15019` (Có sẵn) | `-1` | `-1/-1/-1` | Thỏi vàng khóa không thể giao dịch, có thể quy đổi ra vàng |
| `2017` | `27` | Hộp Ma Quỷ | `15025` (Có sẵn) | `-1` | `-1/-1/-1` | Vật phẩm sự kiện |
| `2018` | `27` | Vé Đổi Kẹo | `15026` (Có sẵn) | `-1` | `-1/-1/-1` | Vật phẩm sự kiện |
| `2019` | `27` | Túi Bảo Vệ | `11716` (Có sẵn) | `-1` | `-1/-1/-1` | Giúp bảo vệ vật phẩm nâng cấp không bị giảm chỉ số |
| `2022` | `27` | Thẻ gia hạn | `6847` (Có sẵn) | `-1` | `-1/-1/-1` | Tới gặp bà hạt mít để gai hạn cải trang thêm 1 ngày hạn sử dụng |
| `2023` | `27` | Vé Tặng Hồng Ngọc | `14518` (Có sẵn) | `-1` | `-1/-1/-1` | Dùng Để Tặng Hồng Ngọc |
| `2024` | `27` | Hộp Quà Tân Thủ | `20178` (Lấy từ SRC NRO NEW 01) | `-1` | `-1/-1/-1` | Hộp quà tân thủ, chứa bên trong rất nhiều phần thưởng quý giá |
| `2025` | `27` | Bùa Đệ Tử Mabư Thông MInh | `1403` (Có sẵn) | `-1` | `-1/-1/-1` | Giúp đệ tử của bạn thông minh hơn,tự động ăn đậu khi hết thể lực |
| `2040` | `5` | Cải trang Black Goku | `5141` (Có sẵn) | `0` | `-1/-1/-1` | Vật phẩm sự kiện |
| `2041` | `5` | Cải trang Super Black Goku | `5173` (Có sẵn) | `0` | `-1/-1/-1` | Vật phẩm sự kiện |
| `2042` | `75` | Cải trang Trung uý Trắng | `1550` (Có sẵn) | `0` | `-1/-1/-1` | Vật phẩm sự kiện |
| `2043` | `75` | Cải trang Trung uý Xanh Lơ | `1495` (Có sẵn) | `0` | `-1/-1/-1` | Vật phẩm sự kiện |
| `2044` | `5` | Cải trang Iron Man | `18079` (Lấy từ SRC NRO NEW 01) | `-1` | `-1/-1/-1` | Cải trang thành Iron Man |
| `2045` | `5` | Cải trang Cumber | `11379` (Có sẵn) | `-1` | `1387/1388/1389` | Cải trang thành Cumber |
| `2046` | `5` | Cải trang Super Cumber | `18145` (Có sẵn) | `-1` | `-1/-1/-1` | Cải trang thành Super Cumber |
| `2047` | `5` | Cải trang Kamin | `18178` (Có sẵn) | `-1` | `-1/-1/-1` | Cải trang thành Kamin |
| `2048` | `5` | Cải trang Oren | `18210` (Có sẵn) | `-1` | `-1/-1/-1` | Cải trang thành Oren |
| `2049` | `5` | Cải trang Kamioren | `18242` (Có sẵn) | `-1` | `-1/-1/-1` | Cải trang thành Kamioren |
| `2050` | `5` | Cải trang Black Panther | `18029` (Có sẵn) | `-1` | `-1/-1/-1` | Cải trang thành Black Panther |
| `2051` | `5` | Cải trang Granolal | `17856` (Lấy từ SRC NRO NEW 01) | `-1` | `-1/-1/-1` | Cải trang thành Granolal |
| `2052` | `5` | Cải trang Cell Max | `16000` (Có sẵn) | `-1` | `-1/-1/-1` | Cải trang thành Xên Max |
| `2053` | `72` | Tử Thần | `18291` (Lấy từ SRC NRO NEW 01) | `-1` | `-1/-1/-1` | Ma Thú Trong Truyền Thuyết |
| `2054` | `72` | Tử Thần 2 | `18293` (Lấy từ SRC NRO NEW 01) | `-1` | `-1/-1/-1` | Ma Thú Trong Truyền Thuyết |
| `2055` | `72` | Thú Lửa | `18281` (Lấy từ SRC NRO NEW 01) | `-1` | `-1/-1/-1` | Có Sức Mạnh To Lớn Của Lửa |
| `2056` | `72` | Thú Băng | `18281` (Lấy từ SRC NRO NEW 01) | `-1` | `-1/-1/-1` | Có Sức Mạnh To Lớn Của Băng |
| `2057` | `72` | Rừng Xanh | `18275` (Có sẵn) | `-1` | `-1/-1/-1` | Pet Thần Thoại Rừng Xanh |
| `2058` | `72` | Chiến Binh | `18277` (Có sẵn) | `-1` | `-1/-1/-1` | Linh Thú Chiến Binh |
| `2059` | `72` | Tử Điểu | `18283` (Lấy từ SRC NRO NEW 01) | `-1` | `-1/-1/-1` | Ma Thú Chim Tối Thượng |
| `2060` | `72` | Phượng Hoàng Lửa | `18305` (Lấy từ SRC NRO NEW 01) | `-1` | `-1/-1/-1` | Vật Phẩm Quý Hiếm |
| `2061` | `27` | Hộp capsule dò boss | `7222` (Có sẵn) | `-1` | `-1/-1/-1` | Vật phẩm hỗ trợ |
| `2062` | `72` | Hồn Ma Hallowen | `20210` (Có sẵn) | `-1` | `-1/-1/-1` | Vật Phẩm Quý Hiếm |
| `2063` | `72` | Mèo Phù Thủy | `20212` (Có sẵn) | `-1` | `-1/-1/-1` | Vật Phẩm Quý Hiếm |
| `2064` | `5` | Yachirobe Xa Lánh | `11494` (Có sẵn) | `-1` | `-1/-1/-1` | Vật phẩm tham khảo từ Bản mẫu 2 |
| `2065` | `27` | Hộp quà Avatar | `-1` (Lấy từ SRC NRO NEW 01) | `-1` | `-1/-1/-1` | Chứa các cải trang mới,  tỉ lệ 10% ra avatar vĩnh viễn với tất cả chỉ số 35% |
| `2066` | `27` | Hộp cải trang (Vip) | `20178` (Lấy từ SRC NRO NEW 01) | `-1` | `-1/-1/-1` | Chứa các cải trang mới,  tỉ lệ 10% ra cải trang vĩnh viễn với tất cả chỉ số 35% |
| `2067` | `27` | Trứng Linh Thú | `14011` (Có sẵn) | `-1` | `-1/-1/-1` | Mở ra để nhận linh thú, có 10% tỉ lệ mở ra linh thú vĩnh viễn 10% |
| `2068` | `27` | Trứng pet | `14010` (Có sẵn) | `-1` | `-1/-1/-1` | Mở ra để nhận pet, có tỉ lệ mở ra pet 10% vĩnh viễn |
| `2069` | `27` | Hộp quà vật phẩm | `20178` (Lấy từ SRC NRO NEW 01) | `-1` | `-1/-1/-1` | Chứa 100 item đeo lưng,có 10 tỉ lệ mở ra 10% vĩnh viễn |
| `2070` | `27` | Hộp cải trang thường | `20178` (Lấy từ SRC NRO NEW 01) | `-1` | `-1/-1/-1` | Chứa hơn 200 cải trang các loại, tỉ lệ 10% ra cải trang vĩnh viễn với tất cả chỉ số 35% |
| `2071` | `72` | Dragon Rider | `14001` (Có sẵn) | `-1` | `-1/-1/-1` | Vật Phẩm Quý Hiếm |
| `2072` | `72` | Angel Rose | `14003` (Có sẵn) | `-1` | `-1/-1/-1` | Vật Phẩm Quý Hiếm |
| `2073` | `72` | Devil (Red) | `14005` (Có sẵn) | `-1` | `-1/-1/-1` | Vật Phẩm Quý Hiếm |
| `2074` | `72` | Devil (Pure) | `14009` (Có sẵn) | `-1` | `-1/-1/-1` | Vật Phẩm Quý Hiếm |
| `2075` | `72` | Angel (White) | `14007` (Có sẵn) | `-1` | `-1/-1/-1` | Vật Phẩm Quý Hiếm |
| `2076` | `27` | Hộp vật phẩm sự kiện | `20178` (Lấy từ SRC NRO NEW 01) | `-1` | `-1/-1/-1` | Vật phẩm sự kiện,có tỉ lệ 10% mở ra vĩnh viễn chỉ số 10% |
| `2077` | `27` | Nâng kỹ năng 5 đệ tử | `7100` (Có sẵn) | `-1` | `-1/-1/-1` | Nâng chiêu 5 của đệ tử lên 1 cấp |
| `2078` | `88` | Gậy thiên sứ | `16214` (Có sẵn) | `77` | `-1/-1/-1` | Vật phẩm sự kiện |
| `2079` | `76` | Đệ tử zenno | `16380` (Có sẵn) | `-1` | `-1/-1/-1` | Vật phẩm sự kiện |
| `2080` | `27` | Cá tiên tri | `16412` (Có sẵn) | `-1` | `-1/-1/-1` | Vật phẩm sự kiện |
| `2081` | `5` | Cải trang zenno | `16380` (Có sẵn) | `-1` | `-1/-1/-1` | Vật phẩm sự kiện |
| `2082` | `27` | Điểm quyền năng | `14614` (Có sẵn) | `-1` | `-1/-1/-1` | Vật phẩm sự kiện |
| `2083` | `27` | Hộp Quà Kích Hoạt | `20178` (Lấy từ SRC NRO NEW 01) | `-1` | `-1/-1/-1` | Mở ra để nhận set kích hoạt |
| `2084` | `27` | Đổi Skill 2 Đệ Tử | `7099` (Có sẵn) | `-1` | `-1/-1/-1` | Dùng để đổi skill đệ tử khi bạn quá đen |
| `2085` | `27` | Đổi Skill 3 Đệ Tử | `7100` (Có sẵn) | `-1` | `-1/-1/-1` | Dùng để đổi skill đệ tử khi bạn quá đen |
| `2086` | `27` | Đổi Skill 4 Đệ Tử | `7101` (Có sẵn) | `-1` | `-1/-1/-1` | Dùng để đổi skill đệ tử khi bạn quá đen |
| `2087` | `27` | Đổi Skill 5 Đệ Tử | `7101` (Có sẵn) | `-1` | `-1/-1/-1` | Dùng để đổi skill đệ tử khi bạn quá đen |
| `2088` | `27` | Hộp quà Avatar | `20178` (Lấy từ SRC NRO NEW 01) | `-1` | `-1/-1/-1` | Chứa các cải trang mới,  tỉ lệ 10% ra avatar vĩnh viễn với tất cả chỉ số 35% |
| `2089` | `27` | Hộp cải trang (Vip) | `20178` (Lấy từ SRC NRO NEW 01) | `-1` | `-1/-1/-1` | Chứa các cải trang mới,  tỉ lệ 10% ra cải trang vĩnh viễn với tất cả chỉ số 35% |
| `2090` | `27` | Trứng Linh Thú | `14011` (Có sẵn) | `-1` | `-1/-1/-1` | Mở ra để nhận linh thú, có 10% tỉ lệ mở ra linh thú vĩnh viễn 10% |
| `2091` | `27` | Trứng pet | `14010` (Có sẵn) | `-1` | `-1/-1/-1` | Mở ra để nhận pet, có tỉ lệ mở ra pet 10% vĩnh viễn |
| `2092` | `27` | Hộp vật phẩm sự kiện | `20178` (Lấy từ SRC NRO NEW 01) | `-1` | `-1/-1/-1` | Vật phẩm sự kiện,có tỉ lệ 10% mở ra vĩnh viễn chỉ số 10% |
| `2093` | `27` | Hộp cải trang thường | `20178` (Lấy từ SRC NRO NEW 01) | `-1` | `-1/-1/-1` | Chứa hơn 200 cải trang các loại, tỉ lệ 10% ra cải trang vĩnh viễn với tất cả chỉ số 35% |
| `2094` | `27` | Hộp Zamasu Tối Thượng | `14015` (Có sẵn) | `-1` | `-1/-1/-1` | Mở ra để nhận cải trang Zamasu Tối Thượng 2% tỉ lệ vĩnh viễn chỉ số đến 40% |
| `2095` | `27` | Hộp quà vật phẩm | `20178` (Lấy từ SRC NRO NEW 01) | `-1` | `-1/-1/-1` | Chứa 100 item đeo lưng,có 10 tỉ lệ mở ra 10% vĩnh viễn |
| `2096` | `27` | Hồn Linh Thú | `14047` (Có sẵn) | `-1` | `-1/-1/-1` | Dùng để nâng cấp linh thú |
| `2097` | `72` | Dragon | `14069` (Có sẵn) | `-1` | `-1/-1/-1` | Vật Phẩm Quý Hiếm |
| `2098` | `5` | Goku Sjj4 | `20371` (Có sẵn) | `-1` | `-1/-1/-1` | Khi mặc trên người sư phụ Goku Sjj4 và đệ tử Vegeta Sjj4 hợp thể sẽ có hình dạng mới |
| `2099` | `5` | Vegeta Sjj4 | `20403` (Có sẵn) | `-1` | `-1/-1/-1` | Khi mặc trên người sư phụ Goku Sjj4 và đệ tử Vegeta Sjj4 hợp thể sẽ có hình dạng mới |
| `2100` | `27` | Hộp ván bay | `20178` (Lấy từ SRC NRO NEW 01) | `-1` | `-1/-1/-1` | 2% vĩnh viễn |
| `2102` | `11` | Quạt gia cát lượng | `20497` (Có sẵn) | `95` | `-1/-1/-1` | Vật phẩm tham khảo từ Bản mẫu 2 |
| `2103` | `11` | Lục thần | `20480` (Có sẵn) | `96` | `-1/-1/-1` | Vật phẩm tham khảo từ Bản mẫu 2 |
| `2104` | `11` | Nguyện thần | `20489` (Có sẵn) | `97` | `-1/-1/-1` | Vật phẩm tham khảo từ Bản mẫu 2 |
| `2105` | `72` | Linh thú Dragon | `20478` (Lấy từ SRC NRO NEW 01) | `0` | `-1/-1/-1` | Vật phẩm tham khảo từ Bản mẫu 2 |
| `2106` | `11` | Kiếm diệt quỷ | `12321` (Có sẵn) | `0` | `-1/-1/-1` | Vật phẩm tham khảo từ Bản mẫu 2 |
| `2107` | `72` | Linh thú ngộ nghĩnh | `20476` (Có sẵn) | `0` | `-1/-1/-1` | Vật phẩm tham khảo từ Bản mẫu 2 |
| `2108` | `23` | Ván bay thần tài | `20498` (Có sẵn) | `24` | `-1/-1/-1` | Vật phẩm tham khảo từ Bản mẫu 2 |
| `2109` | `99` | Chiến Binh Rồng | `0` (Có sẵn) | `0` | `-1/-1/-1` | Vật phẩm tham khảo từ Bản mẫu 2 |
| `2110` | `11` | Gậy như ý | `20513` (Có sẵn) | `0` | `-1/-1/-1` | Vật phẩm tham khảo từ Bản mẫu 2 |
| `2111` | `11` | Trượng đường tăng | `20526` (Có sẵn) | `0` | `-1/-1/-1` | Vật phẩm tham khảo từ Bản mẫu 2 |
| `2112` | `11` | Trượng Bát Giới | `20539` (Có sẵn) | `0` | `-1/-1/-1` | Vật phẩm tham khảo từ Bản mẫu 2 |
| `2113` | `11` | Trượng sa tăng | `20552` (Lấy từ SRC NRO NEW 01) | `0` | `-1/-1/-1` | Vật phẩm tham khảo từ Bản mẫu 2 |
| `2114` | `11` | Dao chém lợn | `20555` (Lấy từ SRC NRO NEW 01) | `0` | `-1/-1/-1` | Vật phẩm tham khảo từ Bản mẫu 2 |
| `2115` | `5` | Cải Trang Kid Bill | `12793` (Có sẵn) | `-1` | `1378/1379/1380` | Vật phẩm tham khảo từ Bản mẫu 2 |
| `2116` | `72` | Linh thú Phượng Hoàng Lửa | `30959` (Lấy từ SRC NRO NEW 01) | `0` | `-1/-1/-1` | Vật phẩm tham khảo từ Bản mẫu 2 |
| `2117` | `5` | Cải Trang Goku Ultra | `18532` (Lấy từ SRC NRO NEW 01) | `-1` | `1053/1054/1055` | Vật phẩm tham khảo từ Bản mẫu 2 |
| `2118` | `72` | Linh thú Rồng Cute | `30957` (Lấy từ SRC NRO NEW 01) | `0` | `-1/-1/-1` | Vật phẩm tham khảo từ Bản mẫu 2 |
| `2119` | `5` | Cải Trang Kid Sên | `13304` (Có sẵn) | `-1` | `1692/1693/1694` | Vật phẩm tham khảo từ Bản mẫu 2 |
| `2120` | `5` | Cải Trang Kid Mabu | `13303` (Có sẵn) | `-1` | `1695/1696/1697` | Vật phẩm tham khảo từ Bản mẫu 2 |
| `2122` | `5` | Cải Trang Kid Fide | `13302` (Có sẵn) | `-1` | `1381/1382/1383` | Vật phẩm tham khảo từ Bản mẫu 2 |
| `2123` | `5` | Cải Trang Frieren | `12987` (Có sẵn) | `-1` | `1384/1385/1386` | Vật phẩm tham khảo từ Bản mẫu 2 |
| `2125` | `5` | Cải Trang Android 21 | `25032` (Lấy từ SRC NRO NEW 01) | `-1` | `1391/1392/1393` | Vật phẩm tham khảo từ Bản mẫu 2 |
| `2126` | `29` | Máy dò Boss | `2758` (Có sẵn) | `-1` | `-1/-1/-1` | Dùng để tìm kiếm Boss ở các khu |
| `2127` | `73` | Hộp quà huỷ diệt | `11251` (Có sẵn) | `-1` | `-1/-1/-1` | Hộp quà hủy diệt |
| `2128` | `5` | Cải Trang Goten | `26239` (Lấy từ SRC NRO NEW 01) | `-1` | `1416/1417/1418` | Vật phẩm tham khảo từ Bản mẫu 2 |
| `2129` | `5` | Cải Trang Fu | `32116` (Có sẵn) | `-1` | `1425/1426/1427` | Vật phẩm tham khảo từ Bản mẫu 2 |
| `2130` | `99` | Vũ thạch | `12182` (Có sẵn) | `-1` | `-1/-1/-1` | Vật phẩm tham khảo từ Bản mẫu 2 |
| `2131` | `11` | Cánh vinh quang | `30404` (Lấy từ SRC NRO NEW 01) | `0` | `-1/-1/-1` | Vật phẩm tham khảo từ Bản mẫu 2 |
| `2132` | `11` | Cánh mặt trời | `30405` (Lấy từ SRC NRO NEW 01) | `0` | `-1/-1/-1` | Vật phẩm tham khảo từ Bản mẫu 2 |
| `2133` | `11` | Cánh hắc ám | `30406` (Lấy từ SRC NRO NEW 01) | `0` | `-1/-1/-1` | Vật phẩm tham khảo từ Bản mẫu 2 |
| `2134` | `5` | Cải Trang Gohan | `26016` (Có sẵn) | `-1` | `1428/1429/1430` | Vật phẩm tham khảo từ Bản mẫu 2 |
| `2136` | `23` | Sắc Điêu Vương | `32083` (Lấy từ SRC NRO NEW 01) | `66` | `-1/-1/-1` | Vật phẩm tham khảo từ Bản mẫu 2 |
| `2137` | `27` | Capsule bang | `5005` (Có sẵn) | `-1` | `-1/-1/-1` | Dùng để nâng cấp level và thành viên trong bang |
| `2140` | `11` | Bạch vô thường | `19162` (Lấy từ SRC NRO NEW 01) | `0` | `-1/-1/-1` | Vật phẩm tham khảo từ Bản mẫu 2 |
| `2141` | `11` | Hắc vô thường | `19165` (Lấy từ SRC NRO NEW 01) | `0` | `-1/-1/-1` | Vật phẩm tham khảo từ Bản mẫu 2 |
| `2142` | `5` | Cải Trang Gogeta Ssj4 | `14047` (Có sẵn) | `-1` | `1434/1435/1436` | Vật phẩm tham khảo từ Bản mẫu 2 |
| `2143` | `11` | Goku chong chóng | `12998` (Có sẵn) | `0` | `-1/-1/-1` | Vật phẩm tham khảo từ Bản mẫu 2 |
| `2144` | `27` | Elixir Phục Hồi | `11472` (Có sẵn) | `-1` | `-1/-1/-1` | Có tác dụng hồi lại tất cả chiêu thức của người chơi |
| `2145` | `5` | Cải Trang Black Goku | `17998` (Có sẵn) | `-1` | `1437/1438/1439` | Vật phẩm tham khảo từ Bản mẫu 2 |
| `2146` | `5` | CT Black Goku Rose | `14336` (Có sẵn) | `-1` | `1440/1438/1439` | Vật phẩm tham khảo từ Bản mẫu 2 |
| `2147` | `5` | CT Black Super Saiyan 5 | `16841` (Có sẵn) | `-1` | `1441/1438/1439` | Vật phẩm tham khảo từ Bản mẫu 2 |
| `2148` | `5` | CT Broly Khởi Nguyên | `9780` (Có sẵn) | `-1` | `1442/1443/1444` | Vật phẩm tham khảo từ Bản mẫu 2 |
| `2150` | `27` | Chuông Nro Not Buff | `12316` (Có sẵn) | `-1` | `-1/-1/-1` | Dùng để thu phục tuần lộc |
| `2151` | `23` | Xe tuần lộc | `7067` (Có sẵn) | `6` | `-1/-1/-1` | Bay không tốn KI |
| `2152` | `21` | Pet tuần lộc cute | `14509` (Có sẵn) | `-1` | `-1/-1/-1` | Vật phẩm tham khảo từ Bản mẫu 2 |

---

## 5. Bảng Tham khảo Giới hạn Sức mạnh (`power_limit` từ Bản mẫu 2)
Dùng để đối chiếu cân bằng chỉ số HP / KI / Sức đánh tối đa theo từng mốc Mở giới hạn tại NPC Thượng Đế / Whis:

| Cấp mốc | Sức mạnh tối đa | HP gốc tối đa | KI gốc tối đa | Sức đánh gốc tối đa | Giáp tối đa | Chí mạng gốc |
| :--- | :---: | :---: | :---: | :---: | :---: | :---: |
| Mốc 1 | `17,999,999,999` | `215,000` | `215,000` | `8,600` | `550` | `5%` |
| Mốc 2 | `18,999,999,999` | `239,000` | `239,000` | `9,565` | `600` | `6%` |
| Mốc 3 | `20,999,999,999` | `265,000` | `265,000` | `10,628` | `700` | `7%` |
| Mốc 4 | `24,999,999,999` | `295,000` | `295,000` | `11,809` | `800` | `8%` |
| Mốc 5 | `30,999,999,999` | `328,000` | `328,000` | `13,122` | `1,000` | `9%` |
| Mốc 6 | `40,999,999,999` | `364,000` | `364,000` | `22,000` | `1,200` | `10%` |
| Mốc 7 | `60,999,999,999` | `500,000` | `500,000` | `14,580` | `1,400` | `11%` |
| Mốc 8 | `80,999,999,999` | `405,000` | `405,000` | `16,200` | `1,600` | `12%` |
| Mốc 9 | `100,999,999,999` | `450,000` | `450,000` | `18,000` | `1,700` | `13%` |
| Mốc 10 | `150,000,000,000` | `500,000` | `500,000` | `25,000` | `1,800` | `14%` |
| Mốc 11 | `200,000,000,000` | `550,000` | `550,000` | `28,000` | `1,900` | `15%` |
| Mốc 12 | `250,000,000,000` | `600,000` | `600,000` | `32,000` | `2,100` | `16%` |

---

## 6. Mẫu SQL Thêm nhanh Vật phẩm & Đưa vào Shop (`nro_newgen`)
```sql
USE nro_newgen;

-- 1) Thêm vật phẩm mới nối tiếp ID 2000 vào item_template:
INSERT INTO item_template (id, type, gender, NAME, description, level, icon_id, part, is_up_to_up, power_require, gold, gem, head, body, leg)
VALUES (2000, 5, 3, 'Cải trang Goku Ultra', 'Cải trang đặc biệt của Ngọc Rồng NewGen', 0, 18532, -1, 0, 0, 0, 0, 1450, 1451, 1452);

-- 2) Đưa vật phẩm vào Cửa hàng Santa (ví dụ tab_id = 18 là Tab Cải Trang của Santa):
INSERT INTO item_shop (tab_id, temp_id, is_new, is_sell, type_sell, cost, icon_spec)
VALUES (18, 2000, 1, 1, 1, 500, 0);
-- (type_sell: 0 = Vàng, 1 = Ngọc xanh, 3 = Hồng ngọc; nếu bán bằng vật phẩm đổi như Thỏi vàng thì type_sell = 4 và icon_spec = 4028)

-- 3) Gắn dòng chỉ số (Option) cho vật phẩm vừa thêm trong Shop (lấy item_shop.id vừa tạo):
SET @new_shop_item_id = LAST_INSERT_ID();
INSERT INTO item_shop_option (item_shop_id, option_id, param) VALUES
(@new_shop_item_id, 50, 35),  -- Sức đánh +#%
(@new_shop_item_id, 77, 35),  -- HP +#%
(@new_shop_item_id, 103, 35); -- KI +#%
```
