package com.xarch.starter.web.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Swagger / Knife4j configuration
 */
@Configuration
public class XarchSwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("xarch API Documentation")
                .description("AI-Enabled Enterprise Backend Framework API\n\n" +
                    "## API Groups\n" +
                    "- **System Management**: User, Role, Menu, Dept, Post, Notice\n" +
                    "- **System Config**: Dict, Config\n" +
                    "- **Log Management**: LoginLog, OpLog\n" +
                    "- **Monitor**: Server, Cache, Online, Job, JobLog\n" +
                    "- **Business**: Captcha, Client, Message, Resource, TempFile, Common\n" +
                    "- **Excel**: Import/Export operations\n" +
                    "- **MCP Servers**: Database, Knowledge, Filesystem")
                .version("1.0.0")
                .contact(new Contact().name("xarch").email("contact@xarch.com"))
                .license(new License().name("MIT").url("https://opensource.org/licenses/MIT")))
            .tags(List.of(
                new Tag().name("System - User").description("User management APIs"),
                new Tag().name("System - Role").description("Role management APIs"),
                new Tag().name("System - Menu").description("Menu management APIs"),
                new Tag().name("System - Dept").description("Department management APIs"),
                new Tag().name("System - Post").description("Post management APIs"),
                new Tag().name("System - Notice").description("Notice management APIs"),
                new Tag().name("System - Dict").description("Dictionary management APIs"),
                new Tag().name("System - Config").description("System config APIs"),
                new Tag().name("Monitor - LoginLog").description("Login log APIs"),
                new Tag().name("Monitor - OpLog").description("Operation log APIs"),
                new Tag().name("Monitor - Server").description("Server monitor APIs"),
                new Tag().name("Monitor - Cache").description("Cache monitor APIs"),
                new Tag().name("Monitor - Online").description("Online user APIs"),
                new Tag().name("Monitor - Job").description("Job scheduling APIs"),
                new Tag().name("Excel").description("Excel import/export APIs"),
                new Tag().name("Common").description("Common operation APIs"),
                new Tag().name("MCP - Database").description("Database MCP Server APIs"),
                new Tag().name("MCP - Knowledge").description("Knowledge Base MCP Server APIs"),
                new Tag().name("MCP - Filesystem").description("Filesystem MCP Server APIs")
            ));
    }
}