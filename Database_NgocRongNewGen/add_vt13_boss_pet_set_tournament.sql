-- Vũ Trụ 13 Endgame Update:
-- 1. Item Options (256, 257) cho Set Thần Vực Vô Cực
-- 2. Trứng Đệ Tử Thiên Sứ (2033) & Mảnh Thần Vực Vô Cực (2034)
-- 3. 15 Trang bị Set Thần Vực Vô Cực (2035..2049 - Cấp 16, trên Đồ Thiên Sứ)
-- 4. NPC Trọng Tài Liên Vũ Trụ (91) & Map -1 Đại Hội Võ Thuật VT13 (186, bên trái Map Home 181)

SET NAMES utf8mb4;

INSERT INTO `item_option_template` (`id`, `NAME`) VALUES
  (256, 'Set Thần Vực Vô Cực'),
  (257, '$[5] +40% Xuyên giáp, +35% SĐ/HP/KI/SĐCM & Hào Quang Vô Cực')
ON DUPLICATE KEY UPDATE
  `NAME` = VALUES(`NAME`);

INSERT INTO `item_template` (`id`, `TYPE`, `gender`, `NAME`, `description`, `level`, `icon_id`, `part`, `is_up_to_up`, `power_require`, `gold`, `gem`, `head`, `body`, `leg`) VALUES
  (2033, 27, 3, 'Trứng Đệ Tử Thiên Sứ', 'Ấp nở Đệ Tử Thiên Sứ tối thượng (Hợp thể 200% sức mạnh, liên tục hồi 5% HP tối đa mỗi giây cho Sư Phụ kể cả khi đang hợp thể)', 1, 27270, -1, 1, 0, 0, 0, -1, -1, -1),
  (2034, 27, 3, 'Mảnh Thần Vực Vô Cực', 'Mảnh năng lượng Vô Cực cực hiếm rơi tại Vũ Trụ 13 & Đại Hội Võ Thuật VT13. Đem tới Đại Thiên Sứ để đổi Trang bị Thần Vực Vô Cực!', 1, 27271, -1, 1, 0, 0, 0, -1, -1, -1),
  (2035, 0, 0, 'Áo Thần Vực Vô Cực', 'Áo giáp tối thượng Vũ Trụ 13 (Mặc đủ 5 món Vô Cực: +40% xuyên giáp, +35% SĐ/HP/KI/SĐCM & Hào Quang Vô Cực)', 16, 27272, 1105, 0, 0, 0, 0, -1, -1, -1),
  (2036, 0, 1, 'Áo Thần Vực Vô Cực', 'Áo giáp tối thượng Vũ Trụ 13 (Mặc đủ 5 món Vô Cực: +40% xuyên giáp, +35% SĐ/HP/KI/SĐCM & Hào Quang Vô Cực)', 16, 27273, 1115, 0, 0, 0, 0, -1, -1, -1),
  (2037, 0, 2, 'Áo Thần Vực Vô Cực', 'Áo giáp tối thượng Vũ Trụ 13 (Mặc đủ 5 món Vô Cực: +40% xuyên giáp, +35% SĐ/HP/KI/SĐCM & Hào Quang Vô Cực)', 16, 27274, 1110, 0, 0, 0, 0, -1, -1, -1),
  (2038, 1, 0, 'Quần Thần Vực Vô Cực', 'Quần tối thượng Vũ Trụ 13 (Mặc đủ 5 món Vô Cực: +40% xuyên giáp, +35% SĐ/HP/KI/SĐCM & Hào Quang Vô Cực)', 16, 27275, 1106, 0, 0, 0, 0, -1, -1, -1),
  (2039, 1, 1, 'Quần Thần Vực Vô Cực', 'Quần tối thượng Vũ Trụ 13 (Mặc đủ 5 món Vô Cực: +40% xuyên giáp, +35% SĐ/HP/KI/SĐCM & Hào Quang Vô Cực)', 16, 27276, 1116, 0, 0, 0, 0, -1, -1, -1),
  (2040, 1, 2, 'Quần Thần Vực Vô Cực', 'Quần tối thượng Vũ Trụ 13 (Mặc đủ 5 món Vô Cực: +40% xuyên giáp, +35% SĐ/HP/KI/SĐCM & Hào Quang Vô Cực)', 16, 27277, 1111, 0, 0, 0, 0, -1, -1, -1),
  (2041, 2, 0, 'Găng Thần Vực Vô Cực', 'Găng tay tối thượng Vũ Trụ 13 (Mặc đủ 5 món Vô Cực: +40% xuyên giáp, +35% SĐ/HP/KI/SĐCM & Hào Quang Vô Cực)', 16, 27278, -1, 0, 0, 0, 0, -1, -1, -1),
  (2042, 2, 1, 'Găng Thần Vực Vô Cực', 'Găng tay tối thượng Vũ Trụ 13 (Mặc đủ 5 món Vô Cực: +40% xuyên giáp, +35% SĐ/HP/KI/SĐCM & Hào Quang Vô Cực)', 16, 27279, -1, 0, 0, 0, 0, -1, -1, -1),
  (2043, 2, 2, 'Găng Thần Vực Vô Cực', 'Găng tay tối thượng Vũ Trụ 13 (Mặc đủ 5 món Vô Cực: +40% xuyên giáp, +35% SĐ/HP/KI/SĐCM & Hào Quang Vô Cực)', 16, 27280, -1, 0, 0, 0, 0, -1, -1, -1),
  (2044, 3, 0, 'Giầy Thần Vực Vô Cực', 'Giầy tối thượng Vũ Trụ 13 (Mặc đủ 5 món Vô Cực: +40% xuyên giáp, +35% SĐ/HP/KI/SĐCM & Hào Quang Vô Cực)', 16, 27281, -1, 0, 0, 0, 0, -1, -1, -1),
  (2045, 3, 1, 'Giầy Thần Vực Vô Cực', 'Giầy tối thượng Vũ Trụ 13 (Mặc đủ 5 món Vô Cực: +40% xuyên giáp, +35% SĐ/HP/KI/SĐCM & Hào Quang Vô Cực)', 16, 27282, -1, 0, 0, 0, 0, -1, -1, -1),
  (2046, 3, 2, 'Giầy Thần Vực Vô Cực', 'Giầy tối thượng Vũ Trụ 13 (Mặc đủ 5 món Vô Cực: +40% xuyên giáp, +35% SĐ/HP/KI/SĐCM & Hào Quang Vô Cực)', 16, 27283, -1, 0, 0, 0, 0, -1, -1, -1),
  (2047, 4, 0, 'Nhẫn Thần Vực Vô Cực', 'Nhẫn tối thượng Vũ Trụ 13 (Mặc đủ 5 món Vô Cực: +40% xuyên giáp, +35% SĐ/HP/KI/SĐCM & Hào Quang Vô Cực)', 16, 27284, -1, 0, 0, 0, 0, -1, -1, -1),
  (2048, 4, 1, 'Nhẫn Thần Vực Vô Cực', 'Nhẫn tối thượng Vũ Trụ 13 (Mặc đủ 5 món Vô Cực: +40% xuyên giáp, +35% SĐ/HP/KI/SĐCM & Hào Quang Vô Cực)', 16, 27285, -1, 0, 0, 0, 0, -1, -1, -1),
  (2049, 4, 2, 'Nhẫn Thần Vực Vô Cực', 'Nhẫn tối thượng Vũ Trụ 13 (Mặc đủ 5 món Vô Cực: +40% xuyên giáp, +35% SĐ/HP/KI/SĐCM & Hào Quang Vô Cực)', 16, 27286, -1, 0, 0, 0, 0, -1, -1, -1)
