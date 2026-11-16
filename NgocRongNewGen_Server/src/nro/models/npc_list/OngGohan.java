package nro.models.npc_list;

import nro.models.consts.ConstNpc;
import nro.models.map.service.ChangeMapService;
import nro.models.npc.Npc;
import nro.models.player.Player;
import nro.models.services.PetService;
import nro.models.services.Service;
import nro.models.services.SkhCollectionService;
import nro.models.services.TaskService;
import nro.models.services.VuTru13Service;
import nro.models.services_func.Input;

public class OngGohan extends Npc {

    public OngGohan(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (canOpenNpc(player)) {
            if (!TaskService.gI().checkDoneTaskTalkNpc(player, this)) {
                this.createOtherMenu(player, ConstNpc.BASE_MENU,
                        "Con cố gắng theo Quy Lão Kame học thành tài, đừng lo lắng cho ta.\n"
                        + "Ta có dựng sẵn Bảng Sưu Tập Set Kích Hoạt trong nhà cho con đây!",
                        "Đổi ngọc xanh\n1 TV = 1k ngọc",
                        "Nhận đệ tử",
                        "Bảng Sưu Tập\nSet Kích Hoạt",
                        "Nhận Hào Quang\n(Đủ 5 Set)",
                        "Hướng dẫn\nSet Kích Hoạt",
                        "Vũ Trụ 13\n& Cải Trang",
                        "Đóng");
            }
        }
    }

    protected void openVuTru13Menu(Player player) {
        if (VuTru13Service.gI().canAwakenAtHome(player)) {
            if (!player.unlockVuTru13) {
                this.createOtherMenu(player, ConstNpc.MENU_VU_TRU_13_HOME,
                        "|7|KHAI MỞ SỨC MẠNH VŨ TRỤ 13 - VŨ TRỤ BỊ NGUYỀN RỦA\n"
                        + "|2|Con đã hoàn thành nhiệm vụ cuối! Hãy tiếp nhận Huyết Ấn Thức Tỉnh để mở khóa Ngoại Hình Vũ Trụ 13, +15% HP/KI/SĐ và nhận [Ấn Khai Mở Vũ Trụ 13]!",
                        "Thức Tỉnh\nVũ Trụ 13",
                        player.hideCaiTrang ? "Hiện lại\nCải Trang" : "Ẩn Cải Trang",
                        "Đóng");
            } else {
                this.createOtherMenu(player, ConstNpc.MENU_VU_TRU_13_HOME,
                        "|8|SỨC MẠNH VŨ TRỤ 13 (ĐÃ THỨC TỈNH: +15% HP/KI/SĐ)\n"
                        + "|2|- Trạng thái hiển thị: " + (player.hideCaiTrang ? "Đang ẨN Cải Trang (Mọi người thấy Ngoại Hình VT13)" : "Đang HIỆN Cải Trang / Hợp thể") + "\n"
                        + "|1|- Có thể dùng [Ấn Khai Mở Vũ Trụ 13] trong hành trang bất cứ lúc nào!",
                        "Đến Vũ Trụ 13\nNgay",
                        player.hideCaiTrang ? "Hiện lại\nCải Trang" : "Ẩn Cải Trang\n(Hiện VT13)",
                        "Nhận lại Ấn\nVũ Trụ 13",
                        "Xem lại Cảnh\nThức Tỉnh",
                        "Đóng");
            }
        } else {
            this.createOtherMenu(player, ConstNpc.MENU_VU_TRU_13_HOME,
                    "|7|PHONG ẤN VŨ TRỤ 13 CHƯA MỞ!\n"
                    + "|2|Con cần hoàn thành nhiệm vụ chính tuyến cuối cùng (Cuộc đối đầu không cân sức) rồi quay về gặp ta để khai mở Sức Mạnh Vũ Trụ 13!\n"
                    + "|1|Con vẫn có thể bật/tắt Ẩn Cải Trang ngay bên dưới:",
                    player.hideCaiTrang ? "Hiện lại\nCải Trang" : "Ẩn Cải Trang",
                    "Đóng");
        }
    }

