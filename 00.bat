@echo off
title ���س���װһ�� APK
setlocal enabledelayedexpansion

:: ===== ·������ =====
set "ADB_PATH=C:\platform-tools\adb.exe"
set "APK_PATH=C:\Users\Administrator\Documents\webchecker\app\build\outputs\apk\debug\app-debug.apk"

:: ===== Ԥ��� =====
if not exist "%ADB_PATH%" (
    echo [����] δ�ҵ� adb.exe��%ADB_PATH%
    pause & exit /b 1
)
if not exist "%APK_PATH%" (
    echo [����] δ�ҵ� apk �ļ���%APK_PATH%
    pause & exit /b 1
)

:: ===== ��ѭ�� =====
:MAIN
echo.
echo ========= �豸�б� =========
"%ADB_PATH%" devices
echo =============================
echo.
echo ���س�����װһ�� APK���� Ctrl+C �˳�...
pause >nul

echo.
echo ���ڰ�װ...
"%ADB_PATH%" install -r "%APK_PATH%"
if %errorlevel% neq 0 (
    echo [ʧ��] ��װδ�ɹ��������豸���ӻ� USB ������Ȩ��
) else (
    echo [�ɹ�] ��װ��ɣ�
)
goto MAIN
