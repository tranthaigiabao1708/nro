# Nhật Ký Cập Nhật Vật Phẩm, Cửa Hàng (Store) & Tính Năng Mới — Ngọc Rồng NewGen

> **Lưu ý đồng bộ Database (An toàn 100% dữ liệu nhân vật):**
> - Toàn bộ dữ liệu cấu hình tĩnh (**27 bảng**: `item_template`, `item_option_template`, `shop`, `tab_shop`, `item_shop`, `item_shop_option`, `map_template`, `mob_template`, `npc_template`, `img_by_name`, `part`, `radar`, ...) đã được xuất ra `Database_NgocRongNewGen/data_cau_hinh_shop_item.sql` và bản khởi tạo sạch `Database_NgocRongNewGen/nro_newgen.sql` thông qua `Xuat_SQL_NewGen.bat`.
> - **Không chứa và không ghi đè** bất kỳ dữ liệu người chơi nào (`account`, `player`, `clan`, `shop_ky_gui`, `super_rank`, ...).
> - Trên máy khác chỉ cần `git pull` rồi chạy `Database_NgocRongNewGen/CapNhat_Shop_Item_Tu_Git.bat` (hoặc khởi động Server) là tự động cập nhật toàn bộ Store & Item mới mà giữ nguyên nhân vật hiện có.

---

## 1. Danh Sách Vật Phẩm Mới & Vật Phẩm Chỉnh Sửa (`item_template` & `img_by_name`)

### 1.1. Vật phẩm thêm mới (`[2017 - 2029]`) + Cập nhật `[723]`
| ID | Tên vật phẩm | Loại (`type`) | Icon ID | `part` | Chức năng / Ghi chú |
| :---: | :--- | :---: | :---: | :---: | :--- |
| **723** | **Hồn Boss Lam** | `27` | `11471` | `-1` | Rơi `100% x1` từ nhóm Boss Sơ - Trung cấp (Kuku, Mập Đầu Đinh, Rambo, Tiểu Đội Sát Thủ, Fide, Android 13-15/19/Dr.Kôrê, Pic-Poc-KingKong, Băng Bojack, Nhân Bản). |
| **2017** | **Hồn Boss Lục** | `27` | `11476` | `-1` | Rơi `100% x1` từ nhóm Boss Sự Kiện, Phó Bản & Phụ Tá Majin (Trung Úy Xanh Lơ, Gấu Tướng Cướp, Bui Bui, Yacon, Drabura, Goku/Cadic Majin, Mabu 12h). Hoặc luyện từ `10 Hồn Lam + 20 Thỏi vàng`. |
| **2018** | **Hồn Boss Tím** | `27` | `11475` | `-1` | Rơi `100% x1` từ nhóm Boss Cao Cấp (Xên Bọ Hùng, Siêu Bọ Hùng, Mabu 2H, Super Bu, Cooler, Golden Frieza, Black Goku, Super Broly, Baby, Cumber, Hirudegarn). Hoặc luyện từ `10 Hồn Lục + 50 Thỏi vàng`. |
| **2019** | **Ma Hồn Hủy Diệt** | `27` | `16187` | `-1` | **Khóa độc quyền** chỉ rơi từ Boss Vũ Trụ 6 (Frost, Magetta, Cabba, Caulifla rơi `x1`; **Sát Thủ Hit rơi `x2`**; **Trùm ẩn Champa rơi `x3`**). Không thể luyện tại Chi Chi. |
| **2020** | **La Bàn Tinh Vân VT6** | `27` | `32715` | `-1` | Bảo vật vĩnh viễn (dùng không mất, giống Nhẫn Thời Không `[992]`) mở cổng 5s sang **Map 169 - Tinh Vân Champa**. Đã bổ sung đủ icon `x1–x4`. |
| **2021** | **Hải Đồ Hành Tinh Sadala** | `27` | `12954` | `-1` | Bảo vật vĩnh viễn mở cổng 5s sang **Map 176 - Hành Tinh Sadala**. |
| **2022** | **Lệnh Bài Võ Đài Champa** | `27` | `15412` | `-1` | Bảo vật vĩnh viễn mở cổng 5s vào thẳng **Map 145 - Võ Đài Siêu Cấp Liên Vũ Trụ 6 & 7**. |
| **2023** | **Hào quang Vô Cực Ám** | `35` | `32111` | `11` | Hào quang săn Boss cao cấp tại Shop Chi Chi (`Aura #11`). |
| **2024** | **Thuốc Giải & Cách Nhiệt** | `27` | `11473` | `-1` | Dược phẩm 30 phút: Miễn nhiễm độc tố sương mù Frost (`Map 173`) & hơi nóng dung nham Metalman (`Map 174`). |
| **2025** | **Ấn Phá Thời Gian** | `27` | `11474` | `-1` | Phù ấn 30 phút: Hóa giải đóng băng Time-Skip của **Sát Thủ Hit** và giảm 50% sát thương điểm huyệt tại Võ Đài Siêu Cấp (`Map 145`). |
| **2026** | **CT Sát Thủ Hit VIP** | `5` | `4969` | `520` | Cải trang Át Chủ Bài Vũ Trụ 6 (`head=520, body=521, leg=522`). |
| **2027** | **CT Thần Hủy Diệt Champa VIP** | `5` | `4879` | `511` | Cải trang Thần Hủy Diệt Vũ Trụ 6 (`head=511, body=512, leg=513`). |
| **2028** | **Hào Quang Quán Quân Vũ Trụ** | `35` | `32148` | **`24`** | Sử dụng hiệu ứng **`Aura #24`** (`aura_24_0` 4 frames lớp sau + `aura_24_1` 4 frames lớp trước trong `img_by_name`, đủ zoom `x1–x4`). |
| **2029** | **Ấn Triệu Tập Zeno** | `27` | `15415` | `-1` | **Vật phẩm cầu nối duy nhất sang Phase 3**: Mở cổng đến chiến trường thu thập **Ngọc Rồng Siêu Cấp** & giao tranh PK theo Bang hội để triệu hồi Rồng Thần ước nguyện. |

