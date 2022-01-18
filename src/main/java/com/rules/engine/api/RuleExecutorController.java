package com.rules.engine.api;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rules.engine.service.LoanApplicationProcess;
import com.rules.engine.service.RuleEngineService;

@CrossOrigin("*")
@RestController
@RequestMapping("rule/execution")
public class RuleExecutorController {

	private RuleEngineService ruleEngineService;

	private LoanApplicationProcess loanApplicationProcess;

	@Autowired
	public RuleExecutorController(RuleEngineService ruleEngineService, LoanApplicationProcess loanApplicationProcess) {
		this.ruleEngineService = ruleEngineService;
		this.loanApplicationProcess = loanApplicationProcess;
	}

	@PostMapping("/{company}/{ruleGroup}")
	public Map<String, Object> execute(@PathVariable String company, @PathVariable String ruleGroup,
			@RequestBody Map<String, Object> payload) {
		return ruleEngineService.execute(company, ruleGroup, payload);
	}

	@PostMapping("/loanApplicationProcess")
	public Map<String, Object> executeLoanApplicationProcess(@RequestBody Map<String, Object> payload) {
		System.out.println("payload => " + payload);		
		return loanApplicationProcess.execute(payload);
	}

}
