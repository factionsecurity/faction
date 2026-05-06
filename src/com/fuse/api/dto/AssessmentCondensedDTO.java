package com.fuse.api.dto;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fuse.dao.Assessment;
import com.fuse.dao.CustomField;
import com.fuse.dao.User;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class AssessmentCondensedDTO {

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

    @JsonProperty("Vulnerabilities")
    private List<VulnerabilityCondensedDTO> vulnerabilities;

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

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }
        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }

    public AssessmentCondensedDTO() {
    }

    public static AssessmentCondensedDTO fromEntity(Assessment assessment) {
        if (assessment == null) {
            return null;
        }

        AssessmentCondensedDTO dto = new AssessmentCondensedDTO();
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

        dto.setStart("" + assessment.getStart().getTime());
        dto.setEnd("" + assessment.getEnd().getTime());
        dto.setCompleted(assessment.getCompleted());
        dto.setStatus(assessment.getStatus());
        dto.setDistributionList(assessment.getDistributionList());

        if (assessment.getAssessor() != null) {
            List<UserDTO> assessorDTOs = new ArrayList<>();
            for (User assessor : assessment.getAssessor()) {
                assessorDTOs.add(new UserDTO(assessor));
            }
            dto.setAssessors(assessorDTOs);
        }

        dto.setEngagementContact(new UserDTO(assessment.getEngagement()));
        dto.setRemediationContact(new UserDTO(assessment.getRemediation()));

        if (assessment.getVulns() != null) {
            dto.setVulnerabilityCount(assessment.getVulns().size());
        }

        return dto;
    }

    public static AssessmentCondensedDTO fromEntityWithVulns(Assessment assessment) {
        if (assessment == null) {
            return null;
        }

        AssessmentCondensedDTO dto = fromEntity(assessment);

        if (assessment.getVulns() != null) {
            List<VulnerabilityCondensedDTO> vulnList = new ArrayList<>();
            for (com.fuse.dao.Vulnerability v : assessment.getVulns()) {
                v.updateRiskLevels();
                VulnerabilityCondensedDTO vDto = VulnerabilityCondensedDTO.fromEntity(v);
                if (v.getCustomFields() != null) {
                    vDto.setCustomFieldsFromEntity(v.getCustomFields());
                }
                vulnList.add(vDto);
            }
            dto.setVulnerabilities(vulnList);
        }

        if (assessment.getCustomFields() != null) {
            dto.customFields = new ArrayList<>();
            for (CustomField field : assessment.getCustomFields()) {
                CustomFieldDTO cfDto = CustomFieldDTO.fromEntity(field);
                if (cfDto != null) {
                    dto.customFields.add(cfDto);
                }
            }
        }

        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getAppId() { return appId; }
    public void setAppId(String appId) { this.appId = appId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public Long getTypeId() { return typeId; }
    public void setTypeId(Long typeId) { this.typeId = typeId; }
    public String getCampaign() { return campaign; }
    public void setCampaign(String campaign) { this.campaign = campaign; }
    public Long getCampaignId() { return campaignId; }
    public void setCampaignId(Long campaignId) { this.campaignId = campaignId; }
    public String getStart() { return start; }
    public void setStart(String start) { this.start = start; }
    public String getEnd() { return end; }
    public void setEnd(String end) { this.end = end; }
    public Date getCompleted() { return completed; }
    public void setCompleted(Date completed) { this.completed = completed; }
   public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDistributionList() { return distributionList; }
    public void setDistributionList(String distributionList) { this.distributionList = distributionList; }
    public List<UserDTO> getAssessors() { return assessors; }
    public void setAssessors(List<UserDTO> assessors) { this.assessors = assessors; }
    public UserDTO getEngagementContact() { return engagementContact; }
    public void setEngagementContact(UserDTO engagementContact) { this.engagementContact = engagementContact; }
    public UserDTO getRemediationContact() { return remediationContact; }
    public void setRemediationContact(UserDTO remediationContact) { this.remediationContact = remediationContact; }
    public List<CustomFieldDTO> getCustomFields() { return customFields; }
    public void setCustomFields(List<CustomFieldDTO> customFields) { this.customFields = customFields; }
    public Integer getVulnerabilityCount() { return vulnerabilityCount; }
    public void setVulnerabilityCount(Integer vulnerabilityCount) { this.vulnerabilityCount = vulnerabilityCount; }
    public List<VulnerabilityCondensedDTO> getVulnerabilities() { return vulnerabilities; }
    public void setVulnerabilities(List<VulnerabilityCondensedDTO> vulnerabilities) { this.vulnerabilities = vulnerabilities; }
}