### 1.2. Vật phẩm chỉnh sửa tên & mô tả trong `item_template` (`[1071 - 1086]`)
- **Công thức Thường (`[1071]` Trái Đất, `[1072]` Namếc, `[1073]` Xayda):**
  - Đổi tên hiển thị kèm hành tinh `Công thức (Trái Đất / Namếc / Xayda)`.
  - Mô tả ghi rõ: *Tỉ lệ gốc 25%, chỉ số gốc +0-25%, 0% may mắn sẵn, dòng thưởng +1-4%. (Bản VIP: tỉ lệ 35%, chỉ số +10-35%, sẵn +10% may mắn, dòng thưởng +2-5%)*.
- **Công thức VIP (`[1084]` Trái Đất, `[1085]` Namếc, `[1086]` Xayda):**
  - Đổi tên hiển thị kèm hành tinh `Công thức VIP (Trái Đất / Namếc / Xayda)`.
  - Mô tả ghi rõ: *Tỉ lệ gốc 35% (thường 25%), đảm bảo chỉ số gốc +10-35% (thường +0-25%), sẵn +10% may mắn nổ dòng thưởng, biên độ dòng thưởng +2-5% (thường +1-4%)*.
- **Đá nâng cấp Cấp 1–5 (`[1074 - 1078]`) & Đá may mắn Cấp 1–5 (`[1079 - 1083]`):**
  - Ghi rõ chính xác mức cộng thêm **`+10% -> +50%` tỉ lệ thành công** và **`+10% -> +50%` tỉ lệ nổ 1–3 dòng Chỉ số thưởng**.

---

## 2. Chi Tiết Chỉnh Sửa & Bổ Sung Các Cửa Hàng (Store / `shop`)

### 2.1. Shop Hồn Boss Chi Chi — Đảo Kamê (`Map 5`, tọa độ `x=220, y=408`)
- **`shop_id = 37`**, `tag_name = CHICHI_SOUL`, `type_shop = 3` (đổi bằng vật phẩm Hồn Boss).
- Khi mở menu NPC Chi Chi, hộp thoại hiển thị trực tiếp **số lượng từng loại Hồn đang có trong hành trang** kèm danh sách nhóm Boss rơi từng bậc Hồn.
- Hỗ trợ **mua trực tiếp ngay lập tức** khi bấm *"Mua bằng X"* (và tương thích các nút *Mua 20 / 50 / 100 lần* của Client, không hỏi lặp lại hộp thoại phụ).

