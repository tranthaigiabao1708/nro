-- =====================================================================
-- DRAGON BOY Z:
-- 1. Cập nhật ngoại hình Pharaoh-Mummy cho Boss Thần Hủy Diệt Bunny (part 2126, 2127, 2128)
-- 2. Thêm bộ 7 Ấn mở khóa (1 -> 7) [2050..2056] & Cải trang Thần Hủy Diệt Bunny [2057]
-- 3. Hoàn thiện chuỗi Nhiệm vụ 30 (Giải Phong Ấn Goku & mở tiếp chuỗi nhiệm vụ Thần Vực mới) + Nhiệm vụ 31 (End-cap)
-- =====================================================================

SET NAMES utf8mb4;

INSERT INTO `part` (`id`, `type`, `DATA`) VALUES
  (2126, 0, '[[27220,1,-23],[27221,1,-22],[27222,0,-22]]'),
  (2127, 1, '[[27228,-7,-21],[27223,-5,-24],[27224,-5,-23],[27224,-2,-24],[27223,-6,-24],[27224,-3,-23],[27224,-4,-23],[27225,-12,-21],[27227,-2,-20],[27228,-5,-16],[27226,-9,-23],[27226,-7,-23],[27225,-12,-20],[27226,-9,-24],[27227,-4,-25],[27226,-9,-23],[27225,-12,-21]]'),
  (2128, 2, '[[27231,-1,-9],[27229,-1,-20],[27230,-7,-17],[27230,-5,-18],[27229,-1,-19],[27230,-5,-17],[27230,-7,-18],[27231,-14,-14],[27232,-12,-14],[27232,-5,-18],[27231,-16,-6],[27231,-16,-6],[27233,-9,-6],[27231,-14,-14]]')
ON DUPLICATE KEY UPDATE
  `type` = VALUES(`type`),
  `DATA` = VALUES(`DATA`);

INSERT INTO `item_template` (`id`, `TYPE`, `gender`, `NAME`, `description`, `level`, `icon_id`, `part`, `is_up_to_up`, `power_require`, `gold`, `gem`, `head`, `body`, `leg`) VALUES
  (2050, 27, 3, 'Ấn mở khóa 1', 'Ấn cổ đại số 1/7 rơi từ Thần Hủy Diệt Bunny. Thu thập đủ 7 Ấn (1 -> 7) đưa cho Goku Bị Phong Ấn để giải phong ấn!', 0, 27290, -1, 1, 0, 0, 0, -1, -1, -1),
  (2051, 27, 3, 'Ấn mở khóa 2', 'Ấn cổ đại số 2/7 rơi từ Thần Hủy Diệt Bunny. Thu thập đủ 7 Ấn (1 -> 7) đưa cho Goku Bị Phong Ấn để giải phong ấn!', 0, 27291, -1, 1, 0, 0, 0, -1, -1, -1),
  (2052, 27, 3, 'Ấn mở khóa 3', 'Ấn cổ đại số 3/7 rơi từ Thần Hủy Diệt Bunny. Thu thập đủ 7 Ấn (1 -> 7) đưa cho Goku Bị Phong Ấn để giải phong ấn!', 0, 27292, -1, 1, 0, 0, 0, -1, -1, -1),
  (2053, 27, 3, 'Ấn mở khóa 4', 'Ấn cổ đại số 4/7 rơi từ Thần Hủy Diệt Bunny. Thu thập đủ 7 Ấn (1 -> 7) đưa cho Goku Bị Phong Ấn để giải phong ấn!', 0, 27293, -1, 1, 0, 0, 0, -1, -1, -1),
  (2054, 27, 3, 'Ấn mở khóa 5', 'Ấn cổ đại số 5/7 rơi từ Thần Hủy Diệt Bunny. Thu thập đủ 7 Ấn (1 -> 7) đưa cho Goku Bị Phong Ấn để giải phong ấn!', 0, 27294, -1, 1, 0, 0, 0, -1, -1, -1),
  (2055, 27, 3, 'Ấn mở khóa 6', 'Ấn cổ đại số 6/7 rơi từ Thần Hủy Diệt Bunny. Thu thập đủ 7 Ấn (1 -> 7) đưa cho Goku Bị Phong Ấn để giải phong ấn!', 0, 27295, -1, 1, 0, 0, 0, -1, -1, -1),
  (2056, 27, 3, 'Ấn mở khóa 7', 'Ấn cổ đại số 7/7 rơi từ Thần Hủy Diệt Bunny. Thu thập đủ 7 Ấn (1 -> 7) đưa cho Goku Bị Phong Ấn để giải phong ấn!', 0, 27296, -1, 1, 0, 0, 0, -1, -1, -1),
  (2057, 5,  3, 'Cải trang Thần Hủy Diệt Bunny', 'Hóa thân thành Thần Hủy Diệt Bunny cổ đại của Vũ Trụ 13', 0, 27234, -1, 0, 40000000000, 0, 0, 2126, 2127, 2128)
