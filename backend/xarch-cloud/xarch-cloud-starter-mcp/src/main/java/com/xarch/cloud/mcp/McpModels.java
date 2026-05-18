package com.xarch.cloud.mcp;

import java.util.List;
import java.util.Map;

/**
 * MCP Protocol Request/Response models
 */
public class McpRequest {
    private String tool;
    private Map<String, Object> arguments;

    public String getTool() {
        return tool;
    }

    public void setTool(String tool) {
        this.tool = tool;
    }

    public Map<String, Object> getArguments() {
        return arguments;
    }

    public void setArguments(Map<String, Object> arguments) {
        this.arguments = arguments;
    }
}

class McpResponse {
    private boolean success;
    private Object result;
    private String error;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}

class McpTool {
    private String name;
    private String description;
    private List<McpToolArgument> arguments;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<McpToolArgument> getArguments() {
        return arguments;
    }

    public void setArguments(List<McpToolArgument> arguments) {
        this.arguments = arguments;
    }
}

class McpToolArgument {
    private String name;
    private String type;
    private boolean required;
    private String description;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}