package com.fuse.actions.dashboard;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.Result;

import com.fuse.actions.FSActionSupport;
import com.fuse.dao.Assessment;
import com.fuse.dao.AssessmentType;
import com.fuse.dao.HibHelper;
import com.fuse.dao.RiskLevel;
import com.fuse.dao.Status;
import com.fuse.dao.Teams;
import com.fuse.dao.User;
import com.fuse.dao.Vulnerability;
import com.fuse.dao.Campaign;
import java.util.stream.Collectors;

@Namespace("/portal")
@Result(name = "success", location = "/WEB-INF/jsp/dashboard/ManagerDashboard.jsp")
public class ManagerDashboard extends FSActionSupport {
	
    // Properties for statistics
    private Map<String, Integer> assessmentStats = new HashMap<>();
    private Map<String, Map<String, Integer>> vulnerabilityStats = new HashMap<>();
    private List<Assessment> searchResults = new ArrayList<>();
    private List<Vulnerability> recentVulnerabilities = new ArrayList<>();
    
    // Filtered data statistics
    private Map<String, Integer> filteredSeverityStats = new LinkedHashMap<>();
    private Map<String, Integer> filteredStatusStats = new LinkedHashMap<>();
    private Map<String, Integer> filteredAssessorStats = new LinkedHashMap<>();
    private Map<String, String> severityColorMap = new LinkedHashMap<>();
    
    // Totals for filtered stats
    private int totalFilteredVulns = 0;
    private int totalFilteredAssessments = 0;
    private int totalCompletedAssessments = 0;
    
    // Search parameters
    private Date startDate;
    private Date endDate;
    private Long typeId;
    private Long teamId;
    private String status;
    private Long assessorId;
    private Long campaignId;
    private String searchAction = "";
    
    // Lists for dropdowns
    private List<AssessmentType> assessmentTypes;
    private List<Teams> teams;
    private List<RiskLevel> riskLevels;
    private List<Status> statuses;
    private List<User> assessors;
    private List<Campaign> campaigns;
    
    // Statistics data
    private int weeklyAssessments;
    private int monthlyAssessments;
    private int yearlyAssessments;
    private int totalAssessments;
    
    private int weeklyVulns;
    private int monthlyVulns;
    private int yearlyVulns;
    private int totalVulns;

