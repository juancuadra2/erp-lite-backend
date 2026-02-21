package com.jcuadrado.erplitebackend.infrastructure.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class SpelConditionEvaluatorTest {

    private SpelConditionEvaluator evaluator;

    @BeforeEach
    void setUp() {
        evaluator = new SpelConditionEvaluator();
    }

    @Test
    @DisplayName("evaluate should return true when condition is null")
    void evaluate_shouldReturnTrue_whenConditionIsNull() {
        assertThat(evaluator.evaluate(null, Map.of())).isTrue();
    }

    @Test
    @DisplayName("evaluate should return true when condition is blank")
    void evaluate_shouldReturnTrue_whenConditionIsBlank() {
        assertThat(evaluator.evaluate("   ", Map.of())).isTrue();
    }

    @Test
    @DisplayName("evaluate should return true when SpEL condition evaluates to true")
    void evaluate_shouldReturnTrue_whenConditionIsTrue() {
        Map<String, Object> context = Map.of("role", "ADMIN");

        boolean result = evaluator.evaluate("#role == 'ADMIN'", context);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("evaluate should return false when SpEL condition evaluates to false")
    void evaluate_shouldReturnFalse_whenConditionIsFalse() {
        Map<String, Object> context = Map.of("role", "USER");

        boolean result = evaluator.evaluate("#role == 'ADMIN'", context);

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("evaluate should return false when SpEL expression is invalid")
    void evaluate_shouldReturnFalse_whenExpressionIsInvalid() {
        boolean result = evaluator.evaluate("this is not valid SpEL !!@#$", Map.of());

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("evaluate should handle numeric context variables correctly")
    void evaluate_shouldHandleNumericVariables() {
        Map<String, Object> context = Map.of("amount", 500);

        boolean result = evaluator.evaluate("#amount > 100", context);

        assertThat(result).isTrue();
    }
}