| Tab | ID Vật phẩm | Tên vật phẩm | Số lượng / Gói | Giá bán | Đơn vị Hồn | Chỉ số (`item_shop_option`) |
| :--- | :---: | :--- | :---: | :---: | :--- | :--- |
| **Tab 1: Nguyên Liệu** (`tab_id=66`) | `220–224` | Đá lục bảo / Saphia / Ruby / Titan / Thạch anh tím | **x100 viên / lần** | `5` | Hồn Boss Lam `[723]` | Đá nâng cấp trang bị (đã tắt rơi khi farm quái thường) |
| **Tab 1: Nguyên Liệu** | `1423` | Hematite | **x5 viên / lần** | `5` | Hồn Boss Lam `[723]` | Sao pha lê cấp 2 & Cường hóa lỗ 8–9 |
| **Tab 1: Nguyên Liệu** | `1438` | Dùi đục | **x2 cái / lần** | `5` | Hồn Boss Lam `[723]` | Đục lỗ trang bị |
| **Tab 1: Nguyên Liệu** | `1439` | Đá mài | **x2 viên / lần** | `5` | Hồn Boss Lam `[723]` | Đánh bóng Sao Pha Lê cấp 2 |
| **Tab 1: Nguyên Liệu** | `987` | Đá bảo vệ | **x10 viên / lần** | `2` | Hồn Boss Lục `[2017]` | Chống rớt cấp khi đập đồ |
| **Tab 1: Nguyên Liệu** | `1084–1086` | Công thức VIP (TĐ / NM / XD) | `x1` | `3` | Hồn Boss Tím `[2018]` | Khóa GD (`30:1`) |
| **Tab 1: Nguyên Liệu** | `1820` | Mảnh vỡ bông tai cấp 3 | **x500 mảnh / lần** | `3` | Hồn Boss Tím `[2018]` | Số lượng `31:500` |
| **Tab 2: Hào Quang** (`tab_id=67`) | `2008` | Hào quang Lam Cực | `x1` | `25` | Hồn Boss Lục `[2017]` | `+10% SĐ, +10% HP, +10% KI, +25% TNSM` |
| **Tab 2: Hào Quang** | `2009` | Hào quang Tử Cực | `x1` | `20` | Hồn Boss Tím `[2018]` | `+14% SĐ, +12% HP, +12% KI, +5% SĐCM` |
| **Tab 2: Hào Quang** | `2023` | Hào quang Vô Cực Ám | `x1` | `45` | Hồn Boss Tím `[2018]` | `+18% SĐ, +18% HP, +18% KI, +8% SĐCM, +15% Kháng KC (227)` |
| **Tab 3: Linh Thú** (`tab_id=68`) | `1654` | Pet Cerberus | `x1` | `30` | Hồn Boss Lục `[2017]` | `+12% SĐ/HP/KI, Kháng lạnh (106)` |
| **Tab 3: Linh Thú** | `1729` | Pet Rồng Xanh | `x1` | `25` | Hồn Boss Tím `[2018]` | `+16% SĐ/HP/KI, +6% SĐCM` |
| **Tab 3: Linh Thú** | `1568` | Pet Shimo | `x1` | `35` | Hồn Boss Tím `[2018]` | `+18% SĐ/HP/KI, Giảm 10% ST (94), +15% Kháng KC (227)` |
| **Tab 3: Linh Thú** | `1550` | Pet Godzilla | `x1` | `50` | Hồn Boss Tím `[2018]` | `+20% SĐ, +22% HP, +18% KI, +8% SĐCM` |
| **Tab 4: Thú Cưỡi** (`tab_id=69`) | `1513` | Song mã hoàng gia | `x1` | `25` | Hồn Boss Lục `[2017]` | `+10% SĐ/HP/KI` |
| **Tab 4: Thú Cưỡi** | `1144` | Phượng hoàng lửa | `x1` | `35` | Hồn Boss Lục `[2017]` | `+12% SĐ/HP/KI, +5% SĐCM` |
| **Tab 4: Thú Cưỡi** | `1676` | Phong hỏa luân | `x1` | `30` | Hồn Boss Tím `[2018]` | `+16% SĐ/HP/KI, +25% Kháng KC (227)` |
| **Tab 4: Thú Cưỡi** | `1734` | Thú cưỡi Phong Xích Lan | `x1` | `45` | Hồn Boss Tím `[2018]` | `+18% SĐ/HP/KI, +8% SĐCM, +30% Kháng KC (227)` |
| **Tab 5: Tín Vật VT6** (`tab_id=70`) | `2020` | La Bàn Tinh Vân VT6 | Vĩnh viễn | `50` | Hồn Boss Tím `[2018]` | Mở cổng Tinh Vân Champa |
| **Tab 5: Tín Vật VT6** | `2021` | Hải Đồ Hành Tinh Sadala | Vĩnh viễn | `80` | Hồn Boss Tím `[2018]` | Mở cổng Hành Tinh Sadala |
| **Tab 5: Tín Vật VT6** | `2022` | Lệnh Bài Võ Đài Champa | Vĩnh viễn | `120` | Hồn Boss Tím `[2018]` | Mở cổng Võ Đài Siêu Cấp |

