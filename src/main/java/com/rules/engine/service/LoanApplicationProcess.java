package com.rules.engine.service;

import java.util.Map;

public interface LoanApplicationProcess {

	Map<String, Object> execute(Map<String, Object> payload);
}
