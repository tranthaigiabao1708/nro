@ECHO OFF
chcp 65001 >nul
set "ROOT_DIR=%~dp0"
cd /d "%ROOT_DIR%Client_ChoNguoiChoiTaiVe"
if exist "%ROOT_DIR%Client_ChoNguoiChoiTaiVe\NgocRong2Tab.exe" (
    start "" "%ROOT_DIR%Client_ChoNguoiChoiTaiVe\NgocRong2Tab.exe"
) else if exist "%ROOT_DIR%Client_ChoNguoiChoiTaiVe\Ngoc Rong.exe" (
    start "" "%ROOT_DIR%Client_ChoNguoiChoiTaiVe\Ngoc Rong.exe"
) else (
    echo Chưa tìm thấy file Client .exe trong %ROOT_DIR%Client_ChoNguoiChoiTaiVe!
    pause
)
