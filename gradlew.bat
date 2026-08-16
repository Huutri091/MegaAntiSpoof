@echo off
setlocal
call "%~dp0blockmodclient\Auto-Clicker\gradlew.bat" -p "%~dp0." %*
exit /b %ERRORLEVEL%
