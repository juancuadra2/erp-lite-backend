package com.jcuadrado.erplitebackend.application.port.security;

import java.util.Map;

public interface ConditionEvaluator {

    boolean evaluate(String condition, Map<String, Object> context);
}
