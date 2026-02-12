#!/bin/bash
# NextERP 测试运行脚本

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 项目根目录
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$PROJECT_ROOT"

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}  NextERP 测试运行脚本${NC}"
echo -e "${GREEN}========================================${NC}"

# 解析命令行参数
TEST_TYPE=""
MODULE=""

while [[ $# -gt 0 ]]; do
    case $1 in
        --unit)
            TEST_TYPE="unit"
            shift
            ;;
        --integration)
            TEST_TYPE="integration"
            shift
            ;;
        --all)
            TEST_TYPE="all"
            shift
            ;;
        --module)
            MODULE="$2"
            shift 2
            ;;
        --help)
            echo "用法: ./scripts/test.sh [选项]"
            echo ""
            echo "选项:"
            echo "  --unit           运行单元测试"
            echo "  --integration    运行集成测试"
            echo "  --all            运行所有测试 (默认)"
            echo "  --module <name>  运行指定模块的测试"
            echo "  --help           显示帮助信息"
            echo ""
            echo "示例:"
            echo "  ./scripts/test.sh --unit"
            echo "  ./scripts/test.sh --module nexterp-platform-auth"
            echo "  ./scripts/test.sh --all"
            exit 0
            ;;
        *)
            echo -e "${RED}未知选项: $1${NC}"
            echo "使用 --help 查看帮助信息"
            exit 1
            ;;
    esac
done

# 默认运行所有测试
if [ -z "$TEST_TYPE" ]; then
    TEST_TYPE="all"
fi

# 检查Docker是否运行
echo -e "${YELLOW}检查Docker状态...${NC}"
if ! docker info > /dev/null 2>&1; then
    echo -e "${RED}Docker未运行，请先启动Docker${NC}"
    exit 1
fi

# 启动测试环境
echo -e "${YELLOW}启动测试环境...${NC}"
docker-compose -f docker-compose.test.yml up -d

# 等待数据库就绪
echo -e "${YELLOW}等待数据库就绪...${NC}"
max_attempts=30
attempt=0
while [ $attempt -lt $max_attempts ]; do
    if docker exec nexterp-postgres-test pg_isready -U nexterp_test > /dev/null 2>&1; then
        echo -e "${GREEN}数据库已就绪${NC}"
        break
    fi
    attempt=$((attempt + 1))
    sleep 1
done

if [ $attempt -eq $max_attempts ]; then
    echo -e "${RED}数据库启动超时${NC}"
    exit 1
fi

# 构建Maven测试命令
MAVEN_CMD="mvn test"

if [ -n "$MODULE" ]; then
    MAVEN_CMD="mvn test -pl $MODULE"
fi

case $TEST_TYPE in
    unit)
        MAVEN_CMD="$MAVEN_CMD -Dtest='**/*Test,!**/*ControllerTest,!**/*IntegrationTest'"
        echo -e "${GREEN}运行单元测试...${NC}"
        ;;
    integration)
        MAVEN_CMD="$MAVEN_CMD -Dtest='**/*ControllerTest,**/*IntegrationTest'"
        echo -e "${GREEN}运行集成测试...${NC}"
        ;;
    all)
        echo -e "${GREEN}运行所有测试...${NC}"
        ;;
esac

# 运行测试
echo -e "${YELLOW}执行命令: $MAVEN_CMD${NC}"
$MAVEN_CMD

# 检查测试结果
if [ $? -eq 0 ]; then
    echo -e "${GREEN}========================================${NC}"
    echo -e "${GREEN}  测试通过！${NC}"
    echo -e "${GREEN}========================================${NC}"
else
    echo -e "${RED}========================================${NC}"
    echo -e "${RED}  测试失败！${NC}"
    echo -e "${RED}========================================${NC}"
    exit 1
fi

# 生成测试覆盖率报告
echo -e "${YELLOW}生成测试覆盖率报告...${NC}"
mvn jacoco:report

echo -e "${GREEN}测试报告位置:${NC}"
echo -e "  - HTML: backend/target/site/jacoco/index.html"
echo -e "  - XML:  backend/target/site/jacoco/jacoco.xml"

# 停止测试环境
echo -e "${YELLOW}停止测试环境...${NC}"
docker-compose -f docker-compose.test.yml down

echo -e "${GREEN}完成！${NC}"
