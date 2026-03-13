package com.fuse.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fuse.dao.LLMConfig;
import com.fuse.dao.Vulnerability;
import com.fuse.utils.FSUtils;

/**
 * Service class for interacting with various LLM providers
 */
public class LLMService {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final int TIMEOUT_MS = 60000; // 60 seconds timeout

    /**
     * Generate AI summary using the configured LLM provider
     */
    public String generateSummary(LLMConfig config, String assessmentName, String appId, List<Vulnerability> vulnerabilities) throws Exception {
        String prompt = buildPrompt(assessmentName, appId, vulnerabilities);
        
        // Normalize provider name for case-insensitive matching
        String provider = config.getProvider();
        if (provider != null) {
            provider = provider.trim().toUpperCase();
        }
        
        switch (provider) {
            case "OPENAI":
                return callOpenAI(config, prompt);
            case "AZURE OPENAI":
                return callAzureOpenAI(config, prompt);
            case "CLAUDE":
                return callClaude(config, prompt);
            case "AWS BEDROCK":
                return callAWSBedrock(config, prompt);
            default:
                throw new IllegalArgumentException("Unsupported provider: " + config.getProvider());
        }
    }
    /**
     * Generate AI summary using the configured LLM provider
     */
    public String generateText(LLMConfig config,String prompt) throws Exception {
        
        // Normalize provider name for case-insensitive matching
        String provider = config.getProvider();
        if (provider != null) {
            provider = provider.trim().toUpperCase();
        }
        
        switch (provider) {
            case "OPENAI":
                return callOpenAI(config, prompt);
            case "AZURE OPENAI":
                return callAzureOpenAI(config, prompt);
            case "CLAUDE":
                return callClaude(config, prompt);
            case "AWS BEDROCK":
                return callAWSBedrock(config, prompt);
            default:
                throw new IllegalArgumentException("Unsupported provider: " + config.getProvider());
        }
    }

    /**
     * Build the prompt for vulnerability summary generation
     */
    private String buildPrompt(String assessmentName, String appId, List<Vulnerability> vulnerabilities) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Please analyze the following security vulnerabilities from an assessment and create a high-level executive summary. ");
        prompt.append("Focus on the overall risk posture, key themes, and recommendations. ");
        prompt.append("Keep the summary professional and concise (2-3 paragraphs). ");
        prompt.append("Format the response as HTML with appropriate headings and paragraphs.\n\n");
        prompt.append("Assessment: ").append(assessmentName).append("\n");
        prompt.append("Application ID: ").append(appId).append("\n\n");
        prompt.append("Vulnerabilities:\n");

        for (Vulnerability vuln : vulnerabilities) {
            prompt.append("- Title: ").append(vuln.getName()).append("\n");
            if (vuln.getDescription() != null && !vuln.getDescription().trim().isEmpty()) {
                prompt.append("  Description: ").append(stripHtml(vuln.getDescription())).append("\n");
            }
            if (vuln.getRecommendation() != null && !vuln.getRecommendation().trim().isEmpty()) {
                prompt.append("  Recommendation: ").append(stripHtml(vuln.getRecommendation())).append("\n");
            }
            if (vuln.getDetails() != null && !vuln.getDetails().trim().isEmpty()) {
                prompt.append("  Details: ").append(stripHtml(vuln.getDetails())).append("\n");
            }
            if (vuln.getOverall() != null) {
                prompt.append("  Risk Level: ").append(vuln.getOverallStr()).append("\n");
            }
            prompt.append("\n");
        }

