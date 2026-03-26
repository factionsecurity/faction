package com.fuse.actions.admin;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.Result;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fuse.actions.FSActionSupport;
import com.fuse.dao.AuditLog;
import com.fuse.dao.HibHelper;
import com.fuse.dao.LLMConfig;
import com.fuse.dao.User;
import com.fuse.utils.FSUtils;

@Namespace("/portal")
public class AIConfig extends FSActionSupport {

    private List<LLMConfig> llmConfigs;
    private LLMConfig selectedConfig;
    private String name;
    private String provider;
    private String model;
    private String apiKey;
    private String baseUrl;
    private String endpoint;
    private String deployment;
    private String accessKey;
    private String secretKey;
    private String region;
    private boolean active;
    private Long configId;
    private List<Map<String, String>> availableModels;

    @Action(value = "AIConfig", results = {
            @Result(name = "success", location = "/WEB-INF/jsp/admin/AIConfig.jsp"),
            @Result(name = "error", location = "/WEB-INF/jsp/admin/AIConfig.jsp"),
            @Result(name = "input", location = "/WEB-INF/jsp/admin/AIConfig.jsp")
    })
    public String aiConfig() {
        if (!this.isAcmanager()) {
            return LOGIN;
        }

        // Load existing configurations
        llmConfigs = em.createQuery("from LLMConfig order by id", LLMConfig.class).getResultList();

        return SUCCESS;
    }

    @Action(value = "GetLLMConfig", results = {
            @Result(name = "success", location = "/WEB-INF/jsp/admin/llmConfigJSON.jsp")
    })
    public String getLLMConfig() {
        if (!this.isAcmanager()) {
            return this.ERRORJSON;
        }

        if (!this.testToken(false)) {
            return this.ERRORJSON;
        }

        if (configId != null) {
            selectedConfig = em.find(LLMConfig.class, configId);
        }

        return SUCCESS;
    }

    @Action(value = "SaveLLMConfig", results = {
            @Result(name = "successJson", location = "/WEB-INF/jsp/successJson.jsp"),
            @Result(name = "errorJson", location = "/WEB-INF/jsp/errorJson.jsp")
    })
    public String saveLLMConfig() {
        if (!this.isAcmanager()) {
            return this.ERRORJSON;
        }

        if (!this.testToken(false)) {
            return this.ERRORJSON;
        }

        try {
            // Validate the configuration
            String validationError = validateProviderConfig();
            if (validationError != null) {
                this._message = validationError;
                return this.ERRORJSON;
            }

            LLMConfig config;
            if (configId != null && configId > 0) {
                // Update existing configuration
                config = em.find(LLMConfig.class, configId);
                if (config == null) {
                    this._message = "Configuration not found";
                    return this.ERRORJSON;
                }
            } else {
                // Create new configuration
                config = new LLMConfig();
            }

            // Set basic properties
            config.setName(this.name);
            config.setProvider(this.provider);
            config.setModel(this.model);
            config.setActive(this.active);

            // Set provider-specific properties
            setProviderProperties(config);
            HibHelper.getInstance().preJoin();
            em.joinTransaction();

            // Save to database
            if (configId == null || configId == 0) {
                em.persist(config);
            } else {
                em.merge(config);
            }
            HibHelper.getInstance().commit();

            // Log the action
            User user = this.getSessionUser();
            AuditLog.saveLog(this, AuditLog.UserAction, "LLM Config",
                    "Saved LLM Configuration: " + config.getProvider(),
                    AuditLog.CompUser, user.getId(), false);

            this._message = "LLM configuration saved successfully";
            return this.SUCCESSJSON;

        } catch (Exception e) {
            this._message = "Error saving configuration: " + e.getMessage();
            return this.ERRORJSON;
        }
    }

