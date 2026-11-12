package nro.models.npc_list;

import nro.models.consts.ConstNpc;
import nro.models.npc.Npc;
import nro.models.player.Player;
import nro.models.services.PetService;
import nro.models.services.Service;
import nro.models.services.SkhCollectionService;
import nro.models.services.TaskService;
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
                        "Đóng");
            }
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
