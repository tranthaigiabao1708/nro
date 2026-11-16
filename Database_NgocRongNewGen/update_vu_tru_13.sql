-- Ngọc Rồng NewGen: Expansion Phase 1 - Vũ Trụ 13 (Vũ Trụ Bị Nguyền Rủa)
SET NAMES utf8mb4;

-- 1. Player persistence columns for Vũ Trụ 13 awakening, Hide Cải Trang & Story Stage
ALTER TABLE `player` MODIFY COLUMN `data_card` TEXT NOT NULL;
ALTER TABLE `player` ADD COLUMN IF NOT EXISTS `unlock_vu_tru_13` TINYINT(1) NOT NULL DEFAULT 0;
ALTER TABLE `player` ADD COLUMN IF NOT EXISTS `hide_cai_trang` TINYINT(1) NOT NULL DEFAULT 0;
ALTER TABLE `player` ADD COLUMN IF NOT EXISTS `vu_tru_13_stage` INT(11) NOT NULL DEFAULT 0;

-- 2. Parts 2105..2119 (Black Fide, VT13 Outfits TD/NM/XD, NPC Goku Bị Phong Ấn)
DELETE FROM `part` WHERE `id` BETWEEN 2105 AND 2119;
INSERT INTO `part` (`id`, `TYPE`, `DATA`) VALUES (2105, 0, '[[27001,6,-2],[27002,6,-2],[3000,0,0]]');
INSERT INTO `part` (`id`, `TYPE`, `DATA`) VALUES (2106, 1, '[[27003,1,-3],[27004,-4,-7],[27005,-1,-8],[27006,0,-8],[27007,1,-7],[27008,4,-5],[27009,2,-6],[27010,0,-6],[27011,0,-6],[27012,-1,-8],[27013,1,-4],[27014,-3,-3],[27015,-1,-4],[27016,0,-5],[27017,0,-6],[27018,0,-5],[3001,0,0]]');
INSERT INTO `part` (`id`, `TYPE`, `DATA`) VALUES (2107, 2, '[[27019,2,-3],[27020,-4,-5],[27021,-14,-2],[27022,-7,-2],[27023,-17,-2],[27024,-9,-2],[27025,-11,-3],[27026,-1,0],[27027,-5,0],[27028,0,-2],[27029,0,0],[27030,0,0],[27031,-4,-2],[3002,0,0]]');
INSERT INTO `part` (`id`, `TYPE`, `DATA`) VALUES (2108, 0, '[[27032,-13,-26],[27033,-13,-31],[2955,0,0]]');
INSERT INTO `part` (`id`, `TYPE`, `DATA`) VALUES (2109, 1, '[[27034,1,-5],[27035,-3,-7],[27036,-1,-9],[27037,0,-10],[27038,1,-8],[27039,1,-7],[27040,2,-8],[27041,1,-7],[27042,1,-8],[27043,-6,-8],[27044,-3,-7],[27045,-3,-9],[27046,0,-9],[27047,-1,-8],[27048,-2,-9],[27049,0,-6],[2955,0,0]]');
INSERT INTO `part` (`id`, `TYPE`, `DATA`) VALUES (2110, 2, '[[27050,5,4],[27051,-4,-4],[27052,0,-7],[27053,0,-5],[27054,0,-4],[27055,0,-3],[27056,1,-4],[27057,0,1],[27058,0,-1],[27059,-2,-7],[27060,1,-7],[27061,-2,0],[27062,-2,-3],[2955,0,0]]');
INSERT INTO `part` (`id`, `TYPE`, `DATA`) VALUES (2111, 0, '[[27063,1,-22],[27064,0,-21],[2955,0,0]]');
INSERT INTO `part` (`id`, `TYPE`, `DATA`) VALUES (2112, 1, '[[27065,-3,-24],[27066,-9,-22],[27067,-8,-21],[27068,-15,-20],[27069,-21,-17],[27070,-15,-20],[27071,-22,-26],[27072,-16,-28],[27073,-8,-38],[27074,-5,-23],[27075,-10,-26],[27076,-9,-23],[27077,-10,-32],[27078,-13,-26],[27079,-10,-20],[27080,-11,-23],[2955,0,0]]');
INSERT INTO `part` (`id`, `TYPE`, `DATA`) VALUES (2113, 2, '[[27081,4,1],[27082,-4,-7],[27083,0,-8],[27084,-5,-10],[27085,-3,-9],[27086,-5,-10],[27087,-1,-9],[27088,-6,-6],[27089,-4,-6],[27090,-8,-9],[27091,-1,-6],[27092,-5,-5],[27093,-5,-7],[2955,0,0]]');
INSERT INTO `part` (`id`, `TYPE`, `DATA`) VALUES (2114, 0, '[[27094,-4,-14],[27095,-3,-11],[2955,0,0]]');
INSERT INTO `part` (`id`, `TYPE`, `DATA`) VALUES (2115, 1, '[[27096,0,-8],[27097,-3,-13],[27098,-3,-12],[27099,-5,-12],[27100,-1,-11],[27101,-1,-11],[27102,1,-11],[27103,-3,-15],[27104,-2,-10],[27105,-2,-14],[27106,0,-9],[27107,-4,-9],[27108,-4,-14],[27109,-1,-13],[27110,-5,-12],[27111,3,-9],[2955,0,0]]');
INSERT INTO `part` (`id`, `TYPE`, `DATA`) VALUES (2116, 2, '[[27112,-1,-6],[27113,-11,-7],[27114,-10,-7],[27115,-10,-7],[27116,-8,-7],[27117,-7,-7],[27118,-11,-7],[27119,-8,-3],[27120,-10,-14],[27121,-10,-9],[27122,-16,-8],[27123,-12,-6],[27124,-9,-11],[2955,0,0]]');
INSERT INTO `part` (`id`, `TYPE`, `DATA`) VALUES (2117, 0, '[[27125,1,-11],[27126,1,-12],[2955,0,0]]');
INSERT INTO `part` (`id`, `TYPE`, `DATA`) VALUES (2118, 1, '[[27127,-2,-4],[27128,-4,-8],[27129,-2,-10],[27130,-1,-10],[27131,-1,-9],[27132,-1,-8],[27133,3,-8],[27134,-9,-10],[27135,-3,-6],[27136,-9,-13],[27137,-2,-8],[27138,-2,-8],[27139,-4,-11],[27140,0,-10],[27141,-4,-10],[27142,-3,-8],[2955,0,0]]');
INSERT INTO `part` (`id`, `TYPE`, `DATA`) VALUES (2119, 2, '[[27143,-1,4],[27144,-4,-3],[27145,-2,-6],[27146,-4,-7],[27147,-5,-6],[27148,-3,-6],[27149,0,-6],[27150,-4,-1],[27151,-3,-4],[27152,-8,-7],[27153,0,-7],[27154,-4,-4],[27155,-6,-6],[2955,0,0]]');

