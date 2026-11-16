@echo off
chcp 65001 >nul
title Cap Nhat Bảng Part & Xoa Cache NR_part Unity Client

echo ============================================================================
echo   CONG CU NAP CUSTOM_PART.SQL VA XOA CACHE NR_PART CLIENT NGOC RONG NEWGEN
echo ============================================================================
echo.

set "MYSQL_EXE=d:\nro\runtime\mariadb\bin\mysql.exe"
set "SQL_FILE=d:\nro\tools\custom_part.sql"

if exist "%SQL_FILE%" (
    echo [1/2] Phat hien file %SQL_FILE% - Dang nap vao MariaDB nro_newgen...
    "%MYSQL_EXE%" -u root -P 3306 -h 127.0.0.1 nro_newgen < "%SQL_FILE%"
    if errorlevel 1 (
        echo [LOI] Khong ket noi duoc MariaDB tren cong 3306! Hay dam bao MariaDB dang chay.
    ) else (
        echo [OK] Da cap nhat bang `part` trong MariaDB thanh cong!
    )
) else (
    echo [1/2] Khong co file %SQL_FILE% (Bo qua buoc nap SQL tu dong).
)

echo.
echo [2/2] Dang xoa cache NR_part va NRdataVersion cua Unity Client (GIU NGUYEN tai khoan/mat khau)...
powershell -NoProfile -ExecutionPolicy Bypass -Command ^
  "$paths = @('HKCU:\Software\NgocRongNewGen\Ngọc Rồng NewGen', 'HKCU:\Software\Unity\UnityEditor\NgocRongNewGen\Ngọc Rồng NewGen');" ^
  "foreach ($p in $paths) {" ^
  "  if (Test-Path $p) {" ^
  "    $names = (Get-Item $p).GetValueNames() | Where-Object { $_ -match '^(NR_part|NRdataVersion)' };" ^
  "    foreach ($n in $names) { Remove-ItemProperty -Path $p -Name $n -ErrorAction SilentlyContinue; Write-Host ('  -> Da xoa cache: ' + $n) }" ^
  "  }" ^
  "}"

echo.
echo ============================================================================
echo   HOAN TAT!
echo   LUU Y QUAN TRONG DE THAY NGAY TRONG GAME:
echo   1. Khoi dong lai Game Server (de Manager.java xuat lai file data/update_data/part).
echo   2. Mo lai Game Client -> Client se tu dong tai bang `part` moi nhat tu Server!
echo ============================================================================
pause
