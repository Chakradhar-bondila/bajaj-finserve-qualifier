package com.bajaj.qualifier.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import com.bajaj.qualifier.model.WebhookRequest;
import com.bajaj.qualifier.model.WebhookResponse;
import com.bajaj.qualifier.model.SqlSubmissionRequest;

@Service
public class WebhookService {
    
    private final RestTemplate restTemplate = new RestTemplate();
    
    public void executeWorkflow(String name, String regNo, String email) {
        try {
            // Step 1: Generate webhook
            WebhookResponse response = generateWebhook(name, regNo, email);
            System.out.println("Webhook URL: " + response.getWebhook());
            System.out.println("Access Token received");
            
            // Step 2: Determine SQL query
            String sqlQuery = determineSqlQuery(regNo);
            System.out.println("SQL Query prepared");
            
            // Step 3: Submit solution
            submitSolution(response.getWebhook(), response.getAccessToken(), sqlQuery);
            
        } catch (Exception e) {
            System.err.println("Error in workflow: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private WebhookResponse generateWebhook(String name, String regNo, String email) {
        String url = "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA";
        
        WebhookRequest request = new WebhookRequest(name, regNo, email);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<WebhookRequest> entity = new HttpEntity<>(request, headers);
        
        try {
            System.out.println("=== Calling generateWebhook API ===");
            ResponseEntity<WebhookResponse> response = restTemplate.postForEntity(url, entity, WebhookResponse.class);
            
            System.out.println("Status Code: " + response.getStatusCode());
            System.out.println("Response received");
            
            return response.getBody();
        } catch (Exception e) {
            System.err.println("=== generateWebhook API Failed ===");
            System.err.println("Error: " + e.getMessage());
            throw e;
        }
    }
    
    private String determineSqlQuery(String regNo) {
        int lastTwoDigits = Integer.parseInt(regNo.substring(regNo.length() - 2));
        
        if (lastTwoDigits % 2 == 1) {
            return "SELECT d.DEPARTMENT_NAME, SUM(p.AMOUNT) as SALARY, " +
                   "CONCAT(e.FIRST_NAME, ' ', e.LAST_NAME) as EMPLOYEE_NAME, " +
                   "TIMESTAMPDIFF(YEAR, e.DOB, CURDATE()) as AGE " +
                   "FROM EMPLOYEE e JOIN PAYMENTS p ON e.EMP_ID = p.EMP_ID " +
                   "JOIN DEPARTMENT d ON e.DEPARTMENT = d.DEPARTMENT_ID " +
                   "WHERE DAY(p.PAYMENT_TIME) != 1 " +
                   "GROUP BY e.EMP_ID, d.DEPARTMENT_NAME " +
                   "ORDER BY SALARY DESC LIMIT 1";
        } else {
            return "SELECT d.DEPARTMENT_NAME, " +
                   "AVG(TIMESTAMPDIFF(YEAR, e.DOB, CURDATE())) as AVERAGE_AGE, " +
                   "GROUP_CONCAT(CONCAT(e.FIRST_NAME, ' ', e.LAST_NAME) SEPARATOR ', ') as EMPLOYEE_LIST " +
                   "FROM EMPLOYEE e JOIN PAYMENTS p ON e.EMP_ID = p.EMP_ID " +
                   "JOIN DEPARTMENT d ON e.DEPARTMENT = d.DEPARTMENT_ID " +
                   "WHERE p.AMOUNT > 70000 " +
                   "GROUP BY d.DEPARTMENT_ID, d.DEPARTMENT_NAME " +
                   "ORDER BY d.DEPARTMENT_ID DESC";
        }
    }
    
    private void submitSolution(String webhookUrl, String accessToken, String sqlQuery) {
        SqlSubmissionRequest request = new SqlSubmissionRequest(sqlQuery);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", accessToken);
        
        HttpEntity<SqlSubmissionRequest> entity = new HttpEntity<>(request, headers);
        
        System.out.println("=== Submitting Solution ===");
        System.out.println("Webhook URL: " + webhookUrl);
        
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(webhookUrl, entity, String.class);
            System.out.println("=== API Response ===");
            System.out.println("Status Code: " + response.getStatusCode());
            System.out.println("Response Body: " + response.getBody());
            
            if (response.getStatusCode().is2xxSuccessful()) {
                System.out.println("✅ Solution submitted successfully!");
            } else {
                System.err.println("❌ Unexpected response code: " + response.getStatusCode());
            }
        } catch (Exception e) {
            System.err.println("=== API ERROR ===");
            System.err.println("Error: " + e.getMessage());
            throw e;
        }
    }
}
