@echo off
REM ============================================================
REM  OCP AT Mobile - Lancement complet en 1 double-clic
REM  1) Demarre l'emulateur Android (Medium_Phone_API_36.1)
REM  2) Attend qu'il soit pret
REM  3) Lance l'application Flutter (backend : http://10.0.2.2:8080)
REM ============================================================
setlocal
cd /d "%~dp0"

set SDK=%LOCALAPPDATA%\Android\Sdk
set EMULATEUR=Medium_Phone_API_36.1

echo.
echo [1/3] Demarrage de l'emulateur Android (%EMULATEUR%)...
start "" "%SDK%\emulator\emulator.exe" -avd %EMULATEUR%

echo [2/3] Attente du demarrage complet de l'emulateur (30-60 s)...
"%SDK%\platform-tools\adb.exe" wait-for-device
timeout /t 10 /nobreak >nul

echo [3/3] Lancement de l'application Flutter (choisir Windows ou l'emulateur)...
echo       Astuce : taper le NUMERO de l'emulateur dans la liste, puis r = recharger, q = quitter
flutter run --dart-define=ENV=dev

pause
