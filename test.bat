@echo off
title UniVibe - Automated Test Suite
echo ======================================================================
echo   RUNNING UNIVIBE TEST SUITE
echo ======================================================================

java -cp "bin;lib/*" com.univibe.test.UniVibeSystemTest

pause
