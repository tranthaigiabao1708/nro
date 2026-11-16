DELETE FROM part WHERE id >= 2105;

UPDATE item_template SET icon_id = 15412 WHERE id = 2031;

UPDATE npc_template SET NAME = 'Goku Bị Phong Ấn', head = 0, body = 523, leg = 524, avatar = 516 WHERE id = 85;

UPDATE mob_template SET TYPE = 1, NAME = 'Chiến Binh Ám Ảnh VT13', hp = 1800000000, range_move = 30, speed = 2, dart_Type = 10, percent_dame = 1, percent_tiem_nang = 25 WHERE id = 119;
UPDATE mob_template SET TYPE = 1, NAME = 'Thực Thể Nguyền Rủa VT13', hp = 2500000000, range_move = 30, speed = 1, dart_Type = 47, percent_dame = 1, percent_tiem_nang = 30 WHERE id = 120;

UPDATE map_template SET
  waypoints = '[[\"Ngục Tù Phong Ấn Goku\",1596,336,1680,480,0,0,179,132,456]]',
  mobs = '[[119,30,1800000000,384,456],[119,30,1800000000,552,456],[119,30,1800000000,720,456],[119,30,1800000000,912,456],[119,30,1800000000,1080,456],[119,30,1800000000,1248,456],[119,30,1800000000,456,336],[119,30,1800000000,840,264],[119,30,1800000000,1224,336]]'
WHERE id = 178;

UPDATE map_template SET
  waypoints = '[[\"Khe Nứt Nguyền Rủa VT13\",0,336,84,480,0,0,178,1548,456],[\"Điện Thờ Thần Hủy Diệt VT13\",1596,336,1680,480,0,0,180,132,456]]',
  mobs = '[[119,30,1800000000,384,456],[119,30,1800000000,552,456],[119,30,1800000000,432,312],[120,32,2500000000,1104,456],[120,32,2500000000,1272,456],[120,32,2500000000,1200,312]]'
WHERE id = 179;

UPDATE map_template SET
  waypoints = '[[\"Ngục Tù Phong Ấn Goku\",0,336,84,480,0,0,179,1548,456]]',
  mobs = '[[120,32,2500000000,408,456],[120,32,2500000000,600,456],[120,32,2500000000,792,456],[120,32,2500000000,1008,456],[120,32,2500000000,1224,456],[120,32,2500000000,456,336],[120,32,2500000000,840,288],[120,32,2500000000,1224,336]]'
WHERE id = 180;

UPDATE player SET hide_cai_trang = 0;