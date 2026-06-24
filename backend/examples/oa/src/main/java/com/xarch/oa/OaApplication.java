package com.xarch.oa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * xarch OA example - Office Automation
 *
 * <p>Demonstrates a small workflow / approval engine on top of the
 * xarch framework. Two business flows are wired up out of the box:
 * <ul>
 *   <li>Leave requests with manager + HR approval</li>
 *   <li>Expense reports with category-based routing</li>
 * </ul>
 */
@SpringBootApplication
public class OaApplication {

    public static void main(String[] args) {
        SpringApplication.run(OaApplication.class, args);
    }
}
