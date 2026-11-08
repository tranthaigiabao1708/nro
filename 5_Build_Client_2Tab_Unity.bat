@ECHO OFF
chcp 65001 >nul
title BUILD CLIENT 2 TAB HÀNH TRANG LƯỚI TỪ UNITY (PRJ_2Tab_550K)
color 0B

echo ====================================================================
echo   ĐANG XUẤT BẢN CLIENT 2 TAB (PRJ_2Tab_550K -^> Client_ChoNguoiChoiTaiVe)
echo   Lưu ý: Hãy đảm bảo bạn đã Đăng nhập tài khoản Unity Hub và kích hoạt
echo          Personal License miễn phí trước khi chạy!
echo ====================================================================
"C:\Program Files\Unity\Hub\Editor\6000.4.3f1\Editor\Unity.exe" -quit -projectPath "C:\mro\PRJ_2Tab_550K" -buildTarget Win64 -executeMethod AutoBuilder.BuildWindows -logFile "C:\mro\unity_build.log"

if exist "C:\mro\Client_ChoNguoiChoiTaiVe\NgocRong2Tab.exe" (
    color 0A
    echo.
    echo [THÀNH CÔNG] Đã tạo xong C:\mro\Client_ChoNguoiChoiTaiVe\NgocRong2Tab.exe!
    echo Từ giờ bạn và người chơi chỉ cần sửa IP trong file server_ip.txt!
) else (
    color 0E
    echo.
    echo [CHƯA TẠO ĐƯỢC EXE] Vui lòng mở Unity Hub -^> Preferences -^> Licenses
    echo để kích hoạt Personal License miễn phí, hoặc mở trực tiếp project
    echo C:\mro\PRJ_2Tab_550K trong Unity rồi bấm menu trên cùng:
    echo   NRO Admin -^> Build Client PC 2 Tab
)
PAUSE

