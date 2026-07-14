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
import com.fuse.utils.ReportImageScaler;


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
        prepareReportImageRenditions();
        
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
     * Backfill: prepares the report-ready rendition (see ReportImageScaler)
     * for stored images that don't have one for the current width cap. New
     * uploads are prepared inline at upload time; this covers images that
     * existed before, plus every image after a FACTION_REPORT_IMAGE_MAX_WIDTH
     * change. Runs on a background thread so startup isn't blocked; images
     * are loaded and committed one at a time so heap usage stays flat.
     *
     * When the renditions are done, the same thread pre-compiles vuln HTML
     * fields for open assessments (see DocxPrecompiler) — run second so the
     * compiled XML embeds the freshly prepared renditions instead of
     * re-downscaling originals.
     */
    private void prepareReportImageRenditions() {
        new Thread(() -> {
            backfillImageRenditions();
            precompileOpenAssessmentVulns();
        }, "report-image-backfill").start();
    }

    private void backfillImageRenditions() {
            try {
                int maxWidth = ReportImageScaler.configuredMaxWidth();
                if (maxWidth <= 0) {
                    return;
                }
                List<Long> ids;
                EntityManager em = HibHelper.getInstance().getEMF().createEntityManager();
                try {
                    // only images without a rendition for the current cap;
                    // fall back to a full scan if the OGM query translation
                    // rejects the null/inequality combination
                    try {
                        ids = em.createQuery(
                            "select i.id from Image i where i.reportWidth is null or i.reportWidth <> :w",
                            Long.class).setParameter("w", maxWidth).getResultList();
                    } catch (Exception e) {
                        ids = em.createQuery("select i.id from Image i", Long.class).getResultList();
                    }
                } finally {
                    em.close();
                }
                if (ids.isEmpty()) {
                    return;
                }
                System.out.println("[ReportImages] Preparing report renditions for up to "
                    + ids.size() + " images (width cap " + maxWidth + ")...");
                int prepared = 0;
                long start = System.currentTimeMillis();
                for (Long id : ids) {
                    EntityManager iem = HibHelper.getInstance().getEMF().createEntityManager();
                    try {
                        Image img = iem.find(Image.class, id);
                        if (img == null || !ReportImageScaler.prepareReportRendition(img)) {
                            continue;
                        }
                        HibHelper.getInstance().preJoin();
                        iem.joinTransaction();
                        iem.merge(img);
                        HibHelper.getInstance().commit();
                        prepared++;
                    } catch (Exception e) {
                        System.err.println("[ReportImages] Error preparing image " + id + ": " + e.getMessage());
                    } finally {
                        if (iem.isOpen()) {
                            iem.close();
                        }
                    }
                }
                System.out.println("[ReportImages] Backfill complete: " + prepared + "/" + ids.size()
                    + " images prepared in " + (System.currentTimeMillis() - start) + "ms");
            } catch (Exception e) {
                System.err.println("[ReportImages] Backfill failed: " + e.getMessage());
                e.printStackTrace();
            }
    }

    /**
     * Pre-compiles HTML fields (desc/rec/details) into cached OOXML for
     * all vulnerabilities in open assessments (see DocxPrecompiler).
     * Vulnerabilities whose cache already matches their current content
     * are skipped, so re-runs at every startup only pay the id query plus
     * a per-vuln hash check — this is also how a CACHE_VERSION bump
     * recompiles the whole install. New and updated vulnerabilities get
     * their cache populated at save time by the save-path hooks.
     */
    private void precompileOpenAssessmentVulns() {
        EntityManager em = null;
        try {
            System.out.println("[DocxPrecompiler] Checking pre-compiled caches for open assessments...");
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

            List<Long> vulnIds = new ArrayList<>();
            for (Long asmtId : openAssessmentIds) {
                vulnIds.addAll(em.createQuery(
                    "select v.id from Vulnerability v where v.assessmentId = :aid",
                    Long.class)
                    .setParameter("aid", asmtId)
                    .getResultList());
            }
            em.close();
            em = null;
            if (vulnIds.isEmpty()) {
                System.out.println("[DocxPrecompiler] No vulnerabilities found in open assessments.");
                return;
            }

            System.out.println("[DocxPrecompiler] Found " + vulnIds.size()
                + " vulnerabilities in open assessments.");

            int totalCompiled = 0;
            int totalSkipped = 0;
            long startTime = System.currentTimeMillis();

            for (Long vulnId : vulnIds) {
                // fresh EM per vuln so a multi-MB cached payload never
                // accumulates in a shared persistence context
                EntityManager vulnEm = null;
                try {
                    vulnEm = HibHelper.getInstance().getEMF().createEntityManager();
                    Vulnerability v = vulnEm.find(Vulnerability.class, vulnId);
                    if (v == null) continue;

                    // fetch the parent assessment so extensions can run
                    // during pre-compilation
                    Assessment asmt = v.getAssessmentId() > 0
                        ? vulnEm.find(Assessment.class, v.getAssessmentId()) : null;
                    DocxPrecompiler pre = new DocxPrecompiler(font, customCSS, asmt);
                    if (pre.compile(v)) {
                        HibHelper.getInstance().preJoin();
                        vulnEm.joinTransaction();
                        vulnEm.merge(v);
                        HibHelper.getInstance().commit();
                        totalCompiled++;
                    } else {
                        totalSkipped++;
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

            System.out.println("[DocxPrecompiler] Migration complete: " + totalCompiled
                + " compiled, " + totalSkipped + " skipped (already cached) in "
                + (System.currentTimeMillis() - startTime) + "ms");
        } catch (Exception e) {
            System.err.println("[DocxPrecompiler] Migration failed: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }

}