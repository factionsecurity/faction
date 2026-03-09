package com.fuse.actions.dashboard;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.Result;

import com.fuse.actions.FSActionSupport;
import com.fuse.dao.Assessment;
import com.fuse.dao.HibHelper;
import com.fuse.dao.RiskLevel;
import com.fuse.dao.Vulnerability;

@Namespace("/portal")
public class ManagerDashboardAjax extends FSActionSupport {

    private Map<String, Object> responseData = new HashMap<>();

    @Action(value = "ManagerDashboardAjax!getStatistics", results = {
        @Result(name = "json", type = "stream", params = {
            "contentType", "application/json",
            "inputName", "_stream"
        })
    })
    public String getStatistics() {
        // Check if user has manager role
        if (!this.isAcmanager()) {
            return jsonOutput("{\"error\":\"Unauthorized\",\"success\":false}");
        }

        try {
            Map<String, Integer> assessmentStats = new HashMap<>();
            Map<String, Object> vulnerabilityStats = new HashMap<>();
            
            // Calculate assessment statistics
            calculateAssessmentStats(assessmentStats);
            
            // Calculate vulnerability statistics
            calculateVulnerabilityStats(vulnerabilityStats);
            
            // Build JSON response manually
            StringBuilder json = new StringBuilder("{");
            json.append("\"success\":true,");
            
            // Add assessment stats
            json.append("\"assessmentStats\":{");
            json.append("\"weekly\":").append(assessmentStats.get("weekly")).append(",");
            json.append("\"monthly\":").append(assessmentStats.get("monthly")).append(",");
            json.append("\"yearly\":").append(assessmentStats.get("yearly")).append(",");
            json.append("\"total\":").append(assessmentStats.get("total"));
            json.append("},");
            
            // Add vulnerability stats
            json.append("\"vulnerabilityStats\":{");
            json.append("\"weeklyTotal\":").append(vulnerabilityStats.get("weeklyTotal")).append(",");
            json.append("\"monthlyTotal\":").append(vulnerabilityStats.get("monthlyTotal")).append(",");
            json.append("\"yearlyTotal\":").append(vulnerabilityStats.get("yearlyTotal")).append(",");
            json.append("\"total\":").append(vulnerabilityStats.get("total")).append(",");
            
            // Add severity breakdown
            json.append("\"severityBreakdown\":{");
            Map<String, Map<String, Integer>> severityBreakdown =
                (Map<String, Map<String, Integer>>) vulnerabilityStats.get("severityBreakdown");
            
            boolean firstPeriod = true;
            for (String period : severityBreakdown.keySet()) {
                if (!firstPeriod) json.append(",");
                json.append("\"").append(period).append("\":{");
                
                Map<String, Integer> severities = severityBreakdown.get(period);
                boolean firstSeverity = true;
                for (String severity : severities.keySet()) {
                    if (!firstSeverity) json.append(",");
                    json.append("\"").append(escapeJson(severity)).append("\":").append(severities.get(severity));
                    firstSeverity = false;
                }
                json.append("}");
                firstPeriod = false;
            }
            json.append("}"); // end severityBreakdown
            json.append("}"); // end vulnerabilityStats
            json.append("}"); // end response
            
            return jsonOutput(json.toString());
            
        } catch (Exception e) {
            e.printStackTrace();
            return jsonOutput("{\"error\":\"" + escapeJson(e.getMessage()) + "\",\"success\":false}");
        }
    }

