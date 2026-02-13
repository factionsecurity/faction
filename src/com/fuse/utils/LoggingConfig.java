package com.fuse.utils;

import java.util.logging.Level;
import java.util.logging.Logger;

public class LoggingConfig {
    public static void configureOpenHTMLTopDFLogging() {
        // System.out.println("DEBUG: Configuring OpenHTMLToPDF logging
        // suppression...");

        // Try to suppress all openhtmltopdf loggers at different levels
        String[] loggers = {
                "com.openhtmltopdf.load",
                "com.openhtmltopdf.css-parse",
                "com.openhtmltopdf.match",
                "com.openhtmltopdf.cascade",
                "com.openhtmltopdf.render",
                "com.openhtmltopdf.layout",
                "com.openhtmltopdf.config",
                "com.openhtmltopdf.exception",
                "com.openhtmltopdf.general",
                "com.openhtmltopdf.init",
                "com.openhtmltopdf.junit",
                "com.openhtmltopdf"
        };

        for (String loggerName : loggers) {
            try {
                Logger logger = Logger.getLogger(loggerName);
                Level currentLevel = logger.getLevel();
                logger.setLevel(Level.OFF);
                logger.setUseParentHandlers(false);
                // System.out.println("DEBUG: Set logger " + loggerName + " from " +
                // currentLevel + " to OFF");
            } catch (Exception e) {
                System.out.println("DEBUG: Error setting logger " + loggerName + ": " + e.getMessage());
            }
        }

        // Note: We intentionally do NOT modify the root logger to preserve
        // System.out.println output
        // Only suppress the specific OpenHTMLToPDF loggers

        // Also try to set system properties for more aggressive suppression
        System.setProperty("com.openhtmltopdf.load.level", "OFF");
        System.setProperty("com.openhtmltopdf.css-parse.level", "OFF");
        System.setProperty("com.openhtmltopdf.match.level", "OFF");

        // System.out.println("DEBUG: OpenHTMLToPDF logging configuration complete");
    }
}