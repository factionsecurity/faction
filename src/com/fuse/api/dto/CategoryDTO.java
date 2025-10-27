package com.fuse.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fuse.dao.Category;

/**
 * DTO for Category entity
 */
public class CategoryDTO {
    
    @JsonProperty("Id")
    private Long id;
    
    @JsonProperty("Name")
    private String name;
    
    public CategoryDTO() {}
    
    /**
     * Create DTO from Category entity
     */
    public static CategoryDTO fromEntity(Category entity) {
        if (entity == null) {
            return null;
        }
        
        CategoryDTO dto = new CategoryDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        
        return dto;
    }
    
    /**
     * Convert DTO to Category entity
     */
    public Category toEntity() {
        Category entity = new Category();
        if (this.id != null) {
            entity.setId(this.id);
        }
        entity.setName(this.name);
        
        return entity;
    }
    
    // Getters and setters
    
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
    }
}