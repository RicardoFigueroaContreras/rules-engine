package com.rules.engine.service;

import com.rules.engine.utils.HttpUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class RuleEngineServiceImpl implements RuleEngineService {

    @Value("${kie-server.base.uri}")
    private String kieBaseUri;

    @Value("${kie-server.username}")
    private String username;

    @Value("${kie-server.password}")
    private String password;

    private RestTemplate restTemplate;

    @Autowired
    public RuleEngineServiceImpl(RestTemplate restTemplate){
        this.restTemplate = restTemplate;
    }

    @Override
    public Map<String, Object> execute(String company, String ruleGroup, Map<String, Object> payload) {

        Map<String, Object> rule1Request = executeKieRule("CreditScoreCardV1",
                getRequestTemplate("CreditScoreCardV1", "com.financialsettings.CreditScoreCardInquiry", payload));
        Map<String, Object> rule1Response = getResponseTemplate("com.financialsettings.CreditScoreCardInquiry", rule1Request);

        Map<String, Object> rule2Request = executeKieRule("FinancialSettingsV1",
                getRequestTemplate("FinancialSettingsV1", "com.financialsettings.FinancialSettingsInquiry", payload));
        Map<String, Object> rule2Response = getResponseTemplate("com.financialsettings.FinancialSettingsInquiry", rule2Request);


        Map<String, Object> response =  new HashMap<>();
        response.putAll(rule1Response);
        response.putAll(rule2Response);

        return response;
    }

    public Map<String, Object> executeKieRule(String project, Map<String, Object> payload){
        ResponseEntity<Map> response = restTemplate
                .exchange(kieBaseUri + "/" + project,
                        HttpMethod.POST,
                        new HttpEntity<>(payload, HttpUtils.createHeaders(username, password)),
                        Map.class,
                        new String[]{});
        return  response.getBody();
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

    public Map<String, Object> getResponseTemplate(String objectType, Map<String, Object> response){
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
