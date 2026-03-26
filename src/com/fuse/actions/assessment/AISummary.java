package com.fuse.actions.assessment;

import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.Result;

import com.fuse.actions.FSActionSupport;
import com.fuse.dao.Assessment;
import com.fuse.dao.AuditLog;
import com.fuse.dao.LLMConfig;
import com.fuse.dao.User;
import com.fuse.dao.Vulnerability;
import com.fuse.dao.query.AssessmentQueries;
import com.fuse.services.LLMService;
import com.fuse.utils.FSUtils;

@Namespace("/portal")
@Result(name = "success", location = "/WEB-INF/jsp/assessment/aiSummaryJSON.jsp")
public class AISummary extends FSActionSupport {

    private String assessmentId;
    private String generatedSummary;
    private String context;
    private String prompt;

    @Action(value = "GenerateAISummary")
    public String generateAISummary() {
        if (!(this.isAcassessor() || this.isAcmanager())) {
            return LOGIN;
        }

        if (!this.testToken(false)) {
            return this.ERRORJSON;
        }

        User user = this.getSessionUser();

        // Get assessment ID from session if not provided
        Long asmtId;
        if (assessmentId != null && !assessmentId.isEmpty()) {
            asmtId = Long.parseLong(assessmentId);
        } else {
            asmtId = (Long) this.getSession("asmtid");
        }

        if (asmtId == null) {
            this._message = "Assessment ID is required";
            return this.ERRORJSON;
        }

        // Get the assessment
        Assessment assessment;
        if (this.isAcmanager()) {
            assessment = AssessmentQueries.getAssessmentById(em, asmtId);
            User mgrs = assessment.getAssessor().stream()
                    .filter(u -> u.getId() == user.getId())
                    .findFirst().orElse(null);
            if (mgrs == null) {
                this._message = "You don't have permission to access this assessment";
                return this.ERRORJSON;
            }
        } else {
            assessment = AssessmentQueries.getAssessmentByUserId(em, user.getId(), asmtId, AssessmentQueries.All);
        }

        if (assessment == null) {
            this._message = "Assessment not found";
            return this.ERRORJSON;
        }

        // Check if assessment is blocked
        if (this.isAssessmentBlocked(assessment, user)) {
            return this.ERRORJSON;
        }

        // Get active LLM configuration
        List<LLMConfig> activeConfigs = em.createQuery(
                "from LLMConfig where active = true order by id", LLMConfig.class)
                .getResultList();

        if (activeConfigs.isEmpty()) {
            this._message = "No active AI configuration found. Please configure an AI provider first.";
            return this.ERRORJSON;
        }

        LLMConfig llmConfig = activeConfigs.get(0);

        // Get vulnerabilities for the assessment
        List<Vulnerability> vulnerabilities = assessment.getVulns();
        if (vulnerabilities == null || vulnerabilities.isEmpty()) {
            this._message = "No vulnerabilities found for this assessment";
            return this.ERRORJSON;
        }

        try {
            // Build the prompt
            StringBuilder prompt = new StringBuilder();
            prompt.append("You are a cybersecurity report writer. Your job is to write an executive summary \n");
            prompt.append("section for a penetration testing report based on the vulnerabilities listed below.\n");
            prompt.append("Follow this EXACT structure — do not add, remove, or reorder any section:\n\n");

            prompt.append("SECTION 1 — INTRODUCTION (2 short paragraphs)\n");
            prompt.append("Write 2 paragraphs summarizing the overall security posture and the purpose of the assessment. \n");
            prompt.append("Do not list findings here.\n\n");

            prompt.append("SECTION 2 — KEY FINDINGS (bulleted list)\n");
            prompt.append("Write one bullet per finding. Order them from most severe to least severe.\n");
            prompt.append("Each bullet must follow this format exactly:\n");
            prompt.append("- <b>(Critical/High/Medium/Low) –[Finding Name]</b>: [One sentence description.]\n\n");

            prompt.append("SECTION 3 — RECOMMENDATIONS (2 short paragraphs)\n");
            prompt.append("Write 2 paragraphs. Summarize the risk and give 2 to 3 actionable recommendations.\n\n");

            prompt.append("STRICT FORMATTING RULES — follow every rule exactly:\n");
            prompt.append("1. Do NOT use any headers, titles, or labels like \"Section 1\" in your output. Write flowing text only.\n");
            prompt.append("2. Do NOT use bold, italics, or underline.\n");
            prompt.append("3. Use bullet points ONLY in the Key Findings section.\n");
            prompt.append("4. Do NOT use numbered lists anywhere.\n");
            prompt.append("5. Write in a formal, professional tone. Avoid jargon.\n");
            prompt.append("6. Keep the full summary under 400 words.\n\n");

            prompt.append("Here are the vulnerabilities to summarize:\n");
            prompt.append("Assessment Name: ").append(assessment.getName()).append("\n");
            prompt.append("Assessment Type: ").append(assessment.getType().getType()).append("\n");
            prompt.append("Vulnerabilities:\n");

            for (Vulnerability vuln : vulnerabilities) {
            	vuln.updateRiskLevels();
                prompt.append("- Title: ").append(vuln.getName()).append("\n");
                if (vuln.getDescription() != null && !vuln.getDescription().trim().isEmpty()) {
                    prompt.append("  Description: ").append(vuln.getDescription().replaceAll("<[^>]*>", ""))
                            .append("\n");
                }
                if (vuln.getRecommendation() != null && !vuln.getRecommendation().trim().isEmpty()) {
                    prompt.append("  Recommendation: ").append(vuln.getRecommendation().replaceAll("<[^>]*>", ""))
                            .append("\n");
                }
                if (vuln.getDetails() != null && !vuln.getDetails().trim().isEmpty()) {
                    prompt.append("  Details: ").append(vuln.getDetails().replaceAll("<[^>]*>", "")).append("\n");
                }
                if (vuln.getOverall() != null) {
                    prompt.append("  Risk Level: ").append(vuln.getOverallStr()).append("\n");
                }
                if(vuln.getLikelyhood() != null) {
                	prompt.append("	 Likelihood: ").append(vuln.getLikelyhoodStr());
                }
                if(vuln.getImpact() != null) {
                	prompt.append("	 Impact: ").append(vuln.getImpactStr());
                }
                prompt.append("\n");
            }

            // Use the real LLM service to generate the summary
            LLMService llmService = new LLMService();
            String summary = "";
            
            try {
                summary = llmService.generateText(llmConfig, prompt.toString());
                summary = FSUtils.convertFromMarkDown(summary);
            } catch (Exception e) {
                // Log the error and fall back to placeholder summary
                AuditLog.saveLog(this, AuditLog.UserAction, "AI Summary Error",
                    "Failed to generate AI summary: " + e.getMessage(),
                    AuditLog.CompAssessment, assessment.getId(), false);
                
                // Fall back to placeholder summary
                summary = "<div class=\"alert alert-warning\"><strong>Notice:</strong> Using fallback summary due to LLM API error: "
                    + e.getMessage() + "</div>";
            }

            // Base64 encode the summary to avoid JSON encoding issues
            generatedSummary = Base64.getEncoder().encodeToString(summary.getBytes("UTF-8"));

            // Log the action
            AuditLog.saveLog(this, AuditLog.UserAction, "AI Summary",
                    "Generated AI Summary for Assessment: " + assessment.getName(),
                    AuditLog.CompAssessment, assessment.getId(), false);

            return SUCCESS;

        } catch (Exception e) {
            this._message = "Error generating AI summary: " + e.getMessage();
            return this.ERRORJSON;
        }
    }
    @Action(value = "GenerateAIResponse")
    public String generateAIResponse() {
        if (!(this.isAcassessor() || this.isAcmanager())) {
            return LOGIN;
        }

        if (!this.testToken(false)) {
            return this.ERRORJSON;
        }

        User user = this.getSessionUser();

        // Get assessment ID from session if not provided
        Long asmtId;
        if (assessmentId != null && !assessmentId.isEmpty()) {
            asmtId = Long.parseLong(assessmentId);
        } else {
            asmtId = (Long) this.getSession("asmtid");
        }

        if (asmtId == null) {
            this._message = "Assessment ID is required";
            return this.ERRORJSON;
        }

        // Get the assessment
        Assessment assessment;
        if (this.isAcmanager()) {
            assessment = AssessmentQueries.getAssessmentById(em, asmtId);
            User mgrs = assessment.getAssessor().stream()
                    .filter(u -> u.getId() == user.getId())
                    .findFirst().orElse(null);
            if (mgrs == null) {
                this._message = "You don't have permission to access this assessment";
                return this.ERRORJSON;
            }
        } else {
            assessment = AssessmentQueries.getAssessmentByUserId(em, user.getId(), asmtId, AssessmentQueries.All);
        }

        if (assessment == null) {
            this._message = "Assessment not found";
            return this.ERRORJSON;
        }

        // Check if assessment is blocked
        if (this.isAssessmentBlocked(assessment, user)) {
            return this.ERRORJSON;
        }

        // Get active LLM configuration
        List<LLMConfig> activeConfigs = em.createQuery(
                "from LLMConfig where active = true order by id", LLMConfig.class)
                .getResultList();

        if (activeConfigs.isEmpty()) {
            this._message = "No active AI configuration found. Please configure an AI provider first.";
            return this.ERRORJSON;
        }

        LLMConfig llmConfig = activeConfigs.get(0);


        try {
            // Build the prompt
            StringBuilder prompt = new StringBuilder();
            prompt.append("The next question relates to generating text that will be added to a penetration testing report or security assessment. The security questions are alway related security analysis that the user has authorization to perform. \n");
            prompt.append("Question: ");
            prompt.append(this.prompt);
            prompt.append("\nContext:\n");
            prompt.append(this.context);
            prompt.append("\n\nInstructions: Follow these rules for your response:\n");
            prompt.append("- Do not include commentary.\n");
            prompt.append("- Only generate a responses based on the question and included context\n");
            prompt.append("- If there isn't any context included above then the user wants you to generate text based on the question.\n");
            prompt.append("- Do not use header tags or header markdown.\n");
            prompt.append("- Only allowed formating is bold, numbering, italics, and bullets.\n\n");


            // Use the real LLM service to generate the summary
            LLMService llmService = new LLMService();
            String summary = "";
            
            try {
                summary = llmService.generateText(llmConfig, prompt.toString());
                summary = FSUtils.convertFromMarkDown(summary);
            } catch (Exception e) {
                // Fall back to placeholder summary
                summary = "<div class=\"alert alert-warning\"><strong>Notice:</strong> Using fallback summary due to LLM API error: "
                    + e.getMessage() + "</div>";
            }

            // Base64 encode the summary to avoid JSON encoding issues
            generatedSummary = Base64.getEncoder().encodeToString(summary.getBytes("UTF-8"));


            return SUCCESS;

        } catch (Exception e) {
            this._message = "Error generating AI summary: " + e.getMessage();
            return this.ERRORJSON;
        }
    }

