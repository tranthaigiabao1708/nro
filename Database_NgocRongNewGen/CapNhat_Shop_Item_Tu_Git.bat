@echo off
chcp 65001 >nul
setlocal EnableDelayedExpansion
title NGOC RONG NEWGEN - CAP NHAT SHOP / ITEM TU GIT (GIU NGUYEN NHAN VAT)
color 0E

set "SCRIPT_DIR=%~dp0"
for %%I in ("%SCRIPT_DIR%..") do set "ROOT_DIR=%%~fI\"
set "MYSQL_EXE=%ROOT_DIR%runtime\mariadb\bin\mysql.exe"
if not exist "%MYSQL_EXE%" set "MYSQL_EXE=%ROOT_DIR%runtime\mariadb\bin\mariadb.exe"
set "CONFIG_SQL=%SCRIPT_DIR%data_cau_hinh_shop_item.sql"
set "SYNC_HASH_FILE=%ROOT_DIR%runtime\mariadb_data\.shop_sync_md5"

echo ====================================================================
echo   CAP NHAT SHOP / VAT PHAM / MAP TU GIT VAO MARIADB
echo   An toan 100%%: GIU NGUYEN account, player, clan, shop_ky_gui
echo ====================================================================
echo.

if not exist "%CONFIG_SQL%" (
    color 0C
    echo [LOI] Khong tim thay file: %CONFIG_SQL%
    pause
    exit /b 1
)

if not exist "%MYSQL_EXE%" (
    color 0C
    echo [LOI] Khong tim thay mysql/mariadb client tai: %MYSQL_EXE%
    pause
    exit /b 1
)

echo [1/2] Dang nap data_cau_hinh_shop_item.sql vao database nro_newgen...
"%MYSQL_EXE%" -u root -P 3306 -h 127.0.0.1 --default-character-set=utf8mb4 nro_newgen < "%CONFIG_SQL%"
if errorlevel 1 (
    color 0C
    echo [LOI] Nap SQL that bai! Hay dam bao MariaDB dang chay tren cong 3306.
    pause
    exit /b 1
)

for /f "skip=1 tokens=1" %%h in ('certutil -hashfile "%CONFIG_SQL%" MD5') do (
    if not defined FILE_MD5 set "FILE_MD5=%%h"
)
if exist "%ROOT_DIR%runtime\mariadb_data" (
    >"%SYNC_HASH_FILE%" echo !FILE_MD5!
)

color 0A
echo [2/2] DA CAP NHAT XONG SHOP ^& VAT PHAM VAO DATABASE!
echo       - Neu Game Server dang tat: bat Server len la nhan ngay.
echo       - Neu Game Server dang chay: go lenh  reload  trong cua so Server (hoac khoi dong lai Server neu co them Item Template moi).
echo.
if /I not "%~1"=="--nopause" pause
exit /b 0

