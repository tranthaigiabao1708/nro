@ECHO OFF
chcp 65001 >nul
title BIÊN DỊCH LẠI MÃ NGUỒN SERVER JAVA (NGỌC RỒNG NEWGEN)
color 0B
set "ROOT_DIR=%~dp0"
set "JDK_BIN=C:\Users\giaba\.antigravity-ide\extensions\redhat.java-1.58.0-win32-x64\jre\25.0.4.1-win32-x86_64\bin"
if exist "%ROOT_DIR%runtime\jdk21\bin\javac.exe" set "JDK_BIN=%ROOT_DIR%runtime\jdk21\bin"

echo ====================================================================
echo   ĐANG BIÊN DỊCH MÃ NGUỒN JAVA TRONG %ROOT_DIR%NgocRongNewGen_Server ...
echo ====================================================================
cd /d "%ROOT_DIR%NgocRongNewGen_Server"
if exist "%ROOT_DIR%runtime\ant\bin\ant.bat" (
    set "JAVA_HOME=%ROOT_DIR%runtime\jdk21"
    set "PATH=%JDK_BIN%;%ROOT_DIR%runtime\ant\bin;%PATH%"
    call "%ROOT_DIR%runtime\ant\bin\ant.bat" jar
) else (
    REM Biên dịch toàn bộ src (Lombok 1.18.42 trong lib/ hỗ trợ JDK 21 - 25) rồi cập nhật dist\NgocRongNewGen.jar
    powershell -NoProfile -Command "$jdk='%JDK_BIN%'; if (Test-Path build) { Remove-Item -Recurse -Force build }; New-Item -ItemType Directory -Force build\classes | Out-Null; $src = Get-ChildItem -Path src -Recurse -Filter *.java | ForEach-Object { $_.FullName }; [System.IO.File]::WriteAllLines((Join-Path (Get-Location) 'build\sources.txt'), $src); & (Join-Path $jdk 'javac.exe') -encoding UTF-8 --release 21 -nowarn -processorpath 'lib/lombok.jar' -cp 'lib/*' -d build\classes '@build\sources.txt'; $code = $LASTEXITCODE; if ($code -eq 0) { & (Join-Path $jdk 'jar.exe') uf dist\NgocRongNewGen.jar -C build\classes .; $code = $LASTEXITCODE; Remove-Item -Recurse -Force build }; exit $code"
)

echo.
if %ERRORLEVEL% EQU 0 (
    color 0A
    echo [THÀNH CÔNG] Đã cập nhật file dist\NgocRongNewGen.jar!
    echo Hãy khởi động lại Server để áp dụng thay đổi code Java mới.
) else (
    color 0C
    echo [LỖI] Biên dịch thất bại! Vui lòng kiểm tra thông báo lỗi phía trên.
)
PAUSE
