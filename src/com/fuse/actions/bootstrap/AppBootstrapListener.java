package com.fuse.actions.bootstrap;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import javax.persistence.EntityManager;
import java.util.List;
import java.util.Base64;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.fuse.dao.Status;
import com.fuse.dao.Assessment;
import com.fuse.dao.HibHelper;
import com.fuse.dao.Image;
import com.fuse.dao.ReportOptions;
import com.fuse.dao.User;
import com.fuse.dao.Vulnerability;
import com.fuse.dao.DefaultVulnerability;
import com.fuse.dao.Category;
import com.fuse.reporting.DocxPrecompiler;
import com.fuse.utils.FSUtils;
import com.fuse.utils.LoggingConfig;


@WebListener
public class AppBootstrapListener implements ServletContextListener {
    
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("Application starting - checking for defaults");
        
        // Configure logging to suppress openhtmltopdf messages
        // Set system property to force java.util.logging config if needed
        System.setProperty("java.util.logging.config.file", "src/logging.properties");
        LoggingConfig.configureOpenHTMLTopDFLogging();
        
        createDefautlStatusIfNeeded();
        fixAssessmentStatuses();
        precompileOpenAssessmentVulns();
        
        // Bootstrap method to create assessment with 500 vulnerabilities and 1000 images
        // Uncomment the following line to enable this bootstrap functionality
        // createTestAssessmentWithVulnerabilitiesAndImages();
    }
    
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("Application shutting down...");
    }
    
    private void createDefautlStatusIfNeeded() {
        try {
        	Status.createBuiltinsIfNotExist();
        } catch (Exception e) {
            System.err.println("Error creating defaults: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void fixAssessmentStatuses() {
        EntityManager em = null;
        try {
            System.out.println("Running assessment status migration...");
            em = HibHelper.getInstance().getEMF().createEntityManager();
            
            // Find all assessments with status "Open" and a non-null completed date
            List<Assessment> assessmentsToFix = em.createQuery(
                "from Assessment where status = :status and completed is not null",
                Assessment.class)
                .setParameter("status", "Open")
                .getResultList();
            
            if (!assessmentsToFix.isEmpty()) {
                System.out.println("Found " + assessmentsToFix.size() + " assessments to fix");
                
                // Begin transaction using EntityManager's built-in transaction management
                em.getTransaction().begin();
                
                for (Assessment assessment : assessmentsToFix) {
                    assessment.setStatus("Completed");
                    em.merge(assessment);  // Use merge instead of persist for existing entities
                    System.out.println("Fixed assessment ID " + assessment.getId() +
                        " - changed status from Open to Completed (completed date: " +
                        assessment.getCompleted() + ")");
                }
                
                // Commit the transaction
                em.getTransaction().commit();
                System.out.println("Assessment status migration completed successfully");
            } else {
                System.out.println("No assessments found requiring status fix");
            }
            
        } catch (Exception e) {
            System.err.println("Error fixing assessment statuses: " + e.getMessage());
            e.printStackTrace();
            if (em != null && em.getTransaction() != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }
    
    /**
     * Pre-compiles HTML fields (desc/rec/details) into cached OOXML for
     * all vulnerabilities in open assessments. Runs on a background thread
     * so application startup isn't blocked.
     *
     * Skips vulnerabilities that already have a valid cache (hash matches
     * current content). Vulnerabilities whose cache is stale or missing
     * are recompiled in batches of 50 to avoid holding too many image
     * bytes in memory at once.
     *
     * This is a one-time migration that populates the cache for existing
     * data. New and updated vulnerabilities get their cache populated
     * automatically at save time by the save-path hooks.
     */
    private void precompileOpenAssessmentVulns() {
        new Thread(() -> {
            EntityManager em = null;
            try {
                System.out.println("[DocxPrecompiler] Starting cache migration for open assessments...");
                em = HibHelper.getInstance().getEMF().createEntityManager();

                // fetch report options once — font/CSS don't change per vuln
                ReportOptions rpo = FSUtils.getOrCreateReportOptionsIfNotExist(em);
                String font = rpo != null ? rpo.getFont() : "Calibri";
                String css = rpo != null ? rpo.getBodyCss() : "";
                String customCSS = css != null ? css : "";

                // find all OPEN assessment IDs first — Hibernate OGM (MongoDB)
                // does not support multi-entity JPQL joins, so we can't join
                // Vulnerability and Assessment in one query
                List<Long> openAssessmentIds = em.createQuery(
                    "select a.id from Assessment a " +
                    "where a.status is null or a.status <> 'Completed'",
                    Long.class).getResultList();

                if (openAssessmentIds.isEmpty()) {
                    System.out.println("[DocxPrecompiler] No open assessments found.");
                    return;
                }

                // collect vulnerability IDs from those open assessments
                List<Long> vulnIds = new ArrayList<>();
                for (Long asmtId : openAssessmentIds) {
                    vulnIds.addAll(em.createQuery(
                        "select v.id from Vulnerability v where v.assessmentId = :aid",
                        Long.class)
                        .setParameter("aid", asmtId)
                        .getResultList());
                }

                if (vulnIds.isEmpty()) {
                    System.out.println("[DocxPrecompiler] No vulnerabilities found in open assessments.");
                    return;
                }

                System.out.println("[DocxPrecompiler] Found " + vulnIds.size()
                    + " vulnerabilities in open assessments. Pre-compiling in batches of 50...");

                int batchSize = 50;
                int totalCompiled = 0;
                int totalSkipped = 0;
                long startTime = System.currentTimeMillis();

                for (int i = 0; i < vulnIds.size(); i += batchSize) {
                    int end = Math.min(i + batchSize, vulnIds.size());
                    List<Long> batchIds = vulnIds.subList(i, end);

                    DocxPrecompiler pre = new DocxPrecompiler(font, customCSS);

                    for (Long vulnId : batchIds) {
                        EntityManager vulnEm = null;
                        try {
                            // fresh EM per vuln — DocxPrecompiler.resolveImages()
                            // internally calls HibHelper.getEM() and closes it,
                            // which would close any shared EM we hold onto
                            vulnEm = HibHelper.getInstance().getEMF().createEntityManager();
                            Vulnerability v = vulnEm.find(Vulnerability.class, vulnId);
                            if (v == null) continue;

                            // check if cache is already valid — skip
                            // to avoid recompiling
                            if (isCacheValid(v, font)) {
                                totalSkipped++;
                                continue;
                            }

                            if (pre.compile(v)) {
                                HibHelper.getInstance().preJoin();
                                vulnEm.joinTransaction();
                                vulnEm.merge(v);
                                HibHelper.getInstance().commit();
                                totalCompiled++;
                            }
                        } catch (Exception e) {
                            System.err.println("[DocxPrecompiler] Error compiling vuln "
                                + vulnId + ": " + e.getMessage());
                        } finally {
                            if (vulnEm != null && vulnEm.isOpen()) {
                                vulnEm.close();
                            }
                        }
                    }

                    // progress logging
                    if ((i / batchSize) % 10 == 0 || end == vulnIds.size()) {
                        long elapsed = System.currentTimeMillis() - startTime;
                        System.out.println("[DocxPrecompiler] Progress: " + end + "/"
                            + vulnIds.size() + " processed (" + totalCompiled + " compiled, "
                            + totalSkipped + " skipped) — " + elapsed + "ms elapsed");
                    }
                }

                long elapsed = System.currentTimeMillis() - startTime;
                System.out.println("[DocxPrecompiler] Migration complete: " + totalCompiled
                    + " compiled, " + totalSkipped + " skipped (already cached) in "
                    + elapsed + "ms");

            } catch (Exception e) {
                System.err.println("[DocxPrecompiler] Migration failed: " + e.getMessage());
                e.printStackTrace();
            } finally {
                if (em != null && em.isOpen()) {
                    em.close();
                }
            }
        }, "docx-precompile-migration").start();
    }

    // checks whether a vulnerability's cache is valid for the current font
    private static boolean isCacheValid(Vulnerability v, String font) {
        String desc = v.getDescription() != null && !v.getDescription().isEmpty()
                ? v.getDescription() : (v.getDefaultVuln() != null ? v.getDefaultVuln().getDescription() : "");
        String rec = v.getRecommendation() != null && !v.getRecommendation().isEmpty()
                ? v.getRecommendation() : (v.getDefaultVuln() != null ? v.getDefaultVuln().getRecommendation() : "");
        String details = v.getDetails() != null ? v.getDetails() : "";

        if (!desc.isEmpty() && !DocxPrecompiler.contentHash(font, desc).equals(v.getCachedDescHash()))
            return false;
        if (!rec.isEmpty() && !DocxPrecompiler.contentHash(font, rec).equals(v.getCachedRecHash()))
            return false;
        if (!details.isEmpty() && !DocxPrecompiler.contentHash(font, details).equals(v.getCachedDetailsHash()))
            return false;

        // all non-empty fields must have both hash and XML populated
        if (!desc.isEmpty() && (v.getCachedDescXml() == null)) return false;
        if (!rec.isEmpty() && (v.getCachedRecXml() == null)) return false;
        if (!details.isEmpty() && (v.getCachedDetailsXml() == null)) return false;

        return true;
    }
}