package com.fuse.dao;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.TableGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import com.fuse.utils.FSUtils;

@Entity
public class LLMConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "llmConfigGen")
    @TableGenerator(name = "llmConfigGen", table = "llmConfigGenseq", pkColumnValue = "llmConfig", valueColumnName = "nextLLMConfig", initialValue = 1, allocationSize = 1)
    private Long id;

    private String name;
    private String provider; // OPENAI, AZURE_OPENAI, AWS_BEDROCK, CLAUDE
    private String apiKey; // encrypted
    private String baseUrl;
    private String endpoint;
    private String deployment;
    private String accessKey; // encrypted (for AWS)
    private String secretKey; // encrypted (for AWS)
    private String region;
    private String model;
    private String apiVersion;
    private Boolean active = true;

    @Temporal(TemporalType.TIMESTAMP)
    private Date createdDate;

    @Temporal(TemporalType.TIMESTAMP)
    private Date modifiedDate;

    // Constructors
    public LLMConfig() {
        this.createdDate = new Date();
        this.modifiedDate = new Date();
    }

    public LLMConfig(String name, String provider) {
        this();
        this.name = name;
        this.provider = provider;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        this.modifiedDate = new Date();
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
        this.modifiedDate = new Date();
    }

    // Encrypted API Key methods
    public String getApiKey() {
        return apiKey == null ? "" : FSUtils.decryptPassword(apiKey);
    }

    public void setApiKey(String apiKey) {
        if (apiKey != null && !apiKey.trim().isEmpty()) {
            this.apiKey = FSUtils.encryptPassword(apiKey.trim());
        }
        this.modifiedDate = new Date();
    }

    @Transient
    public String getEncryptedApiKey() {
        return apiKey;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
        this.modifiedDate = new Date();
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
        this.modifiedDate = new Date();
    }

    public String getDeployment() {
        return deployment;
    }

    public void setDeployment(String deployment) {
        this.deployment = deployment;
        this.modifiedDate = new Date();
    }

    // Encrypted Access Key methods (for AWS)
    public String getAccessKey() {
        return accessKey == null ? "" : FSUtils.decryptPassword(accessKey);
    }

    public void setAccessKey(String accessKey) {
        if (accessKey != null && !accessKey.trim().isEmpty()) {
            this.accessKey = FSUtils.encryptPassword(accessKey.trim());
        }
        this.modifiedDate = new Date();
    }

    @Transient
    public String getEncryptedAccessKey() {
        return accessKey;
    }

    // Encrypted Secret Key methods (for AWS)
    public String getSecretKey() {
        return secretKey == null ? "" : FSUtils.decryptPassword(secretKey);
    }

    public void setSecretKey(String secretKey) {
        if (secretKey != null && !secretKey.trim().isEmpty()) {
            this.secretKey = FSUtils.encryptPassword(secretKey.trim());
        }
        this.modifiedDate = new Date();
    }

    @Transient
    public String getEncryptedSecretKey() {
        return secretKey;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
        this.modifiedDate = new Date();
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
        this.modifiedDate = new Date();
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
        this.modifiedDate = new Date();
    }

    public Boolean getActive() {
        return active == null ? true : active;
    }

    public void setActive(Boolean active) {
        this.active = active;
        this.modifiedDate = new Date();
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public Date getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(Date modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    // Provider validation method
    @Transient
    public boolean isValidProvider() {
        if (provider == null)
            return false;

        switch (provider.toUpperCase()) {
            case "OPENAI":
            case "AZURE_OPENAI":
            case "AWS_BEDROCK":
            case "CLAUDE":
                return true;
            default:
                return false;
        }
    }

    // Configuration validation methods
    @Transient
    public boolean isValidConfiguration() {
        if (name == null || name.trim().isEmpty() || !isValidProvider()) {
            return false;
        }

        switch (provider.toUpperCase()) {
            case "OPENAI":
                return getApiKey() != null && !getApiKey().trim().isEmpty();

            case "AZURE_OPENAI":
                return getApiKey() != null && !getApiKey().trim().isEmpty() &&
                        endpoint != null && !endpoint.trim().isEmpty() &&
                        deployment != null && !deployment.trim().isEmpty();

            case "AWS_BEDROCK":
                return getAccessKey() != null && !getAccessKey().trim().isEmpty() &&
                        getSecretKey() != null && !getSecretKey().trim().isEmpty() &&
                        region != null && !region.trim().isEmpty();

            case "CLAUDE":
                return getApiKey() != null && !getApiKey().trim().isEmpty();

            default:
                return false;
        }
    }

    // Display method for masked API keys (for UI)
    @Transient
    public String getMaskedApiKey() {
        String key = getApiKey();
        if (key == null || key.isEmpty()) {
            return "";
        }
        if (key.length() <= 8) {
            return "****";
        }
        return key.substring(0, 4) + "****" + key.substring(key.length() - 4);
    }

    @Transient
    public String getMaskedAccessKey() {
        String key = getAccessKey();
        if (key == null || key.isEmpty()) {
            return "";
        }
        if (key.length() <= 8) {
            return "****";
        }
        return key.substring(0, 4) + "****" + key.substring(key.length() - 4);
    }

    @Transient
    public String getMaskedSecretKey() {
        String key = getSecretKey();
        if (key == null || key.isEmpty()) {
            return "";
        }
        return "****";
    }

    @Override
    public String toString() {
        return "LLMConfig{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", provider='" + provider + '\'' +
                ", active=" + active +
                ", model='" + model + '\'' +
                '}';
    }
}