package com.nexterp.platform.workflow.expression;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 表达式解析器测试
 *
 * @author NextERP
 */
class WorkflowExpressionParserTest {

    private final WorkflowExpressionParser parser = new WorkflowExpressionParser(null);

    @Test
    @DisplayName("解析变量表达式 - 发起人ID")
    void testParseInitiatorId() {
        // Given
        var context = WorkflowExpressionParser.ExpressionContext.builder()
                .initiator(WorkflowExpressionParser.ExpressionContext.InitiatorInfo.builder()
                        .userId(123L)
                        .username("testuser")
                        .deptId(100L)
                        .roleId(5L)
                        .build())
                .build();

        // When
        Object result = parser.parse("${initiator.id}", context);

        // Then
        assertThat(result).isEqualTo(123L);
    }

    @Test
    @DisplayName("解析变量表达式 - 发起人部门ID")
    void testParseInitiatorDeptId() {
        // Given
        var context = WorkflowExpressionParser.ExpressionContext.builder()
                .initiator(WorkflowExpressionParser.ExpressionContext.InitiatorInfo.builder()
                        .userId(123L)
                        .deptId(100L)
                        .build())
                .build();

        // When
        Object result = parser.parse("${initiator.deptId}", context);

        // Then
        assertThat(result).isEqualTo(100L);
    }

    @Test
    @DisplayName("解析流程变量")
    void testParseVariable() {
        // Given
        Map<String, Object> variables = new HashMap<>();
        variables.put("amount", 10000);

        var context = WorkflowExpressionParser.ExpressionContext.builder()
                .variables(variables)
                .build();

        // When
        Object result = parser.parse("${amount}", context);

        // Then
        assertThat(result).isEqualTo(10000);
    }

    @Test
    @DisplayName("解析SpEL表达式")
    void testParseSpelExpression() {
        // Given
        var context = WorkflowExpressionParser.ExpressionContext.builder()
                .variables(Map.of("x", 10, "y", 20))
                .build();

        // When
        Object result = parser.parse("#{x + y}", context);

        // Then
        assertThat(result).isEqualTo(30);
    }

    @Test
    @DisplayName("解析分配表达式为列表")
    void testParseAssignmentExpression() {
        // Given
        var context = WorkflowExpressionParser.ExpressionContext.builder()
                .initiator(WorkflowExpressionParser.ExpressionContext.InitiatorInfo.builder()
                        .userId(123L)
                        .deptId(100L)
                        .build())
                .build();

        // When
        List<Long> result = parser.parseAssignmentExpression("${initiator.deptId}", context);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(100L);
    }

    @Test
    @DisplayName("解析为布尔值")
    void testParseBoolean() {
        // Given
        var context = WorkflowExpressionParser.ExpressionContext.builder()
                .variables(Map.of("approved", true))
                .build();

        // When
        Boolean result = parser.parseBoolean("${approved}", context);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("解析为整数")
    void testParseInt() {
        // Given
        var context = WorkflowExpressionParser.ExpressionContext.builder()
                .variables(Map.of("amount", 10000))
                .build();

        // When
        Integer result = parser.parseInt("${amount}", context);

        // Then
        assertThat(result).isEqualTo(10000);
    }

    @Test
    @DisplayName("解析为字符串")
    void testParseString() {
        // Given
        var context = WorkflowExpressionParser.ExpressionContext.builder()
                .variables(Map.of("businessKey", "BIZ-001"))
                .build();

        // When
        String result = parser.parseString("${businessKey}", context);

        // Then
        assertThat(result).isEqualTo("BIZ-001");
    }
}