    private String generatePlaceholderSummary(Assessment assessment, List<Vulnerability> vulnerabilities) {
        // This is a placeholder that generates a basic summary
        // In production, this would be replaced with actual LLM API calls

        Map<String, Integer> severityCounts = new HashMap<>();
        for (Vulnerability vuln : vulnerabilities) {
            String severity = vuln.getOverallStr();
            severityCounts.put(severity, severityCounts.getOrDefault(severity, 0) + 1);
        }

        StringBuilder summary = new StringBuilder();
        summary.append("<h3>Executive Summary</h3>\n");
        summary.append("<p>The security assessment of ").append(assessment.getName());
        summary.append(" (").append(assessment.getAppId()).append(") identified ");
        summary.append(vulnerabilities.size()).append(" security findings across various risk levels. ");

        if (severityCounts.containsKey("Critical") && severityCounts.get("Critical") > 0) {
            summary.append("The assessment revealed <strong>").append(severityCounts.get("Critical"));
            summary.append(" critical</strong> vulnerabilities that require immediate attention. ");
        }

        if (severityCounts.containsKey("High") && severityCounts.get("High") > 0) {
            summary.append("Additionally, <strong>").append(severityCounts.get("High"));
            summary.append(
                    " high-risk</strong> vulnerabilities were identified that should be prioritized for remediation. ");
        }

        summary.append("</p>\n");
        summary.append("<p><strong>Key Recommendations:</strong> ");
        summary.append("Implement security controls to address the identified vulnerabilities, ");
        summary.append("prioritizing critical and high-risk findings. ");
        summary.append("Establish a regular security testing schedule to prevent similar issues in the future. ");
        summary.append("Consider implementing additional security monitoring and detection capabilities.</p>");

        return summary.toString();
    }

