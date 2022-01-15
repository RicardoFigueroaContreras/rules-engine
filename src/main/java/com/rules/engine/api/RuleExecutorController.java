package com.rules.engine.api;

import com.rules.engine.service.RuleEngineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@CrossOrigin("*")
@RestController
@RequestMapping("rule/execution")
public class RuleExecutorController {

    private RuleEngineService ruleEngineService;

    @Autowired
    public RuleExecutorController(RuleEngineService ruleEngineService){
        this.ruleEngineService = ruleEngineService;
    }
    
    @PostMapping("/{company}/{ruleGroup}")
    public Map<String, Object> execute(
            @PathVariable String company,
            @PathVariable String ruleGroup,
            @RequestBody Map<String, Object> payload) {
        return ruleEngineService.execute(company, ruleGroup, payload);
    }

}