---

### 2.2. Shop Trọng Tài Võ Đài Liên Vũ Trụ 6 & 7 — Võ Đài Siêu Cấp (`Map 145`)
- **`shop_id = 38`**, `tag_name = TRONG_TAI_VT6`, `type_shop = 3`.

| Tab | ID Vật phẩm | Tên vật phẩm | Giá bán | Đơn vị tiền tệ | Chỉ số (`item_shop_option`) |
| :--- | :---: | :--- | :---: | :--- | :--- |
| **Tab 1: Chiến Thuật VT6** (`tab_id=71`) | `2024` | Thuốc Giải & Cách Nhiệt | `2` | Hồn Boss Tím `[2018]` | Kháng độc Frost & nhiệt Metalman 30 phút |
| **Tab 1: Chiến Thuật VT6** | `2025` | Ấn Phá Thời Gian | `5` | Hồn Boss Tím `[2018]` | Kháng Time-Skip của Hit 30 phút |
| **Tab 1: Chiến Thuật VT6** | `1078` | Đá nâng cấp cấp 5 | `3` | Ma Hồn Hủy Diệt `[2019]` | `+50%` tỉ lệ chế đồ Thiên Sứ |
| **Tab 1: Chiến Thuật VT6** | `1083` | Đá may mắn cấp 5 | `3` | Ma Hồn Hủy Diệt `[2019]` | `+50%` tỉ lệ nổ dòng thưởng Thiên Sứ |
| **Tab 1: Chiến Thuật VT6** | `1084–1086` | Công thức VIP (TĐ / NM / XD) | `2` | Ma Hồn Hủy Diệt `[2019]` | Công thức chế đồ Thiên Sứ VIP |
| **Tab 2: Trang Bị Giải Đấu** (`tab_id=72`) | `2026` | CT Sát Thủ Hit VIP | `60` | Ma Hồn Hủy Diệt `[2019]` | `+18.000 SĐ, +40% SĐ, +28% HP/KI, +30% SĐCM, +18% CM, +15% Xuyên giáp, +15% Né đòn, Kháng lạnh, Khóa GD` |
| **Tab 2: Trang Bị Giải Đấu** | `2027` | CT Thần Hủy Diệt Champa VIP | `90` | Ma Hồn Hủy Diệt `[2019]` | `+22.000 SĐ, +44% SĐ, +35% HP/KI, +35% SĐCM, +20% CM, Kháng lạnh, Kháng TDHS (116), Khóa GD` |
| **Tab 2: Trang Bị Giải Đấu** | `2028` | Hào Quang Quán Quân Vũ Trụ (`Aura #24`) | `75` | Ma Hồn Hủy Diệt `[2019]` | `+24% SĐ, +24% HP, +24% KI, +12% SĐCM, +30% Kháng KC (227), Khóa GD` |
| **Tab 3: Cầu Nối Phase 3** (`tab_id=73`) | `2029` | Ấn Triệu Tập Zeno | `120` | Ma Hồn Hủy Diệt `[2019]` | Bảo vật cầu nối duy nhất sang chiến trường Phase 3 (Ngọc Rồng Siêu Cấp & PK Bang Hội) |

---

