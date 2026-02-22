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
import com.fuse.dao.RiskLevel;
import com.fuse.dao.Status;
import com.fuse.dao.Teams;
import com.fuse.dao.User;
import com.fuse.dao.Vulnerability;

@Namespace("/portal")
public class ManagerDashboardCSV extends FSActionSupport {

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

    @Action(value = "ManagerDashboardExportCSV", results = @Result(name = "success", type = "stream", params = {
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

        // Create header with individual vulnerability severity columns
        StringBuilder header = new StringBuilder();
        header.append("AppId,Name,Type,Team,Assessor,Start Date,End Date,Completed Date,Status");

        // Add severity level columns to header
        for (RiskLevel level : riskLevels) {
            if (level.getRisk() != null && !level.getRisk().trim().isEmpty()) {
                header.append(",").append(level.getRisk());
            }
        }
        header.append("\n");
        csvContent.append(header);

        // Add data rows
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        for (Assessment asmt : searchResults) {
            csvContent.append(escapeCSV(asmt.getAppId())).append(",");
            csvContent.append(escapeCSV(asmt.getName())).append(",");
            csvContent.append(escapeCSV(asmt.getType() != null ? asmt.getType().getType() : "")).append(",");

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

            // Add dates
            csvContent.append(asmt.getStart() != null ? dateFormat.format(asmt.getStart()) : "").append(",");
            csvContent.append(asmt.getEnd() != null ? dateFormat.format(asmt.getEnd()) : "").append(",");
            csvContent.append(asmt.getCompleted() != null ? dateFormat.format(asmt.getCompleted()) : "").append(",");
            csvContent.append(escapeCSV(asmt.getStatus()));

            // Get vulnerability findings count
            List<Vulnerability> vulns = em.createQuery(
                    "from Vulnerability where assessmentId = :aid", Vulnerability.class)
                    .setParameter("aid", asmt.getId())
                    .getResultList();

            // Count by severity
            Map<String, Integer> findings = new LinkedHashMap<>();
            for (RiskLevel level : riskLevels) {
                if (level.getRisk() != null && !level.getRisk().trim().isEmpty()) {
                    findings.put(level.getRisk(), 0);
                }
            }

            for (Vulnerability vuln : vulns) {
                if (vuln.getOverall() != null) {
                    String severityName = getRiskLevelName(vuln.getOverall());
                    if (severityName != null && !severityName.equals("Unassigned")) {
                        findings.put(severityName, findings.getOrDefault(severityName, 0) + 1);
                    }
                }
            }

            // Add vulnerability counts in separate columns
            for (RiskLevel level : riskLevels) {
                if (level.getRisk() != null && !level.getRisk().trim().isEmpty()) {
                    Integer count = findings.get(level.getRisk());
                    csvContent.append(",").append(count != null ? count : 0);
                }
            }
            csvContent.append("\n");
        }

        // Set up the download
        inputStream = new ByteArrayInputStream(csvContent.toString().getBytes());
        SimpleDateFormat filenameFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
        filename = "manager_dashboard_assessments_" + filenameFormat.format(new Date()) + ".csv";

        return SUCCESS;
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
    }

    private List<Assessment> performAssessmentSearch() {
        // Build MongoDB query for search mode (copied from ManagerDashboard)
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        StringBuilder query = new StringBuilder("{");
        boolean hasConditions = false;

        // Build date range condition - include assessments that overlap with the search
        // range
        if (startDate != null && endDate != null) {
            query.append("\"start\": {$lte: ISODate(\"").append(sdf.format(endDate)).append("\")}, ");
            query.append("\"end\": {$gte: ISODate(\"").append(sdf.format(startDate)).append("\")}");
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