-- 3. Item 2031: Ấn Khai Mở Vũ Trụ 13
INSERT INTO `item_template` (`id`, `TYPE`, `gender`, `NAME`, `description`, `level`, `icon_id`, `part`, `is_up_to_up`, `power_require`, `gold`, `gem`, `head`, `body`, `leg`)
VALUES (2031, 27, 3, 'Ấn Khai Mở Vũ Trụ 13', 'Thánh vật mở cổng không gian đến Vũ Trụ 13 - Thế giới khắc nghiệt của những vị thần bị nguyền rủa (Dùng vô hạn)', 0, 27000, -1, 0, 0, 0, 0, -1, -1, -1)
ON DUPLICATE KEY UPDATE `TYPE`=VALUES(`TYPE`), `gender`=VALUES(`gender`), `NAME`=VALUES(`NAME`), `description`=VALUES(`description`), `icon_id`=VALUES(`icon_id`), `is_up_to_up`=VALUES(`is_up_to_up`);

-- 4. NPC 85: Goku Bị Phong Ấn (contiguous index 85 right after 0..84)
INSERT INTO `npc_template` (`id`, `NAME`, `head`, `body`, `leg`, `avatar`) VALUES (85, 'Goku Bị Phong Ấn', 2117, 2118, 2119, 27125)
ON DUPLICATE KEY UPDATE `NAME`=VALUES(`NAME`), `head`=VALUES(`head`), `body`=VALUES(`body`), `leg`=VALUES(`leg`), `avatar`=VALUES(`avatar`);

