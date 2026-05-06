package com.fuse.api.dto;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class AssessmentStatsDTO {

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

    @JsonProperty("Assessors")
    private List<AssessmentDTO.UserDTO> assessors;

    @JsonProperty("EngagementContact")
    private AssessmentDTO.UserDTO engagementContact;

    @JsonProperty("RemediationContact")
    private AssessmentDTO.UserDTO remediationContact;

    @JsonProperty("Vulnerabilities")
    private List<VulnerabilityStatsDTO> vulnerabilities;

    @JsonProperty("RiskSummary")
    private Map<String, Integer> riskSummary;

    public AssessmentStatsDTO() {
    }

    public static AssessmentStatsDTO fromEntity(com.fuse.dao.Assessment asmt, List<VulnerabilityStatsDTO> vulns) {
        if (asmt == null) {
            return null;
        }

        AssessmentStatsDTO dto = new AssessmentStatsDTO();
        dto.setId(asmt.getId());
        dto.setAppId(asmt.getAppId());
        dto.setName(asmt.getName());

        if (asmt.getType() != null) {
            dto.setType(asmt.getType().getType());
            dto.setTypeId(asmt.getType().getId());
        }

        if (asmt.getCampaign() != null) {
            dto.setCampaign(asmt.getCampaign().getName());
            dto.setCampaignId(asmt.getCampaign().getId());
        }

        dto.setStart(String.valueOf(asmt.getStart().getTime()));
        dto.setEnd(String.valueOf(asmt.getEnd().getTime()));
        dto.setCompleted(asmt.getCompleted());
        dto.setStatus(asmt.getStatus());

        if (asmt.getAssessor() != null) {
            List<AssessmentDTO.UserDTO> assessorDTOs = new ArrayList<>();
            for (com.fuse.dao.User u : asmt.getAssessor()) {
                assessorDTOs.add(new AssessmentDTO.UserDTO(u));
            }
            dto.setAssessors(assessorDTOs);
        }

        dto.setEngagementContact(new AssessmentDTO.UserDTO(asmt.getEngagement()));
        dto.setRemediationContact(new AssessmentDTO.UserDTO(asmt.getRemediation()));

        if (vulns != null && !vulns.isEmpty()) {
            dto.setVulnerabilities(vulns);
            dto.setRiskSummary(calculateRiskSummary(vulns));
        }

        return dto;
    }

    private static Map<String, Integer> calculateRiskSummary(List<VulnerabilityStatsDTO> vulns) {
        Map<String, Integer> summary = new HashMap<>();
        for (VulnerabilityStatsDTO v : vulns) {
            String severity = v.getOverallStr();
            if (severity != null && !severity.isEmpty()) {
                summary.merge(severity, 1, Integer::sum);
            }
        }
        return summary;
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
    public List<AssessmentDTO.UserDTO> getAssessors() { return assessors; }
    public void setAssessors(List<AssessmentDTO.UserDTO> assessors) { this.assessors = assessors; }
    public AssessmentDTO.UserDTO getEngagementContact() { return engagementContact; }
    public void setEngagementContact(AssessmentDTO.UserDTO engagementContact) { this.engagementContact = engagementContact; }
    public AssessmentDTO.UserDTO getRemediationContact() { return remediationContact; }
    public void setRemediationContact(AssessmentDTO.UserDTO remediationContact) { this.remediationContact = remediationContact; }
    public List<VulnerabilityStatsDTO> getVulnerabilities() { return vulnerabilities; }
    public void setVulnerabilities(List<VulnerabilityStatsDTO> vulnerabilities) { this.vulnerabilities = vulnerabilities; }
    public Map<String, Integer> getRiskSummary() { return riskSummary; }
    public void setRiskSummary(Map<String, Integer> riskSummary) { this.riskSummary = riskSummary; }
}