    @Action(value = "DeleteLLMConfig", results = {
            @Result(name = "successJson", location = "/WEB-INF/jsp/successJson.jsp"),
            @Result(name = "errorJson", location = "/WEB-INF/jsp/errorJson.jsp")
    })
    public String deleteLLMConfig() {
        if (!this.isAcmanager()) {
            return this.ERRORJSON;
        }

        if (!this.testToken(false)) {
            return this.ERRORJSON;
        }

        try {
            if (configId != null) {
                HibHelper.getInstance().preJoin();
                em.joinTransaction();
                LLMConfig config = em.find(LLMConfig.class, configId);
                if (config != null) {
                    em.remove(config);
                    HibHelper.getInstance().commit();

                    this._message = "Configuration deleted successfully";
                } else {
                    this._message = "Configuration not found";
                    return this.ERRORJSON;
                }
            }
            return this.SUCCESSJSON;

        } catch (Exception e) {
            this._message = "Error deleting configuration: " + e.getMessage();
            return this.ERRORJSON;
        }
    }

    @Action(value = "TestLLMConnection", results = {
            @Result(name = "successJson", location = "/WEB-INF/jsp/successJson.jsp"),
            @Result(name = "errorJson", location = "/WEB-INF/jsp/errorJson.jsp")
    })
    public String testLLMConnection() {
        if (!this.isAcmanager()) {
            return this.ERRORJSON;
        }

        if (!this.testToken(false)) {
            return this.ERRORJSON;
        }

        try {
            LLMConfig testConfig;
            if (configId != null && configId > 0) {
                // Testing a saved config — look it up and decrypt its credentials
                LLMConfig saved = em.find(LLMConfig.class, configId);
                if (saved == null) {
                    this._message = "Configuration not found";
                    return this.ERRORJSON;
                }
                testConfig = buildDecryptedTestConfig(saved);
            } else {
                // Testing from the add/edit form — use raw form values
                testConfig = new LLMConfig();
                testConfig.setProvider(this.provider);
                testConfig.setModel(this.model);
                testConfig.setBaseUrl(this.baseUrl);
                testConfig.setEndpoint(this.endpoint);
                testConfig.setDeployment(this.deployment);
                testConfig.setRegion(this.region);
                testConfig.setApiKey(this.apiKey);
                testConfig.setAccessKey(this.accessKey);
                testConfig.setSecretKey(this.secretKey);
            }

            boolean connectionSuccessful = testConnection(testConfig);

            if (connectionSuccessful) {
                this._message = "Connection test successful";
                return this.SUCCESSJSON;
            } else {
                this._message = "Connection test failed. Please check your configuration.";
                return this.ERRORJSON;
            }

        } catch (Exception e) {
            this._message = "Connection test error: " + e.getMessage();
            return this.ERRORJSON;
        }
    }

    @Action(value = "GetProviderModels", results = {
            @Result(name = "success", location = "/WEB-INF/jsp/admin/providerModelsJSON.jsp")
    })
    public String getProviderModels() {
        if (!this.isAcmanager()) {
            return this.ERRORJSON;
        }

        if (!this.testToken(false)) {
            return this.ERRORJSON;
        }

        try {
            // If API credentials are provided, try to fetch real models
            boolean canFetchDynamic = provider != null && (
                (apiKey != null && !apiKey.trim().isEmpty()) ||
                ("OPENAI_COMPATIBLE".equals(provider) && baseUrl != null && !baseUrl.trim().isEmpty())
            );
            if (canFetchDynamic) {
                List<String> dynamicModels = fetchModelsFromAPI();
                if (dynamicModels != null && !dynamicModels.isEmpty()) {
                    // Convert to the format expected by the frontend
                    availableModels = new ArrayList<>();
                    for (String model : dynamicModels) {
                        Map<String, String> modelMap = new HashMap<>();
                        modelMap.put("value", model);
                        modelMap.put("label", model);
                        availableModels.add(modelMap);
                    }
                    return SUCCESS;
                }
            }

            // Fallback to static models if API fetch fails or no credentials provided
            availableModels = getStaticModelsForProvider(provider);
            return SUCCESS;

        } catch (Exception e) {
            // Fallback to static models on error
            availableModels = getStaticModelsForProvider(provider);
            return SUCCESS;
        }
    }

    private List<String> fetchModelsFromAPI() {
        if (provider == null)
            return null;

        switch (provider.toUpperCase()) {
            case "OPENAI":
                return fetchOpenAIModels();
            case "OPENAI_COMPATIBLE":
                return fetchOpenAICompatibleModels();
            case "CLAUDE":
                return fetchClaudeModels();
            case "AZURE_OPENAI":
                // Azure OpenAI models are deployment-specific, return static list
                return null;
            case "AWS_BEDROCK":
                // AWS Bedrock models are region/account specific, return static list
                return null;
            default:
                return null;
        }
    }

