package com.xarch.cms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * xarch CMS example - Content Management System
 *
 * <p>Demonstrates the xarch framework with a content management use case:
 * articles, categories, tags and comments with multi-table joins, paginated
 * search, soft delete and a small publish/archive workflow.
 */
@SpringBootApplication
public class CmsApplication {

    public static void main(String[] args) {
        SpringApplication.run(CmsApplication.class, args);
    }
}
