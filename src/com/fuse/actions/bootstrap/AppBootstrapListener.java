package com.fuse.actions.bootstrap;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import javax.persistence.EntityManager;
import java.util.List;

import com.fuse.dao.Status;
import com.fuse.dao.Assessment;
import com.fuse.dao.HibHelper;


@WebListener
public class AppBootstrapListener implements ServletContextListener {
    
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("Application starting - checking for defaults");
        createDefautlStatusIfNeeded();
        fixAssessmentStatuses();
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
}