        return prompt.toString();
    }

    /**
     * Call OpenAI API
     */
    private String callOpenAI(LLMConfig config, String prompt) throws Exception {
        String baseUrl = config.getBaseUrl();
        if (baseUrl == null || baseUrl.trim().isEmpty()) {
            baseUrl = "https://api.openai.com/v1";
        }
        
        URL url = new URL(baseUrl + "/chat/completions");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        
        try {
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", "Bearer " + config.getApiKey());
            connection.setDoOutput(true);
            connection.setConnectTimeout(TIMEOUT_MS);
            connection.setReadTimeout(TIMEOUT_MS);

            // Build request body
            String requestBody = String.format(
                "{\"model\": \"%s\", \"messages\": [{\"role\": \"user\", \"content\": %s}], \"max_tokens\": 1500}",
                config.getModel(), 
                objectMapper.writeValueAsString(prompt)
            );

            try (OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream(), StandardCharsets.UTF_8)) {
                writer.write(requestBody);
                writer.flush();
            }

            return handleResponse(connection);
            
        } finally {
            connection.disconnect();
        }
    }

    /**
     * Call Azure OpenAI API
     */
    private String callAzureOpenAI(LLMConfig config, String prompt) throws Exception {
        String endpoint = config.getEndpoint();
        if (!endpoint.endsWith("/")) {
            endpoint += "/";
        }
        
        URL url = new URL(endpoint + "openai/deployments/" + config.getDeployment() + "/chat/completions?api-version=2024-02-15-preview");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        
        try {
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("api-key", config.getApiKey());
            connection.setDoOutput(true);
            connection.setConnectTimeout(TIMEOUT_MS);
            connection.setReadTimeout(TIMEOUT_MS);

            // Build request body
            String requestBody = String.format(
                "{\"messages\": [{\"role\": \"user\", \"content\": %s}], \"max_tokens\": 1500}",
                objectMapper.writeValueAsString(prompt)
            );

            try (OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream(), StandardCharsets.UTF_8)) {
                writer.write(requestBody);
                writer.flush();
            }

            return handleResponse(connection);
            
        } finally {
            connection.disconnect();
        }
    }

    /**
     * Call Claude API
     */
    private String callClaude(LLMConfig config, String prompt) throws Exception {
        URL url = new URL("https://api.anthropic.com/v1/messages");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        
        try {
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("x-api-key", FSUtils.decryptPassword(config.getApiKey()));
            connection.setRequestProperty("anthropic-version", "2023-06-01");
            connection.setDoOutput(true);
            connection.setConnectTimeout(TIMEOUT_MS);
            connection.setReadTimeout(TIMEOUT_MS);

            // Build request body
            String requestBody = String.format(
                "{\"model\": \"%s\", \"max_tokens\": 1500, \"messages\": [{\"role\": \"user\", \"content\": %s}]}",
                config.getModel(),
                objectMapper.writeValueAsString(prompt)
            );

            try (OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream(), StandardCharsets.UTF_8)) {
                writer.write(requestBody);
                writer.flush();
            }

            return handleClaudeResponse(connection);
            
        } finally {
            connection.disconnect();
        }
    }

    /**
     * Call AWS Bedrock API (placeholder - requires AWS SDK integration)
     */
    private String callAWSBedrock(LLMConfig config, String prompt) throws Exception {
        // This would require AWS SDK integration for Bedrock
        // For now, return a placeholder indicating this needs implementation
        throw new UnsupportedOperationException("AWS Bedrock integration not yet implemented. Please configure OpenAI, Azure OpenAI, or Claude instead.");
    }

    /**
     * Handle standard OpenAI/Azure response format
     */
    private String handleResponse(HttpURLConnection connection) throws Exception {
        int responseCode = connection.getResponseCode();
        
        BufferedReader reader;
        if (responseCode >= 200 && responseCode < 300) {
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
        } else {
            reader = new BufferedReader(new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8));
        }

        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();

        if (responseCode >= 200 && responseCode < 300) {
            // Parse response and extract content
            JsonNode jsonResponse = objectMapper.readTree(response.toString());
            JsonNode choices = jsonResponse.get("choices");
            if (choices != null && choices.isArray() && choices.size() > 0) {
                JsonNode firstChoice = choices.get(0);
                JsonNode message = firstChoice.get("message");
                if (message != null) {
                    JsonNode content = message.get("content");
                    if (content != null) {
                        return content.asText();
                    }
                }
            }
            throw new Exception("Unexpected response format from LLM API");
        } else {
            throw new Exception("LLM API call failed with status " + responseCode + ": " + response.toString());
        }
    }

    /**
     * Handle Claude response format
     */
    private String handleClaudeResponse(HttpURLConnection connection) throws Exception {
        int responseCode = connection.getResponseCode();
        
        BufferedReader reader;
        if (responseCode >= 200 && responseCode < 300) {
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
        } else {
            reader = new BufferedReader(new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8));
        }

        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();

        if (responseCode >= 200 && responseCode < 300) {
            // Parse Claude response format
            JsonNode jsonResponse = objectMapper.readTree(response.toString());
            JsonNode content = jsonResponse.get("content");
            if (content != null && content.isArray() && content.size() > 0) {
                JsonNode firstContent = content.get(0);
                JsonNode text = firstContent.get("text");
                if (text != null) {
                    return text.asText();
                }
            }
            throw new Exception("Unexpected response format from Claude API");
        } else {
            throw new Exception("Claude API call failed with status " + responseCode + ": " + response.toString());
        }
    }

    /**
     * Strip HTML tags from text
     */
    private String stripHtml(String html) {
        if (html == null) return "";
        return html.replaceAll("<[^>]*>", "").trim();
    }
}