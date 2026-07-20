package com.fuse.actions.dashboard;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.Result;

import com.fuse.actions.FSActionSupport;
import com.fuse.dao.Assessment;
import com.fuse.dao.AssessmentType;
import com.fuse.dao.Campaign;
import com.fuse.dao.CustomField;
import com.fuse.dao.CustomType;
import com.fuse.dao.RiskLevel;
import com.fuse.dao.Status;
import com.fuse.dao.Teams;
import com.fuse.dao.User;
import com.fuse.dao.Vulnerability;

@Namespace("/portal")
public class ManagerDashboardVulnerabilitiesCSV extends FSActionSupport {

    // Properties for CSV download
    private InputStream inputStream;
    private String filename;

    // Search parameters (same as ManagerDashboard)
    private Date startDate;
    private Date endDate;
    private Long typeId;
    private Long teamId;
    private String status;
    private Long assessorId;
    private Long campaignId;
    private String searchAction = "search"; // Always search mode for CSV

    // Lists for dropdowns
    private List<AssessmentType> assessmentTypes;
    private List<Teams> teams;
    private List<RiskLevel> riskLevels;
    private List<Status> statuses;
    private List<User> assessors;
    private List<Campaign> campaigns;
    private List<CustomType> assessmentCustomTypes;
    private List<CustomType> vulnerabilityCustomTypes;

    @Action(value = "ManagerDashboardExportVulnerabilitiesCSV", results = @Result(name = "success", type = "stream", params = {
            "contentType", "text/csv",
            "inputName", "inputStream",
            "contentDisposition", "attachment;filename=\"${filename}\"" }))
    public String exportCSV() {
        // Check if user has manager role
        if (!this.isAcmanager()) {
            return LOGIN;
        }

        // Load dropdown data needed for search
        loadDropdownData();

        // Perform the assessment search using the same logic as ManagerDashboard
        List<Assessment> searchResults = performAssessmentSearch();

        // Generate CSV content
        StringBuilder csvContent = new StringBuilder();

        // Create header
        StringBuilder header = new StringBuilder();
        header.append("Vulnerability ID,Vulnerability Name,Assessment ID,Assessment Name,Assessment AppId,");
        header.append("Assessment Type");

        // Add assessment custom field columns to header (non-richtext)
        for (CustomType customType : assessmentCustomTypes) {
            if (customType.getKey() != null && !customType.getKey().trim().isEmpty()) {
                header.append(",").append(escapeCSV(customType.getKey()));
            }
        }

        header.append(",Team,Assessor,Severity,CVSS Score,Category,");
        header.append("Opened Date,Closed Date,Closed Dev Date,Closed Staging Date,");
        header.append("Closed In Dev,Closed In Staging,Closed In Prod,Status,Tracking ID");

        // Add vulnerability custom field columns to header
        for (CustomType customType : vulnerabilityCustomTypes) {
            if (customType.getKey() != null && !customType.getKey().trim().isEmpty()) {
                header.append(",").append(escapeCSV(customType.getKey()));
            }
        }

        header.append("\n");
        csvContent.append(header);

        // Add data rows - iterate through assessments and their vulnerabilities
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        int vulnCount = 0;
        
        for (Assessment asmt : searchResults) {
            // Get vulnerabilities for this assessment that were opened in the date range
            List<Vulnerability> vulns = getVulnerabilitiesInDateRange(asmt.getId());
            
            for (Vulnerability vuln : vulns) {
                vulnCount++;
                
                // Vulnerability ID and Name
                csvContent.append(escapeCSV(String.valueOf(vuln.getId()))).append(",");
                csvContent.append(escapeCSV(vuln.getName())).append(",");

                // Assessment info
                csvContent.append(escapeCSV(String.valueOf(asmt.getId()))).append(",");
                csvContent.append(escapeCSV(asmt.getName())).append(",");
                csvContent.append(escapeCSV(asmt.getAppId())).append(",");
                csvContent.append(escapeCSV(asmt.getType() != null ? asmt.getType().getType() : "")).append(",");

                // Add assessment custom field values (non-richtext)
                for (CustomType customType : assessmentCustomTypes) {
                    String customFieldValue = getPopulatedCustomFieldValue(asmt.getCustomFields(), customType);
                    csvContent.append(escapeCSV(customFieldValue)).append(",");
                }

                // Get team name from first assessor
                String teamName = "";
                if (asmt.getAssessor() != null && !asmt.getAssessor().isEmpty()) {
                    User firstAssessor = asmt.getAssessor().get(0);
                    if (firstAssessor.getTeam() != null) {
                        teamName = firstAssessor.getTeam().getTeamName();
                    }
                }
                csvContent.append(escapeCSV(teamName)).append(",");

                // Get assessor names
                StringBuilder assessorNames = new StringBuilder();
                if (asmt.getAssessor() != null) {
                    for (int i = 0; i < asmt.getAssessor().size(); i++) {
                        if (i > 0)
                            assessorNames.append(", ");
                        User assessor = asmt.getAssessor().get(i);
                        assessorNames.append(assessor.getFname()).append(" ").append(assessor.getLname());
                    }
                }
                csvContent.append(escapeCSV(assessorNames.toString())).append(",");

                // Severity
                String severityName = getRiskLevelName(vuln.getOverall());
                csvContent.append(escapeCSV(severityName)).append(",");
                
                // CVSS Score
                csvContent.append(escapeCSV(vuln.getCvssScore() != null ? vuln.getCvssScore() : "")).append(",");
                
                // Category
                csvContent.append(escapeCSV(vuln.getCategory() != null ? vuln.getCategory().getName() : "")).append(",");
                
                // Dates
                csvContent.append(vuln.getOpened() != null ? dateFormat.format(vuln.getOpened()) : "").append(",");
                csvContent.append(vuln.getClosed() != null ? dateFormat.format(vuln.getClosed()) : "").append(",");
                csvContent.append(vuln.getDevClosed() != null ? dateFormat.format(vuln.getDevClosed()) : "").append(",");
                csvContent.append(vuln.getStagingClosed() != null ? dateFormat.format(vuln.getStagingClosed()) : "").append(",");

                // Closed-per-environment booleans
                csvContent.append(vuln.getDevClosed() != null ? "true" : "false").append(",");
                csvContent.append(vuln.getStagingClosed() != null ? "true" : "false").append(",");
                csvContent.append(vuln.getClosed() != null ? "true" : "false").append(",");

                // Status
                String vulnStatus = vuln.getDisplayStatus();
                csvContent.append(escapeCSV(vulnStatus)).append(",");
                
                // Tracking ID
                csvContent.append(escapeCSV(vuln.getTracking() != null ? vuln.getTracking() : ""));

                // Add vulnerability custom field values
                for (CustomType customType : vulnerabilityCustomTypes) {
                    String customFieldValue = getPopulatedCustomFieldValue(vuln.getCustomFields(), customType);
                    csvContent.append(",").append(escapeCSV(customFieldValue));
                }
                
                csvContent.append("\n");
            }
        }

        // Set up the download
        inputStream = new ByteArrayInputStream(csvContent.toString().getBytes());
        SimpleDateFormat filenameFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
        filename = "manager_dashboard_vulnerabilities_" + filenameFormat.format(new Date()) + ".csv";

        return SUCCESS;
    }

