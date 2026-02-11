package com.nexterp.platform.workflow.expression;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexterp.shared.security.context.UserContext;
import com.nexterp.shared.core.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 表达式解析器
 * 支持任务分配表达式、条件表达式等
 *
 * 表达式语法:
 * - ${initiator.id} - 发起人ID
 * - ${initiator.deptId} - 发起人部门ID
 * - ${initiator.roleId} - 发起人角色ID
 * - ${variable.varName} - 流程变量
 * - #{user.userId} - 指定用户
 * - #{role.roleId} - 指定角色
 * - #{dept.deptId} - 指定部门
 * - SpEL表达式 - #{T(java.lang.Math).random()}
 *
 * @author NextERP
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExpressionParser {

    private final ObjectMapper objectMapper;
    private final ExpressionParser spelParser = new SpelExpressionParser();

    /**
     * 解析表达式并返回结果
     *
     * @param expression 表达式
     * @param context    上下文
     * @return 解析结果
     */
    public Object parse(String expression, ExpressionContext context) {
        if (expression == null || expression.isBlank()) {
            return null;
        }

        // 处理变量替换 ${...}
        if (expression.contains("${")) {
            return parseVariableExpression(expression, context);
        }

        // 处理SpEL表达式 #{...}
        if (expression.contains("#{")) {
            return parseSpelExpression(expression, context);
        }

        // 直接返回表达式值
        return expression;
    }

    /**
     * 解析为字符串
     *
     * @param expression 表达式
     * @param context    上下文
     * @return 字符串结果
     */
    public String parseString(String expression, ExpressionContext context) {
        Object result = parse(expression, context);
        return result != null ? result.toString() : null;
    }

    /**
     * 解析为整数
     *
     * @param expression 表达式
     * @param context    上下文
     * @return 整数结果
     */
    public Integer parseInt(String expression, ExpressionContext context) {
        Object result = parse(expression, context);
        if (result == null) {
            return null;
        }
        if (result instanceof Number) {
            return ((Number) result).intValue();
        }
        try {
            return Integer.parseInt(result.toString());
        } catch (NumberFormatException e) {
            log.warn("无法解析为整数: {}", result);
            return null;
        }
    }

    /**
     * 解析为布尔值
     *
     * @param expression 表达式
     * @param context    上下文
     * @return 布尔结果
     */
    public Boolean parseBoolean(String expression, ExpressionContext context) {
        Object result = parse(expression, context);
        if (result == null) {
            return false;
        }
        if (result instanceof Boolean) {
            return (Boolean) result;
        }
        return Boolean.parseBoolean(result.toString());
    }

    /**
     * 解析为列表
     *
     * @param expression 表达式
     * @param context    上下文
     * @return 列表结果
     */
    @SuppressWarnings("unchecked")
    public List<Object> parseList(String expression, ExpressionContext context) {
        Object result = parse(expression, context);
        if (result == null) {
            return Collections.emptyList();
        }
        if (result instanceof List) {
            return (List<Object>) result;
        }
        if (result instanceof String) {
            String str = (String) result;
            if (str.startsWith("[") && str.endsWith("]")) {
                try {
                    return objectMapper.readValue(str, List.class);
                } catch (Exception e) {
                    log.warn("无法解析为列表: {}", str, e);
                }
            }
            // 逗号分隔的字符串
            return Arrays.asList(str.split(","));
        }
        return Collections.singletonList(result);
    }

    /**
     * 解析任务分配表达式，返回候选人列表
     *
     * @param expression 分配表达式
     * @param context    上下文
     * @return 候选人ID列表
     */
    public List<Long> parseAssignmentExpression(String expression, ExpressionContext context) {
        List<Object> results = parseList(expression, context);
        List<Long> assigneeIds = new ArrayList<>();

        for (Object result : results) {
            if (result instanceof Number) {
                assigneeIds.add(((Number) result).longValue());
            } else if (result instanceof String) {
                try {
                    assigneeIds.add(Long.parseLong((String) result));
                } catch (NumberFormatException e) {
                    // 忽略非数字字符串
                }
            }
        }

        return assigneeIds;
    }

    /**
     * 解析变量表达式 ${...}
     */
    private Object parseVariableExpression(String expression, ExpressionContext context) {
        Pattern pattern = Pattern.compile("\\$\\{([^}]+)}");
        Matcher matcher = pattern.matcher(expression);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String variable = matcher.group(1);
            Object value = resolveVariable(variable, context);
            matcher.appendReplacement(result, value != null ? value.toString() : "");
        }

        matcher.appendTail(result);

        String resultStr = result.toString();

        // 如果整个表达式就是一个变量，返回原始类型
        if (expression.matches("^\\$\\{[^}]+}$")) {
            return context.getVariable(matcher.group(1));
        }

        return resultStr;
    }

    /**
     * 解析SpEL表达式 #{...}
     */
    private Object parseSpelExpression(String expression, ExpressionContext context) {
        try {
            StandardEvaluationContext evalContext = new StandardEvaluationContext();

            // 设置变量
            if (context.getVariables() != null) {
                context.getVariables().forEach(evalContext::setVariable);
            }

            // 设置发起人信息
            if (context.getInitiator() != null) {
                evalContext.setVariable("initiator", context.getInitiator());
            }

            // 设置当前用户
            try {
                Long currentUserId = UserContext.getUserId();
                evalContext.setVariable("currentUser", currentUserId);
            } catch (Exception e) {
                // 忽略获取当前用户失败
            }

            Expression exp = spelParser.parseExpression(expression.substring(2, expression.length() - 1));
            return exp.getValue(evalContext);

        } catch (Exception e) {
            log.error("SpEL表达式解析失败: {}", expression, e);
            throw new BusinessException("表达式解析失败: " + e.getMessage());
        }
    }

    /**
     * 解析变量
     */
    private Object resolveVariable(String variable, ExpressionContext context) {
        String[] parts = variable.split("\\.");

        if (parts.length == 0) {
            return null;
        }

        // 处理发起人相关变量
        if ("initiator".equals(parts[0])) {
            if (context.getInitiator() == null) {
                return null;
            }

            return getInitiatorProperty(context.getInitiator(), parts);
        }

        // 处理流程变量
        if (parts.length == 1 && context.getVariables() != null) {
            return context.getVariables().get(variable);
        }

        // 处理嵌套变量 (如: user.name)
        if (parts.length > 1 && context.getVariables() != null) {
            Object value = context.getVariables().get(parts[0]);
            if (value instanceof Map) {
                return ((Map<?, ?>) value).get(parts[1]);
            }
        }

        return null;
    }

    /**
     * 获取发起人属性
     */
    private Object getInitiatorProperty(ExpressionContext.InitiatorInfo initiator, String[] parts) {
        if (parts.length == 1) {
            return initiator.getUserId();
        }

        String property = parts[1];
        return switch (property) {
            case "id", "userId" -> initiator.getUserId();
            case "username" -> initiator.getUsername();
            case "deptId" -> initiator.getDeptId();
            case "roleId" -> initiator.getRoleId();
            case "orgId" -> initiator.getOrgId();
            default -> null;
        };
    }

    /**
     * 表达式上下文
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ExpressionContext {
        /**
         * 流程变量
         */
        private Map<String, Object> variables;

        /**
         * 发起人信息
         */
        private InitiatorInfo initiator;

        /**
         * 业务数据
         */
        private Map<String, Object> businessData;

        /**
         * 当前时间
         */
        @lombok.Builder.Default
        private LocalDateTime currentTime = LocalDateTime.now();

        /**
         * 发起人信息
         */
        @lombok.Data
        @lombok.Builder
        @lombok.NoArgsConstructor
        @lombok.AllArgsConstructor
        public static class InitiatorInfo {
            private Long userId;
            private String username;
            private Long deptId;
            private Long roleId;
            private Long orgId;
        }
    }

    /**
     * 表达式函数注册
     * 可以在SpEL表达式中使用的自定义函数
     */
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ExpressionFunctions {
        /**
         * 获取当前日期
         */
        public String currentDate() {
            return LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        }

        /**
         * 获取当前时间
         */
        public String currentTime() {
            return LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME);
        }

        /**
         * 获取当前日期时间
         */
        public String currentDateTime() {
            return LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }

        /**
         * 计算日期差（天数）
         */
        public long daysBetween(String startDate, String endDate) {
            LocalDateTime start = LocalDateTime.parse(startDate);
            LocalDateTime end = LocalDateTime.parse(endDate);
            return java.time.temporal.ChronoUnit.DAYS.between(start, end);
        }

        /**
         * 判断数值是否在范围内
         */
        public boolean between(Number value, Number min, Number max) {
            BigDecimal bdValue = new BigDecimal(value.toString());
            BigDecimal bdMin = new BigDecimal(min.toString());
            BigDecimal bdMax = new BigDecimal(max.toString());
            return bdValue.compareTo(bdMin) >= 0 && bdValue.compareTo(bdMax) <= 0;
        }

        /**
         * 包含判断
         */
        public boolean contains(Collection<?> collection, Object value) {
            return collection != null && collection.contains(value);
        }

        /**
         * 列表包含任意一个
         */
        public boolean containsAny(Collection<?> collection, Object... values) {
            if (collection == null || values == null) {
                return false;
            }
            return Arrays.stream(values).anyMatch(collection::contains);
        }

        /**
         * 格式化数字
         */
        public String formatNumber(Number value, String pattern) {
            return new java.text.DecimalFormat(pattern).format(value);
        }

        /**
         * 格式化日期
         */
        public String formatDate(LocalDateTime date, String pattern) {
            return date.format(DateTimeFormatter.ofPattern(pattern));
        }
    }
}
