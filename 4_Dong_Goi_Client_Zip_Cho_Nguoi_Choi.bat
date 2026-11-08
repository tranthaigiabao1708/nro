@ECHO OFF
chcp 65001 >nul
title ĐÓNG GÓI BỘ CÀI GAME (.ZIP) CHO NGƯỜI CHƠI TẢI VỀ
color 0E

echo ====================================================================
echo   ĐANG NÉN C:\mro\Client_ChoNguoiChoiTaiVe -^> C:\mro\NgocRong2026_Client_Full.zip
echo   (Nhớ kiểm tra IP máy chủ trong Client_ChoNguoiChoiTaiVe\server_ip.txt trước khi gửi!)
echo ====================================================================
powershell -NoProfile -Command "if (Test-Path 'c:\mro\NgocRong2026_Client_Full.zip') { Remove-Item -Force 'c:\mro\NgocRong2026_Client_Full.zip' }; Compress-Archive -Path 'c:\mro\Client_ChoNguoiChoiTaiVe\*' -DestinationPath 'c:\mro\NgocRong2026_Client_Full.zip' -Force"

if %ERRORLEVEL% EQU 0 (
    color 0A
    echo [THÀNH CÔNG] Đã tạo bộ cài hoàn chỉnh tại: C:\mro\NgocRong2026_Client_Full.zip
    echo Bạn có thể gửi file .zip này qua Google Drive / MediaFire / Discord cho người chơi!
) else (
    color 0C
    echo [LỖI] Không thể nén thư mục Client (hãy tắt game trước khi nén).
)
PAUSE

