@ECHO OFF
chcp 65001 >nul
cd /d "c:\mro\Client_ChoNguoiChoiTaiVe"
if exist "c:\mro\Client_ChoNguoiChoiTaiVe\NgocRong2Tab.exe" (
    start "" "c:\mro\Client_ChoNguoiChoiTaiVe\NgocRong2Tab.exe"
) else if exist "c:\mro\Client_ChoNguoiChoiTaiVe\Ngoc Rong.exe" (
    start "" "c:\mro\Client_ChoNguoiChoiTaiVe\Ngoc Rong.exe"
) else (
    echo Chưa tìm thấy file Client .exe trong C:\mro\Client_ChoNguoiChoiTaiVe!
    pause
)
