@echo off
REM NextERP 测试运行脚本 (Windows)

setlocal enabledelayedexpansion

REM 颜色设置 (Windows 10+)
set "GREEN=[92m"
set "RED=[91m"
set "YELLOW=[93m"
set "NC=[0m"

REM 项目根目录
cd /d "%~dp0.."

echo %GREEN%========================================%NC%
echo %GREEN%  NextERP 测试运行脚本%NC%
echo %GREEN%========================================%NC%

REM 解析命令行参数
set TEST_TYPE=all
set MODULE=

:parse_args
if "%~1"=="--unit" (
    set TEST_TYPE=unit
    shift
    goto parse_args
)
if "%~1"=="--integration" (
    set TEST_TYPE=integration
    shift
    goto parse_args
)
if "%~1"=="--all" (
    set TEST_TYPE=all
    shift
    goto parse_args
)
if "%~1"=="--module" (
    set MODULE=%~2
    shift
    shift
    goto parse_args
)
if "%~1"=="--help" (
    echo 用法: scripts\test.bat [选项]
    echo.
    echo 选项:
    echo   --unit           运行单元测试
    echo   --integration    运行集成测试
    echo   --all            运行所有测试 (默认)
    echo   --module ^<name^>  运行指定模块的测试
    echo   --help           显示帮助信息
    echo.
    echo 示例:
    echo   scripts\test.bat --unit
    echo   scripts\test.bat --module nexterp-platform-auth
    echo   scripts\test.bat --all
    exit /b 0
)
if not "%~1"=="" (
    echo %RED%未知选项: %~1%NC%
    echo 使用 --help 查看帮助信息
    exit /b 1
)

REM 检查Docker是否运行
echo %YELLOW%检查Docker状态...%NC%
docker info >nul 2>&1
if errorlevel 1 (
    echo %RED%Docker未运行，请先启动Docker%NC%
    exit /b 1
)

REM 启动测试环境
echo %YELLOW%启动测试环境...%NC%
docker-compose -f docker-compose.test.yml up -d

REM 等待数据库就绪
echo %YELLOW%等待数据库就绪...%NC%
set max_attempts=30
set attempt=0

:wait_db
if !attempt! geq %max_attempts% (
    echo %RED%数据库启动超时%NC%
    exit /b 1
)
docker exec nexterp-postgres-test pg_isready -U nexterp_test >nul 2>&1
if errorlevel 1 (
    set /a attempt+=1
    timeout /t 1 >nul
    goto wait_db
)
echo %GREEN%数据库已就绪%NC%

REM 构建Maven测试命令
set MAVEN_CMD=mvn test

if not "%MODULE%"=="" (
    set MAVEN_CMD=mvn test -pl %MODULE%
)

if "%TEST_TYPE%"=="unit" (
    set MAVEN_CMD=%MAVEN_CMD% -Dtest="**/*Test,!**/*ControllerTest,!**/*IntegrationTest"
    echo %GREEN%运行单元测试...%NC%
) else if "%TEST_TYPE%"=="integration" (
    set MAVEN_CMD=%MAVEN_CMD% -Dtest="**/*ControllerTest,**/*IntegrationTest"
    echo %GREEN%运行集成测试...%NC%
) else (
    echo %GREEN%运行所有测试...%NC%
)

REM 运行测试
echo %YELLOW%执行命令: %MAVEN_CMD%%NC%
call %MAVEN_CMD%

REM 检查测试结果
if errorlevel 1 (
    echo %RED%========================================%NC%
    echo %RED%  测试失败！%NC%
    echo %RED%========================================%NC%
    docker-compose -f docker-compose.test.yml down
    exit /b 1
)

echo %GREEN%========================================%NC%
echo %GREEN%  测试通过！%NC%
echo %GREEN%========================================%NC%

REM 生成测试覆盖率报告
echo %YELLOW%生成测试覆盖率报告...%NC%
call mvn jacoco:report

echo %GREEN%测试报告位置:%NC%
echo   - HTML: backend\target\site\jacoco\index.html
echo   - XML:  backend\target\site\jacoco\jacoco.xml

REM 停止测试环境
echo %YELLOW%停止测试环境...%NC%
docker-compose -f docker-compose.test.yml down

echo %GREEN%完成！%NC%