    private List<Vulnerability> getVulnerabilitiesInDateRange(Long assessmentId) {
        // Get all vulnerabilities for this assessment
        List<Vulnerability> allVulns = em.createQuery(
                "from Vulnerability where assessmentId = :aid", Vulnerability.class)
                .setParameter("aid", assessmentId)
                .getResultList();
        
        // Filter by opened date if date range is specified
        if (startDate != null && endDate != null) {
            Date adjustedEndDate = new Date(endDate.getTime());
            adjustedEndDate.setDate(adjustedEndDate.getDate() + 1); // Include end date
            
            return allVulns.stream()
                    .filter(v -> v.getOpened() != null)
                    .filter(v -> !v.getOpened().before(startDate) && v.getOpened().before(adjustedEndDate))
                    .collect(Collectors.toList());
        }
        
        // If no date range, return vulnerabilities that have an opened date
        return allVulns.stream()
                .filter(v -> v.getOpened() != null)
                .collect(Collectors.toList());
    }

    /**
     * Returns the value actually populated on the entity for the given custom type,
     * or an empty string. The custom type's configured default is never emitted: a
     * field whose stored value still equals the type default is treated as not
     * populated. This also covers unselected dropdowns, whose stored value is the
     * full comma-separated option list held in the type's defaultValue.
     */
    private String getPopulatedCustomFieldValue(List<CustomField> customFields, CustomType customType) {
        if (customFields == null) {
            return "";
        }
        for (CustomField cf : customFields) {
            if (cf.getType() != null && cf.getType().getId().equals(customType.getId())) {
                String value = cf.getValue();
                if (value == null || value.isEmpty()) {
                    return "";
                }
                String defaultValue = customType.getDefaultValue();
                if (defaultValue != null && value.equals(defaultValue)) {
                    return "";
                }
                return value;
            }
        }
        return "";
    }