ON DUPLICATE KEY UPDATE
  `TYPE` = VALUES(`TYPE`),
  `gender` = VALUES(`gender`),
  `NAME` = VALUES(`NAME`),
  `description` = VALUES(`description`),
  `level` = VALUES(`level`),
  `icon_id` = VALUES(`icon_id`),
  `part` = VALUES(`part`),
  `is_up_to_up` = VALUES(`is_up_to_up`),
  `power_require` = VALUES(`power_require`);

INSERT INTO `npc_template` (`id`, `NAME`, `head`, `body`, `leg`, `avatar`) VALUES
  (91, 'Trọng Tài Liên Vũ Trụ', 114, 115, 116, 1411)
ON DUPLICATE KEY UPDATE
  `NAME` = VALUES(`NAME`),
  `head` = VALUES(`head`),
  `body` = VALUES(`body`),
  `leg` = VALUES(`leg`),
  `avatar` = VALUES(`avatar`);

UPDATE `map_template`
SET `waypoints` = '[["Đại Hội Võ Thuật VT13",0,360,24,480,0,0,186,1620,456],["Khe Nứt Nguyền Rủa VT13",1656,360,1680,480,0,0,178,60,456]]'
WHERE `id` = 181;

INSERT INTO `map_template` (`id`, `NAME`, `zones`, `max_player`, `data`, `type`, `planet_id`, `bg_type`, `tile_id`, `bg_id`, `waypoints`, `mobs`, `npcs`, `is_map_double`) VALUES
  (186, 'Đại Hội Võ Thuật VT13', 20, 30, '[500,500]', 0, 2, 0, 27, 35, '[["Thánh Điện Toàn Vương VT13",1656,360,1680,480,0,0,181,60,456]]', '[]', '[[91,840,456]]', 0)
ON DUPLICATE KEY UPDATE
  `NAME` = VALUES(`NAME`),
  `zones` = VALUES(`zones`),
  `max_player` = VALUES(`max_player`),
  `data` = VALUES(`data`),
  `type` = VALUES(`type`),
  `planet_id` = VALUES(`planet_id`),
  `bg_type` = VALUES(`bg_type`),
  `tile_id` = VALUES(`tile_id`),
  `bg_id` = VALUES(`bg_id`),
  `waypoints` = VALUES(`waypoints`),
  `mobs` = VALUES(`mobs`),
  `npcs` = VALUES(`npcs`),
  `is_map_double` = VALUES(`is_map_double`);
