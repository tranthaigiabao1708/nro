@ECHO OFF
chcp 65001 >nul
title HỆ THỐNG KHỞI ĐỘNG NGỌC RỒNG TEAMOBI 2026 (DATABASE + SERVER)
color 0A
set "ROOT_DIR=%~dp0"
set "JAVA_EXE=C:\Users\giaba\.antigravity-ide\extensions\redhat.java-1.58.0-win32-x64\jre\25.0.4.1-win32-x86_64\bin\java.exe"
if exist "%ROOT_DIR%runtime\jdk21\bin\java.exe" set "JAVA_EXE=%ROOT_DIR%runtime\jdk21\bin\java.exe"

echo ====================================================================
echo   [1/2] KIỂM TRA CƠ SỞ DỮ LIỆU MARIADB (PORT 3306 - DB: team2026)
echo ====================================================================
netstat -ano | findstr ":3306 " | findstr "LISTENING" >nul
if %ERRORLEVEL% EQU 0 (
    echo [OK] MariaDB đang chạy sẵn trên cổng 3306!
) else (
    echo [INFO] Đang khởi động MariaDB Portable...
    start "MariaDB_Teamobi2026" /MIN "%ROOT_DIR%runtime\mariadb\bin\mysqld.exe" --defaults-file="%ROOT_DIR%runtime\mariadb_data\my.ini" --console
    timeout /t 4 /nobreak >nul
    echo [OK] Đã bật MariaDB thành công!
)

echo.
echo ====================================================================
echo   [2/2] KIỂM TRA ^& KHỞI ĐỘNG GAME SERVER TEAMOBI 2026 (PORT 14445)
echo ====================================================================
netstat -ano | findstr ":14445 " | findstr "LISTENING" >nul
if %ERRORLEVEL% EQU 0 (
    echo [OK] Game Server ĐANG CHẠY SẴN trên cổng 14445!
    echo      Đang mở luôn Client Game để bạn vào kiểm tra...
    start "" "%ROOT_DIR%Client_ChoNguoiChoiTaiVe\Ngoc Rong.exe"
    echo.
    echo Nếu muốn tắt Server chạy ngầm để bật lại tại cửa sổ này, hãy gõ R rồi nhấn Enter.
    set /p RESTART_CHOICE="Nhấn Enter để đóng thông báo (hoặc gõ R + Enter để khởi động lại Server): "
    if /I not "%RESTART_CHOICE%"=="R" exit /b 0
    for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":14445 " ^| findstr "LISTENING"') do taskkill /F /PID %%a >nul 2>&1
    timeout /t 2 /nobreak >nul
)

echo   - Gõ "reload" + Enter tại cửa sổ này để nạp nóng Config ^& Shop
echo   - Gõ "bt" + Enter để bảo trì an toàn (lưu Clan ^& Ký gửi)
echo ====================================================================
cd /d "%ROOT_DIR%Teamobi2026\SRC"
"%JAVA_EXE%" -server -Xms512M -Xmx2048M -Dfile.encoding=UTF-8 -cp "lib/*;dist/NgocRongOnline.jar" nro.models.server.ServerManager
PAUSE
