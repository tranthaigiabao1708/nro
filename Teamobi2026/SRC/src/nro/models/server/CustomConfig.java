package nro.models.server;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import nro.models.utils.Logger;

/**
 * Cấu hình mở rộng cho Admin dễ dàng tùy chỉnh Tỉ lệ rơi đồ, Quà Tân Thủ,
 * Nhiệm vụ khởi đầu, Vật phẩm rơi toàn map và Tự động đăng ký tài khoản
 * ngay trong file Config.properties (hỗ trợ nạp nóng bằng lệnh Admin "reload").
 */
public class CustomConfig {

    public static class GlobalDropEntry {
        public final int itemId;
        public final int quantity;
        public final int chancePer1000;

        public GlobalDropEntry(int itemId, int quantity, int chancePer1000) {
            this.itemId = itemId;
            this.quantity = quantity;
            this.chancePer1000 = chancePer1000;
        }
    }

    // Tự động đăng ký tài khoản khi người chơi mới tải game đăng nhập lần đầu
    public static boolean AUTO_REGISTER = true;
    public static int STARTER_VND = 100_000;

    // Cấu hình Nhân vật Tân Thủ khi mới tạo
    public static long STARTER_GOLD = 50_000_000L;
    public static int STARTER_GEM = 50_000;
    public static int STARTER_RUBY = 10_000;
    public static long STARTER_POWER = 1_500_000L;
    public static int STARTER_TASK_ID = 0;
    public static int STARTER_GOLD_BAR = 20;
    public static boolean STARTER_CAPSULE_VIP = true;

    // Cấu hình Tỉ lệ rơi đồ & Vàng/Ngọc khi đánh quái
    public static int DROP_RATE_MULT = 3;
    public static int DROP_GOLD_MULT = 3;
    public static int DROP_GEM_PERCENT = 8;
    public static int DROP_GEM_AMOUNT = 5;
    public static volatile List<GlobalDropEntry> CUSTOM_GLOBAL_DROPS = new ArrayList<>();

    static {
        load();
    }

    public static synchronized void load() {
        Properties prop = new Properties();
        try (InputStreamReader reader = new InputStreamReader(new FileInputStream("Config.properties"), StandardCharsets.UTF_8)) {
            prop.load(reader);

            AUTO_REGISTER = Boolean.parseBoolean(prop.getProperty("custom.autoregister", "true").trim());
            STARTER_VND = parseInt(prop.getProperty("custom.starter.vnd"), 100_000);

            STARTER_GOLD = parseLong(prop.getProperty("custom.starter.gold"), 50_000_000L);
            STARTER_GEM = parseInt(prop.getProperty("custom.starter.gem"), 50_000);
            STARTER_RUBY = parseInt(prop.getProperty("custom.starter.ruby"), 10_000);
            STARTER_POWER = parseLong(prop.getProperty("custom.starter.power"), 1_500_000L);
            STARTER_TASK_ID = parseInt(prop.getProperty("custom.starter.task"), 0);
            STARTER_GOLD_BAR = parseInt(prop.getProperty("custom.starter.goldbar"), 20);
            STARTER_CAPSULE_VIP = Boolean.parseBoolean(prop.getProperty("custom.starter.capsule_vip", "true").trim());

            DROP_RATE_MULT = Math.max(1, parseInt(prop.getProperty("custom.drop.rate_mult"), 3));
            DROP_GOLD_MULT = Math.max(1, parseInt(prop.getProperty("custom.drop.gold_mult"), 3));
            DROP_GEM_PERCENT = Math.max(0, Math.min(100, parseInt(prop.getProperty("custom.drop.gem_percent"), 8)));
            DROP_GEM_AMOUNT = Math.max(1, parseInt(prop.getProperty("custom.drop.gem_amount"), 5));

            List<GlobalDropEntry> parsedDrops = new ArrayList<>();
            String rawDrops = prop.getProperty("custom.drop.global_items", "").trim();
            if (!rawDrops.isEmpty()) {
                for (String token : rawDrops.split(",")) {
                    String[] parts = token.trim().split(":");
                    if (parts.length >= 3) {
                        int itemId = parseInt(parts[0], -1);
                        int qty = Math.max(1, parseInt(parts[1], 1));
                        int chance = Math.max(1, Math.min(1000, parseInt(parts[2], 10)));
                        if (itemId >= 0) {
                            parsedDrops.add(new GlobalDropEntry(itemId, qty, chance));
                        }
                    }
                }
            }
            CUSTOM_GLOBAL_DROPS = parsedDrops;
            Logger.success("[CustomConfig] Đã nạp cấu hình mở rộng: AutoReg=" + AUTO_REGISTER
                    + ", DropMult=x" + DROP_RATE_MULT + ", StarterGold=" + STARTER_GOLD + "\n");
        } catch (Exception e) {
            Logger.error("[CustomConfig] Không đọc được cấu hình mở rộng, dùng mặc định: " + e.getMessage() + "\n");
        }
    }

    private static int parseInt(String val, int def) {
        if (val == null || val.trim().isEmpty()) return def;
        try {
            return Integer.parseInt(val.trim());
        } catch (NumberFormatException e) {
            return def;
        }
    }

    private static long parseLong(String val, long def) {
        if (val == null || val.trim().isEmpty()) return def;
        try {
            return Long.parseLong(val.trim());
        } catch (NumberFormatException e) {
            return def;
        }
    }
}

