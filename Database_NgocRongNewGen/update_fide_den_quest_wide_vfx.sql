-- 1. Mở rộng bề ngang Boss Fide Đen thêm +30%, giữ nguyên chiều cao (icon mới 27405..27433 tránh cache cũ)
UPDATE part SET DATA = '[[27405,2,-7],[27406,0,-8],[27407,-1,-5]]' WHERE id = 2114;
UPDATE part SET DATA = '[[27408,-11,-2],[27409,-12,-10],[27410,-12,-10],[27411,-10,-11],[27412,-14,-9],[27413,-12,-9],[27414,-13,-10],[27415,-5,-2],[27416,-6,2],[27417,-14,4],[27418,-14,-7],[27419,-11,-7],[27415,-11,0],[27420,-9,-10],[27421,-13,-8],[27421,-15,-7],[27422,-10,-1]]' WHERE id = 2115;
UPDATE part SET DATA = '[[27423,2,5],[27424,-12,-6],[27425,-10,-5],[27426,-8,-6],[27427,-12,-5],[27428,-8,-5],[27429,-10,-6],[27430,-11,1],[27431,-14,-3],[27432,-8,-7],[27430,-16,9],[27430,-16,9],[27433,-12,5],[27430,-14,1]]' WHERE id = 2116;

-- 2. Sửa nhiệm vụ Vũ Trụ 13 (Task 29): Chỉ cần tiêu diệt Fide Đen (xuất hiện ngẫu nhiên ở bất kỳ đâu trong 3 map VT13)
UPDATE task_main_template
SET detail = 'Thế giới khắc nghiệt của những vị thần bị nguyền rủa đã mở ra! Goku đang bị xiềng xích phong ấn giữa ngục tối, hãy lùng sục khắp 3 bản đồ Vũ Trụ 13 và tiêu diệt Fide Đen!'
WHERE id = 29;

UPDATE task_sub_template
SET NAME = 'Tiêu diệt Fide Đen tại Vũ Trụ 13 (Khe Nứt, Ngục Tù hoặc Vực Thẳm)',
    max_count = 1,
    notify = 'Truy lùng và tiêu diệt Boss Fide Đen xuất hiện ngẫu nhiên tại 1 trong 3 map Vũ Trụ 13!',
    npc_id = -1,
    map = -1
WHERE task_main_id = 29 AND ducvupro = 130;

DELETE FROM task_sub_template WHERE task_main_id = 29 AND ducvupro = 131;

UPDATE task_sub_template
SET NAME = 'Quay lại báo tin cho Goku Bị Phong Ấn',
    notify = 'Đã đánh bại Fide Đen! Mau quay lại Ngục Tù Phong Ấn báo tin cho Son Goku!'
WHERE task_main_id = 29 AND ducvupro = 132;

-- Đồng bộ tiến độ người chơi đang ở bước 6 cũ (báo cáo Goku sau khi gộp 2 bước Fide thành 1 bước) về index 5 mới
UPDATE player SET data_task = REPLACE(data_task, '[29,6,', '[29,5,') WHERE data_task LIKE '[29,6,%';
