@ECHO OFF
chcp 65001 >nul
setlocal EnableDelayedExpansion
title HỆ THỐNG KHỞI ĐỘNG NGỌC RỒNG NEWGEN (DATABASE + SERVER)
color 0A
set "ROOT_DIR=%~dp0"
set "JAVA_EXE=C:\Users\giaba\.antigravity-ide\extensions\redhat.java-1.58.0-win32-x64\jre\25.0.4.1-win32-x86_64\bin\java.exe"
if exist "%ROOT_DIR%runtime\jdk21\bin\java.exe" set "JAVA_EXE=%ROOT_DIR%runtime\jdk21\bin\java.exe"
set "MYSQL_EXE=%ROOT_DIR%runtime\mariadb\bin\mysql.exe"
if not exist "%MYSQL_EXE%" set "MYSQL_EXE=%ROOT_DIR%runtime\mariadb\bin\mariadb.exe"
set "NEWGEN_SQL=%ROOT_DIR%Database_NgocRongNewGen\nro_newgen.sql"
set "CONFIG_SQL=%ROOT_DIR%Database_NgocRongNewGen\data_cau_hinh_shop_item.sql"
set "SYNC_HASH_FILE=%ROOT_DIR%runtime\mariadb_data\.shop_sync_md5"

echo ====================================================================
echo   [1/2] KIỂM TRA CƠ SỞ DỮ LIỆU MARIADB (PORT 3306 - DB: nro_newgen)
echo ====================================================================
netstat -ano | findstr ":3306 " | findstr "LISTENING" >nul
if %ERRORLEVEL% EQU 0 (
    echo [OK] MariaDB đang chạy sẵn trên cổng 3306!
) else (
    echo [INFO] Đang khởi động MariaDB Portable...
    start "MariaDB_NgocRongNewGen" /MIN "%ROOT_DIR%runtime\mariadb\bin\mysqld.exe" --defaults-file="%ROOT_DIR%runtime\mariadb_data\my.ini" --console
    timeout /t 4 /nobreak >nul
    echo [OK] Đã bật MariaDB thành công!
)

if exist "%MYSQL_EXE%" (
    "%MYSQL_EXE%" -u root -P 3306 -h 127.0.0.1 -e "USE nro_newgen;" >nul 2>&1
    if errorlevel 1 (
        if exist "%NEWGEN_SQL%" (
            echo [INFO] Chưa thấy database nro_newgen, đang tự động nạp từ %NEWGEN_SQL%...
            "%MYSQL_EXE%" -u root -P 3306 -h 127.0.0.1 --default-character-set=utf8mb4 < "%NEWGEN_SQL%"
            if exist "%CONFIG_SQL%" (
                set "CUR_MD5="
                for /f "skip=1 tokens=1" %%h in ('certutil -hashfile "%CONFIG_SQL%" MD5') do if not defined CUR_MD5 set "CUR_MD5=%%h"
                if defined CUR_MD5 >"%SYNC_HASH_FILE%" echo !CUR_MD5!
            )
            echo [OK] Đã khởi tạo thành công database nro_newgen!
        )
    ) else if exist "%CONFIG_SQL%" (
        set "CUR_MD5="
        set "OLD_MD5="
        for /f "skip=1 tokens=1" %%h in ('certutil -hashfile "%CONFIG_SQL%" MD5') do if not defined CUR_MD5 set "CUR_MD5=%%h"
        if exist "%SYNC_HASH_FILE%" set /p OLD_MD5=<"%SYNC_HASH_FILE%"
        if /I not "!CUR_MD5!"=="!OLD_MD5!" (
            echo [INFO] Phát hiện cập nhật Shop / Vật phẩm mới từ Git, đang đồng bộ an toàn ^(giữ nguyên nhân vật^)...
            "%MYSQL_EXE%" -u root -P 3306 -h 127.0.0.1 --default-character-set=utf8mb4 nro_newgen < "%CONFIG_SQL%"
            if not errorlevel 1 (
                >"%SYNC_HASH_FILE%" echo !CUR_MD5!
                echo [OK] Đã cập nhật Shop / Vật phẩm mới nhất từ Git!
            )
        )
    )
)

echo.
echo ====================================================================
echo   [2/2] KIỂM TRA ^& KHỞI ĐỘNG GAME SERVER NGỌC RỒNG NEWGEN (PORT 14445)
echo ====================================================================
netstat -ano | findstr ":14445 " | findstr "LISTENING" >nul
if %ERRORLEVEL% EQU 0 (
    echo [OK] Game Server Ngọc Rồng NewGen ĐANG CHẠY SẴN trên cổng 14445!
    echo      Đang mở luôn Client Game để bạn vào kiểm tra...
    if exist "%ROOT_DIR%NgocRongNewGen_Client\NgocRongNewGen.exe" (
        start "" "%ROOT_DIR%NgocRongNewGen_Client\NgocRongNewGen.exe"
    ) else if exist "%ROOT_DIR%NgocRongNewGen_Client\Ngoc Rong.exe" (
        start "" "%ROOT_DIR%NgocRongNewGen_Client\Ngoc Rong.exe"
    )
    echo.
    echo Nếu muốn tắt Server chạy ngầm để bật lại tại cửa sổ này, hãy gõ R rồi nhấn Enter.
    set /p RESTART_CHOICE="Nhấn Enter để đóng thông báo (hoặc gõ R + Enter để khởi động lại Server): "
    if /I not "%RESTART_CHOICE%"=="R" exit /b 0
    for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":14445 " ^| findstr "LISTENING"') do (
        taskkill /PID %%a >nul 2>&1
        timeout /t 2 /nobreak >nul
        taskkill /F /PID %%a >nul 2>&1
    )
    timeout /t 1 /nobreak >nul
)

echo   - Gõ "reload" + Enter tại cửa sổ này để nạp nóng Config ^& Shop
echo   - Gõ "bt" + Enter để bảo trì an toàn (lưu Clan ^& Ký gửi)
echo ====================================================================
cd /d "%ROOT_DIR%NgocRongNewGen_Server"
"%JAVA_EXE%" -server -Xms512M -Xmx2048M -Dfile.encoding=UTF-8 -cp "lib/*;dist/NgocRongNewGen.jar" nro.models.server.ServerManager
PAUSE