    @Action(value = "ManagerDashboardAjax!getRecentVulnerabilities", results = {
        @Result(name = "json", type = "stream", params = {
            "contentType", "application/json",
            "inputName", "_stream"
        })
    })
    public String getRecentVulnerabilities() {
        if (!this.isAcmanager()) {
            return jsonOutput("{\"error\":\"Unauthorized\"}");
        }

        try {
            List<Vulnerability> recentVulns = em.createQuery(
                "from Vulnerability order by created desc")
                .setMaxResults(20)
                .getResultList();
            
            List<RiskLevel> riskLevels = em.createQuery("from RiskLevel order by riskId").getResultList();
            
            // Build response
            StringBuilder json = new StringBuilder("{\"vulnerabilities\":[");
            for (int i = 0; i < recentVulns.size(); i++) {
                if (i > 0) json.append(",");
                Vulnerability vuln = recentVulns.get(i);
                json.append("{");
                json.append("\"id\":").append(vuln.getId()).append(",");
                json.append("\"name\":\"").append(escapeJson(vuln.getName())).append("\",");
                json.append("\"assessmentId\":").append(vuln.getAssessmentId()).append(",");
                json.append("\"created\":\"").append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(vuln.getCreated())).append("\",");
                json.append("\"severity\":\"").append(getRiskLevelName(vuln.getOverall(), riskLevels)).append("\",");
                json.append("\"tracking\":\"").append(escapeJson(vuln.getTracking())).append("\"");
                json.append("}");
            }
            json.append("]}");
            
            return jsonOutput(json.toString());
            
        } catch (Exception e) {
            e.printStackTrace();
            return jsonOutput("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    private void calculateAssessmentStats(Map<String, Integer> stats) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        Date now = new Date();
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
        
        // Query for each period
        String weekQuery = "{\"completed\": {$exists: true}, \"completed\": {$gte: ISODate(\"" 
            + sdf.format(weekStart) + "\"), $lte: ISODate(\"" + sdf.format(now) + "\")}}";
        int weeklyCount = em.createNativeQuery(weekQuery, Assessment.class).getResultList().size();
        
        String monthQuery = "{\"completed\": {$exists: true}, \"completed\": {$gte: ISODate(\"" 
            + sdf.format(monthStart) + "\"), $lte: ISODate(\"" + sdf.format(now) + "\")}}";
        int monthlyCount = em.createNativeQuery(monthQuery, Assessment.class).getResultList().size();
        
        String yearQuery = "{\"completed\": {$exists: true}, \"completed\": {$gte: ISODate(\"" 
            + sdf.format(yearStart) + "\"), $lte: ISODate(\"" + sdf.format(now) + "\")}}";
        int yearlyCount = em.createNativeQuery(yearQuery, Assessment.class).getResultList().size();
        
        int totalCount = em.createQuery("from Assessment where completed is not null").getResultList().size();
        
        stats.put("weekly", weeklyCount);
        stats.put("monthly", monthlyCount);
        stats.put("yearly", yearlyCount);
        stats.put("total", totalCount);
    }

    private void calculateVulnerabilityStats(Map<String, Object> stats) {
        Date now = new Date();
        Calendar cal = Calendar.getInstance();
        
        // Calculate date ranges
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

        // Get vulnerability counts
        List<Vulnerability> weeklyVulns = em.createQuery("from Vulnerability where created >= :startDate and created <= :endDate")
            .setParameter("startDate", weekStart)
            .setParameter("endDate", now)
            .getResultList();
        
        List<Vulnerability> monthlyVulns = em.createQuery("from Vulnerability where created >= :startDate and created <= :endDate")
            .setParameter("startDate", monthStart)
            .setParameter("endDate", now)
            .getResultList();
        
        List<Vulnerability> yearlyVulns = em.createQuery("from Vulnerability where created >= :startDate and created <= :endDate")
            .setParameter("startDate", yearStart)
            .setParameter("endDate", now)
            .getResultList();
        
        List<Vulnerability> totalVulns = em.createQuery("from Vulnerability").getResultList();
        
        stats.put("weeklyTotal", weeklyVulns.size());
        stats.put("monthlyTotal", monthlyVulns.size());
        stats.put("yearlyTotal", yearlyVulns.size());
        stats.put("total", totalVulns.size());
        
        // Get risk levels for severity mapping
        List<RiskLevel> riskLevels = em.createQuery("from RiskLevel order by riskId").getResultList();
        
        // Calculate severity distribution for each period
        Map<String, Map<String, Integer>> severityBreakdown = new HashMap<>();
        severityBreakdown.put("weekly", calculateSeverityDistribution(weeklyVulns, riskLevels));
        severityBreakdown.put("monthly", calculateSeverityDistribution(monthlyVulns, riskLevels));
        severityBreakdown.put("yearly", calculateSeverityDistribution(yearlyVulns, riskLevels));
        severityBreakdown.put("total", calculateSeverityDistribution(totalVulns, riskLevels));
        
        stats.put("severityBreakdown", severityBreakdown);
    }

    private Map<String, Integer> calculateSeverityDistribution(List<Vulnerability> vulns, List<RiskLevel> riskLevels) {
        Map<String, Integer> distribution = new HashMap<>();
        
        // Initialize all severity levels with 0
        for (RiskLevel level : riskLevels) {
            if (level.getRisk() != null) {
                distribution.put(level.getRisk(), 0);
            }
        }
        
        // Count vulnerabilities by severity
        for (Vulnerability vuln : vulns) {
            String severity = getRiskLevelName(vuln.getOverall(), riskLevels);
            distribution.put(severity, distribution.getOrDefault(severity, 0) + 1);
        }
        
        return distribution;
    }

    private String getRiskLevelName(Long riskId, List<RiskLevel> riskLevels) {
        if (riskId == null) return "Unassigned";
        
        for (RiskLevel level : riskLevels) {
            if (riskId.equals(Long.valueOf(level.getRiskId()))) {
                return level.getRisk() != null ? level.getRisk() : "Unassigned";
            }
        }
        return "Unassigned";
    }

    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\"", "\\\"")
                  .replace("\\", "\\\\")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
}