### 2.3. Chuẩn Hóa Shop Bill (`BILL`) & Shop/Chế Tạo Thiên Sứ Whis (`THIEN_SU`)
1. **Shop Thần Hủy Diệt Bill (`Map 48`, `shop_id = 11`):**
   - Bắt buộc mặc đủ **5 món Đồ Thần Linh (`[555 - 567]`)** và có ít nhất **`x99` Thức ăn cùng loại (`[663 - 667]`)** cả khi mở Shop lẫn ngay trước khi trừ tiền mua món (tránh lỗi cũ trừ vàng trước khi kiểm tra đủ thức ăn).
   - **Tăng tỉ lệ săn Thức ăn (`[663 - 667]` — có nhân đầy đủ chỉ số May mắn, Cỏ Bốn Lá & Thú cưỡi):**
     - Mặc đủ 5 món Đồ Thần Linh đánh quái bất kỳ: tăng từ `0.9%` -> **`2%` gốc**.
     - Đánh quái tại cụm map **Vũ Trụ 6**: tăng từ `1.4%–1.8%` -> **`5%` gốc**.
     - Tự động gộp chồng (`x99`) đồng nhất mọi nguồn thức ăn trong hành trang.
2. **Shop & Chế Tạo Đồ Thiên Sứ Whis (`Map 154`, `shop_id = 20`):**
   - Sửa triệt để **3 lỗi logic nghiêm trọng** trong `CheTaoTrangBiThienSu.java`:
     1. Tách riêng bước **Xem trước thông tin (`showInfoCombine`)** và bước **Bấm nút "Chế tạo" (`cheTaoTrangBiThienSu`)**, xóa bỏ hoàn toàn hàm rác cũ lệch công thức.
     2. Phân biệt rõ **Công thức Thường (`25%` gốc)** vs **Công thức VIP (`35%` gốc)** + Đá nâng cấp Cấp 1–5 (`+10% -> +50%`, tối đa `75%` bản Thường / `85%` bản VIP). Thất bại chỉ mất **`99 Mảnh Thiên Sứ`** (giữ lại `900 mảnh`), thành công mới trừ `999 mảnh`.
     3. Sửa dứt điểm lỗi nhân đôi (`+100%`) chỉ số và nhân nhầm vào `Yêu cầu sức mạnh 30 tỉ`: chỉ số chính tăng chuẩn **`+0% -> +25%`** (Thường) và **`+10% -> +35%`** (VIP, áp dụng đầy đủ cho cả Găng/Áo/Quần/Giày/Nhẫn); sửa dòng thưởng từ `option 15` bị nhầm sang đúng **`option 41` (`Chỉ số thưởng +N:`)** với Đá may mắn Cấp 1–5 (`+10% -> +50%`, bản VIP sẵn `+10%`, tối đa `60%`).

---

## 3. Tóm Tắt Giai Đoạn 2 — Cụm Map & Dàn Boss Giải Đấu Liên Vũ Trụ 6 & 7
- **7 Bản đồ liên thông:** `169` (*Tinh Vân Champa*) -> `170` (*Vành Đai Hành Tinh Vô Danh*) -> `173` (*Đế Chế Frost - Độc tố*) -> `174` (*Lõi Dung Nham Metalman - Hơi nóng*) -> `176` (*Hành Tinh Sadala*) -> `177` (*Đỉnh Thiêng Sadala*) -> `145` (*Võ Đài Siêu Cấp Liên Vũ Trụ 6 & 7*).
- **6 Tuyển thủ & Thần Hủy Diệt Vũ Trụ 6 (`src/nro/models/boss/vodai_vt6/`):**
  1. **Frost 3 Cấp** (`Frost Dạng 1 -> Xung Kích -> Hoàn Thiện`, Map `173`)
  2. **Người Máy Auta Magetta** (Map `174`)
  3. **Saiyan Cabba** (Map `176`)
  4. **Caulifla -> SSJ2 Caulifla** (Map `177`)
  5. **Sát Thủ Hit** (Map `145` — tuyệt kỹ *Time-Skip*, rơi **`x2` Ma Hồn Hủy Diệt**)
  6. **Trùm ẩn Thần Hủy Diệt Champa** (Map `145` — triệu hồi tự động `25%` sau khi hạ Hit hoặc gọi bằng `10 Ma Hồn Hủy Diệt` tại Trọng Tài, rơi **`x3` Ma Hồn Hủy Diệt**).
- **Cơ chế Giáp Boss Cao Cấp 2 Tầng (Không khóa trần sát thương mỗi đòn):** Đòn đánh càng mạnh sát thương thực càng lớn; cấu hình max đồ Hủy Diệt SKH full sao + phụ kiện tung tuyệt kỹ mạnh nhất đạt ngưỡng **`<= 2 tỷ sát thương/đòn`** trên Frost và giảm dần theo bậc giáp uy áp cho tới Hit & Champa.
