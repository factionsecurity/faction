package com.faction.reporting;

public class ReportFeatures {

    public static byte [] finalizeReport( byte [] report, String reportType){
        return report;
    }

    public static String [] getReportOptions() {
        return new String[] {"docx"};
    }

    public static Boolean allowSections(){
        return false;
    }

    public static String [] getFeatures(String features){
        return new String[0];
    }

}
