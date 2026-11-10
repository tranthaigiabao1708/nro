@ECHO OFF
chcp 65001 >nul
title NGOC RONG NEWGEN - GAME SERVER (PORT 14445)
cd /d "%~dp0"
for %%I in ("%~dp0..\..") do set "ROOT_DIR=%%~fI\"
set "JAVA_EXE=C:\Users\giaba\.antigravity-ide\extensions\redhat.java-1.58.0-win32-x64\jre\25.0.4.1-win32-x86_64\bin\java.exe"
if exist "%ROOT_DIR%runtime\jdk21\bin\java.exe" set "JAVA_EXE=%ROOT_DIR%runtime\jdk21\bin\java.exe"
set "MYSQL_EXE=%ROOT_DIR%runtime\mariadb\bin\mysql.exe"
set "NEWGEN_SQL=%ROOT_DIR%Database_NgocRongNewGen\nro_newgen.sql"

netstat -ano | findstr ":3306 " | findstr "LISTENING" >nul
if %ERRORLEVEL% NEQ 0 (
    echo [INFO] Đang bật MariaDB trên cổng 3306...
    start "MariaDB_NgocRongNewGen" /MIN "%ROOT_DIR%runtime\mariadb\bin\mysqld.exe" --defaults-file="%ROOT_DIR%runtime\mariadb_data\my.ini" --console
    timeout /t 4 /nobreak >nul
)

if exist "%MYSQL_EXE%" (
    "%MYSQL_EXE%" -u root -P 3306 -h 127.0.0.1 -e "USE nro_newgen;" >nul 2>&1
    if errorlevel 1 (
        if exist "%NEWGEN_SQL%" (
            echo [INFO] Đang nạp database nro_newgen lần đầu...
            "%MYSQL_EXE%" -u root -P 3306 -h 127.0.0.1 --default-character-set=utf8mb4 < "%NEWGEN_SQL%"
        )
    )
)

netstat -ano | findstr ":14445 " | findstr "LISTENING" >nul
if %ERRORLEVEL% EQU 0 (
    echo [INFO] Phát hiện cổng 14445 đang có Server chạy ngầm, đang chuyển sang cửa sổ hiện tại...
    for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":14445 " ^| findstr "LISTENING"') do taskkill /F /PID %%a >nul 2>&1
    timeout /t 2 /nobreak >nul
)

"%JAVA_EXE%" -server -Xms512M -Xmx2048M -Dfile.encoding=UTF-8 -cp "lib/*;dist/NgocRongOnline.jar" nro.models.server.ServerManager
PAUSE
