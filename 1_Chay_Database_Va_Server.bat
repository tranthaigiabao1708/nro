@ECHO OFF
chcp 65001 >nul
title HỆ THỐNG KHỞI ĐỘNG NGỌC RỒNG TEAMOBI 2026 (DATABASE + SERVER)
color 0A

echo ====================================================================
echo   [1/2] KIỂM TRA CƠ SỞ DỮ LIỆU MARIADB (PORT 3306 - DB: team2026)
echo ====================================================================
netstat -ano | findstr ":3306 " | findstr "LISTENING" >nul
if %ERRORLEVEL% EQU 0 (
    echo [OK] MariaDB đang chạy sẵn trên cổng 3306!
) else (
    echo [INFO] Đang khởi động MariaDB Portable...
    start "MariaDB_Teamobi2026" /MIN "c:\mro\runtime\mariadb\bin\mysqld.exe" --defaults-file="c:\mro\runtime\mariadb_data\my.ini" --console
    timeout /t 4 /nobreak >nul
    echo [OK] Đã bật MariaDB thành công!
)

echo.
echo ====================================================================
echo   [2/2] KHỞI ĐỘNG GAME SERVER TEAMOBI 2026 (PORT 14445)
echo   - Gõ "reload" + Enter tại cửa sổ này để nạp nóng Config ^& Shop
echo   - Gõ "bt" + Enter để bảo trì an toàn (lưu Clan ^& Ký gửi)
echo ====================================================================
cd /d "c:\mro\Teamobi2026\SRC"
"c:\mro\runtime\jdk21\bin\java.exe" -server -Xms512M -Xmx2048M -Dfile.encoding=UTF-8 -cp "lib/*;dist/NgocRongOnline.jar" nro.models.server.ServerManager
PAUSE

