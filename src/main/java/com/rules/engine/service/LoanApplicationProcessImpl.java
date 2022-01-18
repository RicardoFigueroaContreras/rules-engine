package com.rules.engine.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.rules.engine.utils.HttpUtils;

@Service
public class LoanApplicationProcessImpl implements LoanApplicationProcess {

	private String kieBaseUri = "http://localhost:8180/kie-server/services/rest/server/containers/instances";
	private String username = "admin";
	private String password = "admin";

	@Autowired
	private RestTemplate restTemplate;

	@Override
	public Map<String, Object> execute(Map<String, Object> payload) {
		System.out.println("execute : payload => " + payload);
		
		String objectType = null;
		String containerId = "LoanApplicationProcess_1.0.0";
		
		objectType = "com.financial.department.loans.model.ScoreCardInquiry";

		Map<String, Object> scoreCardAgeMap = Map.ofEntries(Map.entry("variableName", "AGE"),
				Map.entry("sourceName", "APPLICANT"), Map.entry("variableNameValue", payload.get("age")));

		Map<String, Object> scoreCardMonthlyIncomeMap = Map.ofEntries(Map.entry("variableName", "MONTHLY_INCOME"),
				Map.entry("sourceName", "APPLICANT"), Map.entry("variableNameValue", payload.get("monthlyIncome")));

		System.out.println("scoreCardAgeMap => " + scoreCardAgeMap);
		System.out.println("scoreCardMonthlyIncomeMap => " + scoreCardMonthlyIncomeMap);

		Map<String, Object> scoreCardAgeRequestMap = executeKieRule(containerId, getRequestTemplate(containerId, objectType, scoreCardAgeMap));
		Map<String, Object> scoreCardMonthlyIncomeRequestMap = executeKieRule(containerId, getRequestTemplate(objectType, scoreCardMonthlyIncomeMap));
		
		Map<String, Object> scoreCardAgeResponeMap = getResponseTemplate(objectType, scoreCardAgeRequestMap);
		Map<String, Object> scoreCardMonthlyIncomeResponeMap = getResponseTemplate(objectType, scoreCardMonthlyIncomeRequestMap);

		System.out.println("scoreCardAgeRequestMap => " + scoreCardAgeRequestMap);
		System.out.println("scoreCardMonthlyIncomeRequestMap => " + scoreCardMonthlyIncomeRequestMap);
		System.out.println("scoreCardAgeResponeMap => " + scoreCardAgeResponeMap);
		System.out.println("scoreCardMonthlyIncomeResponeMap => " + scoreCardMonthlyIncomeResponeMap);

		Integer ics = (Integer) scoreCardAgeResponeMap.get("variableNameScorePoint") + (Integer) scoreCardMonthlyIncomeResponeMap.get("variableNameScorePoint");
		
//		payload = Map.of("ics", ics);
		payload.put("ics", ics);

		System.out.println("ics => " + ics);
		System.out.println("payload include ics => " + payload);
		
		objectType = "com.financial.department.loans.model.ApplicationInquiry";

		Map<String, Object> applicationInquiryRequestMap = executeKieRule(containerId, getRequestTemplate(objectType, payload));
		Map<String, Object> applicationInquiryResponeMap = getResponseTemplate(objectType, applicationInquiryRequestMap);

		Map<String, Object> responsePayload = new HashMap<>();
		responsePayload.putAll(applicationInquiryResponeMap);

		System.out.println("applicationInquiryRequestMap => " + applicationInquiryRequestMap);
		System.out.println("applicationInquiryResponeMap => " + applicationInquiryResponeMap);
		return responsePayload;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Map<String, Object> executeKieRule(String containerId, Map<String, Object> payload) {
		System.out.println("-");
		ResponseEntity<Map> response = restTemplate.exchange(kieBaseUri + "/" + containerId, HttpMethod.POST, new HttpEntity<>(payload, HttpUtils.createHeaders(username, password)), Map.class, new String[] {});
		return response.getBody();
	}
	
	private Map<String, Object> getRequestTemplate(String objectType, Map<String, Object> objectValues) {
        Map<String, Object> object = Map.of(objectType, objectValues);
        Map<String, Object> insertValue = Map.of(
                "out-identifier", objectType,
                "return-object", true,
                "entry-point", "DEFAULT",
                "disconnected", false,
                "object", object);
        Map<String, Object> insert = Map.of("insert", insertValue);
        Map<String, Object> fireAllRules = Map.of("fire-all-rules", "");
        List<Map<String, Object>> commandsList = List.of(insert, fireAllRules);
        return Map.of("commands", commandsList);
    }

	public Map<String, Object> getRequestTemplate(String outIdentifier, String objectType, Map<String, Object> objectValues) {
		Map<String, Object> object = Map.of(objectType, objectValues);
		Map<String, Object> insertValue = Map.of(
				"out-identifier", outIdentifier,
				"return-object", true,
				"entry-point", "DEFAULT",
				"disconnected", false,
				"object", object);
		Map<String, Object> insert = Map.of("insert", insertValue);
		Map<String, Object> fireAllRules = Map.of("fire-all-rules", "");
		List<Map<String, Object>> commandsList = List.of(insert, fireAllRules);
		return Map.of("commands", commandsList);
	}

	private Map<String, Object> getResponseTemplate(String objectType, Map<String, Object> response){
        Map<String, Object> returnValue =  null;
        Map<String, Object> result = Map.class.cast(response.get("result"));
        Map<String, Object> executionResults = Map.class.cast(result.get("execution-results"));
        List<Map<String, Object>> results = List.class.cast(executionResults.get("results"));
        for (Map<String, Object> r : results) {
            Object value = r.get("value");
            if(value instanceof Map){
                returnValue = Map.class.cast(value);
                if(returnValue.containsKey(objectType)){
                    returnValue = Map.class.cast(returnValue.get(objectType));
                }
            }
        }
        return returnValue;
    }

}
