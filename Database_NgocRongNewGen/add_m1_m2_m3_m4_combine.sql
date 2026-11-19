-- Cập nhật M1, M2, M3, M4:
-- 1. Option 258, 259 hiển thị cấp Cường hóa SKH (Lv1 -> Lv10, tối đa +20%/món = +100% khi đủ 5 món)
-- 2. Vật phẩm [1826] Đá nâng cấp SKH (icon 12181 có sẵn ở x1..x4)
-- 3. Mở bán Con dấu [1794] & Đá nâng cấp SKH [1826] tại Cửa Hàng Santa (Tab 17, đổi bằng Thỏi vàng)

SET NAMES utf8mb4;

INSERT INTO `item_option_template` (`id`, `NAME`) VALUES
  (258, '$Cường hóa SKH Cấp #'),
  (259, '$+Thêm #% uy lực Set Kích Hoạt')
ON DUPLICATE KEY UPDATE
  `NAME` = VALUES(`NAME`);

INSERT INTO `item_template` (`id`, `TYPE`, `gender`, `NAME`, `description`, `level`, `icon_id`, `part`, `is_up_to_up`, `power_require`, `gold`, `gem`, `head`, `body`, `leg`) VALUES
  (1826, 27, 3, 'Đá nâng cấp SKH', 'Đá tinh luyện đặc biệt dùng để cường hóa cấp độ dòng Set Kích Hoạt (Lv1 -> Lv10) tại Bà Hạt Mít Đảo Kamê', 0, 12181, -1, 1, 0, 0, 0, -1, -1, -1)
ON DUPLICATE KEY UPDATE
  `TYPE` = VALUES(`TYPE`),
  `gender` = VALUES(`gender`),
  `NAME` = VALUES(`NAME`),
  `description` = VALUES(`description`),
  `level` = VALUES(`level`),
  `icon_id` = VALUES(`icon_id`),
  `part` = VALUES(`part`),
  `is_up_to_up` = VALUES(`is_up_to_up`);

DELETE FROM `item_shop` WHERE `tab_id` = 17 AND `temp_id` IN (1794, 1826);
INSERT INTO `item_shop` (`tab_id`, `temp_id`, `is_new`, `is_sell`, `type_sell`, `cost`, `icon_spec`) VALUES
  (17, 1794, 1, 1, 1, 10, 4028);
