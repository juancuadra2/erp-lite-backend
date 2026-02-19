package com.jcuadrado.erplitebackend.infrastructure.security;

import com.jcuadrado.erplitebackend.application.port.security.ConditionEvaluator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
public class SpelConditionEvaluator implements ConditionEvaluator {

    private final SpelExpressionParser parser = new SpelExpressionParser();

    @Override
    public boolean evaluate(String condition, Map<String, Object> context) {
        if (condition == null || condition.isBlank()) {
            return true;
        }
        try {
            Expression expression = parser.parseExpression(condition);
            StandardEvaluationContext evalContext = new StandardEvaluationContext();
            context.forEach(evalContext::setVariable);
            Boolean result = expression.getValue(evalContext, Boolean.class);
            return result != null && result;
        } catch (Exception e) {
            log.warn("Error evaluando condición SpEL '{}': {}", condition, e.getMessage());
            return false;
        }
    }
}
