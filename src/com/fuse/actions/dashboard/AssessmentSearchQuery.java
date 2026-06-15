package com.fuse.actions.dashboard;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Shared MongoDB query fragment builder for the manager dashboard assessment
 * search (the dashboard view and both CSV exports), so the date-range semantics
 * stay in sync across all of them.
 */
final class AssessmentSearchQuery {

    private AssessmentSearchQuery() {
    }

    /**
     * Builds the {@code $and: [...]} condition selecting assessments whose
     * lifecycle overlaps the search window [rangeStart, rangeEnd]. An assessment
     * is included when it started on or before the end of the window and any of
     * the following hold:
     * <ul>
     *   <li>it ends on or after the window start (its scheduled interval overlaps
     *       the window);</li>
     *   <li>it is still open / not completed (ongoing through the window);</li>
     *   <li>it was completed within the window.</li>
     * </ul>
     * Together these cover every overlap case: assessments spanning the entire
     * window, sitting fully inside it, starting or ending inside it, still-open
     * assessments whose scheduled dates predate the window, and assessments whose
     * completion date falls within the window.
     *
     * @param rangeStart inclusive window start
     * @param rangeEnd   window end; callers that want an inclusive end pass the
     *                   selected end date + 1 day
     * @param sdf        formatter producing ISODate-compatible timestamps
     */
    static String dateOverlapCondition(Date rangeStart, Date rangeEnd, SimpleDateFormat sdf) {
        String start = sdf.format(rangeStart);
        String end = sdf.format(rangeEnd);
        StringBuilder q = new StringBuilder();
        q.append("$and: [");
        // Must have started on or before the end of the search window.
        q.append(" { \"start\": {$lte: ISODate(\"").append(end).append("\")}}, ");
        // ...and overlap the window in at least one of these ways:
        q.append(" { $or: [ ");
        // ends on/after the window start (scheduled interval overlaps the window)
        q.append("   { \"end\": {$gte: ISODate(\"").append(start).append("\")}},");
        // still open (not completed), so it is ongoing through the window
        q.append("   { \"completed\": {$exists: false}},");
        // completed within the window
        q.append("   { \"completed\": {$gte: ISODate(\"").append(start)
                .append("\"), $lte: ISODate(\"").append(end).append("\")}}");
        q.append(" ]}");
        q.append("]");
        return q.toString();
    }
}