ON DUPLICATE KEY UPDATE
  `TYPE` = VALUES(`TYPE`),
  `gender` = VALUES(`gender`),
  `NAME` = VALUES(`NAME`),
  `description` = VALUES(`description`),
  `icon_id` = VALUES(`icon_id`),
  `is_up_to_up` = VALUES(`is_up_to_up`),
  `power_require` = VALUES(`power_require`),
  `head` = VALUES(`head`),
  `body` = VALUES(`body`),
  `leg` = VALUES(`leg`);

INSERT INTO `task_main_template` (`id`, `NAME`, `detail`) VALUES
  (30, 'Giải Phong Ấn Goku & Đại Chiến Thần Vực', 'Săn Thần Hủy Diệt Bunny tại Điện Thờ VT13 để thu thập đủ 7 Ấn mở khóa (1 -> 7), giải thoát Son Goku khỏi xiềng xích phong ấn và bước vào cuộc chiến Thần Vực Vô Cực!'),
  (31, 'Huyền Thoại Dragon Boy Z', 'Chúc mừng bạn đã hoàn thành toàn bộ nhiệm vụ cốt truyện hiện tại của Dragon Boy Z!')
ON DUPLICATE KEY UPDATE
  `NAME` = VALUES(`NAME`),
  `detail` = VALUES(`detail`);

DELETE FROM `task_sub_template` WHERE `task_main_id` IN (30, 31);

INSERT INTO `task_sub_template` (`task_main_id`, `NAME`, `max_count`, `notify`, `npc_id`, `map`) VALUES
  (30, 'Thu thập đủ 7 Ấn mở khóa (1 -> 7) từ Boss Bunny và Giải Phong Ấn cho Goku', 1, 'Săn Thần Hủy Diệt Bunny tại Điện Thờ VT13 nhặt đủ 7 Ấn mở khóa (1 -> 7) rồi đưa cho Goku Bị Phong Ấn!', 85, 179),
  (30, 'Sát cánh cùng ý chí Goku: Hạ 50 Thực Thể Nguyền Rủa VT13 tại Vực Thẳm', 50, 'Goku đã thoát khỏi xiềng xích! Hãy rèn luyện tiêu diệt 50 Thực Thể Nguyền Rủa VT13!', -1, 182),
  (30, 'Thu thập 15 Mảnh Thần Vực Vô Cực tại Vũ Trụ 13', 15, 'Tiêu diệt quái vật tại Vũ Trụ 13 để thu thập 15 Mảnh Thần Vực Vô Cực!', -1, 180),
  (30, 'Hạ gục Thần Hủy Diệt Bunny tại Điện Thờ Thần Hủy Diệt VT13', 1, 'Tiến vào Điện Thờ Thần Hủy Diệt VT13 và kết liễu Thần Hủy Diệt Bunny!', -1, 180),
  (30, 'Đến Thánh Điện gặp Đại Thần Quan nhận ban phước Vô Cực', 1, 'Quay về Thánh Điện Zeno VT13 gặp Đại Thần Quan để nhận đại thưởng hoàn tất nhiệm vụ!', 86, 181),
  (31, 'Bạn đã hoàn thành hết nhiệm vụ chính tuyến!', -1, 'Chúc mừng chiến binh huyền thoại của Dragon Boy Z!', -1, -1);

UPDATE `player` SET `data_task` = '[30,0,0]' WHERE `data_task` LIKE '[30,%';
