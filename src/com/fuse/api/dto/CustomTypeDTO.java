package com.fuse.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fuse.dao.CustomType;

/**
 * DTO for CustomType API responses - defines allowed custom fields
 */
public class CustomTypeDTO {
    
    @JsonProperty("Id")
    private Long id;
    
    @JsonProperty("Key")
    private String key;
    
    @JsonProperty("Variable")
    private String variable;
    
    @JsonProperty("Type")
    private String type;
    
    @JsonProperty("FieldType")
    private String fieldType;
    
    @JsonProperty("DefaultValue")
    private String defaultValue;
    
    @JsonProperty("Readonly")
    private Boolean readonly;
    
    /**
     * Default constructor
     */
    public CustomTypeDTO() {
    }
    
    /**
     * Factory method to create DTO from entity
     */
    public static CustomTypeDTO fromEntity(CustomType type) {
        if (type == null) {
            return null;
        }
        
        CustomTypeDTO dto = new CustomTypeDTO();
        dto.setId(type.getId());
        dto.setKey(type.getKey());
        dto.setVariable(type.getVariable());
        dto.setType(type.getTypeStr()); // Assessment, Vulnerability, User, Retest
        dto.setFieldType(type.getFieldTypeStr()); // String, Boolean, List
        dto.setDefaultValue(type.getDefaultValue());
        dto.setReadonly(type.getReadonly());
        
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

    public String getVariable() {
        return variable;
    }

    public void setVariable(String variable) {
        this.variable = variable;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFieldType() {
        return fieldType;
    }

    public void setFieldType(String fieldType) {
        this.fieldType = fieldType;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public Boolean getReadonly() {
        return readonly;
    }

    public void setReadonly(Boolean readonly) {
        this.readonly = readonly;
    }
}