-- 5. Mobs 119 & 120 (contiguous after 0..118)
INSERT INTO `mob_template` (`id`, `TYPE`, `NAME`, `hp`, `range_move`, `speed`, `dart_Type`, `percent_dame`, `percent_tiem_nang`) VALUES
(119, 1, 'Chiến Binh Ám Ảnh VT13', 1800000000, 45, 2, 47, 8, 25),
(120, 1, 'Thực Thể Nguyền Rủa VT13', 2500000000, 45, 2, 47, 10, 30)
ON DUPLICATE KEY UPDATE `TYPE`=VALUES(`TYPE`), `NAME`=VALUES(`NAME`), `hp`=VALUES(`hp`), `range_move`=VALUES(`range_move`), `speed`=VALUES(`speed`), `dart_Type`=VALUES(`dart_Type`), `percent_dame`=VALUES(`percent_dame`), `percent_tiem_nang`=VALUES(`percent_tiem_nang`);

-- 6. Maps 178, 179, 180 (Vũ Trụ 13 - Thế Giới Của Những Vị Thần Bị Nguyền Rủa)
INSERT INTO `map_template` (`id`, `NAME`, `zones`, `max_player`, `data`, `type`, `planet_id`, `bg_type`, `tile_id`, `bg_id`, `waypoints`, `mobs`, `npcs`, `is_map_double`) VALUES
(178, 'Khe Nứt Nguyền Rủa VT13', 10, 15, '[0,2,0,30,17]', 0, 2, 0, 30, 17, '[["Ngục Tù Phong Ấn Goku", 1608, 408, 1656, 456, 0, 0, 179, 84, 456]]', '[[119, 30, 1800000000, 264, 456], [119, 30, 1800000000, 456, 456], [119, 30, 1800000000, 672, 456], [119, 30, 1800000000, 912, 456], [119, 30, 1800000000, 1152, 456], [119, 30, 1800000000, 1392, 456], [119, 30, 1800000000, 432, 336], [119, 30, 1800000000, 840, 264], [119, 30, 1800000000, 1248, 336], [119, 30, 1800000000, 1488, 456]]', '[]', 0),
(179, 'Ngục Tù Phong Ấn Goku', 10, 15, '[0,2,0,25,8]', 0, 2, 0, 25, 8, '[["Khe Nứt Nguyền Rủa VT13", 24, 408, 72, 456, 0, 0, 178, 1560, 456], ["Điện Thờ Thần Hủy Diệt VT13", 1608, 408, 1656, 456, 0, 0, 180, 84, 456]]', '[[119, 30, 1800000000, 240, 456], [119, 30, 1800000000, 408, 456], [119, 30, 1800000000, 576, 456], [119, 30, 1800000000, 360, 312], [120, 32, 2500000000, 480, 312], [120, 32, 2500000000, 1128, 456], [120, 32, 2500000000, 1296, 456], [120, 32, 2500000000, 1464, 456], [120, 32, 2500000000, 1224, 312], [120, 32, 2500000000, 1392, 312]]', '[[85, 840, 384]]', 0),
(180, 'Điện Thờ Thần Hủy Diệt VT13', 10, 15, '[0,2,0,32,18]', 0, 2, 0, 32, 18, '[["Ngục Tù Phong Ấn Goku", 24, 408, 72, 456, 0, 0, 179, 1560, 456]]', '[[120, 32, 2500000000, 264, 456], [120, 32, 2500000000, 456, 456], [120, 32, 2500000000, 672, 456], [120, 32, 2500000000, 888, 456], [120, 32, 2500000000, 1104, 456], [120, 32, 2500000000, 1320, 456], [120, 32, 2500000000, 1512, 456], [120, 32, 2500000000, 408, 336], [120, 32, 2500000000, 840, 288], [120, 32, 2500000000, 1272, 336]]', '[]', 0)
ON DUPLICATE KEY UPDATE `NAME`=VALUES(`NAME`), `zones`=VALUES(`zones`), `max_player`=VALUES(`max_player`), `data`=VALUES(`data`), `type`=VALUES(`type`), `planet_id`=VALUES(`planet_id`), `bg_type`=VALUES(`bg_type`), `tile_id`=VALUES(`tile_id`), `bg_id`=VALUES(`bg_id`), `waypoints`=VALUES(`waypoints`), `mobs`=VALUES(`mobs`), `npcs`=VALUES(`npcs`), `is_map_double`=VALUES(`is_map_double`);

-- 7. Update Task 28 (reduce boss kills from 10 -> 5, add sub-task 8 Home Awakening) + Task 29 (Vũ Trụ 13 Phase 1) + Task 30 (End placeholder)
DELETE FROM `task_sub_template` WHERE `task_main_id` >= 28;
DELETE FROM `task_main_template` WHERE `id` >= 29;

