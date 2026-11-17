package nro.models.services;

import nro.models.consts.ConstTask;
import nro.models.database.PlayerDAO;
import nro.models.item.Item;
import nro.models.map.Zone;
import nro.models.network.Message;
import nro.models.npc.Npc;
import nro.models.player.Player;
import nro.models.server.ServerNotify;
import nro.models.utils.Logger;

public class VuTru13Service {

    public static final byte CMD_VU_TRU_13 = -126;
    public static final short ITEM_AN_KHAI_MO_VT13 = 2031;

    public static final int MAP_KHE_NUT_VT13 = 178;
    public static final int MAP_VUC_THAM_VT13 = 182;
    public static final int MAP_NGUC_TU_GOKU_VT13 = 179;
    public static final int MAP_DIEN_THO_HUY_DIET_VT13 = 180;
    public static final int MAP_THANH_DIEN_ZENO_VT13 = 181;

    public static final byte VO_CUC_NONE = 0;
    public static final byte BAN_NANG_VO_CUC = 1;
    public static final byte BAN_NGA_VO_CUC = 2;

    // Bitflags lưu trong vuTru13Stage (0..127)
    public static final int STAGE_FLAG_AWAKENED = 1;
    public static final int STAGE_FLAG_BLACK_FIDE_SEAL = 2;
    public static final int STAGE_FLAG_KHE_NUT_INTRO = 4;
    public static final int STAGE_FLAG_CHAMPA_SOUL = 8;
    public static final int STAGE_FLAG_THAN_HUY_DIET_SLAIN = 16;

    // Cutscene IDs
    public static final int CUTSCENE_KHE_NUT_12_GODS = 5;

    // Boss Thần Hủy Diệt VT13 Combat VFX types (subCmd = 3)
    public static final byte BOSS_VFX_HAKAI_SPHERE = 1;
    public static final byte BOSS_VFX_EGYPT_SIGIL_PILLAR = 2;
    public static final byte BOSS_VFX_SOUL_FLASH_STORM = 3;

    public static final int PYRAMID_KHE_NUT_X = 840;
    public static final int PYRAMID_KHE_NUT_Y = 456;

    private static VuTru13Service instance;

    public static VuTru13Service gI() {
        if (instance == null) {
            instance = new VuTru13Service();
        }
        return instance;
    }

    public static boolean isMapVuTru13(int mapId) {
        return mapId == MAP_THANH_DIEN_ZENO_VT13
                || mapId == MAP_KHE_NUT_VT13
                || mapId == MAP_VUC_THAM_VT13
                || mapId == MAP_NGUC_TU_GOKU_VT13
                || mapId == MAP_DIEN_THO_HUY_DIET_VT13;
    }

    public static boolean isCombatMapVuTru13(int mapId) {
        return mapId == MAP_KHE_NUT_VT13
                || mapId == MAP_VUC_THAM_VT13
                || mapId == MAP_NGUC_TU_GOKU_VT13
                || mapId == MAP_DIEN_THO_HUY_DIET_VT13;
    }

