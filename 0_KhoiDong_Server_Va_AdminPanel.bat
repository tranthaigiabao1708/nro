@ECHO OFF
chcp 65001 >nul
setlocal EnableDelayedExpansion
title NGỌC RỒNG NEWGEN - KHỞI ĐỘNG SERVER + ADMIN PANEL
color 0E
set "ROOT_DIR=%~dp0"
set "SERVER_DIR=%ROOT_DIR%NgocRongNewGen_Server"
set "JAVA_EXE=C:\Users\giaba\.antigravity-ide\extensions\redhat.java-1.58.0-win32-x64\jre\25.0.4.1-win32-x86_64\bin\java.exe"
if exist "%ROOT_DIR%runtime\jdk21\bin\java.exe" set "JAVA_EXE=%ROOT_DIR%runtime\jdk21\bin\java.exe"
if not exist "%JAVA_EXE%" set "JAVA_EXE=java"
set "MYSQL_EXE=%ROOT_DIR%runtime\mariadb\bin\mysql.exe"
set "NEWGEN_SQL=%ROOT_DIR%Database_NgocRongNewGen\nro_newgen.sql"

REM Đọc cổng game & cổng Admin Panel từ Config.properties
set "GAME_PORT=14445"
set "PANEL_PORT=8686"
for /f "usebackq tokens=1,* delims==" %%a in ("%SERVER_DIR%\Config.properties") do (
    if /i "%%a"=="server.port" set "GAME_PORT=%%b"
    if /i "%%a"=="admin.panel.port" set "PANEL_PORT=%%b"
)
set "PANEL_URL=http://localhost:%PANEL_PORT%"

echo ====================================================================
echo              NGỌC RỒNG NEWGEN - SERVER + ADMIN PANEL
echo ====================================================================
echo.
echo [1/3] Kiểm tra cơ sở dữ liệu MariaDB (cổng 3306, DB: nro_newgen)...
set /a WAIT_DB=0
netstat -ano | findstr ":3306 " | findstr "LISTENING" >nul
if not errorlevel 1 goto db_ready
echo       Đang khởi động MariaDB...
start "MariaDB_NgocRongNewGen" /MIN "%ROOT_DIR%runtime\mariadb\bin\mysqld.exe" --defaults-file="%ROOT_DIR%runtime\mariadb_data\my.ini" --console
:wait_db
timeout /t 1 /nobreak >nul
set /a WAIT_DB+=1
netstat -ano | findstr ":3306 " | findstr "LISTENING" >nul
if not errorlevel 1 goto db_ready
if !WAIT_DB! LSS 30 goto wait_db
color 0C
echo [LỖI] Không bật được MariaDB. Kiểm tra thư mục runtime\mariadb.
pause
exit /b 1

:db_ready
echo       [OK] MariaDB đang chạy.

if exist "%MYSQL_EXE%" (
    "%MYSQL_EXE%" -u root -P 3306 -h 127.0.0.1 -e "USE nro_newgen;" >nul 2>&1
    if errorlevel 1 (
        echo       Chưa có database nro_newgen, đang nạp từ nro_newgen.sql...
        "%MYSQL_EXE%" -u root -P 3306 -h 127.0.0.1 -e "CREATE DATABASE IF NOT EXISTS nro_newgen CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;"
        "%MYSQL_EXE%" -u root -P 3306 -h 127.0.0.1 --default-character-set=utf8mb4 nro_newgen < "%NEWGEN_SQL%"
        echo       [OK] Đã nạp database nro_newgen.
    )
)

echo.
echo [2/3] Khởi động Game Server (cổng %GAME_PORT%)...
netstat -ano | findstr ":%GAME_PORT% " | findstr "LISTENING" >nul
if not errorlevel 1 (
    echo       [OK] Game Server đã chạy sẵn, bỏ qua bước khởi động.
) else (
    start "NGOC RONG NEWGEN - GAME SERVER (go 'stop' de luu va tat)" /D "%SERVER_DIR%" "%JAVA_EXE%" -server -Xms512M -Xmx2048M -Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8 -cp "lib/*;dist/NgocRongNewGen.jar" nro.models.server.ServerManager
    echo       Đã mở cửa sổ Game Server, đang chờ server sẵn sàng...
)

echo.
echo [3/3] Chờ Admin Panel (cổng %PANEL_PORT%)...
set /a WAIT_PANEL=0
:wait_panel
netstat -ano | findstr ":%PANEL_PORT% " | findstr "LISTENING" >nul
if not errorlevel 1 goto panel_ready
set /a WAIT_PANEL+=1
if !WAIT_PANEL! GEQ 90 goto panel_timeout
timeout /t 1 /nobreak >nul
goto wait_panel

:panel_ready
color 0A
echo       [OK] Admin Panel đã sẵn sàng!
start "" "%PANEL_URL%"
echo.
echo ====================================================================
echo   GAME SERVER : 127.0.0.1:%GAME_PORT%
echo   ADMIN PANEL : %PANEL_URL%
echo   Mật khẩu    : admin.panel.password trong NgocRongNewGen_Server\Config.properties
echo.
echo   LƯU Ý: Muốn tắt server hãy bấm "Lưu ^& Tắt server" trong Admin Panel
echo          hoặc gõ  stop  trong cửa sổ Game Server.
echo          KHÔNG bấm nút X của cửa sổ Game Server (dễ mất dữ liệu)!
echo ====================================================================
echo.
pause
exit /b 0

:panel_timeout
color 0C
echo [LỖI] Sau 90 giây Admin Panel vẫn chưa mở.
echo       - Xem lỗi trong cửa sổ Game Server.
echo       - Kiểm tra admin.panel.enabled / admin.panel.password trong Config.properties.
pause
exit /b 1
