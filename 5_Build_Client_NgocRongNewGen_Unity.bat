@ECHO OFF
chcp 65001 >nul
title BUILD CLIENT 2 TAB HÀNH TRANG LƯỚI TỪ UNITY (NgocRongNewGen_Unity)
color 0B
set "ROOT_DIR=%~dp0"

echo ====================================================================
echo   ĐANG XUẤT BẢN CLIENT 2 TAB (NgocRongNewGen_Unity -^> NgocRongNewGen_Client)
echo   Lưu ý: Hãy đảm bảo bạn đã Đăng nhập tài khoản Unity Hub và kích hoạt
echo          Personal License miễn phí trước khi chạy!
echo ====================================================================
"C:\Program Files\Unity\Hub\Editor\6000.4.3f1\Editor\Unity.com" -batchmode -nographics -accept-apiupdate -quit -projectPath "%ROOT_DIR%NgocRongNewGen_Unity" -buildTarget Win64 -executeMethod AutoBuilder.BuildWindows -logFile "%ROOT_DIR%unity_build.log"

if exist "%ROOT_DIR%NgocRongNewGen_Client\NgocRongNewGen.exe" (
    color 0A
    echo.
    echo [THÀNH CÔNG] Đã tạo xong %ROOT_DIR%NgocRongNewGen_Client\NgocRongNewGen.exe!
    echo Từ giờ bạn và người chơi chỉ cần sửa IP trong file server_ip.txt!
) else (
    color 0E
    echo.
    echo [CHƯA TẠO ĐƯỢC EXE] Vui lòng mở Unity Hub -^> Preferences -^> Licenses
    echo để kích hoạt Personal License miễn phí, hoặc mở trực tiếp project
    echo %ROOT_DIR%NgocRongNewGen_Unity trong Unity rồi bấm menu trên cùng:
    echo   NRO Admin -^> Build Client PC 2 Tab
)
PAUSE

