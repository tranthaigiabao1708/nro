@ECHO OFF
chcp 65001 >nul
title NGOC RONG TEAMOBI 2026 - GAME SERVER (PORT 14445)
cd /d "%~dp0"
"c:\mro\runtime\jdk21\bin\java.exe" -server -Xms512M -Xmx2048M -Dfile.encoding=UTF-8 -cp "lib/*;dist/NgocRongOnline.jar" nro.models.server.ServerManager
PAUSE
