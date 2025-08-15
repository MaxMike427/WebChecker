@echo off
title 按回车安装一次 APK
setlocal enabledelayedexpansion

:: ===== 路径设置 =====
set "ADB_PATH=C:\platform-tools\adb.exe"
set "APK_PATH=C:\Users\Administrator\Documents\webchecker\app\build\outputs\apk\debug\app-debug.apk"

:: ===== 预检查 =====
if not exist "%ADB_PATH%" (
    echo [错误] 未找到 adb.exe：%ADB_PATH%
    pause & exit /b 1
)
if not exist "%APK_PATH%" (
    echo [错误] 未找到 apk 文件：%APK_PATH%
    pause & exit /b 1
)

:: ===== 主循环 =====
:MAIN
echo.
echo ========= 设备列表 =========
"%ADB_PATH%" devices
echo =============================
echo.
echo 按回车键安装一次 APK，按 Ctrl+C 退出...
pause >nul

echo.
echo 正在安装...
"%ADB_PATH%" install -r "%APK_PATH%"
if %errorlevel% neq 0 (
    echo [失败] 安装未成功，请检查设备连接或 USB 调试授权。
) else (
    echo [成功] 安装完成！
)
goto MAIN