    protected void openPetMenu(Player player) {
        this.createOtherMenu(player, ConstNpc.MENU_NHAN_DE_TU,
                "|8|HỆ THỐNG ĐỆ TỬ\n"
                + "|2|Con hãy chọn loại Đệ tử muốn nhận hoặc xem cách sở hữu:",
                "Đệ tử\nthường",
                "Đệ tử\nMabư thường",
                "Đệ tử\nMabư Vip",
                "Đệ tử Uub /\nKid Beer /\nKid Jiren",
                "Đóng");
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (canOpenNpc(player)) {
            if (player.idMark.isBaseMenu()) {
                switch (select) {
                    case 0 -> Input.gI().createFormDoiThoiVangRaNgoc(player);
                    case 1 -> openPetMenu(player);
                    case 2 -> SkhCollectionService.gI().openCollectionUI(player, 0);
                    case 3 -> SkhCollectionService.gI().claimFiveSetAuraReward(player, false);
                    case 4 -> SkhCollectionService.gI().showGuide(player);
                    case 5 -> openVuTru13Menu(player);
                }
            } else if (player.idMark.getIndexMenu() == ConstNpc.MENU_VU_TRU_13_HOME) {
                if (VuTru13Service.gI().canAwakenAtHome(player)) {
                    if (!player.unlockVuTru13) {
                        switch (select) {
                            case 0 -> VuTru13Service.gI().triggerHomeAwakening(player, this);
                            case 1 -> VuTru13Service.gI().toggleHideCaiTrang(player);
                        }
                    } else {
                        switch (select) {
                            case 0 -> ChangeMapService.gI().changeMapBySpaceShip(player, VuTru13Service.MAP_THANH_DIEN_ZENO_VT13, -1, 300);
                            case 1 -> VuTru13Service.gI().toggleHideCaiTrang(player);
                            case 2 -> VuTru13Service.gI().ensureRelicItem(player, true);
                            case 3 -> VuTru13Service.gI().triggerHomeAwakening(player, this);
                        }
                    }
                } else if (select == 0) {
                    VuTru13Service.gI().toggleHideCaiTrang(player);
                }
            } else if (player.idMark.getIndexMenu() == ConstNpc.MENU_NHAN_DE_TU) {
                switch (select) {
                    case 0 -> {
                        synchronized (player) {
                            if (player.pet != null) {
                                Service.gI().sendThongBao(player, "Con đã có đệ tử rồi!");
                            } else {
                                PetService.gI().createNormalPet(player);
                                Service.gI().sendThongBao(player, "Nhận Đệ tử thường thành công! (Hợp thể 40% chỉ số)");
                            }
                        }
                    }
                    case 1 -> this.createOtherMenu(player, ConstNpc.MENU_HUONG_DAN_DE_TU,
                            "|8|ĐỆ TỬ MABƯ THƯỜNG (Hợp thể 60% chỉ số)\n"
                            + "|2|- Cách sở hữu: Ước bộ Ngọc Rồng Bí Ngô (farm trong Bản Đồ Kho Báu - cấp càng cao rơi càng nhiều) gọi Rồng Xương nhận Quả Trứng Mabư Thường (tỉ lệ 50%).\n"
                            + "|1|- Dùng trứng đặt tại Nhà rồi ấp nở để nhận đệ.\n"
                            + "|7|- Lưu ý: Mabư thường không thể nâng cấp lên đệ cấp cuối.",
                            "Quay lại", "Đóng");
                    case 2 -> this.createOtherMenu(player, ConstNpc.MENU_HUONG_DAN_DE_TU,
                            "|8|ĐỆ TỬ MABƯ VIP (Hợp thể 100% chỉ số)\n"
                            + "|2|- Cách sở hữu: Tiêu diệt Boss Mabư ở Phòng chỉ huy (tầng cuối Map Mabư) tại sự kiện Mabư lúc 12h - 13h hàng ngày (gặp NPC Ôsin ở Đại hội võ thuật để vào, tỉ lệ 10% rơi trứng).\n"
                            + "|1|- Dùng Quả Trứng Mabư Vip đặt tại Nhà rồi ấp nở (đạt 40 tỷ SM được phép nâng lên đệ cấp cuối).",
                            "Quay lại", "Đóng");
                    case 3 -> this.createOtherMenu(player, ConstNpc.MENU_HUONG_DAN_DE_TU,
                            "|8|ĐỆ TỬ UUB / KID BEER / KID JIREN\n"
                            + "|2|- Điều kiện: Đang sở hữu Đệ Mabư Vip đạt từ 40 tỷ sức mạnh trở lên.\n"
                            + "- Săn Bình hút năng lượng: Hạ Boss Hirudegarn tại Thành phố Santa (gặp NPC Tapion ở Thành phố Vegeta từ 1h - 23h, tỉ lệ 20% rơi bình).\n"
                            + "|1|- Tiến hóa: Gặp NPC Ôsin (Đại hội võ thuật) chọn \"Bình hút năng lượng\" để vào map 165 (Sa mạc hoang vu) đánh quái tích điểm Kilis lên đủ 3.000 rồi dùng bình; Đệ Mabư Vip sẽ tiến hóa ngẫu nhiên (tỉ lệ đều 33,3% mỗi loại).",
                            "Quay lại", "Đóng");
                }
            } else if (player.idMark.getIndexMenu() == ConstNpc.MENU_HUONG_DAN_DE_TU) {
                if (select == 0) {
                    openPetMenu(player);
                }
            }
        }
    }
}
