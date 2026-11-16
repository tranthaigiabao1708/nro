SET NAMES utf8mb4;

-- 1. Restore previous item icon (27000) for Ấn Khai Mở Vũ Trụ 13 [2031]
UPDATE `item_template` SET `icon_id` = 27000 WHERE `id` = 2031;

-- 2. Update Maps 178, 179, 180:
--    * Edge-only waypoints ('đi kịch map'): Left x=0..24 (arrive x=60), Right x=1656..1680 (arrive x=1620)
--    * Map 178 (Khe Nứt Nguyền Rủa VT13): Dark Cosmic Universe (bg_id=22, tile_id=27)
--    * Map 179 (Ngục Tù Phong Ấn Goku): Dark Void & Chains (bg_id=34, tile_id=27)
--    * Map 180 (Điện Thờ Thần Hủy Diệt VT13): Dark Crimson Abyss (bg_id=21, tile_id=27)
UPDATE `map_template`
SET `data` = '[0,2,0,27,22]',
    `bg_type` = 0,
    `tile_id` = 27,
    `bg_id` = 22,
    `waypoints` = '[["Ngục Tù Phong Ấn Goku", 1656, 384, 1680, 480, 0, 0, 179, 60, 456]]'
WHERE `id` = 178;

UPDATE `map_template`
SET `data` = '[0,2,0,27,34]',
    `bg_type` = 0,
    `tile_id` = 27,
    `bg_id` = 34,
    `waypoints` = '[["Khe Nứt Nguyền Rủa VT13", 0, 384, 24, 480, 0, 0, 178, 1620, 456], ["Điện Thờ Thần Hủy Diệt VT13", 1656, 384, 1680, 480, 0, 0, 180, 60, 456]]'
WHERE `id` = 179;

UPDATE `map_template`
SET `data` = '[0,2,0,27,21]',
    `bg_type` = 0,
    `tile_id` = 27,
    `bg_id` = 21,
    `waypoints` = '[["Ngục Tù Phong Ấn Goku", 0, 384, 24, 480, 0, 0, 179, 1620, 456]]'
WHERE `id` = 180;
