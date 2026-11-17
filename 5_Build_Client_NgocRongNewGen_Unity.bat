@ECHO OFF
chcp 65001 >nul
title BUILD CLIENT 2 TAB NGỌC RỒNG NEWGEN (UNITY / ROSLYN FAST BUILD)
color 0B
set "ROOT_DIR=%~dp0"
set "UNITY_COM=C:\Program Files\Unity\Hub\Editor\6000.4.3f1\Editor\Unity.com"

if exist "%UNITY_COM%" (
    echo ====================================================================
    echo   ĐANG XUẤT BẢN CLIENT 2 TAB QUA UNITY EDITOR 6000.4.3f1...
    echo ====================================================================
    "%UNITY_COM%" -batchmode -nographics -accept-apiupdate -quit -projectPath "%ROOT_DIR%NgocRongNewGen_Unity" -buildTarget Win64 -executeMethod AutoBuilder.BuildWindows -logFile "%ROOT_DIR%unity_build.log"
) else (
    echo ====================================================================
    echo   BIÊN DỊCH NHANH MÃ NGUỒN C# VÀO CLIENT 2 TAB QUA DOTNET ROSLYN...
    echo ====================================================================
    powershell -NoProfile -ExecutionPolicy Bypass -Command ^
      "$managed = '%ROOT_DIR%NgocRongNewGen_Client\NgocRongNewGen_Data\Managed';" ^
      "$csc = Get-ChildItem -Path 'C:\Program Files\dotnet\sdk' -Recurse -Filter 'csc.dll' -ErrorAction SilentlyContinue | Sort-Object FullName -Descending | Select-Object -First 1;" ^
      "if (-not $csc) { Write-Error 'Không tìm thấy dotnet SDK csc.dll hay Unity Editor!'; exit 1 };" ^
      "$rsp = Join-Path $env:TEMP 'nro_csc_build.rsp';" ^
      "$outDll = Join-Path $managed 'Assembly-CSharp.dll';" ^
      "$lines = @('-target:library','-nostdlib','-optimize+','-unsafe+','-nowarn:0168,0219,0414,0618,0649,0108,0114,0169,1998', ('-out:\"' + $outDll + '\"'));" ^
      "Get-ChildItem -Path $managed -Filter '*.dll' | Where-Object { $_.Name -ne 'Assembly-CSharp.dll' -and $_.Name -notlike 'System.EnterpriseServices*' } | ForEach-Object { $lines += ('-r:\"' + $_.FullName + '\"') };" ^
      "Get-ChildItem -Path '%ROOT_DIR%NgocRongNewGen_Unity\Assets\Scripts\Assembly-CSharp' -Recurse -Filter '*.cs' | ForEach-Object { $lines += ('\"' + $_.FullName + '\"') };" ^
      "[System.IO.File]::WriteAllLines($rsp, $lines);" ^
      "& 'C:\Program Files\dotnet\dotnet.exe' $csc.FullName ('@' + $rsp);" ^
      "exit $LASTEXITCODE"
)

if %ERRORLEVEL% EQU 0 (
    color 0A
    echo.
    echo [THÀNH CÔNG] Đã cập nhật Client 2 Tab tại %ROOT_DIR%NgocRongNewGen_Client\NgocRongNewGen.exe!
) else (
    color 0C
    echo.
    echo [LỖI] Biên dịch Client thất bại! Hãy kiểm tra thông báo phía trên.
)
PAUSE
