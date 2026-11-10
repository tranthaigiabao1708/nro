@ECHO OFF
chcp 65001 >nul
title ĐÓNG GÓI BỘ CÀI GAME NGỌC RỒNG NEWGEN (.ZIP) CHO NGƯỜI CHƠI
color 0E
set "ROOT_DIR=%~dp0"

echo ====================================================================
echo   ĐANG NÉN %ROOT_DIR%NgocRongNewGen_Client -^> %ROOT_DIR%NgocRongNewGen_Client_Full.zip
echo   (Nhớ kiểm tra IP máy chủ trong NgocRongNewGen_Client\server_ip.txt trước khi gửi!)
echo ====================================================================
powershell -NoProfile -Command "$zip = Join-Path '%ROOT_DIR%' 'NgocRongNewGen_Client_Full.zip'; $src = Join-Path '%ROOT_DIR%' 'NgocRongNewGen_Client\*'; if (Test-Path $zip) { Remove-Item -Force $zip }; Compress-Archive -Path $src -DestinationPath $zip -Force"

if %ERRORLEVEL% EQU 0 (
    color 0A
    echo [THÀNH CÔNG] Đã tạo bộ cài hoàn chỉnh tại: %ROOT_DIR%NgocRongNewGen_Client_Full.zip
    echo Bạn có thể gửi file .zip này qua Google Drive / MediaFire / Discord cho người chơi!
) else (
    color 0C
    echo [LỖI] Không thể nén thư mục Client (hãy tắt game trước khi nén).
)
PAUSE
