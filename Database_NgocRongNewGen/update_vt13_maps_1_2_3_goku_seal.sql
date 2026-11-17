-- Vũ Trụ 13: Map 1 & Map 2 (Basic Mob Maps) + Map 3 (Ngục Tù Phong Ấn Goku: Background 36, Tileset 43, Monument NPC 85)
SET NAMES utf8mb4;

-- 1. Modular Parts for Interactive Monument NPC 85 (Cột + Xích + Goku Chibi)
REPLACE INTO part (id, TYPE, DATA) VALUES
(2117, 0, '[[27210,-35,-52],[27210,-38,-53],[27210,-39,-50]]'),
(2118, 1, '[[27212,-17,-39],[27212,-17,-47],[27212,-18,-47],[27212,-15,-48],[27212,-19,-46],[27212,-17,-46],[27212,-18,-47],[27212,-11,-39],[27212,-11,-35],[27212,-19,-33],[27212,-19,-44],[27212,-16,-44],[27212,-16,-37],[27212,-14,-47],[27212,-18,-45],[27212,-20,-44],[27212,-15,-38]]'),
(2119, 2, '[[27215,-74,-105],[27214,-87,-116],[27215,-85,-115],[27214,-83,-116],[27215,-87,-115],[27214,-83,-115],[27215,-85,-116],[27214,-87,-109],[27215,-90,-113],[27214,-83,-117],[27215,-92,-101],[27214,-92,-101],[27215,-87,-105],[27214,-90,-109]]');

REPLACE INTO head_avatar (head_id, avatar_id) VALUES (2117, 27216);

UPDATE npc_template
SET NAME = 'Goku Bị Phong Ấn',
    head = 2117,
    body = 2118,
    leg  = 2119,
    avatar = 27216
WHERE id = 85;

-- 2. Map 0 (181): Thánh Điện Toàn Vương VT13 -> Cổng phải tới Map 1 (178)
UPDATE map_template
SET waypoints = '[["Khe Nứt Nguyền Rủa VT13",1656,360,1680,480,0,0,178,60,456]]'
WHERE id = 181;

-- 3. Map 1 (178): Khe Nứt Nguyền Rủa VT13 (Map Quái Cơ Bản 1: Chiến Binh Ám Ảnh VT13)
UPDATE map_template
SET data = '[0,2,0,43,34]',
    tile_id = 43,
    bg_id = 34,
    waypoints = '[["Thánh Điện Toàn Vương VT13",0,360,24,480,0,0,181,1620,456],["Vực Thẳm Nguyền Rủa VT13",1656,360,1680,480,0,0,182,60,456]]',
    mobs = '[[119,30,1800000000,264,456],[119,30,1800000000,456,456],[119,30,1800000000,1224,456],[119,30,1800000000,1416,456],[119,30,1800000000,456,336],[119,30,1800000000,840,264],[119,30,1800000000,1224,336]]',
    npcs = '[[88,840,456],[89,730,456],[90,950,456]]'
WHERE id = 178;

-- 4. Map 2 (182): Vực Thẳm Nguyền Rủa VT13 (Map Quái Cơ Bản 2: Thực Thể Nguyền Rủa VT13)
REPLACE INTO map_template (id, NAME, zones, max_player, data, type, planet_id, bg_type, tile_id, bg_id, waypoints, mobs, npcs, is_map_double)
VALUES (
    182,
    'Vực Thẳm Nguyền Rủa VT13',
    10,
    15,
    '[0,2,0,43,34]',
    0,
    2,
    0,
    43,
    34,
    '[["Khe Nứt Nguyền Rủa VT13",0,360,24,480,0,0,178,1620,456],["Ngục Tù Phong Ấn Goku",1656,360,1680,480,0,0,179,60,456]]',
    '[[120,32,2500000000,264,456],[120,32,2500000000,456,456],[120,32,2500000000,648,456],[120,32,2500000000,840,456],[120,32,2500000000,1032,456],[120,32,2500000000,1224,456],[120,32,2500000000,1416,456],[120,32,2500000000,408,312],[120,32,2500000000,840,336],[120,32,2500000000,1248,312]]',
    '[]',
    0
);

-- 5. Map 3 (179): Ngục Tù Phong Ấn Goku (Chủ đề Goku bị phong ấn: Sàn đá phẳng 43, Background 36, Cụm NPC 85 ở giữa map)
UPDATE map_template
SET data = '[0,2,0,43,36]',
    tile_id = 43,
    bg_id = 36,
    waypoints = '[["Vực Thẳm Nguyền Rủa VT13",0,360,24,480,0,0,182,1620,456],["Điện Thờ Thần Hủy Diệt VT13",1656,360,1680,480,0,0,180,60,456]]',
    mobs = '[]',
    npcs = '[[85,840,456]]'
WHERE id = 179;

-- 6. Map 4 (180): Điện Thờ Thần Hủy Diệt VT13 (Đồng bộ sàn đá hắc ám 43 & cổng trái về Map 3: 179)
UPDATE map_template
SET data = '[0,2,0,43,21]',
    tile_id = 43,
    waypoints = '[["Ngục Tù Phong Ấn Goku",0,360,24,480,0,0,179,1620,456]]'
WHERE id = 180;
