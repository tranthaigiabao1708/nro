@echo off
chcp 65001 >nul
title NGOC RONG NEWGEN - XUAT DATABASE CHUAN UTF-8 (nro_newgen.sql)
color 0B

set "SCRIPT_DIR=%~dp0"
for %%I in ("%SCRIPT_DIR%..") do set "ROOT_DIR=%%~fI\"
set "MYSQLDUMP_EXE=%ROOT_DIR%runtime\mariadb\bin\mysqldump.exe"
set "OUT_SQL=%SCRIPT_DIR%nro_newgen.sql"

echo ==============================================================
echo     XUAT DATABASE CHINH THUC: NGOC RONG NEWGEN (nro_newgen)
echo ==============================================================
echo.

if not exist "%MYSQLDUMP_EXE%" (
    echo [LOI] Khong tim thay mysqldump tai: %MYSQLDUMP_EXE%
    pause
    exit /b 1
)

echo [1/2] Dang xuat du lieu tu MariaDB (127.0.0.1:3306 / DB: nro_newgen)...
"%MYSQLDUMP_EXE%" -u root -P 3306 -h 127.0.0.1 --default-character-set=utf8mb4 --single-transaction --routines --triggers --databases nro_newgen --result-file="%OUT_SQL%"

if errorlevel 1 (
    color 0C
    echo [LOI] Xuat file SQL that bai! Hay dam bao MariaDB dang chay tren cong 3306.
    pause
    exit /b 1
)

echo [2/2] Xuat thanh cong file SQL chuan UTF-8 tai:
echo       %OUT_SQL%
echo.
pause

