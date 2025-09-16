package com.fuse.actions.bootstrap;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import com.fuse.dao.Status;


@WebListener
public class AppBootstrapListener implements ServletContextListener {
    
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("Application starting - checking for defaults");
        createDefautlStatusIfNeeded();
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
}