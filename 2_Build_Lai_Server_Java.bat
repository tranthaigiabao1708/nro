@ECHO OFF
chcp 65001 >nul
title BIÊN DỊCH LẠI MÃ NGUỒN SERVER JAVA (TEAMOBI 2026)
color 0B
set "ROOT_DIR=%~dp0"
set "JDK_BIN=C:\Users\giaba\.antigravity-ide\extensions\redhat.java-1.58.0-win32-x64\jre\25.0.4.1-win32-x86_64\bin"
if exist "%ROOT_DIR%runtime\jdk21\bin\javac.exe" set "JDK_BIN=%ROOT_DIR%runtime\jdk21\bin"

echo ====================================================================
echo   ĐANG BIÊN DỊCH MÃ NGUỒN JAVA TRONG %ROOT_DIR%Teamobi2026\SRC ...
echo ====================================================================
cd /d "%ROOT_DIR%Teamobi2026\SRC"
if exist "%ROOT_DIR%runtime\ant\bin\ant.bat" (
    set "JAVA_HOME=%ROOT_DIR%runtime\jdk21"
    set "PATH=%JDK_BIN%;%ROOT_DIR%runtime\ant\bin;%PATH%"
    call "%ROOT_DIR%runtime\ant\bin\ant.bat" jar
) else (
    powershell -NoProfile -Command "$jdk='%JDK_BIN%'; if (Test-Path build\classes) { Remove-Item -Recurse -Force build\classes }; New-Item -ItemType Directory -Force build\classes | Out-Null; $src = Get-ChildItem -Path src -Recurse -Filter *.java | ForEach-Object { $_.FullName }; [System.IO.File]::WriteAllLines('build\sources.txt', $src); & \"$jdk\javac.exe\" -encoding UTF-8 --release 21 -proc:none -cp 'lib/*;dist/NgocRongOnline.jar' -d build\classes '@build\sources.txt'; if ($LASTEXITCODE -eq 0) { & \"$jdk\jar.exe\" uf dist\NgocRongOnline.jar -C build\classes .; Remove-Item -Recurse -Force build }"
)

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

