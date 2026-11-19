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
    public static final short ITEM_TRUNG_DE_TU_THIEN_SU = 2033;
    public static final short ITEM_MANH_THAN_VUC_VO_CUC = 2034;
    public static final short ITEM_AN_MO_KHOA_START = 2050; // Ấn mở khóa 1..7 (2050..2056)
    public static final short ITEM_AN_MO_KHOA_END = 2056;
    public static final short ITEM_CAI_TRANG_BUNNY = 2057;

    public static final int MAP_KHE_NUT_VT13 = 178;
    public static final int MAP_VUC_THAM_VT13 = 182;
    public static final int MAP_NGUC_TU_GOKU_VT13 = 179;
    public static final int MAP_DIEN_THO_HUY_DIET_VT13 = 180;
    public static final int MAP_THANH_DIEN_ZENO_VT13 = 181;
    public static final int MAP_DAI_HOI_VT13 = 186; // Map -1 (bên trái Map Home 181)

    public static final byte VO_CUC_NONE = 0;
    public static final byte BAN_NANG_VO_CUC = 1;
    public static final byte BAN_NGA_VO_CUC = 2;

    // Bitflags lưu trong vuTru13Stage (0..127)
    public static final int STAGE_FLAG_AWAKENED = 1;
    public static final int STAGE_FLAG_BLACK_FIDE_SEAL = 2;
    public static final int STAGE_FLAG_KHE_NUT_INTRO = 4;
    public static final int STAGE_FLAG_CHAMPA_SOUL = 8;
    public static final int STAGE_FLAG_THAN_HUY_DIET_SLAIN = 16;
    public static final int STAGE_FLAG_GOKU_UNSEALED = 32;

    // Cutscene IDs
    public static final int CUTSCENE_KHE_NUT_12_GODS = 5;
    public static final int CUTSCENE_GOD_BOSS_FINISHER = 6;
    public static final int CUTSCENE_GOKU_UNSEAL = 7;

    // Boss Thần Hủy Diệt VT13 & Fide Đen Combat VFX types (subCmd = 3)
    public static final byte BOSS_VFX_HAKAI_SPHERE = 1;
    public static final byte BOSS_VFX_EGYPT_SIGIL_PILLAR = 2;
    public static final byte BOSS_VFX_SOUL_FLASH_STORM = 3;
    public static final byte BOSS_VFX_MUMMY_BANDAGE = 4;
    public static final byte BOSS_VFX_SCARAB_CURSE = 5;
    public static final byte BOSS_VFX_DARK_KI_BIND = 6;
    public static final byte BOSS_VFX_DARK_DEATH_BALL = 7;
    public static final byte BOSS_VFX_DARK_ABSORB_SHIELD = 8;
    public static final byte BOSS_VFX_DARK_SHIELD_BREAK = 9;

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
                || mapId == MAP_DAI_HOI_VT13
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
            msg.writer().writeBoolean(player.setClothes != null && player.setClothes.setVoCuc >= 5);
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
            msg.writer().writeByte(2); // subCmd = 2: sync outer Vo Cuc aura & Set Vo Cuc sigil for any player in zone
            msg.writer().writeInt((int) target.id);
            msg.writer().writeByte(target.voCucType);
            msg.writer().writeBoolean(target.setClothes != null && target.setClothes.setVoCuc >= 5);
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
        if (player.idMark != null) {
            player.idMark.setIndexMenu(nro.models.consts.ConstNpc.IGNORE_MENU);
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
            Service.gI().sendThongBao(player, "Bạn đang theo hướng [" + getVoCucPathName(pathType) + "] rồi!");
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
                {"Đại Thần Quan", "Hãy giữ tâm trí hoàn toàn tĩnh lặng, gạt bỏ mọi suy nghĩ thừa để cơ thể tự phản xạ trước đòn tấn công!", "220"},
                {"Zeno-Sama", "Ồ ồ~ Luồng khí màu Bạc đẹp quá kìa! Lấp lánh thật đấy nhỉ!", "210"},
                {"Đại Thần Quan", "Hào Quang Bạc của Bản Năng Vô Cực đã thức tỉnh hoàn toàn trên người ngươi rồi đấy!", "240"}
            };
            sendCutscene(player, 3, fx, fy, "", steps);
        } else {
            String[][] steps = new String[][]{
                {"Đại Thần Quan", "Hãy biến toàn bộ ý chí chiến đấu và sát thương nhận vào thành nguồn năng lượng Hủy Diệt mạnh mẽ nhất!", "220"},
                {"Zeno-Sama", "Woa~ Hào quang Tím Đen dữ dội ghê! Nhìn ngầu cực kỳ luôn á!", "210"},
                {"Đại Thần Quan", "Hào Quang Tím Đen của Bản Ngã Vô Cực đã thức tỉnh hoàn toàn trên người ngươi rồi đấy!", "240"}
            };
            sendCutscene(player, 4, fx, fy, "", steps);
        }

        if (firstChoose) {
            ServerNotify.gI().notify("Thánh Điện Toàn Vương VT13: Chiến binh " + player.name + " đã đánh thức [" + getVoCucPathName(pathType) + "] trước Đại Thần Quan!");
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
            Service.gI().sendThongBao(player, "[" + getVoCucPathName(player.voCucType) + "] của bạn đã đạt tối đa Cấp 5!");
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
                Service.gI().sendThongBao(player, "Nâng lên Cấp " + (curLv + 1) + " cần " + reqHonTim + " Hồn Boss Tím (đang có " + curHon + ") và " + reqThoiVang + " Thỏi vàng (đang có " + curTv + ")!");
                return;
            }
            int lockedTvBefore = InventoryService.gI().getTotalLockedQuantityBag(player, 457);
            if (!InventoryService.gI().subQuantityItemsBagPriorityLocked(player, 2018, reqHonTim)) {
                Service.gI().sendThongBao(player, "Không đủ " + reqHonTim + " Hồn Boss Tím để nâng cấp!");
                return;
            }
            if (!InventoryService.gI().subQuantityItemsBagPriorityLocked(player, 457, reqThoiVang)) {
                Item refundHon = ItemService.gI().createNewItem((short) 2018, reqHonTim);
                refundHon.itemOptions.add(new Item.ItemOption(73, 0));
                InventoryService.gI().addItemBag(player, refundHon);
                InventoryService.gI().sendItemBags(player);
                Service.gI().sendThongBao(player, "Không đủ " + reqThoiVang + " Thỏi vàng để nâng cấp!");
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
        Service.gI().sendThongBao(player, "Nâng cấp thành công [" + getVoCucPathName(player.voCucType) + "] lên Cấp " + player.voCucLevel + upgradePaymentNote + "!");
        if (player.voCucLevel >= 5) {
            ServerNotify.gI().notify("Chúc mừng " + player.name + " đã nâng cấp [" + getVoCucPathName(player.voCucType) + "] đạt tối đa Cấp 5!");
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
            {elderName, "Con đã hoàn thành mọi chiến công lừng lẫy nhất... Nhưng hãy nhìn lên bầu trời kia, không gian đang nứt toác!", "230"},
            {elderName, "Cánh cổng dẫn tới VŨ TRỤ SỐ 13 — vùng không gian bị phong ấn — đang mở ra!", "240"},
            {elderName, "Qua khe nứt không gian kia, Son Goku đang bị xiềng xích khóa chặt toàn bộ sức mạnh!", "240"},
            {elderName, "Hãy nhận lấy Ấn Khai Mở Vũ Trụ 13 (+15% HP/KI/SĐ vĩnh viễn) và lên đường giải cứu Goku ngay!", "260"}
        };
        sendCutscene(player, 1, fx, fy, "", steps);

        if (firstUnlock) {
            ServerNotify.gI().notify("Chiến binh " + player.name + " đã khai mở Sức Mạnh Vũ Trụ 13!");
        }
        PlayerDAO.savePlayerAsync(player);
    }

    public void triggerBlackFideSealBreak(Player killer, Zone zone) {
        int focusX = 960;
        int focusY = 420;
        String[][] steps = new String[][]{
            {"Black Fide", "Khặc khặc... Khá lắm! Nhưng ngươi tưởng đánh gục ta là ngăn cản được kế hoạch này sao?!", "230"},
            {"Black Fide", "Toàn bộ chấn động chiến đấu vừa rồi đã nạp đầy năng lượng phá vỡ xiềng xích! THẦN HỦY DIỆT BUNNY... THỨC TỈNH ĐI!!", "250"},
            {"Khối Phong Ấn Cổ", "RẮC... ĐÙNG ĐÙNG!! Xiềng xích giam giữ nổ tung! Cột năng lượng Hủy Diệt xé toạc bầu trời Vũ Trụ 13!", "250"},
            {"Thần Hủy Diệt Bunny", "Ai vừa đánh thức ta khỏi giấc ngủ dài... Tất cả chuẩn bị đón nhận sức mạnh Hủy Diệt của Bunny đi!", "250"}
        };

        if (zone != null) {
            for (Player pl : zone.getPlayers()) {
                if (pl == null || !pl.isPl()) {
                    continue;
                }
                if (pl.equals(killer) || pl.unlockVuTru13) {
                    pl.vuTru13Stage |= STAGE_FLAG_BLACK_FIDE_SEAL;
                    sendState(pl);
                    sendCutscene(pl, 2, focusX, focusY, "", steps);
                    PlayerDAO.savePlayerAsync(pl);
                }
            }
        } else if (killer != null && killer.isPl()) {
            killer.vuTru13Stage |= STAGE_FLAG_BLACK_FIDE_SEAL;
            sendState(killer);
            sendCutscene(killer, 2, focusX, focusY, "", steps);
            PlayerDAO.savePlayerAsync(killer);
        }

        if (killer != null) {
            ServerNotify.gI().notify("Chấn động Vũ Trụ 13: " + killer.name + " vừa đánh bại Black Fide, Thần Hủy Diệt Bunny đã thức tỉnh!");
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
            {"Khối Phong Ấn Cổ", "RẦM RẦM!! Khối đá cổ rạn nứt dữ dội, toàn bộ xiềng xích giam giữ đứt tung!", "300"},
            {"Thần Hủy Diệt Bunny", "XIỀNG XÍCH ĐÃ VỠ!! Cuối cùng ta cũng thoát ra ngoài rồi! HAKAI FLASH!!", "300"},
            {"Thần Hủy Diệt Beerus (VT7)", "Tất cả vào vị trí!! Cả 12 Thần Hủy Diệt cùng ra tay khóa chặt Bunny lại ngay!", "300"},
            {"Thần Hủy Diệt Bunny", "Chỉ với 12 Thần Hủy Diệt các ngươi mà đòi phong ấn ta lần nữa sao?! HAKAI TOÀN LỰC!!", "330"},
            {"Thần Hủy Diệt Belmod (VT11)", "Không thể tin nổi... Đòn càn quét mạnh quá! Chỉ còn ta, Beerus và Iwan trụ vững, 9 vị Thần Hủy Diệt kia đã bị đánh gục thành cầu năng lượng tím!", "320"},
            {"Thần Hủy Diệt Bunny", "Hahaha! Nguồn năng lượng Hủy Diệt tuyệt vời! Tất cả đều thuộc về ta!!", "350"},
            {"Thần Hủy Diệt Beerus (VT7)", "CHAMPA!! Đừng hòng cướp luôn phần năng lượng của em trai ta!! HAKAI!!", "330"},
            {"Thần Hủy Diệt Bunny", "Khá lắm con mèo Vũ Trụ 7... Cứ giữ lấy viên cuối cùng đó đi! Đợi ta hấp thụ xong 8 nguồn năng lượng này ở Điện Thờ, cả 13 Vũ Trụ sẽ bị xóa sổ!", "360"}
        };
        sendCutscene(player, CUTSCENE_KHE_NUT_12_GODS, PYRAMID_KHE_NUT_X, 340, "", steps);
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
            Service.gI().sendThongBao(player, "Chấn động từ Thần Hủy Diệt Bunny vừa đánh thức bầy quái vật khắp Khe Nứt!");
            ServerNotify.gI().notify("Khe Nứt Vũ Trụ 13: " + player.name
                    + " vừa chứng kiến xiềng xích vỡ tung — THẦN HỦY DIỆT BUNNY đã xuất hiện!");
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

    /**
     * Cutscene kết liễu đồng bộ toàn khu vực (subCmd = 5):
     * - Toàn bộ người chơi trong khu đứng dưới mặt đất chứng kiến.
     * - Thần Hủy Diệt Vũ Trụ 13 bay vút lên không trung, nén Đại Cầu Hakai khổng lồ định xóa sổ cả khu vực.
     * - DUY NHẤT người chơi tiêu diệt Boss (killer) bộc phát khí lực, lao vút từ mặt đất lên trời tung cú đấm kết liễu xuyên thủng Đại Cầu Hakai và hạ gục Thần Hủy Diệt!
     */
    public void broadcastGodBossFinisherCutscene(Zone zone, Player killer, int bossX, int floorY) {
        if (zone == null || zone.map == null) {
            return;
        }
        int mapW = Math.max(400, zone.map.mapWidth);
        int centerBossX = Math.max(240, Math.min(mapW - 240, bossX));
        int groundY = floorY > 0 ? floorY : 456;

        // Đặt toàn bộ người chơi trong khu đứng xuống mặt đất để quan sát pha kết liễu
        int idx = 0;
        for (Player pl : zone.getPlayers()) {
            if (pl == null || !pl.isPl()) {
                continue;
            }
            int gx = Math.max(80, Math.min(mapW - 80, pl.location.x));
            if (killer != null && pl.id == killer.id) {
                gx = Math.max(120, Math.min(mapW - 120, centerBossX - 150));
            } else if (Math.abs(gx - centerBossX) < 60) {
                gx = (idx % 2 == 0) ? Math.max(80, centerBossX - 190 - (idx * 22))
                        : Math.min(mapW - 80, centerBossX + 140 + (idx * 22));
            }
            int gy = zone.map.yPhysicInTop(gx, groundY - 24);
            if (gy <= 0 || gy > groundY + 48) {
                gy = groundY;
            }
            PlayerService.gI().playerMove(pl, gx, gy);
            idx++;
        }

        int killerId = killer != null ? (int) killer.id : -1;
        String killerName = killer != null ? killer.name : "Chiến Binh Tối Thượng";
        short killerHead = killer != null ? killer.getHead() : 0;
        short killerBody = killer != null ? killer.getBody() : 1;
        short killerLeg = killer != null ? killer.getLeg() : 2;
        short killerGroundX = (short) (killer != null ? killer.location.x : Math.max(120, centerBossX - 150));

        String[][] steps = new String[][]{
            {"Thần Hủy Diệt Bunny", "Khốn kiếp!! Dám dồn ta tới bước đường này... Tất cả các ngươi hãy tan biến cùng ĐẠI CẦU HAKAI này đi!!", "210"},
            {killerName, "KHÔNG ĐỂ NGƯƠI LÀM VẬY ĐÂU!! " + killerName.toUpperCase() + " — CÚ ĐẤM KẾT LIỄU!!", "220"},
            {"Thần Hủy Diệt Beerus (VT7)", "ẦM ẦM!! Đại Cầu Hakai vỡ tan trên bầu trời! " + killerName + " đã tung cú đấm quyết định đánh bại Thần Hủy Diệt Bunny!", "190"}
        };

        for (Player pl : zone.getPlayers()) {
            if (pl == null || !pl.isPl() || pl.getSession() == null) {
                continue;
            }
            Message msg = null;
            try {
                msg = new Message(CMD_VU_TRU_13);
                msg.writer().writeByte(5); // subCmd = 5: Multiplayer God Boss Finisher Cutscene
                msg.writer().writeInt(killerId);
                msg.writer().writeUTF(killerName);
                msg.writer().writeShort(killerHead);
                msg.writer().writeShort(killerBody);
                msg.writer().writeShort(killerLeg);
                msg.writer().writeShort(killerGroundX);
                msg.writer().writeShort(centerBossX);
                msg.writer().writeShort(groundY);
                msg.writer().writeUTF("");
                msg.writer().writeByte(steps.length);
                for (String[] step : steps) {
                    msg.writer().writeUTF(step[0]);
                    msg.writer().writeUTF(step[1]);
                    msg.writer().writeShort(Integer.parseInt(step[2]));
                }
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

    public static final int MENU_EXCHANGE_VOCUC_PLANET = 20340;
    public static final int MENU_EXCHANGE_VOCUC_TD = 20341;
    public static final int MENU_EXCHANGE_VOCUC_NM = 20342;
    public static final int MENU_EXCHANGE_VOCUC_XD = 20343;
    public static final int REQ_MANH_VO_CUC_PER_ITEM = 200;
    public static final int REQ_THOI_VANG_PER_ITEM = 100;

    private static final short[][] VO_CUC_SET_IDS = {
        {2035, 2038, 2041, 2044, 2047}, // Trái Đất: Áo, Quần, Găng, Giầy, Nhẫn
        {2036, 2039, 2042, 2045, 2048}, // Namếc
        {2037, 2040, 2043, 2046, 2049}  // Xayda
    };

    public void openVoCucSetPlanetMenu(Player player, Npc npc) {
        if (player == null || npc == null) {
            return;
        }
        int shards = InventoryService.gI().getTotalQuantityBag(player, ITEM_MANH_THAN_VUC_VO_CUC);
        int tv = InventoryService.gI().getTotalQuantityBag(player, 457);
        String shardColor = shards >= REQ_MANH_VO_CUC_PER_ITEM ? "|2|" : "|7|";
        String tvColor = tv >= REQ_THOI_VANG_PER_ITEM ? "|2|" : "|7|";
        npc.createOtherMenu(player, MENU_EXCHANGE_VOCUC_PLANET,
                "|8|ĐỔI TRANG BỊ THẦN VỰC VÔ CỰC (CẤP 16 - TRÊN ĐỒ THIÊN SỨ)\n"
                + "|2|★ Hiệu ứng đủ 5 món: +40% Xuyên giáp, +35% SĐ/HP/KI/SĐCM & Hào Quang Vô Cực dưới chân!\n"
                + shardColor + "• Mảnh Đại Thiên Sứ: cần " + REQ_MANH_VO_CUC_PER_ITEM + "/món (đang có: " + shards + ")\n"
                + tvColor + "• Thỏi vàng: cần " + REQ_THOI_VANG_PER_ITEM + "/món (đang có: " + tv + ")\n"
                + "|1|(Săn Mảnh Đại Thiên Sứ tại các Map Vũ Trụ 13, Boss Fide Đen, Boss Bunny hoặc Đại Hội Võ Thuật VT13 lúc 20h00!)",
                "Đổi đồ\nTrái Đất",
                "Đổi đồ\nNamếc",
                "Đổi đồ\nXayda",
                "Đóng");
    }

    public void openVoCucSetSlotMenu(Player player, Npc npc, int planetGender) {
        if (player == null || npc == null || planetGender < 0 || planetGender > 2) {
            return;
        }
        String planetName = planetGender == 0 ? "TRÁI ĐẤT" : (planetGender == 1 ? "NAMẾC" : "XAYDA");
        int menuId = MENU_EXCHANGE_VOCUC_TD + planetGender;
        int shards = InventoryService.gI().getTotalQuantityBag(player, ITEM_MANH_THAN_VUC_VO_CUC);
        int tv = InventoryService.gI().getTotalQuantityBag(player, 457);
        npc.createOtherMenu(player, menuId,
                "|8|CHỌN MÓN THẦN VỰC VÔ CỰC [" + planetName + "]\n"
                + "|2|Chi phí mỗi món: " + REQ_MANH_VO_CUC_PER_ITEM + " Mảnh Đại Thiên Sứ + " + REQ_THOI_VANG_PER_ITEM + " Thỏi vàng\n"
                + "|1|Hành trang hiện có: " + shards + " Mảnh Đại Thiên Sứ & " + tv + " Thỏi vàng.",
                "Áo Vô Cực\n(" + planetName + ")",
                "Quần Vô Cực\n(" + planetName + ")",
                "Găng Vô Cực\n(" + planetName + ")",
                "Giầy Vô Cực\n(" + planetName + ")",
                "Nhẫn Vô Cực\n(" + planetName + ")",
                "Quay lại");
    }

    public boolean handleVoCucSetMenuConfirm(Player player, Npc npc, int menuIdx, int select) {
        if (menuIdx == MENU_EXCHANGE_VOCUC_PLANET) {
            if (select >= 0 && select <= 2) {
                openVoCucSetSlotMenu(player, npc, select);
            }
            return true;
        }
        if (menuIdx >= MENU_EXCHANGE_VOCUC_TD && menuIdx <= MENU_EXCHANGE_VOCUC_XD) {
            int gender = menuIdx - MENU_EXCHANGE_VOCUC_TD;
            if (select >= 0 && select <= 4) {
                exchangeVoCucSetItem(player, gender, select);
            } else {
                openVoCucSetPlanetMenu(player, npc);
            }
            return true;
        }
        return false;
    }

    public void exchangeVoCucSetItem(Player player, int gender, int slotIdx) {
        if (player == null || !player.isPl() || gender < 0 || gender > 2 || slotIdx < 0 || slotIdx > 4) {
            return;
        }
        if (InventoryService.gI().getCountEmptyBag(player) <= 0) {
            Service.gI().sendThongBao(player, "Hành trang cần ít nhất 1 ô trống để nhận trang bị Thần Vực Vô Cực!");
            return;
        }
        if (!player.isAdmin()) {
            int shards = InventoryService.gI().getTotalQuantityBag(player, ITEM_MANH_THAN_VUC_VO_CUC);
            int tv = InventoryService.gI().getTotalQuantityBag(player, 457);
            if (shards < REQ_MANH_VO_CUC_PER_ITEM || tv < REQ_THOI_VANG_PER_ITEM) {
                Service.gI().sendThongBao(player, "Cần đủ " + REQ_MANH_VO_CUC_PER_ITEM + " Mảnh Đại Thiên Sứ (có " + shards
                        + ") và " + REQ_THOI_VANG_PER_ITEM + " Thỏi vàng (có " + tv + ")!");
                return;
            }
            if (!InventoryService.gI().subQuantityItemsBagPriorityLocked(player, ITEM_MANH_THAN_VUC_VO_CUC, REQ_MANH_VO_CUC_PER_ITEM)) {
                Service.gI().sendThongBao(player, "Không đủ Mảnh Đại Thiên Sứ!");
                return;
            }
            if (!InventoryService.gI().subQuantityItemsBagPriorityLocked(player, 457, REQ_THOI_VANG_PER_ITEM)) {
                Item refund = ItemService.gI().createNewItem(ITEM_MANH_THAN_VUC_VO_CUC, REQ_MANH_VO_CUC_PER_ITEM);
                refund.itemOptions.add(new Item.ItemOption(30, 0));
                InventoryService.gI().addItemBag(player, refund);
                InventoryService.gI().sendItemBags(player);
                Service.gI().sendThongBao(player, "Không đủ Thỏi vàng!");
                return;
            }
        }
        short targetItemId = VO_CUC_SET_IDS[gender][slotIdx];
        Item crafted = ItemService.gI().DoThanVucVoCuc(targetItemId, gender);
        InventoryService.gI().addItemBag(player, crafted);
        InventoryService.gI().sendItemBags(player);
        Service.gI().sendThongBao(player, "Chúc mừng! Bạn đã đổi thành công [" + crafted.template.name + "]!");
        ServerNotify.gI().notify("Đại Thiên Sứ trao trang bị: Chiến binh " + player.name + " vừa đổi thành công [" + crafted.template.name + "]!");
        PlayerDAO.savePlayerAsync(player);
    }

    public void summonTrialBossOnKheNut(Player player) {
        if (player == null || !player.isPl() || player.zone == null || player.zone.map == null) {
            return;
        }
        if (player.zone.map.mapId != MAP_KHE_NUT_VT13) {
            Service.gI().sendThongBao(player, "Chỉ có thể triệu hồi Phân Thân Thử Nghiệm tại Khe Nứt VT13!");
            return;
        }
        if (nro.models.boss.Boss_Manager.BossManager.gI().checkBosses(player.zone, nro.models.boss.BossID.THAN_HUY_DIET_VT13_TRIAL)) {
            Service.gI().sendThongBao(player, "Phân Thân Thần Hủy Diệt Bunny đang có mặt ngay trong khu vực này rồi!");
            return;
        }
        try {
            nro.models.boss.vu_tru_13.ThanHuyDietVt13 trial = new nro.models.boss.vu_tru_13.ThanHuyDietVt13(
                    player.zone, PYRAMID_KHE_NUT_X + 150, PYRAMID_KHE_NUT_Y);
            trial.changeStatus(nro.models.consts.BossStatus.RESPAWN);
            Service.gI().sendThongBao(player, "ẦM ẦM!! Phân Thân Thần Hủy Diệt Bunny đã xuất hiện bên cạnh khu phong ấn!");
        } catch (Exception e) {
            Logger.logException(VuTru13Service.class, e);
            Service.gI().sendThongBao(player, "Không thể triệu hồi Phân Thân lúc này!");
        }
    }

    // [TEST TẠM THỜI] Triệu hồi ngay lập tức Boss Thần Hủy Diệt Bunny hoặc Fide Đen ra ngay cạnh người chơi để test
    public synchronized void testSummonBossNow(Player player, int bossId) {
        if (player == null || !player.isPl() || player.zone == null || player.zone.map == null) {
            return;
        }
        try {
            nro.models.boss.Boss targetBoss = null;
            for (nro.models.boss.Boss b : nro.models.boss.Boss_Manager.BossManager.gI().getBosses()) {
                if (b != null && b.id == bossId) {
                    targetBoss = b;
                    break;
                }
            }
            if (targetBoss == null) {
                targetBoss = nro.models.boss.Boss_Manager.BossManager.gI().createBoss(bossId);
            }
            if (targetBoss == null) {
                Service.gI().sendThongBao(player, "Không tìm thấy dữ liệu Boss để triệu hồi!");
                return;
            }

            if (player.zone.map.mapId == MAP_KHE_NUT_VT13) {
                player.playingKheNutCutscene = false;
                player.vuTru13Stage |= STAGE_FLAG_KHE_NUT_INTRO;
                sendState(player);
            }

            if (targetBoss.zone == null) {
                // ChangeMapService.changeMap yêu cầu pl.zone != null ở bước kiểm tra đầu vào;
                // nếu targetBoss đang ở map/zone khác thì goToMap bên trong changeMap sẽ tự exitMap(oldZone) chuẩn xác!
                targetBoss.zone = player.zone;
            }

            targetBoss.currentLevel = 0;
            targetBoss.initBase();
            if (targetBoss instanceof nro.models.boss.vu_tru_13.FideDen fideDen) {
                fideDen.resetCombatTimersOnTestSummon();
            }
            int spawnX = player.zone.map.clampBossX(player.location.x + 75);
            int spawnY = player.zone.map.resolveBossGroundY(spawnX, player.location.y > 0 ? player.location.y : 100);
            nro.models.map.service.ChangeMapService.gI().changeMap(targetBoss, player.zone, spawnX, spawnY);
            Service.gI().sendFlagBag(targetBoss);
            targetBoss.changeToTypePK();
            targetBoss.changeStatus(nro.models.consts.BossStatus.ACTIVE);
            PlayerService.gI().sendTypePk(targetBoss);

            byte vfx = (bossId == nro.models.boss.BossID.THAN_HUY_DIET_VT13)
                    ? BOSS_VFX_EGYPT_SIGIL_PILLAR
                    : BOSS_VFX_DARK_SHIELD_BREAK;
            broadcastBossSkillVfx(player.zone, vfx, spawnX, spawnY, spawnX, spawnY);
            Service.gI().sendThongBao(player, "[TEST] Đã triệu hồi ngay [" + targetBoss.name + "] (100% HP) ra cạnh bạn!");
        } catch (Exception e) {
            Logger.logException(VuTru13Service.class, e);
            Service.gI().sendThongBao(player, "Lỗi khi triệu hồi Boss test: " + e.getMessage());
        }
    }

    public int countDistinctUnsealRunes(Player player) {
        if (player == null || player.inventory == null) {
            return 0;
        }
        int count = 0;
        for (int id = ITEM_AN_MO_KHOA_START; id <= ITEM_AN_MO_KHOA_END; id++) {
            if (InventoryService.gI().getTotalQuantityBag(player, id) > 0) {
                count++;
            }
        }
        return count;
    }

    public String formatUnsealRuneProgress(Player player) {
        int count = countDistinctUnsealRunes(player);
        StringBuilder owned = new StringBuilder();
        StringBuilder missing = new StringBuilder();
        for (int i = 0; i < 7; i++) {
            int id = ITEM_AN_MO_KHOA_START + i;
            int qty = InventoryService.gI().getTotalQuantityBag(player, id);
            if (qty > 0) {
                if (owned.length() > 0) {
                    owned.append(", ");
                }
                owned.append("Ấn ").append(i + 1).append(" (x").append(qty).append(")");
            } else {
                if (missing.length() > 0) {
                    missing.append(", ");
                }
                missing.append("Ấn ").append(i + 1);
            }
        }
        StringBuilder sb = new StringBuilder();
        sb.append(count == 7 ? "|2|" : "|8|").append("Tiến độ Ấn mở khóa trong hành trang: ").append(count).append("/7");
        if (owned.length() > 0) {
            sb.append("\n|2|Đã có: ").append(owned);
        }
        if (missing.length() > 0) {
            sb.append("\n|7|Còn thiếu: ").append(missing);
        }
        return sb.toString();
    }

    public void playGokuUnsealCutscene(Player player, Npc gokuNpc) {
        int fx = gokuNpc != null ? gokuNpc.cx : 840;
        int fy = gokuNpc != null ? gokuNpc.cy - 48 : 408;
        String[][] steps = new String[][]{
            {player.name, "Goku! Ta đã thu thập đủ trọn bộ 7 Ấn mở khóa từ Thần Hủy Diệt Bunny rồi! Giải khai phong ấn ngay!!", "230"},
            {"Phong Ấn Ngục Tối", "ẦM ẦM ẦM!! Bảy luồng hào quang cổ ấn xoay tròn cộng hưởng, 4 cột đá đen nứt toác, toàn bộ xiềng xích phong ấn đứt tung!", "250"},
            {"Son Goku", "HAAA!! Nguồn khí Bản Năng Vô Cực đang tràn về khắp cơ thể ta rồi!! Cảm ơn cậu rất nhiều, " + player.name + "!", "240"},
            {"Son Goku", "Sức mạnh của Thần Hủy Diệt Bunny ở Điện Thờ vẫn đang đe dọa toàn cõi Vũ Trụ 13... Hãy cùng ta bước vào nhiệm vụ mới và quét sạch thế lực Hủy Diệt này!", "250"}
        };
        sendCutscene(player, CUTSCENE_GOKU_UNSEAL, fx, fy, "", steps);
    }

    public void unsealGoku(Player player, Npc gokuNpc) {
        if (player == null || !player.isPl()) {
            return;
        }
        if ((player.vuTru13Stage & STAGE_FLAG_GOKU_UNSEALED) != 0) {
            TaskService.gI().completeGokuUnsealAndOpenNewQuest(player);
            playGokuUnsealCutscene(player, gokuNpc);
            return;
        }

        int haveCount = countDistinctUnsealRunes(player);
        if (haveCount < 7) {
            StringBuilder missing = new StringBuilder();
            for (int i = 0; i < 7; i++) {
                if (InventoryService.gI().getTotalQuantityBag(player, ITEM_AN_MO_KHOA_START + i) <= 0) {
                    if (missing.length() > 0) {
                        missing.append(", ");
                    }
                    missing.append("Ấn ").append(i + 1);
                }
            }
            Service.gI().sendThongBaoOK(player, "Chưa đủ bộ 7 Ấn mở khóa trong hành trang (" + haveCount + "/7)!\n"
                    + "Còn thiếu: " + missing + "\n"
                    + "(Hãy săn Boss Thần Hủy Diệt Bunny tại Điện Thờ VT13 để nhặt Ấn mở khóa 1 -> 7!)");
            return;
        }

        if (InventoryService.gI().getCountEmptyBag(player) < 2) {
            Service.gI().sendThongBao(player, "Hãy dọn trống ít nhất 2 ô hành trang để nhận quà cảm tạ từ Son Goku!");
            return;
        }

        for (int id = ITEM_AN_MO_KHOA_START; id <= ITEM_AN_MO_KHOA_END; id++) {
            if (!InventoryService.gI().subQuantityItemsBagPriorityLocked(player, id, 1)) {
                Service.gI().sendThongBao(player, "Không thể trừ Ấn mở khóa trong hành trang, vui lòng thử lại!");
                return;
            }
        }

        player.unlockVuTru13 = true;
        player.vuTru13Stage |= (STAGE_FLAG_AWAKENED | STAGE_FLAG_GOKU_UNSEALED);

        // Quà cảm tạ khi giải phong ấn Goku: 20 Thỏi vàng + 5 Mảnh Thần Vực Vô Cực
        Item tvReward = ItemService.gI().createNewItem((short) 457, 20);
        tvReward.itemOptions.add(new Item.ItemOption(73, 0));
        InventoryService.gI().addItemBag(player, tvReward);

        Item shardReward = ItemService.gI().createNewItem(ITEM_MANH_THAN_VUC_VO_CUC, 5);
        shardReward.itemOptions.add(new Item.ItemOption(30, 0));
        InventoryService.gI().addItemBag(player, shardReward);

        InventoryService.gI().sendItemBags(player);
        sendState(player);

        // Hoàn tất bước giải phong ấn và mở tiếp chuỗi nhiệm vụ mới ngay lập tức!
        TaskService.gI().completeGokuUnsealAndOpenNewQuest(player);

        playGokuUnsealCutscene(player, gokuNpc);
        Service.gI().sendThongBao(player, "Giải phong ấn Son Goku thành công! Nhận 20 Thỏi vàng, 5 Mảnh Đại Thiên Sứ & MỞ NHIỆM VỤ MỚI!");
        ServerNotify.gI().notify("CHẤN ĐỘNG VŨ TRỤ 13: Chiến binh " + player.name
                + " đã thu thập đủ 7 Ấn mở khóa và GIẢI PHONG ẤN THÀNH CÔNG cho SON GOKU, khai mở nhiệm vụ Đại Chiến Thần Vực!");
        PlayerDAO.savePlayerAsync(player);
    }
}
