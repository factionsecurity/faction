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
import com.fuse.dao.User;
import com.fuse.dao.Vulnerability;
import com.fuse.dao.DefaultVulnerability;
import com.fuse.dao.Category;
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
     * Bootstrap method to create an assessment with 500 vulnerabilities and 1000 images
     * Uncomment this method to run it during application startup
     */
    private void createTestAssessmentWithVulnerabilitiesAndImages() {
        EntityManager em = null;
        try {
            System.out.println("Creating test assessment with 500 vulnerabilities and 1000 images...");
            em = HibHelper.getInstance().getEMF().createEntityManager();
            
            // Create base64 encoded image from faction-logo.png
            String base64Image = null;
            try {
                byte[] imageBytes = Files.readAllBytes(Paths.get("/Users/joshsummitt/Code/faction-all/free/faction/WebContent/faction-logo.png"));
                base64Image = "data:image/png;base64," + Base64.getEncoder().encodeToString(imageBytes);
            } catch (IOException e) {
                System.err.println("Error reading faction logo file: " + e.getMessage());
                return;
            }
            
            // Create 1000 Images
            List<Image> images = new ArrayList<>();
            for (int i = 0; i < 1000; i++) {
                Image image = new Image();
                image.setBase64Image(base64Image);
                image.setName("Test Image " + i);
                image.setContentType("image/png");
                images.add(image);
            }
            
            // Create Assessment
            Assessment assessment = new Assessment();
            assessment.setName("Test Assessment with 500 Vulnerabilities and 1000 Images");
            assessment.setSummary("This is a test assessment created for development purposes");
            assessment.setRiskAnalysis("Risk analysis for test assessment");
            assessment.setStart(new Date());
            assessment.setEnd(new Date());
            
            // Assign user with ID 2 as the assessor
            User assessor = em.find(User.class, 2L);
            if (assessor != null) {
                List<User> assessors = new ArrayList<>();
                assessors.add(assessor);
                assessment.setAssessor(assessors);
            } else {
                System.err.println("User with ID 2 not found for assessor assignment");
            }
            
            // Set the images to the assessment
            assessment.setImages(images);
            
            // For JTA transactions, use the TransactionManager from HibHelper
            HibHelper.getInstance().preJoin();
            em.persist(assessment);
            em.flush();
            
            // Create 500 vulnerabilities with image links (but don't try to set them on the assessment directly)
            // This avoids the collection management issue with cascade="all-delete-orphan"
            int imgId=0;
            for (int i = 0; i < 500; i++) {
                Vulnerability vuln = new Vulnerability();
                vuln.setName("Test Vulnerability " + i);
                vuln.setDescription("This is a test vulnerability description for vulnerability number " + i);
                vuln.setRecommendation("Recommendation for vulnerability " + i);
                vuln.setOverall(5L); // Set overall level to 5 (Critical)
                vuln.setAssessmentId(assessment.getId());
                
                // Create the details with image links
                StringBuilder details = new StringBuilder();
                details.append("<p>Vulnerability details with images:</p>");
                
                // Add image link using a placeholder that will be replaced with actual assessment ID
                String imageId1 = assessment.getId().toString() + ":" + images.get(imgId++).getGuid();
                details.append("<img src=\"getImage?id=" + imageId1 + "\" alt=\"image.png\">");
                String imageId2 = assessment.getId().toString() + ":" + images.get(imgId++).getGuid();
                details.append("<img src=\"getImage?id=" + imageId2 + "\" alt=\"image.png\">");
                
                vuln.setDescription(details.toString());
                
                // Persist vulnerability directly rather than trying to manage the collection
                em.persist(vuln);
                assessment.getVulns().add(vuln);
            }
           
            em.merge(assessment);
            HibHelper.getInstance().commit();
            
            System.out.println("Successfully created test assessment with 500 vulnerabilities and 1000 images");
            
        } catch (Exception e) {
            System.err.println("Error creating test assessment: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }
}