    private List<String> fetchOpenAICompatibleModels() {
        try {
            if (baseUrl == null || baseUrl.trim().isEmpty()) {
                return null;
            }
            String url = baseUrl.trim();
            if (!url.endsWith("/")) url += "/";
            URL urlObj = new URL(url + "models");
            HttpURLConnection connection = (HttpURLConnection) urlObj.openConnection();
            connection.setRequestMethod("GET");
            if (apiKey != null && !apiKey.trim().isEmpty()) {
                connection.setRequestProperty("Authorization", "Bearer " + apiKey);
            }
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);

            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                ObjectMapper mapper = new ObjectMapper();
                JsonNode jsonResponse = mapper.readTree(response.toString());
                JsonNode data = jsonResponse.get("data");

                List<String> models = new ArrayList<>();
                if (data != null && data.isArray()) {
                    for (JsonNode model : data) {
                        JsonNode id = model.get("id");
                        if (id != null) {
                            models.add(id.asText());
                        }
                    }
                }
                return models.isEmpty() ? null : models;
            }
            connection.disconnect();
        } catch (Exception e) {
            // Return null to indicate failure
        }
        return null;
    }

    private List<String> fetchOpenAIModels() {
        try {
            String baseUrl = this.baseUrl;
            if (baseUrl == null || baseUrl.trim().isEmpty()) {
                baseUrl = "https://api.openai.com/v1";
            }

            URL url = new URL(baseUrl + "/models");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "Bearer " + apiKey);
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);

            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                // Parse JSON response and extract model names
                ObjectMapper mapper = new ObjectMapper();
                JsonNode jsonResponse = mapper.readTree(response.toString());
                JsonNode data = jsonResponse.get("data");

                List<String> models = new ArrayList<>();
                if (data != null && data.isArray()) {
                    for (JsonNode model : data) {
                        JsonNode id = model.get("id");
                        if (id != null) {
                            String modelId = id.asText();
                            // Filter to only chat models
                            if (modelId.contains("gpt")) {
                                models.add(modelId);
                            }
                        }
                    }
                }
                return models;
            }
            connection.disconnect();

        } catch (Exception e) {
            // Return null to indicate failure
        }
        return null;
    }

    private List<String> fetchClaudeModels() {
        try {
            // Fetch available models from Claude's models API endpoint
            URL url = new URL("https://api.anthropic.com/v1/models");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("GET");
            connection.setRequestProperty("x-api-key", apiKey);
            connection.setRequestProperty("anthropic-version", "2023-06-01");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);

            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                // Parse JSON response and extract model names
                ObjectMapper mapper = new ObjectMapper();
                JsonNode jsonResponse = mapper.readTree(response.toString());
                JsonNode data = jsonResponse.get("data");

                List<String> models = new ArrayList<>();
                if (data != null && data.isArray()) {
                    for (JsonNode model : data) {
                        JsonNode id = model.get("id");
                        if (id != null) {
                            String modelId = id.asText();
                            // Add all Claude models returned by the API
                            models.add(modelId);
                        }
                    }
                }
                return models;
            }
            connection.disconnect();

        } catch (Exception e) {
            // Return null to fallback to static models
        }
        return null;
    }

    private List<Map<String, String>> getStaticModelsForProvider(String provider) {
        List<Map<String, String>> models = new ArrayList<>();
        List<String> modelNames = new ArrayList<>();

        if (provider != null) {
            switch (provider.toUpperCase()) {
                case "OPENAI":
                    modelNames = Arrays.asList("gpt-4", "gpt-4-turbo", "gpt-3.5-turbo", "gpt-3.5-turbo-16k");
                    break;
                case "OPENAI_COMPATIBLE":
                    // No static models — loaded dynamically from the server's /models endpoint
                    break;
                case "AZURE_OPENAI":
                    modelNames = Arrays.asList("gpt-35-turbo", "gpt-4", "gpt-4-turbo", "gpt-4-32k");
                    break;
                case "AWS_BEDROCK":
                    modelNames = Arrays.asList("claude-3-opus-20240229", "claude-3-sonnet-20240229",
                            "claude-3-haiku-20240307", "claude-instant-v1", "claude-v2");
                    break;
                case "CLAUDE":
                    // Updated static models to include the latest Claude models
                    modelNames = Arrays.asList("claude-3-5-sonnet-20241022", "claude-3-5-sonnet-20240620",
                            "claude-3-5-haiku-20241022", "claude-3-opus-20240229", "claude-3-sonnet-20240229",
                            "claude-3-haiku-20240307");
                    break;
            }
        }

        for (String modelName : modelNames) {
            Map<String, String> modelMap = new HashMap<>();
            modelMap.put("value", modelName);
            modelMap.put("label", modelName);
            models.add(modelMap);
        }

        return models;
    }

    private String validateProviderConfig() {
        if (this.isNullString(this.provider)) {
            return "Provider is required";
        }

        if (this.isNullString(this.model)) {
            return "Model is required";
        }

        switch (this.provider) {
            case "OPENAI":
                if (this.isNullString(this.apiKey)) {
                    return "API Key is required for OpenAI";
                }
                break;

            case "AZURE_OPENAI":
                if (this.isNullString(this.apiKey) || this.isNullString(this.endpoint)
                        || this.isNullString(this.deployment)) {
                    return "API Key, Endpoint, and Deployment are required for Azure OpenAI";
                }
                break;

            case "AWS_BEDROCK":
                if (this.isNullString(this.accessKey) || this.isNullString(this.secretKey)
                        || this.isNullString(this.region)) {
                    return "Access Key, Secret Key, and Region are required for AWS Bedrock";
                }
                break;

            case "CLAUDE":
                if (this.isNullString(this.apiKey)) {
                    return "API Key is required for Claude";
                }
                break;

            case "OPENAI_COMPATIBLE":
                if (this.isNullString(this.baseUrl)) {
                    return "Base URL is required for OpenAI Compatible";
                }
                break;

            default:
                return "Invalid provider selected";
        }
        return null;
    }

    private void setProviderProperties(LLMConfig config) {
        switch (config.getProvider()) {
            case "OPENAI":
                config.setApiKey(FSUtils.encryptPassword(this.apiKey));
                config.setBaseUrl(this.baseUrl);
                break;

            case "AZURE_OPENAI":
                config.setApiKey(FSUtils.encryptPassword(this.apiKey));
                config.setEndpoint(this.endpoint);
                config.setDeployment(this.deployment);
                break;

            case "AWS_BEDROCK":
                config.setAccessKey(FSUtils.encryptPassword(this.accessKey));
                config.setSecretKey(FSUtils.encryptPassword(this.secretKey));
                config.setRegion(this.region);
                break;

            case "CLAUDE":
                config.setApiKey(FSUtils.encryptPassword(this.apiKey));
                break;

            case "OPENAI_COMPATIBLE":
                config.setBaseUrl(this.baseUrl);
                if (!this.isNullString(this.apiKey)) {
                    config.setApiKey(FSUtils.encryptPassword(this.apiKey));
                }
                break;
        }
    }

    private LLMConfig buildDecryptedTestConfig(LLMConfig saved) {
        LLMConfig test = new LLMConfig();
        test.setProvider(saved.getProvider());
        test.setModel(saved.getModel());
        test.setBaseUrl(saved.getBaseUrl());
        test.setEndpoint(saved.getEndpoint());
        test.setDeployment(saved.getDeployment());
        test.setRegion(saved.getRegion());
        if (saved.getApiKey() != null && !saved.getApiKey().isEmpty()) {
            test.setApiKey(FSUtils.decryptPassword(saved.getApiKey()));
        }
        if (saved.getAccessKey() != null && !saved.getAccessKey().isEmpty()) {
            test.setAccessKey(FSUtils.decryptPassword(saved.getAccessKey()));
        }
        if (saved.getSecretKey() != null && !saved.getSecretKey().isEmpty()) {
            test.setSecretKey(FSUtils.decryptPassword(saved.getSecretKey()));
        }
        return test;
    }

    private boolean testConnection(LLMConfig config) {
        switch (config.getProvider()) {
            case "OPENAI":
                return testOpenAIConnection(config);
            case "AZURE_OPENAI":
                return testAzureOpenAIConnection(config);
            case "AWS_BEDROCK":
                return testAWSBedrockConnection(config);
            case "CLAUDE":
                return testClaudeConnection(config);
            case "OPENAI_COMPATIBLE":
                return testOpenAICompatibleConnection(config);
            default:
                return false;
        }
    }

    private boolean testOpenAIConnection(LLMConfig config) {
        try {
            String baseUrl = config.getBaseUrl();
            if (baseUrl == null || baseUrl.trim().isEmpty()) {
                baseUrl = "https://api.openai.com/v1";
            }

            URL url = new URL(baseUrl + "/models");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "Bearer " + config.getApiKey());
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);

            int responseCode = connection.getResponseCode();
            connection.disconnect();
            return responseCode == 200;

        } catch (Exception e) {
            return false;
        }
    }

    private boolean testAzureOpenAIConnection(LLMConfig config) {
        try {
            String endpoint = config.getEndpoint();
            if (!endpoint.endsWith("/")) {
                endpoint += "/";
            }

            URL url = new URL(endpoint + "openai/deployments?api-version=2024-02-15-preview");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("api-key", config.getApiKey());
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);

            int responseCode = connection.getResponseCode();
            connection.disconnect();
            return responseCode == 200;

        } catch (Exception e) {
            return false;
        }
    }

    private boolean testOpenAICompatibleConnection(LLMConfig config) {
        try {
            String url = config.getBaseUrl();
            if (url == null || url.trim().isEmpty()) {
                return false;
            }
            url = url.trim();
            if (!url.endsWith("/")) url += "/";
            URL urlObj = new URL(url + "models");
            HttpURLConnection connection = (HttpURLConnection) urlObj.openConnection();
            connection.setRequestMethod("GET");
            if (config.getApiKey() != null && !config.getApiKey().trim().isEmpty()) {
                connection.setRequestProperty("Authorization", "Bearer " + config.getApiKey());
            }
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);

            int responseCode = connection.getResponseCode();
            connection.disconnect();
            return responseCode == 200;

        } catch (Exception e) {
            return false;
        }
    }

    private boolean testAWSBedrockConnection(LLMConfig config) {
        // AWS Bedrock requires AWS SDK - placeholder for now
        return !config.getAccessKey().isEmpty() && !config.getSecretKey().isEmpty();
    }

    private boolean testClaudeConnection(LLMConfig config) {
        try {
            URL url = new URL("https://api.anthropic.com/v1/messages");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("x-api-key", config.getApiKey());
            connection.setRequestProperty("anthropic-version", "2023-06-01");
            connection.setDoOutput(true);
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);

            // Send a minimal test request
            String testRequest = "{\"model\": \"" + config.getModel() +
                    "\", \"max_tokens\": 10, \"messages\": [{\"role\": \"user\", \"content\": \"Test\"}]}";

            try (OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream(),
                    StandardCharsets.UTF_8)) {
                writer.write(testRequest);
                writer.flush();
            }

            int responseCode = connection.getResponseCode();
            connection.disconnect();

            // 200 means success, other codes indicate issues with API key or configuration
            return responseCode == 200;

        } catch (Exception e) {
            return false;
        }
    }

    // Utility method for null/empty string checking
    private boolean isNullString(String str) {
        return str == null || str.trim().isEmpty();
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<LLMConfig> getLlmConfigs() {
        return llmConfigs;
    }

    public void setLlmConfigs(List<LLMConfig> llmConfigs) {
        this.llmConfigs = llmConfigs;
    }

    public LLMConfig getSelectedConfig() {
        return selectedConfig;
    }

    public void setSelectedConfig(LLMConfig selectedConfig) {
        this.selectedConfig = selectedConfig;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getDeployment() {
        return deployment;
    }

    public void setDeployment(String deployment) {
        this.deployment = deployment;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Long getConfigId() {
        return configId;
    }

    public void setConfigId(Long configId) {
        this.configId = configId;
    }

    public List<Map<String, String>> getAvailableModels() {
        return availableModels;
    }

    public void setAvailableModels(List<Map<String, String>> availableModels) {
        this.availableModels = availableModels;
    }
}