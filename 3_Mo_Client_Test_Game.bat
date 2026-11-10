@ECHO OFF
chcp 65001 >nul
set "ROOT_DIR=%~dp0"
cd /d "%ROOT_DIR%NgocRongNewGen_Client"
if exist "%ROOT_DIR%NgocRongNewGen_Client\NgocRongNewGen.exe" (
    start "" "%ROOT_DIR%NgocRongNewGen_Client\NgocRongNewGen.exe"
) else if exist "%ROOT_DIR%NgocRongNewGen_Client\Ngoc Rong.exe" (
    start "" "%ROOT_DIR%NgocRongNewGen_Client\Ngoc Rong.exe"
) else (
    echo Chưa tìm thấy file Client .exe trong %ROOT_DIR%NgocRongNewGen_Client!
    pause
)