INSERT INTO `task_main_template` (`id`, `NAME`, `detail`) VALUES
(29, 'Vũ Trụ 13 - Vũ Trụ Bị Nguyền Rủa', 'Thế giới khắc nghiệt của những vị thần bị nguyền rủa đã mở ra! Goku đang bị xiềng xích phong ấn giữa ngục tối, còn Đại Đế Fide đã tiến hóa thành Black Fide nhằm phá vỡ phong ấn Thần Hủy Diệt Vũ Trụ 13!'),
(30, '..', '...');

INSERT INTO `task_sub_template` (`task_main_id`, `NAME`, `max_count`, `notify`, `npc_id`, `map`, `ducvupro`) VALUES
(28, 'Đi theo Ôsin', 1, 'Đi theo Ôsin', 44, -1, 117),
(28, 'Hạ vua địa ngục Drabura', 5, 'Tiếp theo là hạ 5 Drabura', -1, -1, 118),
(28, 'Hạ Pui Pui', 5, 'Tiếp theo là hạ 5 Pui Pui', -1, -1, 119),
(28, 'Hạ Pui Pui lần 2', 5, 'Tiếp tục tiêu diệt 5 Pui Pui', -1, -1, 120),
(28, 'Hạ Yacôn', 5, 'Tiếp theo là hạ 5 Yacôn', -1, -1, 121),
(28, 'Hạ Drabura lần 2', 5, 'Tiếp tục tiêu diệt 5 Drabura', -1, -1, 122),
(28, 'Hạ Mabư', 5, 'Tiếp theo là hạ 5 Mabư', -1, -1, 123),
(28, 'Báo cáo với Ôsin', 1, 'Chúc mừng bạn! Giờ hãy quay về báo cáo với Ôsin', 44, -1, 124),
(28, 'Về nhà gặp %2 khai mở Sức Mạnh Vũ Trụ 13', 1, 'Hãy quay về Nhà gặp ông nội/trưởng lão tại quê nhà để khai mở Sức Mạnh Vũ Trụ 13!', -2, -2, 125),
(29, 'Dùng Ấn Khai Mở Vũ Trụ 13 tiến vào Khe Nứt Nguyền Rủa', 1, 'Sử dụng Ấn Khai Mở Vũ Trụ 13 trong hành trang để bước vào Vũ Trụ 13!', -1, -1, 126),
(29, 'Tìm Goku Bị Phong Ấn tại Ngục Tù Phong Ấn', 1, 'Tiến sang Ngục Tù Phong Ấn Goku và trò chuyện với Goku đang bị xiềng xích!', 85, -1, 127),
(29, 'Luyện tập: Hạ 30 Chiến Binh Ám Ảnh VT13', 30, 'Rèn luyện bản thân giữa trọng lực nguyền rủa: Hạ 30 Chiến Binh Ám Ảnh VT13!', -1, -1, 128),
(29, 'Luyện tập: Hạ 30 Thực Thể Nguyền Rủa VT13', 30, 'Tiếp tục tiêu diệt 30 Thực Thể Nguyền Rủa VT13 để thích nghi sát khí thần vực!', -1, -1, 129),
(29, 'Chạm trán và đánh bại Đại Đế Fide tại Điện Thờ', 1, 'Tiến vào Điện Thờ Thần Hủy Diệt VT13 và đánh bại Đại Đế Fide!', -1, -1, 130),
(29, 'Quyết chiến Black Fide ngăn chặn mở phong ấn', 1, 'Đại Đế Fide đã biến hình thành Black Fide! Hãy đánh bại hắn ngay!', -1, -1, 131),
(29, 'Quay lại báo tin dữ cho Goku Bị Phong Ấn', 1, 'Phong ấn Thần Hủy Diệt Vũ Trụ 13 đã bị Black Fide phá vỡ! Mau báo cho Goku!', 85, -1, 132),
(30, 'bạn đã xong nhiệm vụ rồi', -1, '.....', -1, -1, 133);

-- 8. Migrate players who already reached old end-marker [29,0,0,0] before unlocking Vũ Trụ 13 -> [28,8,0,0] so they immediately get the Home Base Awakening quest!
UPDATE `player` SET `data_task` = '[28,8,0,0]' WHERE (`data_task` LIKE '[29,%' OR `data_task` LIKE '[30,%') AND `unlock_vu_tru_13` = 0;