    @Action(value = "ManagerDashboard")
    public String execute() {
        // Check if user has manager role
        if (!this.isAcmanager()) {
            return LOGIN;
        }

        // Load dropdown data
        loadDropdownData();
        
        // Calculate statistics
        calculateAssessmentStatistics();
        calculateVulnerabilityStatistics();
        
        // Always load assessments (search or default view)
        performAssessmentSearch();
        
        // Calculate statistics for filtered data
        calculateFilteredStatistics();

        return SUCCESS;
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

    private void calculateAssessmentStatistics() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        Date now = new Date();
        
        // Calculate date ranges
        Calendar cal = Calendar.getInstance();
        
        // This week
        cal.setTime(now);
        cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        Date weekStart = cal.getTime();
        
        // This month
        cal.setTime(now);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        Date monthStart = cal.getTime();
        
        // This year
        cal.setTime(now);
        cal.set(Calendar.DAY_OF_YEAR, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        Date yearStart = cal.getTime();
        
        // Weekly assessments
        String weekQuery = "{\"completed\": {$exists: true}, \"completed\": {$gte: ISODate(\"" 
            + sdf.format(weekStart) + "\"), $lte: ISODate(\"" + sdf.format(now) + "\")}}";
        weeklyAssessments = em.createNativeQuery(weekQuery, Assessment.class).getResultList().size();
        
        // Monthly assessments
        String monthQuery = "{\"completed\": {$exists: true}, \"completed\": {$gte: ISODate(\"" 
            + sdf.format(monthStart) + "\"), $lte: ISODate(\"" + sdf.format(now) + "\")}}";
        monthlyAssessments = em.createNativeQuery(monthQuery, Assessment.class).getResultList().size();
        
        // Yearly assessments
        String yearQuery = "{\"completed\": {$exists: true}, \"completed\": {$gte: ISODate(\"" 
            + sdf.format(yearStart) + "\"), $lte: ISODate(\"" + sdf.format(now) + "\")}}";
        yearlyAssessments = em.createNativeQuery(yearQuery, Assessment.class).getResultList().size();
        
        // Total assessments
        totalAssessments = em.createQuery("from Assessment where completed is not null")
            .getResultList().size();
        
        // Store in map for easy access
        assessmentStats.put("weekly", weeklyAssessments);
        assessmentStats.put("monthly", monthlyAssessments);
        assessmentStats.put("yearly", yearlyAssessments);
        assessmentStats.put("total", totalAssessments);
    }

    private void calculateVulnerabilityStatistics() {
        Date now = new Date();
        Calendar cal = Calendar.getInstance();
        
        // Calculate date ranges (same as assessments)
        cal.setTime(now);
        cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        Date weekStart = cal.getTime();
        
        cal.setTime(now);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        Date monthStart = cal.getTime();
        
        cal.setTime(now);
        cal.set(Calendar.DAY_OF_YEAR, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        Date yearStart = cal.getTime();

        // Create level map for quick lookup - only include levels with non-empty names
        Map<Integer, String> levelMap = new HashMap<>();
        List<RiskLevel> validLevels = new ArrayList<>();
        for (RiskLevel level : riskLevels) {
            if (level.getRisk() != null && !level.getRisk().trim().isEmpty()) {
                levelMap.put(level.getRiskId(), level.getRisk());
                validLevels.add(level);
            }
        }

        // Get vulnerabilities and calculate stats
        List<String> periods = Arrays.asList("weekly", "monthly", "yearly", "total");
        List<Date> periodStarts = Arrays.asList(weekStart, monthStart, yearStart, null);
        
        for (int i = 0; i < periods.size(); i++) {
            String period = periods.get(i);
            Date periodStart = periodStarts.get(i);
            
            // Use LinkedHashMap to preserve order
            LinkedHashMap<String, Integer> severityMap = new LinkedHashMap<>();
            
            // Initialize severity counts in order of risk levels
            for (RiskLevel level : validLevels) {
                severityMap.put(level.getRisk(), 0);
            }
            
            String query;
            if (periodStart != null) {
                query = "from Vulnerability where created >= :startDate and created <= :endDate";
            } else {
                query = "from Vulnerability";
            }
            
            List<Vulnerability> vulns;
            if (periodStart != null) {
                vulns = em.createQuery(query)
                    .setParameter("startDate", periodStart)
                    .setParameter("endDate", now)
                    .getResultList();
            } else {
                vulns = em.createQuery(query).getResultList();
            }
            
            // Update counts based on period
            if (period.equals("weekly")) {
                weeklyVulns = vulns.size();
            } else if (period.equals("monthly")) {
                monthlyVulns = vulns.size();
            } else if (period.equals("yearly")) {
                yearlyVulns = vulns.size();
            } else {
                totalVulns = vulns.size();
            }
            
            // Count by severity
            for (Vulnerability vuln : vulns) {
                if (vuln.getOverall() != null) {
                    String severityName = levelMap.get(vuln.getOverall().intValue());
                    if (severityName == null && !levelMap.isEmpty()) {
                        // Get first valid severity if the vulnerability has an invalid overall value
                        severityName = levelMap.values().iterator().next();
                    }
                    if (severityName != null) {
                        severityMap.put(severityName, severityMap.get(severityName) + 1);
                    }
                }
            }
            
            vulnerabilityStats.put(period, severityMap);
        }
    }

    private String getRiskLevelName(Long riskId) {
        if (riskId == null) return "Unassigned";
        
        for (RiskLevel level : riskLevels) {
            if (riskId.equals(Long.valueOf(level.getRiskId()))) {
                return level.getRisk() != null ? level.getRisk() : "Unassigned";
            }
        }
        return "Unassigned";
    }

    // Remove loadRecentVulnerabilities method - no longer needed
    
    private void calculateFilteredStatistics() {
        // Initialize maps
        filteredSeverityStats.clear();
        filteredStatusStats.clear();
        filteredAssessorStats.clear();
        severityColorMap.clear();
        
        // Initialize severity stats with risk levels and build color map
        List<String> colors = getColors();
        int colorIndex = 0;
        for (RiskLevel level : riskLevels) {
            if (level.getRisk() != null && !level.getRisk().trim().isEmpty()) {
                filteredSeverityStats.put(level.getRisk(), 0);
                // Map severity to color (highest severity gets first color)
                if (colorIndex < colors.size()) {
                    severityColorMap.put(level.getRisk(), colors.get(colorIndex));
                    colorIndex++;
                }
            }
        }
        
        // Initialize status stats
        for (Status s : statuses) {
            filteredStatusStats.put(s.getName(), 0);
        }
        
        // Process each assessment in search results
        for (Assessment assessment : searchResults) {
            // Count by status
            String status = assessment.getStatus();
            if (status != null) {
                filteredStatusStats.put(status, filteredStatusStats.getOrDefault(status, 0) + 1);
            }
            
            // For completed assessments, count by assessor
            if ( ("Completed".equals(status) || status == null && assessment.getCompleted() != null || status == "Open" && assessment.getCompleted() != null) && assessment.getAssessor() != null) {
                for (User assessor : assessment.getAssessor()) {
                    String assessorName = assessor.getFname() + " " + assessor.getLname();
                    filteredAssessorStats.put(assessorName,
                        filteredAssessorStats.getOrDefault(assessorName, 0) + 1);
                }
            }
            
            // Get vulnerabilities for this assessment
            List<Vulnerability> vulns = em.createQuery(
                "from Vulnerability where assessmentId = :aid", Vulnerability.class)
                .setParameter("aid", assessment.getId())
                .getResultList();
            
            // Count vulnerabilities by severity
            for (Vulnerability vuln : vulns) {
                if (vuln.getOverall() != null) {
                    String severityName = getRiskLevelName(vuln.getOverall());
                    if (severityName != null && !severityName.equals("Unassigned")) {
                        filteredSeverityStats.put(severityName,
                            filteredSeverityStats.getOrDefault(severityName, 0) + 1);
                    }
                }
            }
        }
        
        // Sort assessor stats by count (descending)
        filteredAssessorStats = filteredAssessorStats.entrySet()
            .stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                (e1, e2) -> e1,
                LinkedHashMap::new
            ));
        
        // Calculate totals
        totalFilteredVulns = filteredSeverityStats.values().stream().mapToInt(Integer::intValue).sum();
        totalFilteredAssessments = searchResults.size();
        totalCompletedAssessments = filteredAssessorStats.values().stream().mapToInt(Integer::intValue).sum();
    }

