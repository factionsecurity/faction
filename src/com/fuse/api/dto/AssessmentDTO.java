package com.fuse.api.dto;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fuse.dao.Assessment;
import com.fuse.dao.CustomField;
import com.fuse.dao.User;

/**
 * DTO for Assessment API responses with backward-compatible field names
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AssessmentDTO {
    
    @JsonProperty("Id")
    private Long id;
    
    @JsonProperty("AppId")
    private String appId;
    
    @JsonProperty("Name")
    private String name;
    
    @JsonProperty("Type")
    private String type;
    
    @JsonProperty("TypeId")
    private Long typeId;
    
    @JsonProperty("Campaign")
    private String campaign;
    
    @JsonProperty("CampaignId")
    private Long campaignId;
    
    @JsonProperty("Start")
    private String start;
    
    @JsonProperty("End")
    private String end;
    
    @JsonProperty("Completed")
    private Date completed;
    
    @JsonProperty("Status")
    private String status;
    
    @JsonProperty("Notes")
    private String notes;
    
    @JsonProperty("AccessNotes")
    private String accessNotes;
    
    @JsonProperty("Summary")
    private String summary;
    
    @JsonProperty("DistributionList")
    private String distributionList;
    
    @JsonProperty("Assessors")
    private List<UserDTO> assessors;
    
    @JsonProperty("EngagementContact")
    private UserDTO engagementContact;
    
    @JsonProperty("RemediationContact")
    private UserDTO remediationContact;
    
    @JsonProperty("CustomFields")
    private List<CustomFieldDTO> customFields = new ArrayList<>();
    
    @JsonProperty("VulnerabilityCount")
    private Integer vulnerabilityCount;
    
    /**
     * Inner class for simplified user representation
     */
    public static class UserDTO {
        @JsonProperty("Id")
        private Long id;
        
        @JsonProperty("Username")
        private String username;
        
        @JsonProperty("FirstName")
        private String firstName;
        
        @JsonProperty("LastName")
        private String lastName;
        
        @JsonProperty("Email")
        private String email;
        
        public UserDTO() {}
        
        public UserDTO(User user) {
            if (user != null) {
                this.id = user.getId();
                this.username = user.getUsername();
                this.firstName = user.getFname();
                this.lastName = user.getLname();
                this.email = user.getEmail();
            }
        }
        
        // Getters and Setters
        public Long getId() {
            return id;
        }
        
        public void setId(Long id) {
            this.id = id;
        }
        
        public String getUsername() {
            return username;
        }
        
        public void setUsername(String username) {
            this.username = username;
        }
        
        public String getFirstName() {
            return firstName;
        }
        
        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }
        
        public String getLastName() {
            return lastName;
        }
        
        public void setLastName(String lastName) {
            this.lastName = lastName;
        }
        
        public String getEmail() {
            return email;
        }
        
        public void setEmail(String email) {
            this.email = email;
        }
    }
    
    /**
     * Default constructor
     */
    public AssessmentDTO() {
    }
    
    /**
     * Factory method to create DTO from entity
     */
    public static AssessmentDTO fromEntity(Assessment assessment) {
        if (assessment == null) {
            return null;
        }
        
        AssessmentDTO dto = new AssessmentDTO();
        dto.setId(assessment.getId());
        dto.setAppId(assessment.getAppId());
        dto.setName(assessment.getName());
        
        if (assessment.getType() != null) {
            dto.setType(assessment.getType().getType());
            dto.setTypeId(assessment.getType().getId());
        }
        
        if (assessment.getCampaign() != null) {
            dto.setCampaign(assessment.getCampaign().getName());
            dto.setCampaignId(assessment.getCampaign().getId());
        }
        
        dto.setStart(""+assessment.getStart().getTime());
        dto.setEnd(""+assessment.getEnd().getTime());
        dto.setCompleted(assessment.getCompleted());
        dto.setStatus(assessment.getStatus());
        if(assessment.getNotebook() == null || assessment.getNotebook().size() == 0) {
        	dto.setNotes("");
        }else {
        	dto.setNotes(assessment.getNotebook().get(0).getNote());
        }
        dto.setAccessNotes(assessment.getAccessNotes());
        dto.setSummary(assessment.getSummary());
        dto.setDistributionList(assessment.getDistributionList());
        
        // Convert assessors
        if (assessment.getAssessor() != null) {
            List<UserDTO> assessorDTOs = new ArrayList<>();
            for (User assessor : assessment.getAssessor()) {
                assessorDTOs.add(new UserDTO(assessor));
            }
            dto.setAssessors(assessorDTOs);
        }
        
        // Convert engagement contact
        dto.setEngagementContact(new UserDTO(assessment.getEngagement()));
        
        // Convert remediation contact
        dto.setRemediationContact(new UserDTO(assessment.getRemediation()));
        
        // Set vulnerability count
        if (assessment.getVulns() != null) {
            dto.setVulnerabilityCount(assessment.getVulns().size());
        }
        
        return dto;
    }
    
    /**
     * Add custom fields from entity
     */
    public void setCustomFieldsFromEntity(List<CustomField> fields) {
        if (fields != null) {
            customFields = new ArrayList<>();
            for (CustomField field : fields) {
                CustomFieldDTO cfDto = CustomFieldDTO.fromEntity(field);
                if (cfDto != null) {
                    customFields.add(cfDto);
                }
            }
        }
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

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

    public Long getTypeId() {
        return typeId;
    }

    public void setTypeId(Long typeId) {
        this.typeId = typeId;
    }

    public String getCampaign() {
        return campaign;
    }

    public void setCampaign(String campaign) {
        this.campaign = campaign;
    }

    public Long getCampaignId() {
        return campaignId;
    }

    public void setCampaignId(Long campaignId) {
        this.campaignId = campaignId;
    }

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
        this.end = end;
    }

    public Date getCompleted() {
        return completed;
    }

    public void setCompleted(Date completed) {
        this.completed = completed;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
    public String getAccessNotes() {
        return accessNotes;
    }

    public void setAccessNotes(String accessNotes) {
        this.accessNotes = accessNotes;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getDistributionList() {
        return distributionList;
    }

    public void setDistributionList(String distributionList) {
        this.distributionList = distributionList;
    }

    public List<UserDTO> getAssessors() {
        return assessors;
    }

    public void setAssessors(List<UserDTO> assessors) {
        this.assessors = assessors;
    }

    public UserDTO getEngagementContact() {
        return engagementContact;
    }

    public void setEngagementContact(UserDTO engagementContact) {
        this.engagementContact = engagementContact;
    }

    public UserDTO getRemediationContact() {
        return remediationContact;
    }

    public void setRemediationContact(UserDTO remediationContact) {
        this.remediationContact = remediationContact;
    }

    public List<CustomFieldDTO> getCustomFields() {
        return customFields;
    }

    public void setCustomFields(List<CustomFieldDTO> customFields) {
        this.customFields = customFields;
    }

    public Integer getVulnerabilityCount() {
        return vulnerabilityCount;
    }

    public void setVulnerabilityCount(Integer vulnerabilityCount) {
        this.vulnerabilityCount = vulnerabilityCount;
    }
}