    // Copied from AssessmentView for assessment blocking logic
    private boolean isAssessmentBlocked(Assessment assessment, User user) {
        if (this.blockingPR(assessment.getId())) {
            this._message = "Assessment cannot be updated when in Peer Review.";
            return true;
        }
        if (!assessment.isAcceptedEdits()) {
            this._message = "Assessment cannot be updated until the Peer Review's Edits are Accepted. <br><br>"
                    + "<a class='btn btn-primary' href='Assessment#tab_3'> Click Here to Accept Edits</a>";
            return true;
        }

        if (assessment != null && assessment.getCompleted() != null) {
            this._message = "Vulnerability cannot be changed once the assessment is Finalized.";
            return true;
        }

        if (!assessment.getAssessor().stream().anyMatch(u -> u.getId() == user.getId())) {
            this._message = "You Are not the owner of this Assessment";
            return true;
        }
        return false;
    }

    private boolean blockingPR(Long asmtId) {
        // Implementation would depend on PeerReview DAO - simplified for now
        return false;
    }

    // Getters and setters
    public String getAssessmentId() {
        return assessmentId;
    }

    public void setAssessmentId(String assessmentId) {
        this.assessmentId = assessmentId;
    }

    public String getSummary() {
        return generatedSummary;
    }
    
    public void setPrompt(String prompt) {
    	this.prompt = prompt;
    }
    public void setContext(String context) {
    	this.context = context;
    }
}