    public void sendState(Player player) {
        if (player == null || !player.isPl() || player.getSession() == null) {
            return;
        }
        Message msg = null;
        try {
            msg = new Message(CMD_VU_TRU_13);
            msg.writer().writeByte(0); // subCmd = 0: sync state
            msg.writer().writeBoolean(player.unlockVuTru13);
            msg.writer().writeBoolean(player.hideCaiTrang);
            msg.writer().writeByte(Math.max(0, Math.min(127, player.vuTru13Stage)));
            msg.writer().writeByte(player.voCucType);
            msg.writer().writeByte(Math.max(1, Math.min(5, player.voCucLevel)));
            player.sendMessage(msg);
        } catch (Exception e) {
            Logger.logException(VuTru13Service.class, e);
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    public void sendVoCucAuraToPlayer(Player receiver, Player target) {
        if (receiver == null || !receiver.isPl() || receiver.getSession() == null || target == null || !target.isPl()) {
            return;
        }
        Message msg = null;
        try {
            msg = new Message(CMD_VU_TRU_13);
            msg.writer().writeByte(2); // subCmd = 2: sync outer Vo Cuc aura for any player in zone
            msg.writer().writeInt((int) target.id);
            msg.writer().writeByte(target.voCucType);
            receiver.sendMessage(msg);
        } catch (Exception e) {
            Logger.logException(VuTru13Service.class, e);
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    public void broadcastVoCucAura(Player target) {
        if (target == null || !target.isPl()) {
            return;
        }
        sendVoCucAuraToPlayer(target, target);
        if (target.zone != null) {
            for (Player pl : target.zone.getPlayers()) {
                if (pl != null && pl.isPl() && pl.id != target.id) {
                    sendVoCucAuraToPlayer(pl, target);
                }
            }
        }
    }

    public void sendCutscene(Player player, int cutsceneId, int focusX, int focusY, String title, String[][] steps) {
        if (player == null || !player.isPl() || player.getSession() == null || steps == null || steps.length == 0) {
            return;
        }
        Message msg = null;
        try {
            msg = new Message(CMD_VU_TRU_13);
            msg.writer().writeByte(1); // subCmd = 1: play cinematic cutscene ("cassin")
            msg.writer().writeByte(cutsceneId);
            msg.writer().writeShort(focusX);
            msg.writer().writeShort(focusY);
            msg.writer().writeUTF(title != null ? title : "");
            msg.writer().writeByte(steps.length);
            for (String[] step : steps) {
                String speaker = (step != null && step.length > 0 && step[0] != null) ? step[0] : "";
                String text = (step != null && step.length > 1 && step[1] != null) ? step[1] : "";
                int durationTicks = 230;
                if (step != null && step.length > 2 && step[2] != null) {
                    try {
                        durationTicks = Integer.parseInt(step[2]);
                    } catch (NumberFormatException ignored) {
                    }
                }
                msg.writer().writeUTF(speaker);
                msg.writer().writeUTF(text);
                msg.writer().writeShort(durationTicks);
            }
            player.sendMessage(msg);
        } catch (Exception e) {
            Logger.logException(VuTru13Service.class, e);
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    public static String getVoCucPathName(byte voCucType) {
        return switch (voCucType) {
            case BAN_NANG_VO_CUC -> "Bản Năng Vô Cực";
            case BAN_NGA_VO_CUC -> "Bản Ngã Vô Cực";
            default -> "Chưa thức tỉnh";
        };
    }

    public void chooseVoCucPath(Player player, byte pathType, Npc npc) {
        if (player == null || !player.isPl() || (pathType != BAN_NANG_VO_CUC && pathType != BAN_NGA_VO_CUC)) {
            return;
        }
        if (!player.unlockVuTru13) {
            Service.gI().sendThongBao(player, "Bạn cần khai mở Vũ Trụ 13 trước!");
            return;
        }
        if (player.voCucType == pathType) {
            Service.gI().sendThongBao(player, "Bạn đang tu luyện theo hướng [" + getVoCucPathName(pathType) + "] rồi!");
            return;
        }

        boolean firstChoose = (player.voCucType == VO_CUC_NONE);
        String paymentNote = "";
        if (!firstChoose && !player.isAdmin()) {
            int totalTv = InventoryService.gI().getTotalQuantityBag(player, 457);
            if (totalTv < 50) {
                Service.gI().sendThongBao(player, "Đổi hướng phát triển Vô Cực cần 50 Thỏi vàng trong hành trang (đang có " + totalTv + ")!");
                return;
            }
            int lockedBefore = InventoryService.gI().getTotalLockedQuantityBag(player, 457);
            if (!InventoryService.gI().subQuantityItemsBagPriorityLocked(player, 457, 50)) {
                Service.gI().sendThongBao(player, "Đổi hướng phát triển Vô Cực cần 50 Thỏi vàng trong hành trang!");
                return;
            }
            InventoryService.gI().sendItemBags(player);
            int usedLocked = Math.min(lockedBefore, 50);
            int usedUnlocked = 50 - usedLocked;
            if (usedLocked > 0 && usedUnlocked > 0) {
                paymentNote = " (-" + usedLocked + " Thỏi vàng khóa, -" + usedUnlocked + " Thỏi vàng thường)";
            } else if (usedLocked > 0) {
                paymentNote = " (-" + usedLocked + " Thỏi vàng khóa)";
            } else {
                paymentNote = " (-50 Thỏi vàng)";
            }
        }

        player.voCucType = pathType;
        if (player.voCucLevel < 1) {
            player.voCucLevel = 1;
        }

        player.nPoint.calPoint();
        player.nPoint.setFullHpMp();
        Service.gI().point(player);
        Service.gI().Send_Info_NV(player);
        Service.gI().Send_Caitrang(player);
        sendState(player);
        broadcastVoCucAura(player);

        int fx = npc != null ? npc.cx : player.location.x;
        int fy = npc != null ? npc.cy - 24 : player.location.y - 24;
        if (pathType == BAN_NANG_VO_CUC) {
            String[][] steps = new String[][]{
                {"Đại Thần Quan", "Tâm tĩnh lặng như mặt hồ vĩnh cửu... Hãy buông bỏ mọi tạp niệm để cơ thể tự phản xạ trước vạn vật!", "220"},
                {"Zeno-Sama", "Ồ ồ~ Luồng khí màu Bạc đẹp quá kìa! Lấp lánh thật đấy nhỉ!", "210"},
                {"Bản Năng Vô Cực", "VÙÙÙ!! Hào Quang Bạc bùng nổ bao trùm lên toàn bộ khí tức trang bị — Chibi Bản Năng Vô Cực thức tỉnh!", "240"}
            };
            sendCutscene(player, 3, fx, fy, "THỨC TỈNH: BẢN NĂNG VÔ CỰC (HÀO QUANG BẠC)", steps);
        } else {
            String[][] steps = new String[][]{
                {"Đại Thần Quan", "Lấy chiến ý làm ngọn lửa, biến mọi vết thương và sinh mệnh cuồn cuộn thành sức mạnh Hủy Diệt tuyệt đối!", "220"},
                {"Zeno-Sama", "Woa~ Hào quang Tím Đen dữ dội ghê! Nhìn ngầu cực kỳ luôn á!", "210"},
                {"Bản Ngã Vô Cực", "ẦM ẦM!! Hào Quang Tím Đen Hủy Diệt nuốt trọn không gian quanh hào quang trang bị — Chibi Bản Ngã Vô Cực thức tỉnh!", "240"}
            };
            sendCutscene(player, 4, fx, fy, "THỨC TỈNH: BẢN NGÃ VÔ CỰC (HÀO QUANG TÍM ĐEN)", steps);
        }

        if (firstChoose) {
            ServerNotify.gI().notify("Thánh Điện Toàn Vương VT13: Chiến binh " + player.name + " đã thức tỉnh [" + getVoCucPathName(pathType) + "] trước Đại Thần Quan!");
        } else {
            Service.gI().sendThongBao(player, "Đã chuyển sang hướng [" + getVoCucPathName(pathType) + "] (Cấp " + player.voCucLevel + ")" + paymentNote + "!");
        }
        PlayerDAO.savePlayerAsync(player);
    }

    public void upgradeVoCucLevel(Player player, Npc npc) {
        if (player == null || !player.isPl()) {
            return;
        }
        if (player.voCucType == VO_CUC_NONE) {
            Service.gI().sendThongBao(player, "Hãy chọn 1 trong 2 hướng (Bản Năng Vô Cực / Bản Ngã Vô Cực) tại Đại Thần Quan trước!");
            return;
        }
        if (player.voCucLevel >= 5) {
            Service.gI().sendThongBao(player, "Cảnh giới [" + getVoCucPathName(player.voCucType) + "] của bạn đã đạt tối đa Cấp 5!");
            return;
        }
        int curLv = Math.max(1, player.voCucLevel);
        int reqHonTim = curLv * 20;
        int reqThoiVang = curLv * 50;

        String upgradePaymentNote = "";
        if (!player.isAdmin()) {
            int curHon = InventoryService.gI().getTotalQuantityBag(player, 2018);
            int curTv = InventoryService.gI().getTotalQuantityBag(player, 457);
            if (curHon < reqHonTim || curTv < reqThoiVang) {
                Service.gI().sendThongBao(player, "Đột phá Cấp " + (curLv + 1) + " cần " + reqHonTim + " Hồn Boss Tím (đang có " + curHon + ") và " + reqThoiVang + " Thỏi vàng (đang có " + curTv + ")!");
                return;
            }
            int lockedTvBefore = InventoryService.gI().getTotalLockedQuantityBag(player, 457);
            if (!InventoryService.gI().subQuantityItemsBagPriorityLocked(player, 2018, reqHonTim)) {
                Service.gI().sendThongBao(player, "Không đủ " + reqHonTim + " Hồn Boss Tím để đột phá!");
                return;
            }
            if (!InventoryService.gI().subQuantityItemsBagPriorityLocked(player, 457, reqThoiVang)) {
                Item refundHon = ItemService.gI().createNewItem((short) 2018, reqHonTim);
                refundHon.itemOptions.add(new Item.ItemOption(73, 0));
                InventoryService.gI().addItemBag(player, refundHon);
                InventoryService.gI().sendItemBags(player);
                Service.gI().sendThongBao(player, "Không đủ " + reqThoiVang + " Thỏi vàng để đột phá!");
                return;
            }
            InventoryService.gI().sendItemBags(player);
            int usedLockedTv = Math.min(lockedTvBefore, reqThoiVang);
            int usedUnlockedTv = reqThoiVang - usedLockedTv;
            if (usedLockedTv > 0 && usedUnlockedTv > 0) {
                upgradePaymentNote = " (-" + reqHonTim + " Hồn Boss Tím, -" + usedLockedTv + " Thỏi vàng khóa, -" + usedUnlockedTv + " Thỏi vàng thường)";
            } else if (usedLockedTv > 0) {
                upgradePaymentNote = " (-" + reqHonTim + " Hồn Boss Tím, -" + usedLockedTv + " Thỏi vàng khóa)";
            } else {
                upgradePaymentNote = " (-" + reqHonTim + " Hồn Boss Tím, -" + reqThoiVang + " Thỏi vàng)";
            }
        }

        player.voCucLevel = curLv + 1;
        player.nPoint.calPoint();
        Service.gI().point(player);
        Service.gI().Send_Info_NV(player);
        sendState(player);
        broadcastVoCucAura(player);
        Service.gI().sendThongBao(player, "Đột phá thành công [" + getVoCucPathName(player.voCucType) + "] lên Cấp " + player.voCucLevel + upgradePaymentNote + "!");
        if (player.voCucLevel >= 5) {
            ServerNotify.gI().notify("Chúc mừng " + player.name + " đã tu luyện [" + getVoCucPathName(player.voCucType) + "] đạt cảnh giới viên mãn Cấp 5!");
        }
        PlayerDAO.savePlayerAsync(player);
    }

    public void toggleHideCaiTrang(Player player) {
        if (player == null || !player.isPl()) {
            return;
        }
        player.hideCaiTrang = !player.hideCaiTrang;
        Service.gI().Send_Caitrang(player);
        sendState(player);
        broadcastVoCucAura(player);
        if (player.hideCaiTrang) {
            if (player.voCucType > 0) {
                Service.gI().sendThongBao(player, "Đã BẬT Ẩn Cải Trang: Hiển thị ngoại hình Chibi [" + getVoCucPathName(player.voCucType) + "] (vẫn giữ 100% chỉ số Cải trang)!");
            } else {
                Service.gI().sendThongBao(player, "Đã BẬT Ẩn Cải Trang: Trở về ngoại hình mặc định (vẫn giữ nguyên 100% chỉ số Cải trang)!");
            }
        } else {
            Service.gI().sendThongBao(player, "Đã TẮT Ẩn Cải Trang: Hiển thị lại ngoại hình Cải trang đang đeo.");
        }
        PlayerDAO.savePlayerAsync(player);
    }

    public boolean hasRelicItem(Player player) {
        if (player == null || player.inventory == null) {
            return false;
        }
        if (InventoryService.gI().findItemBag(player, ITEM_AN_KHAI_MO_VT13) != null) {
            return true;
        }
        return InventoryService.gI().findItemBox(player, ITEM_AN_KHAI_MO_VT13) != null;
    }

    public boolean ensureRelicItem(Player player, boolean notify) {
        if (player == null || !player.isPl() || player.inventory == null) {
            return false;
        }
        if (hasRelicItem(player)) {
            if (notify) {
                Service.gI().sendThongBao(player, "Bạn đã có [Ấn Khai Mở Vũ Trụ 13] trong hành trang hoặc rương đồ!");
            }
            return true;
        }
        if (InventoryService.gI().getCountEmptyBag(player) <= 0) {
            Service.gI().sendThongBao(player, "Hành trang cần ít nhất 1 ô trống để nhận [Ấn Khai Mở Vũ Trụ 13]!");
            return false;
        }
        Item relic = ItemService.gI().createNewItem(ITEM_AN_KHAI_MO_VT13, 1);
        relic.itemOptions.clear();
        relic.itemOptions.add(new Item.ItemOption(30, 0)); // Không thể giao dịch
        InventoryService.gI().addItemBag(player, relic);
        InventoryService.gI().sendItemBags(player);
        if (notify) {
            Service.gI().sendThongBao(player, "Bạn nhận được [Ấn Khai Mở Vũ Trụ 13]!");
        }
        return true;
    }

    public boolean canAwakenAtHome(Player player) {
        if (player == null || !player.isPl()) {
            return false;
        }
        if (player.unlockVuTru13) {
            return true;
        }
        int taskId = TaskService.gI().getIdTask(player);
        if (taskId == ConstTask.TASK_28_8) {
            return true;
        }
        return player.playerTask != null
                && player.playerTask.taskMain != null
                && player.playerTask.taskMain.id >= 29;
    }

    public void triggerHomeAwakening(Player player, Npc homeNpc) {
        if (player == null || !player.isPl()) {
            return;
        }
        if (!player.unlockVuTru13 && InventoryService.gI().getCountEmptyBag(player) <= 0 && !hasRelicItem(player)) {
            Service.gI().sendThongBao(player, "Hãy dọn trống ít nhất 1 ô hành trang để nhận [Ấn Khai Mở Vũ Trụ 13]!");
            return;
        }

        boolean firstUnlock = !player.unlockVuTru13;
        player.unlockVuTru13 = true;
        player.vuTru13Stage = Math.max(1, player.vuTru13Stage);

        ensureRelicItem(player, false);

        player.nPoint.calPoint();
        Service.gI().point(player);
        Service.gI().Send_Caitrang(player);
        sendState(player);

        if (TaskService.gI().getIdTask(player) == ConstTask.TASK_28_8) {
            TaskService.gI().doneTask(player, ConstTask.TASK_28_8);
        }

        String elderName = homeNpc != null && homeNpc.tempId == 0 ? "Ông Gôhan"
                : (homeNpc != null && homeNpc.tempId == 1 ? "Ông Moori"
                : (homeNpc != null && homeNpc.tempId == 2 ? "Ông Paragus" : "Trưởng Lão Quê Nhà"));
        int fx = homeNpc != null ? homeNpc.cx : player.location.x;
        int fy = homeNpc != null ? homeNpc.cy - 24 : player.location.y - 24;

        String[][] steps = new String[][]{
            {elderName, "Con đã hoàn thành mọi chiến công lừng lẫy nhất... Nhưng hãy nhìn lên bầu trời kia, thực tại đang nứt toác!", "230"},
            {elderName, "Cánh cổng dẫn tới VŨ TRỤ SỐ 13 — thế giới khắc nghiệt của những vị thần bị nguyền rủa — đang mở ra giữa không gian!", "240"},
            {"Dị Biến Vũ Trụ 13", "ẦM ẦM!! Qua khe nứt không gian, hình bóng Son Goku đang bị xiềng xích cổ thần phong ấn giữa ngục tối hiện rõ!", "240"},
            {elderName, "Hãy tiếp nhận Ấn Khai Mở Cổ Thần! Sức mạnh thức tỉnh sẽ giúp con sống sót trước lời nguyền của Vũ Trụ 13!", "240"},
            {"Thức Tỉnh Vũ Trụ 13", "KHAI MỞ THÀNH CÔNG! Nhận +15% HP/KI/SĐ vĩnh viễn & vật phẩm [Ấn Khai Mở Vũ Trụ 13]!", "260"}
        };
        sendCutscene(player, 1, fx, fy, "KHAI MỞ SỨC MẠNH VŨ TRỤ 13 - VŨ TRỤ BỊ NGUYỀN RỦA", steps);

        if (firstUnlock) {
            ServerNotify.gI().notify("Chiến binh " + player.name + " đã khai mở Sức Mạnh Vũ Trụ 13 và bước vào Thế Giới Của Những Vị Thần Bị Nguyền Rủa!");
        }
        PlayerDAO.savePlayerAsync(player);
    }

    public void triggerBlackFideSealBreak(Player killer, Zone zone) {
        int focusX = 960;
        int focusY = 420;
        String[][] steps = new String[][]{
            {"Black Fide", "Khặc khặc... Khá lắm! Nhưng ngươi tưởng đánh gục bản đế là ngăn cản được kế hoạch này sao?!", "230"},
            {"Black Fide", "Mọi xung lực chiến đấu vừa rồi đã rót đầy Huyết Tế vào Bia Đá! HỠI THẦN HỦY DIỆT VŨ TRỤ 13... PHÁ ẤN!!", "250"},
            {"Bia Phong Ấn Cổ Đại", "RẮC... ĐÙNG ĐÙNG!! Xiềng xích vạn năm nổ tung! Cột sáng Hắc Ám Hủy Diệt xé toạc bầu trời Vũ Trụ 13!", "250"},
            {"Thần Hủy Diệt VT13", "Kẻ phàm trần nào dám đánh thức bản tọa khỏi lời nguyền vĩnh hằng... Mọi sinh linh sẽ hóa thành tro bụi!", "250"},
            {"Cảnh Báo Vũ Trụ 13", "PHONG ẤN THẦN HỦY DIỆT ĐÃ MỞ! Sát khí cổ thần thức tỉnh khắp Vũ Trụ 13 — mau quay về báo cho Goku Bị Phong Ấn!", "260"}
        };

        if (zone != null) {
            for (Player pl : zone.getPlayers()) {
                if (pl == null || !pl.isPl()) {
                    continue;
                }
                if (pl.equals(killer) || pl.unlockVuTru13) {
                    pl.vuTru13Stage |= STAGE_FLAG_BLACK_FIDE_SEAL;
                    sendState(pl);
                    sendCutscene(pl, 2, focusX, focusY, "BLACK FIDE PHÁ VỠ PHONG ẤN THẦN HỦY DIỆT VŨ TRỤ 13", steps);
                    PlayerDAO.savePlayerAsync(pl);
                }
            }
        } else if (killer != null && killer.isPl()) {
            killer.vuTru13Stage |= STAGE_FLAG_BLACK_FIDE_SEAL;
            sendState(killer);
            sendCutscene(killer, 2, focusX, focusY, "BLACK FIDE PHÁ VỠ PHONG ẤN THẦN HỦY DIỆT VŨ TRỤ 13", steps);
            PlayerDAO.savePlayerAsync(killer);
        }

        if (killer != null) {
            ServerNotify.gI().notify("Chấn động Vũ Trụ 13: " + killer.name + " vừa đánh bại Black Fide, nhưng Phong Ấn Thần Hủy Diệt Vũ Trụ 13 đã bị phá vỡ!");
        }
    }

    public boolean isKheNutMobsSuppressed(Player pl) {
        if (pl == null) {
            return false;
        }
        Player owner = pl;
        if (pl.isPet && pl instanceof nro.models.player.Pet petObj && petObj.master != null) {
            owner = petObj.master;
        }
        if (!owner.isPl() || owner.zone == null || owner.zone.map == null) {
            return false;
        }
        if (owner.zone.map.mapId != MAP_KHE_NUT_VT13) {
            return false;
        }
        return owner.playingKheNutCutscene || (owner.vuTru13Stage & STAGE_FLAG_KHE_NUT_INTRO) == 0;
    }

    public void checkKheNutPyramidTrigger(Player player) {
        if (player == null || !player.isPl() || player.isDie() || player.zone == null || player.zone.map == null) {
            return;
        }
        if (player.zone.map.mapId != MAP_KHE_NUT_VT13) {
            player.playingKheNutCutscene = false;
            return;
        }
        if (player.getSession() == null || !player.getSession().finishUpdate
                || player.idMark == null || !player.idMark.isLoadedAllDataPlayer()) {
            return;
        }
        if (player.kheNutMapReadyTime > 0L && System.currentTimeMillis() - player.kheNutMapReadyTime < 1200L) {
            return;
        }
        if (player.playingKheNutCutscene) {
            // Watchdog an toàn nếu quá 150s chưa nhận gói hoàn tất từ Client
            if (System.currentTimeMillis() - player.kheNutCutsceneStartTime > 150_000L) {
                completeKheNutGodsCutscene(player);
            }
            return;
        }
        if ((player.vuTru13Stage & STAGE_FLAG_KHE_NUT_INTRO) != 0) {
            return;
        }
        if (Math.abs(player.location.x - PYRAMID_KHE_NUT_X) <= 210) {
            triggerKheNutGodsCutscene(player, true);
        }
    }

    public void triggerKheNutGodsCutscene(Player player, boolean firstTrigger) {
        if (player == null || !player.isPl()) {
            return;
        }
        player.unlockVuTru13 = true;
        player.vuTru13Stage |= STAGE_FLAG_AWAKENED;
        player.playingKheNutCutscene = true;
        player.kheNutCutsceneStartTime = System.currentTimeMillis();
        sendState(player);

        String[][] steps = new String[][]{
            // Cảnh 1 (Steps 0..1): Tháp Ai Cập vỡ -> Thần Hủy Diệt VT13 xuất hiện giữa không trung -> Nổ Flash (tốc độ 1/2: 300 ticks/bước)
            {"Tháp Ai Cập Phong Ấn", "RẦM RẦM!! Khi bạn tiến lại gần, cổ ngữ trên Tháp Ai Cập nứt toác dữ dội, xiềng xích phong ấn vạn năm đứt tung!", "300"},
            {"Thần Hủy Diệt Vũ Trụ 13", "PHONG ẤN VẠN CỔ ĐÃ VỠ!! Kẻ nào dám đánh thức Bản Tôn?! HỦY DIỆT BÙNG NỔ — FLASH!!", "300"},
            // Cảnh 2 (Steps 2..3): 12 Thần Hủy Diệt giáng lâm -> Boss VT13 hóa thân tấn công tất cả
            {"12 Thần Hủy Diệt (VT1 - VT12)", "Cảnh báo chấn động đa vũ trụ!! Beerus, Champa, Belmod, Iwan cùng toàn bộ 12 Thần Hủy Diệt đồng loạt giáng lâm bao vây!", "300"},
            {"Boss Thần Hủy Diệt VT13", "Lũ hậu bối yếu đuối của 12 vũ trụ mà đòi phong ấn ta lần nữa sao?! HÓA THÂN CỔ THẦN — VẠN VẬT QUY HƯ VÔ!!", "330"},
            // Cảnh 3 (Steps 4..5): VT7, VT11, VT1 trụ vững; 9 Thần gục ngã hóa Cục Năng Lượng Tím -> Boss lướt thu thập
            {"Thảm Kịch 12 Vũ Trụ", "Chỉ còn Beerus (VT7), Belmod (VT11) và Iwan (VT1) gắng gượng đứng vững! 9 Thần Hủy Diệt còn lại gục ngã, hóa thành các Hồn Năng Lượng Tím!", "320"},
            {"Boss Thần Hủy Diệt VT13", "Hahaha! Tinh hoa thần lực hủy diệt tuyệt hảo! Tất cả đều thuộc về Bản Tôn!!", "350"},
            // Cảnh 4 (Steps 6..7): Beerus lao ra đánh & đoạt 1 Hồn Tím ở chỗ Champa VT6 -> Va chạm, đối thoại & Boss rời đi, Beerus thành NPC
            {"Thần Hủy Diệt Beerus (VT7)", "CHAMPA!! Ngươi đừng hòng nuốt trọn linh hồn em trai ta!! HAKAI — ĐOẠT HỒN!!", "330"},
            {"Boss Thần Hủy Diệt VT13", "Khá lắm con mèo Vũ Trụ 7... Giữ lấy mảnh hồn cuối cùng đó đi! Khi ta luyện hóa xong 8 thần hồn ở Điện Thờ Sâu Thẳm, cả 13 Vũ Trụ sẽ bị xóa sổ!", "360"}
        };
        sendCutscene(player, CUTSCENE_KHE_NUT_12_GODS, PYRAMID_KHE_NUT_X, 340,
                "THÁP AI CẬP VỠ NÁT - ĐẠI CHIẾN 13 THẦN HỦY DIỆT", steps);
    }

    public void completeKheNutGodsCutscene(Player player) {
        if (player == null || !player.isPl()) {
            return;
        }
        player.playingKheNutCutscene = false;
        boolean firstDone = (player.vuTru13Stage & STAGE_FLAG_KHE_NUT_INTRO) == 0;
        player.unlockVuTru13 = true;
        player.vuTru13Stage |= (STAGE_FLAG_AWAKENED | STAGE_FLAG_KHE_NUT_INTRO);
        sendState(player);
        PlayerDAO.savePlayerAsync(player);

        if (firstDone) {
            Service.gI().sendThongBao(player, "Dư chấn từ Tháp Ai Cập vỡ nát đã đánh thức bầy Quái Vật Nguyền Rủa khắp Khe Nứt!");
            ServerNotify.gI().notify("Dị biến tại Khe Nứt Nguyền Rủa: " + player.name
                    + " vừa chứng kiến Tháp Ai Cập vỡ toác — CỔ THẦN HỦY DIỆT VŨ TRỤ 13 đã tái xuất!");
        }
    }

    public void broadcastBossSkillVfx(Zone zone, byte vfxType, int srcX, int srcY, int dstX, int dstY) {
        if (zone == null) {
            return;
        }
        for (Player pl : zone.getPlayers()) {
            if (pl == null || !pl.isPl() || pl.getSession() == null) {
                continue;
            }
            Message msg = null;
            try {
                msg = new Message(CMD_VU_TRU_13);
                msg.writer().writeByte(3); // subCmd = 3: Boss Thần Hủy Diệt VT13 combat VFX
                msg.writer().writeByte(vfxType);
                msg.writer().writeShort(srcX);
                msg.writer().writeShort(srcY);
                msg.writer().writeShort(dstX);
                msg.writer().writeShort(dstY);
                pl.sendMessage(msg);
            } catch (Exception e) {
                Logger.logException(VuTru13Service.class, e);
            } finally {
                if (msg != null) {
                    msg.cleanup();
                }
            }
        }
    }

    public void summonTrialBossOnKheNut(Player player) {
        if (player == null || !player.isPl() || player.zone == null || player.zone.map == null) {
            return;
        }
        if (player.zone.map.mapId != MAP_KHE_NUT_VT13) {
            Service.gI().sendThongBao(player, "Chỉ có thể triệu hồi Phân Thân Thử Nghiệm tại Khe Nứt Nguyền Rủa VT13!");
            return;
        }
        if (nro.models.boss.Boss_Manager.BossManager.gI().checkBosses(player.zone, nro.models.boss.BossID.THAN_HUY_DIET_VT13_TRIAL)) {
            Service.gI().sendThongBao(player, "Phân Thân Thần Hủy Diệt VT13 đang hiện diện ngay trong khu vực này rồi!");
            return;
        }
        try {
            nro.models.boss.vu_tru_13.ThanHuyDietVt13 trial = new nro.models.boss.vu_tru_13.ThanHuyDietVt13(
                    player.zone, PYRAMID_KHE_NUT_X + 150, PYRAMID_KHE_NUT_Y);
            trial.changeStatus(nro.models.consts.BossStatus.RESPAWN);
            Service.gI().sendThongBao(player, "ẦM ẦM!! Phân Thân Thần Hủy Diệt Vũ Trụ 13 đã giáng lâm bên cạnh Tháp Ai Cập!");
        } catch (Exception e) {
            Logger.logException(VuTru13Service.class, e);
            Service.gI().sendThongBao(player, "Không thể triệu hồi Phân Thân lúc này!");
        }
    }
}
