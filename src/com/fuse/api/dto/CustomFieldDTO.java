package com.fuse.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fuse.dao.CustomField;

/**
 * DTO for CustomField API responses
 */
public class CustomFieldDTO {
    
    @JsonProperty("Id")
    private Long id;
    
    @JsonProperty("Key")
    private String key;
    
    @JsonProperty("Value")
    private String value;
    
    @JsonProperty("Type")
    private String type;
    
    @JsonProperty("Variable")
    private String variable;
    
    /**
     * Default constructor
     */
    public CustomFieldDTO() {
    }
    
    /**
     * Factory method to create DTO from entity
     */
    public static CustomFieldDTO fromEntity(CustomField field) {
        if (field == null) {
            return null;
        }
        
        CustomFieldDTO dto = new CustomFieldDTO();
        dto.setId(field.getId());
        dto.setValue(field.getValue());
        
        if (field.getType() != null) {
            dto.setKey(field.getType().getKey());
            dto.setType(field.getType().getTypeStr());
            dto.setVariable(field.getType().getVariable());
        }
        
        return dto;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getVariable() {
        return variable;
    }

    public void setVariable(String variable) {
        this.variable = variable;
    }
}