    private void performAssessmentSearch() {
        List<Assessment> results;
        
        // If not searching, get current month's assessments
        if (!"search".equals(searchAction)) {
            // Get current month date range
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.DAY_OF_MONTH, 1);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            Date monthStart = cal.getTime();
            
            cal.add(Calendar.MONTH, 1);
            cal.add(Calendar.DAY_OF_MONTH, -1);
            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.SECOND, 59);
            Date monthEnd = cal.getTime();
            
            // Query assessments for current month
            results = em.createQuery("from Assessment where start >= :monthStart and start <= :monthEnd order by start desc", Assessment.class)
                .setParameter("monthStart", monthStart)
                .setParameter("monthEnd", monthEnd)
                .getResultList();
            searchResults = results;
            return;
        }
        
        // Build MongoDB query for search mode
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        StringBuilder query = new StringBuilder("{");
        boolean hasConditions = false;
        
        // Build date range condition
        if (startDate != null && endDate != null) {
            query.append("\"start\": {$gte: ISODate(\"").append(sdf.format(startDate)).append("\")}, ");
            query.append("\"end\": {$lte: ISODate(\"").append(sdf.format(endDate)).append("\")}");
            hasConditions = true;
        }
        
        // Build assessment type condition
        if (typeId != null && typeId > 0) {
            if (hasConditions) query.append(", ");
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
                    if (hasConditions) query.append(", ");
                    query.append("\"assessor\": {$in: [");
                    boolean first = true;
                    for (User user : teamUsers) {
                        if (!first) query.append(", ");
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
			if (hasConditions) query.append(", ");
			query.append("\"assessor\": ").append(assessorId);
			hasConditions = true;
		}
		
		// Build campaign condition
		if (campaignId != null && campaignId > 0) {
			if (hasConditions) query.append(", ");
			query.append("\"campaign_id\": ").append(campaignId);
			hasConditions = true;
		}
        
        query.append("}");
        // Execute the native MongoDB query
        List<Assessment> mongoResults = em.createNativeQuery(query.toString(), Assessment.class).getResultList();
        
        // Filter Results based on status
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
        
        // Sort by start date descending (MongoDB doesn't guarantee order)
        Collections.sort(searchResults, new Comparator<Assessment>() {
            @Override
            public int compare(Assessment a1, Assessment a2) {
                if (a1.getStart() == null && a2.getStart() == null) return 0;
                if (a1.getStart() == null) return 1;
                if (a2.getStart() == null) return -1;
                return a2.getStart().compareTo(a1.getStart());
            }
        });
    }

    // Getters and Setters
    public Map<String, Integer> getAssessmentStats() {
        return assessmentStats;
    }

    public void setAssessmentStats(Map<String, Integer> assessmentStats) {
        this.assessmentStats = assessmentStats;
    }

    public Map<String, Map<String, Integer>> getVulnerabilityStats() {
        return vulnerabilityStats;
    }

    public void setVulnerabilityStats(Map<String, Map<String, Integer>> vulnerabilityStats) {
        this.vulnerabilityStats = vulnerabilityStats;
    }

    public List<Assessment> getSearchResults() {
        return searchResults;
    }

    public void setSearchResults(List<Assessment> searchResults) {
        this.searchResults = searchResults;
    }

    public List<Vulnerability> getRecentVulnerabilities() {
        return recentVulnerabilities;
    }

    public void setRecentVulnerabilities(List<Vulnerability> recentVulnerabilities) {
        this.recentVulnerabilities = recentVulnerabilities;
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

    public String getSearchAction() {
        return searchAction;
    }

    public void setSearchAction(String searchAction) {
        this.searchAction = searchAction;
    }

    public List<AssessmentType> getAssessmentTypes() {
        return assessmentTypes;
    }

    public void setAssessmentTypes(List<AssessmentType> assessmentTypes) {
        this.assessmentTypes = assessmentTypes;
    }

    public List<Teams> getTeams() {
        return teams;
    }

    public void setTeams(List<Teams> teams) {
        this.teams = teams;
    }

    public List<RiskLevel> getRiskLevels() {
        return riskLevels;
    }

    public void setRiskLevels(List<RiskLevel> riskLevels) {
        this.riskLevels = riskLevels;
    }

    public int getWeeklyAssessments() {
        return weeklyAssessments;
    }

    public void setWeeklyAssessments(int weeklyAssessments) {
        this.weeklyAssessments = weeklyAssessments;
    }

    public int getMonthlyAssessments() {
        return monthlyAssessments;
    }

    public void setMonthlyAssessments(int monthlyAssessments) {
        this.monthlyAssessments = monthlyAssessments;
    }

    public int getYearlyAssessments() {
        return yearlyAssessments;
    }

    public void setYearlyAssessments(int yearlyAssessments) {
        this.yearlyAssessments = yearlyAssessments;
    }

    public int getTotalAssessments() {
        return totalAssessments;
    }

    public void setTotalAssessments(int totalAssessments) {
        this.totalAssessments = totalAssessments;
    }

    public int getWeeklyVulns() {
        return weeklyVulns;
    }

    public void setWeeklyVulns(int weeklyVulns) {
        this.weeklyVulns = weeklyVulns;
    }

    public int getMonthlyVulns() {
        return monthlyVulns;
    }

    public void setMonthlyVulns(int monthlyVulns) {
        this.monthlyVulns = monthlyVulns;
    }

    public int getYearlyVulns() {
        return yearlyVulns;
    }

    public void setYearlyVulns(int yearlyVulns) {
        this.yearlyVulns = yearlyVulns;
    }

    public int getTotalVulns() {
        return totalVulns;
    }

    public void setTotalVulns(int totalVulns) {
        this.totalVulns = totalVulns;
    }
    
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    
    public List<Status> getStatuses() {
        return statuses;
    }

    public void setStatuses(List<Status> statuses) {
        this.statuses = statuses;
    }
    
    public List<String> getColors() {
        // Colors ordered from highest severity to lowest
        // Critical/Highest = Red shades, Medium = Orange/Yellow, Low = Green/Blue
        return Arrays.asList(
            "#dd4b39",  // Red (Critical/Highest)
            "#f39c12",  // Orange (High)
            "#00c0ef",  // Orange-Yellow (Medium-High)
            "#39cccc",  // Yellow (Medium)
            "#00a65a",  // Light Blue (Medium-Low)
            "#95A5A6",  // Cyan (Low)
            "#34495E",  // Green (Low)
            "#2C3E50",  // Teal (Info)
            "#9B59B6",   // Gray (Minimal/Info)
            "#8E44AD"
        );
    }
    
    public Map<String, Integer> getFilteredSeverityStats() {
        return filteredSeverityStats;
    }

    public void setFilteredSeverityStats(Map<String, Integer> filteredSeverityStats) {
        this.filteredSeverityStats = filteredSeverityStats;
    }

    public Map<String, Integer> getFilteredStatusStats() {
        return filteredStatusStats;
    }

    public void setFilteredStatusStats(Map<String, Integer> filteredStatusStats) {
        this.filteredStatusStats = filteredStatusStats;
    }

    public Map<String, Integer> getFilteredAssessorStats() {
        return filteredAssessorStats;
    }

    public void setFilteredAssessorStats(Map<String, Integer> filteredAssessorStats) {
        this.filteredAssessorStats = filteredAssessorStats;
    }
    
    public Map<String, String> getSeverityColorMap() {
        return severityColorMap;
    }

    public void setSeverityColorMap(Map<String, String> severityColorMap) {
        this.severityColorMap = severityColorMap;
    }

    public int getTotalFilteredVulns() {
        return totalFilteredVulns;
    }

    public void setTotalFilteredVulns(int totalFilteredVulns) {
        this.totalFilteredVulns = totalFilteredVulns;
    }

    public int getTotalFilteredAssessments() {
        return totalFilteredAssessments;
    }

    public void setTotalFilteredAssessments(int totalFilteredAssessments) {
        this.totalFilteredAssessments = totalFilteredAssessments;
    }

    public int getTotalCompletedAssessments() {
        return totalCompletedAssessments;
    }

    public void setTotalCompletedAssessments(int totalCompletedAssessments) {
        this.totalCompletedAssessments = totalCompletedAssessments;
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

    public List<User> getAssessors() {
        return assessors;
    }

    public void setAssessors(List<User> assessors) {
        this.assessors = assessors;
    }
    
    public List<Campaign> getCampaigns() {
        return campaigns;
    }

    public void setCampaigns(List<Campaign> campaigns) {
        this.campaigns = campaigns;
    }
    
	public String getActiveMDB() {
		return "active";
	}
    
}