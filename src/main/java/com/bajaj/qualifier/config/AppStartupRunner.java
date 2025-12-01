package com.bajaj.qualifier.config;

import org.springframework.stereotype.Component;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.ApplicationArguments;
import org.springframework.beans.factory.annotation.Autowired;
import com.bajaj.qualifier.service.WebhookService;

@Component
public class AppStartupRunner implements ApplicationRunner {
    
    @Autowired
    private WebhookService webhookService;
    
    @Override
    public void run(ApplicationArguments args) {
        try {
            System.out.println("=== Application started ===");
            System.out.println("Executing workflow...");
            
            // REPLACE WITH YOUR ACTUAL VALUES
            webhookService.executeWorkflow(
                "Your Full Name",     // Your name
                "REG12347",           // Your registration number
                "your@email.com"      // Your email
            );
            
            System.out.println("=== Workflow completed ===");
        } catch (Exception e) {
            System.err.println("=== ERROR in workflow ===");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            // Don't re-throw - let app stay running for debugging
        }
    }
}
