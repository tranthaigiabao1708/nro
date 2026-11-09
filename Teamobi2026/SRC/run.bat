@ECHO OFF
chcp 65001 >nul
title NGOC RONG TEAMOBI 2026 - GAME SERVER (PORT 14445)
cd /d "%~dp0"

netstat -ano | findstr ":3306 " | findstr "LISTENING" >nul
if %ERRORLEVEL% NEQ 0 (
    echo [INFO] Đang bật MariaDB trên cổng 3306...
    start "MariaDB_Teamobi2026" /MIN "c:\mro\runtime\mariadb\bin\mysqld.exe" --defaults-file="c:\mro\runtime\mariadb_data\my.ini" --console
    timeout /t 4 /nobreak >nul
)

netstat -ano | findstr ":14445 " | findstr "LISTENING" >nul
if %ERRORLEVEL% EQU 0 (
    echo [INFO] Phát hiện cổng 14445 đang có Server chạy ngầm, đang chuyển sang cửa sổ hiện tại...
    for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":14445 " ^| findstr "LISTENING"') do taskkill /F /PID %%a >nul 2>&1
    timeout /t 2 /nobreak >nul
)

"c:\mro\runtime\jdk21\bin\java.exe" -server -Xms512M -Xmx2048M -Dfile.encoding=UTF-8 -cp "lib/*;dist/NgocRongOnline.jar" nro.models.server.ServerManager
PAUSE
