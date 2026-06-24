package com.xarch.crm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * xarch CRM example - Customer Relationship Management
 *
 * <p>Demonstrates a customer / opportunity / follow-up / contract
 * domain with a small sales analytics layer (funnel, conversion
 * rate, revenue forecast) on top of the xarch framework.
 */
@SpringBootApplication
public class CrmApplication {

    public static void main(String[] args) {
        SpringApplication.run(CrmApplication.class, args);
    }
}
