package com.rules.engine.service;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

public interface RuleEngineService {

    Map<String, Object> execute(String company, String ruleGroup, Map<String, Object> payload);
}
