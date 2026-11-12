@echo off
chcp 65001 >nul
setlocal EnableDelayedExpansion
title NGOC RONG NEWGEN - XUAT DU LIEU SHOP / ITEM LEN GIT (AN TOAN NHAN VAT)
color 0B

set "SCRIPT_DIR=%~dp0"
for %%I in ("%SCRIPT_DIR%..") do set "ROOT_DIR=%%~fI\"
set "MYSQLDUMP_EXE=%ROOT_DIR%runtime\mariadb\bin\mysqldump.exe"
if not exist "%MYSQLDUMP_EXE%" set "MYSQLDUMP_EXE=%ROOT_DIR%runtime\mariadb\bin\mariadb-dump.exe"

set "CONFIG_SQL=%SCRIPT_DIR%data_cau_hinh_shop_item.sql"
set "FULL_CLEAN_SQL=%SCRIPT_DIR%nro_newgen.sql"
set "SYNC_HASH_FILE=%ROOT_DIR%runtime\mariadb_data\.shop_sync_md5"

set "STATIC_TABLES=achievement_template array_head_2_frames bg_item_template clan_task_template data_badges flag_bag head_avatar img_by_name intrinsic item_option_template item_shop item_shop_option item_template map_template mob_template notify npc_template part radar settings shop side_task_template skill_template tab_shop task_badges_template task_main_template task_sub_template"
set "RUNTIME_TABLES=account adminpanel bank_transfers clan comments giftcode history_transaction napthe payments phongchat player posts shop_ky_gui super_rank"

echo ====================================================================
echo   XUAT DU LIEU CAU HINH GAME (SHOP / ITEM / MAP / NPC) CHO GITHUB
echo   Dam bao KHONG xuat du lieu nhan vat ca nhan (player / account)
echo ====================================================================
echo.

if not exist "%MYSQLDUMP_EXE%" (
    color 0C
    echo [LOI] Khong tim thay mysqldump tai: %MYSQLDUMP_EXE%
    pause
    exit /b 1
)

echo [1/3] Xuat 27 bang cau hinh Shop / Item / Map ra data_cau_hinh_shop_item.sql...
"%MYSQLDUMP_EXE%" -u root -P 3306 -h 127.0.0.1 --default-character-set=utf8mb4 --single-transaction --skip-dump-date nro_newgen %STATIC_TABLES% --result-file="%CONFIG_SQL%"
if errorlevel 1 (
    color 0C
    echo [LOI] Xuat data_cau_hinh_shop_item.sql that bai! Hay bat MariaDB cong 3306 truoc.
    pause
    exit /b 1
)

echo [2/3] Xuat ban khoi tao sach nro_newgen.sql cho may moi (khong chua nhan vat cu)...
(
    echo CREATE DATABASE IF NOT EXISTS `nro_newgen` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
    echo USE `nro_newgen`;
) > "%FULL_CLEAN_SQL%"

"%MYSQLDUMP_EXE%" -u root -P 3306 -h 127.0.0.1 --default-character-set=utf8mb4 --single-transaction --no-data --skip-dump-date nro_newgen %RUNTIME_TABLES% >> "%FULL_CLEAN_SQL%"
if errorlevel 1 (
    color 0C
    echo [LOI] Xuat khung bang runtime that bai!
    pause
    exit /b 1
)

(
    echo.
    echo -- Tai khoan Admin mac dinh cho may moi
    echo INSERT IGNORE INTO `account` ^(`id`, `username`, `password`, `is_admin`, `active`^) VALUES ^(1, 'admin', '123456', 1, 1^);
    echo INSERT IGNORE INTO `giftcode` ^(`id`, `code`, `count_left`, `detail`, `datecreate`, `expired`^) VALUES ^(1, 'tanthu', -1, '[{"id":457,"quantity":20,"options":[{"id":30,"param":0}]},{"id":1635,"quantity":5,"options":[{"id":30,"param":0}]},{"id":1787,"quantity":2,"options":[{"id":30,"param":0}]}]', '2026-01-01 00:00:00', '2035-12-31 23:59:59'^);
    echo.
) >> "%FULL_CLEAN_SQL%"

type "%CONFIG_SQL%" >> "%FULL_CLEAN_SQL%"

REM Luu MD5 de may hien tai khong can nap lai chinh file vua xuat
for /f "skip=1 tokens=1" %%h in ('certutil -hashfile "%CONFIG_SQL%" MD5') do (
    if not defined FILE_MD5 set "FILE_MD5=%%h"
)
if exist "%ROOT_DIR%runtime\mariadb_data" (
    >"%SYNC_HASH_FILE%" echo !FILE_MD5!
)

color 0A
echo [3/3] HOAN TAT! Da xuat thanh cong 2 file chuan UTF-8:
echo       - %CONFIG_SQL%  (tu dong cap nhat Shop/Item cho may khac ma GIU NGUYEN nhan vat)
echo       - %FULL_CLEAN_SQL%  (khoi tao DB sach cho may moi tinh)
echo.
echo   Buoc tiep theo: git add . ^&^& git commit -m "cap nhat shop va item" ^&^& git push
echo.
if /I not "%~1"=="--nopause" pause
exit /b 0
