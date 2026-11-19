-- 1. Đổi tên Mảnh Thần Vực Vô Cực [2034] -> Mảnh Đại Thiên Sứ
UPDATE item_template
SET NAME = 'Mảnh Đại Thiên Sứ',
    description = 'Mảnh thần lực cực hiếm rơi tại Vũ Trụ 13 & Đại Hội Võ Thuật VT13. Đem tới Đại Thiên Sứ để đổi Trang bị Thần Vực Vô Cực!'
WHERE id = 2034;

-- 2. Tách riêng icon độc quyền [27297] cho Đá nâng cấp SKH [1826] (không còn trùng icon 12181 với Sao pha lê đỏ lấp lánh)
UPDATE item_template
SET icon_id = 27297
WHERE id = 1826;

-- 3. Đồng bộ tên Mảnh Đại Thiên Sứ trong nhiệm vụ chính tuyến Task 30
UPDATE task_sub_template
SET NAME = 'Thu thập 15 Mảnh Đại Thiên Sứ tại Vũ Trụ 13',
    notify = 'Tiêu diệt quái vật hoặc Boss tại Vũ Trụ 13 để thu thập 15 Mảnh Đại Thiên Sứ!'
WHERE ducvupro = 166;

UPDATE task_sub_template
SET notify = 'Thần Hủy Diệt Bunny đã được Black Fide đánh thức! Mau báo cho Goku!'
WHERE ducvupro = 132;
