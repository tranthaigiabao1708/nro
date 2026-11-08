@ECHO OFF
chcp 65001 >nul
title BIÊN DỊCH LẠI MÃ NGUỒN SERVER JAVA (TEAMOBI 2026)
color 0B

echo ====================================================================
echo   ĐANG BIÊN DỊCH MÃ NGUỒN JAVA TRONG C:\mro\Teamobi2026\SRC ...
echo ====================================================================
set "JAVA_HOME=c:\mro\runtime\jdk21"
set "PATH=c:\mro\runtime\jdk21\bin;c:\mro\runtime\ant\bin;%PATH%"
cd /d "c:\mro\Teamobi2026\SRC"
call "c:\mro\runtime\ant\bin\ant.bat" jar

echo.
if %ERRORLEVEL% EQU 0 (
    color 0A
    echo [THÀNH CÔNG] Đã cập nhật file dist\NgocRongOnline.jar!
    echo Hãy khởi động lại Server để áp dụng thay đổi code Java mới.
) else (
    color 0C
    echo [LỖI] Biên dịch thất bại! Vui lòng kiểm tra thông báo lỗi phía trên.
)
PAUSE