    private String escapeCSV(String value) {
        if (value == null)
            return "";

        // Escape quotes by doubling them and wrap in quotes if contains comma, quote,
        // or newline
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            value = "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private void loadDropdownData() {
        assessmentTypes = em.createQuery("from AssessmentType order by type").getResultList();
        teams = em.createQuery("from Teams order by TeamName").getResultList();
        riskLevels = em.createQuery("from RiskLevel order by riskId desc").getResultList();
        statuses = em.createQuery("from Status order by name").getResultList();
        // Get all users with assessor permission
        assessors = em.createQuery("from User order by lname, fname", User.class)
                .getResultList()
                .stream()
                .filter(a -> a.getPermissions() != null && a.getPermissions().isAssessor())
                .collect(Collectors.toList());
        // Get all campaigns
        campaigns = em.createQuery("from Campaign order by name").getResultList();
        // Get all assessment custom types (type 0 = ASMT, fieldType < 3 excludes
        // richtext fields that only exist in Enterprise versions)
        assessmentCustomTypes = em.createQuery(
                "from CustomType where type = 0 and fieldType < 3 and deleted = false order by key",
                CustomType.class)
                .getResultList();
        // Get all vulnerability custom types (type 1 = VULN, fieldType < 3 excludes forms)
        vulnerabilityCustomTypes = em.createQuery(
                "from CustomType where type = 1 and fieldType < 3 and deleted = false order by key",
                CustomType.class)
                .getResultList();
    }

    private List<Assessment> performAssessmentSearch() {
        // Build MongoDB query for search mode (copied from ManagerDashboard)
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        StringBuilder query = new StringBuilder("{");
        boolean hasConditions = false;

        // Build date range condition - include assessments that overlap with the search
        // range
        if (startDate != null && endDate != null) {
            endDate.setDate(endDate.getDate() + 1);
            query.append(AssessmentSearchQuery.dateOverlapCondition(startDate, endDate, sdf));
            hasConditions = true;
        }

        // Build assessment type condition
        if (typeId != null && typeId > 0) {
            if (hasConditions)
                query.append(", ");
            query.append("\"type_id\": ").append(typeId);
            hasConditions = true;
        }

        // Build team condition - need to check if any assessor belongs to the team
        if (teamId != null && teamId > 0) {
            // First, get the team entity
            Teams team = em.find(Teams.class, teamId);

            if (team != null) {
                // Use a native MongoDB query to find users with this team
                String userQuery = "{\"team_id\": " + teamId + "}";
                List<User> teamUsers = em.createNativeQuery(userQuery, User.class).getResultList();

                if (!teamUsers.isEmpty()) {
                    if (hasConditions)
                        query.append(", ");
                    query.append("\"assessor\": {$in: [");
                    boolean first = true;
                    for (User user : teamUsers) {
                        if (!first)
                            query.append(", ");
                        query.append(user.getId());
                        first = false;
                    }
                    query.append("]}");
                    hasConditions = true;
                }
            }
        }

        // Build assessor condition
        if (assessorId != null && assessorId > 0) {
            if (hasConditions)
                query.append(", ");
            query.append("\"assessor\": ").append(assessorId);
            hasConditions = true;
        }

        // Build campaign condition
        if (campaignId != null && campaignId > 0) {
            if (hasConditions)
                query.append(", ");
            query.append("\"campaign_id\": ").append(campaignId);
            hasConditions = true;
        }

        query.append("}");
        // Execute the native MongoDB query
        List<Assessment> mongoResults = em.createNativeQuery(query.toString(), Assessment.class).getResultList();

        // Filter Results based on status
        List<Assessment> searchResults;
        if (status != null && !status.isEmpty() && !status.equals("0")) {
            // Find the status name by ID
            String statusName = null;
            for (Status s : statuses) {
                if (s.getId().toString().equals(status)) {
                    statusName = s.getName();
                    break;
                }
            }

            if (statusName != null) {
                final String finalStatusName = statusName;
                searchResults = mongoResults.stream()
                        .filter(a -> finalStatusName.equals(a.getStatus()))
                        .collect(Collectors.toList());
            } else {
                searchResults = mongoResults;
            }
        } else {
            searchResults = mongoResults;
        }

        // Sort by start date descending
        searchResults.sort((a1, a2) -> {
            if (a1.getStart() == null && a2.getStart() == null)
                return 0;
            if (a1.getStart() == null)
                return 1;
            if (a2.getStart() == null)
                return -1;
            return a2.getStart().compareTo(a1.getStart());
        });

        return searchResults;
    }

    private String getRiskLevelName(Long riskId) {
        if (riskId == null)
            return "Unassigned";

        for (RiskLevel level : riskLevels) {
            if (riskId.equals(Long.valueOf(level.getRiskId()))) {
                return level.getRisk() != null ? level.getRisk() : "Unassigned";
            }
        }
        return "Unassigned";
    }

    // Getters and setters
    public InputStream getInputStream() {
        return inputStream;
    }

    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Long getTypeId() {
        return typeId;
    }

    public void setTypeId(Long typeId) {
        this.typeId = typeId;
    }

    public Long getTeamId() {
        return teamId;
    }

    public void setTeamId(Long teamId) {
        this.teamId = teamId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getAssessorId() {
        return assessorId;
    }

    public void setAssessorId(Long assessorId) {
        this.assessorId = assessorId;
    }

    public Long getCampaignId() {
        return campaignId;
    }

    public void setCampaignId(Long campaignId) {
        this.campaignId = campaignId;
    }

    public String getSearchAction() {
        return searchAction;
    }

    public void setSearchAction(String searchAction) {
        this.searchAction = searchAction;
    }
}
