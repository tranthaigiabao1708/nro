package nro.server;

import nro.services.Service;
import nro.utils.Log;

/**
 *
 * @author 💖 YTB KhanhDTK 💖
 * 
 *
 */
public class Maintenance extends Thread {

    public static boolean isRuning = false;

    private static Maintenance i;

    private int seconds;

    private Maintenance() {

    }

    public static Maintenance gI() {
        if (i == null) {
            i = new Maintenance();
        }
        return i;
    }

    public void start(int seconds) {
        if (!isRuning) {
            isRuning = true;
            this.seconds = seconds;
            this.start();
        }
    }

    @Override
    public void run() {
        while (this.seconds > 0) {
            this.seconds--;
            Service.getInstance().sendThongBaoAllPlayer("Hệ thống sẽ bảo trì sau " + seconds
                    + " giây nữa, vui lòng thoát game để tránh mất vật phẩm");
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
            }
        }
        Log.error("BEGIN MAINTENANCE...............................");
        ServerManager.gI().close(100);
    }

}
