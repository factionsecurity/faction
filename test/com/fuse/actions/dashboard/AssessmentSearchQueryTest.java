package com.fuse.actions.dashboard;

import static org.junit.Assert.*;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Test;

/**
 * Verifies the assessment-search date-overlap condition emits every branch
 * needed to cover the supported overlap scenarios (spanning, contained,
 * starting/ending inside the window, still-open, and completed-in-window).
 */
public class AssessmentSearchQueryTest {

    private static final SimpleDateFormat ISO =
        new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");

    @Test
    public void conditionCoversAllOverlapBranches() throws Exception {
        SimpleDateFormat parse = new SimpleDateFormat("yyyy-MM-dd");
        Date start = parse.parse("2024-01-01");
        Date end = parse.parse("2024-02-01");

        String q = AssessmentSearchQuery.dateOverlapCondition(start, end, ISO);

        String startIso = ISO.format(start);
        String endIso = ISO.format(end);

        // Outer structure: started on/before the window end, AND one of the
        // overlap branches.
        assertTrue("uses $and", q.contains("$and"));
        assertTrue("uses $or", q.contains("$or"));
        assertTrue("constrains start", q.contains("\"start\""));
        assertTrue("start on/before window end",
            q.contains("\"start\": {$lte: ISODate(\"" + endIso + "\")}"));

        // Branch 1: scheduled interval overlaps (end on/after window start).
        assertTrue("end on/after window start",
            q.contains("\"end\": {$gte: ISODate(\"" + startIso + "\")}"));

        // Branch 2: still open / not completed.
        assertTrue("includes open assessments",
            q.contains("\"completed\": {$exists: false}"));

        // Branch 3: completed within the window.
        assertTrue("completed within window",
            q.contains("\"completed\": {$gte: ISODate(\"" + startIso
                + "\"), $lte: ISODate(\"" + endIso + "\")}"));
    }
}
