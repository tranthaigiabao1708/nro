SET NAMES utf8mb4;
-- Add Vo Cuc progression columns to player if not exists
ALTER TABLE `player` ADD COLUMN IF NOT EXISTS `vo_cuc_type` TINYINT NOT NULL DEFAULT 0;
ALTER TABLE `player` ADD COLUMN IF NOT EXISTS `vo_cuc_level` INT NOT NULL DEFAULT 1;

-- Insert new Chibi UI, Chibi UE, and Zeno-Sama parts (2105..2113)
REPLACE INTO `part` (`id`, `TYPE`, `DATA`) VALUES (2105, 0, '[[27010,-6,-22],[27011,-4,-21],[2955,0,0]]');
REPLACE INTO `part` (`id`, `TYPE`, `DATA`) VALUES (2106, 1, '[[27012,-4,-7],[27013,-7,-11],[27014,-10,-13],[2955,0,0],[2955,0,0],[2955,0,0],[2955,0,0],[27015,-5,-15],[27016,-12,-17],[27017,-6,-14],[27018,-11,-17],[27019,-12,-19],[27020,-7,-18],[27021,-9,-16],[27022,-11,-15],[27023,-8,-15],[2955,0,0]]');
REPLACE INTO `part` (`id`, `TYPE`, `DATA`) VALUES (2107, 2, '[[27024,0,5],[27025,-4,-5],[27026,-4,-6],[27027,-9,-19],[27028,-11,-17],[27029,-7,-17],[27030,-7,-17],[27031,-5,-1],[27032,-6,-13],[27033,-6,-7],[27034,-5,-5],[27035,-6,-5],[27036,-4,-8],[2955,0,0]]');
REPLACE INTO `part` (`id`, `TYPE`, `DATA`) VALUES (2108, 0, '[[27045,-2,-18],[27046,-4,-21],[2955,0,0]]');
REPLACE INTO `part` (`id`, `TYPE`, `DATA`) VALUES (2109, 1, '[[27047,1,-7],[27048,-2,-11],[27049,-7,-15],[27050,-5,-15],[27051,-6,-12],[27052,-4,-11],[27053,-2,-13],[27054,-3,-13],[27055,-5,-14],[27056,-5,-13],[27057,-5,-10],[27058,-6,-12],[27059,-4,-14],[27060,-4,-14],[27061,-5,-14],[27062,-2,-9],[2955,0,0]]');
REPLACE INTO `part` (`id`, `TYPE`, `DATA`) VALUES (2110, 2, '[[27063,6,3],[27064,-2,-6],[27065,-3,-7],[27066,-2,-6],[27067,-5,-6],[27068,-3,-7],[27069,-2,-7],[27070,0,-2],[27071,0,-2],[27072,-8,-8],[27073,-2,-6],[27074,-6,-3],[27075,-5,-5],[2955,0,0]]');
REPLACE INTO `part` (`id`, `TYPE`, `DATA`) VALUES (2111, 0, '[[27090,-4,-11],[27090,-4,-11],[27090,-4,-11]]');
REPLACE INTO `part` (`id`, `TYPE`, `DATA`) VALUES (2112, 1, '[[27091,-3,-10],[27091,-3,-10],[27091,-3,-10],[27091,-3,-10],[27091,-3,-10],[27091,-3,-10],[27091,-3,-10],[27091,-3,-10],[27091,-3,-10],[27091,-3,-10],[27091,-3,-10],[27091,-3,-10],[27091,-3,-10],[27091,-3,-10],[27091,-3,-10],[27091,-3,-10],[27091,-3,-10]]');
REPLACE INTO `part` (`id`, `TYPE`, `DATA`) VALUES (2113, 2, '[[27092,-1,-1],[27092,-1,-1],[27092,-1,-1],[27092,-1,-1],[27092,-1,-1],[27092,-1,-1],[27092,-1,-1],[27092,-1,-1],[27092,-1,-1],[27092,-1,-1],[27092,-1,-1],[27092,-1,-1],[27092,-1,-1],[27092,-1,-1]]');

-- Register Outer Wrapping Auras in img_by_name
INSERT INTO `img_by_name` (`NAME`, `n_frame`) VALUES ('aura_vt13_ui_0', 4) ON DUPLICATE KEY UPDATE `n_frame` = 4;
INSERT INTO `img_by_name` (`NAME`, `n_frame`) VALUES ('aura_vt13_ui_1', 4) ON DUPLICATE KEY UPDATE `n_frame` = 4;
INSERT INTO `img_by_name` (`NAME`, `n_frame`) VALUES ('aura_vt13_ue_0', 4) ON DUPLICATE KEY UPDATE `n_frame` = 4;
INSERT INTO `img_by_name` (`NAME`, `n_frame`) VALUES ('aura_vt13_ue_1', 4) ON DUPLICATE KEY UPDATE `n_frame` = 4;

-- Insert NPC 86 (Đại Thần Quan) & NPC 87 (Zeno-Sama)
REPLACE INTO `npc_template` (`id`, `NAME`, `head`, `body`, `leg`, `avatar`) VALUES (86, 'Đại Thần Quan', 703, 704, 705, 6578);
REPLACE INTO `npc_template` (`id`, `NAME`, `head`, `body`, `leg`, `avatar`) VALUES (87, 'Zeno-Sama', 2111, 2112, 2113, 27095);

-- Insert Map 0 of Vu Tru 13 (id = 181: Thánh Điện Toàn Vương VT13, no mobs, Home Sanctuary)
REPLACE INTO `map_template` (`id`, `NAME`, `zones`, `max_player`, `data`, `type`, `planet_id`, `bg_type`, `tile_id`, `bg_id`, `waypoints`, `mobs`, `npcs`) VALUES (181, 'Thánh Điện Toàn Vương VT13', 5, 20, '[500,500]', 0, 2, 0, 27, 35, '[["Khe Nứt Nguyền Rủa VT13",1656,384,1680,480,0,0,178,60,456]]', '[]', '[[86,168,456],[87,420,456]]');

-- Update Map 1 (178: Khe Nứt Nguyền Rủa VT13) waypoints so left edge goes back to Map 0 (181)
UPDATE `map_template` SET `waypoints` = '[["Thánh Điện Toàn Vương VT13",0,384,24,480,0,0,181,1620,456],["Ngục Tù Phong Ấn Goku",1656,384,1680,480,0,0,179,60,456]]' WHERE `id` = 178;
