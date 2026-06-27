@echo off

if "%MPJ_HOME"=="" (
    echo MPJ_HOME is not set.
    echo Set it to your MPJExpress folder first.
    exit /b 1
)

set CORES=%NUMBER_OF_PROCESSORS%

if "%CORES%"=="" set CORES 2

echo Using %CORES% MPI ranks
set /a WORKERS=%CORES%-1
echo Rank 0 = controller, %WORKERS% ranks = workers

"%MPJ_HOME\bin\mpjrun.bat" -np %CORES% -cp target\classes org.example.MpiText
