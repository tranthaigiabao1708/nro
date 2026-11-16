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
        if (!firstChoose && !player.isAdmin()) {
            Item thoiVang = InventoryService.gI().findItemBag(player, 457);
            if (thoiVang == null || thoiVang.quantity < 50) {
                Service.gI().sendThongBao(player, "Đổi hướng phát triển Vô Cực cần 50 Thỏi vàng trong hành trang (giữ nguyên cấp cảnh giới)!");
                return;
            }
            InventoryService.gI().subQuantityItemsBag(player, thoiVang, 50);
            InventoryService.gI().sendItemBags(player);
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
            Service.gI().sendThongBao(player, "Đã chuyển sang hướng [" + getVoCucPathName(pathType) + "] (Cấp " + player.voCucLevel + ")!");
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

        if (!player.isAdmin()) {
            Item honTim = InventoryService.gI().findItemBag(player, 2018);
            Item thoiVang = InventoryService.gI().findItemBag(player, 457);
            int curHon = honTim != null ? honTim.quantity : 0;
            int curTv = thoiVang != null ? thoiVang.quantity : 0;
            if (curHon < reqHonTim || curTv < reqThoiVang) {
                Service.gI().sendThongBao(player, "Đột phá Cấp " + (curLv + 1) + " cần " + reqHonTim + " Hồn Boss Tím (đang có " + curHon + ") và " + reqThoiVang + " Thỏi vàng (đang có " + curTv + ")!");
                return;
            }
            InventoryService.gI().subQuantityItemsBag(player, honTim, reqHonTim);
            InventoryService.gI().subQuantityItemsBag(player, thoiVang, reqThoiVang);
            InventoryService.gI().sendItemBags(player);
        }

        player.voCucLevel = curLv + 1;
        player.nPoint.calPoint();
        Service.gI().point(player);
        Service.gI().Send_Info_NV(player);
        sendState(player);
        broadcastVoCucAura(player);
        Service.gI().sendThongBao(player, "Đột phá thành công [" + getVoCucPathName(player.voCucType) + "] lên Cấp " + player.voCucLevel + "!");
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
                    pl.vuTru13Stage = Math.max(2, pl.vuTru13Stage);
                    sendState(pl);
                    sendCutscene(pl, 2, focusX, focusY, "BLACK FIDE PHÁ VỠ PHONG ẤN THẦN HỦY DIỆT VŨ TRỤ 13", steps);
                    PlayerDAO.savePlayerAsync(pl);
                }
            }
        } else if (killer != null && killer.isPl()) {
            killer.vuTru13Stage = Math.max(2, killer.vuTru13Stage);
            sendState(killer);
            sendCutscene(killer, 2, focusX, focusY, "BLACK FIDE PHÁ VỠ PHONG ẤN THẦN HỦY DIỆT VŨ TRỤ 13", steps);
            PlayerDAO.savePlayerAsync(killer);
        }

        if (killer != null) {
            ServerNotify.gI().notify("Chấn động Vũ Trụ 13: " + killer.name + " vừa đánh bại Black Fide, nhưng Phong Ấn Thần Hủy Diệt Vũ Trụ 13 đã bị phá vỡ!");
        }
    }
}
