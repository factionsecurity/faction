package com.fuse.unittests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.fuse.dao.Comment;
import com.fuse.dao.Vulnerability;

/**
 * Peer review "notes" fields are rendered into live suneditor rich-text editors
 * on the TrackChanges page (peerreviewedit.js). Output-encoding the textarea does
 * not help there, because the editor reads textarea.value (already entity-decoded)
 * and injects it as innerHTML. The only defense is sanitizing the stored value, so
 * these setters must strip active content the way the description/recommendation/
 * details setters already do. Comment.exportAssessment() reconstructs a vuln through
 * these same setters on every display, so this also neutralizes notes that were
 * stored before the fix.
 */
public class NotesXssSanitizationTest {

	private static final String IMG = "<img src=x onerror=alert(document.cookie)>";
	private static final String SCRIPT = "<script>alert(1)</script>";
	private static final String TRACKED = "ok <span class=\"ins cts-1\">added</span>";

	private static void assertStripped(String value) {
		assertFalse("onerror handler must be removed: " + value, value.toLowerCase().contains("onerror"));
		assertFalse("script tag must be removed: " + value, value.toLowerCase().contains("<script"));
	}

	@Test
	public void vulnerabilityNoteSettersSanitize() {
		Vulnerability v = new Vulnerability();
		v.setDesc_notes(IMG);
		v.setRec_notes(SCRIPT);
		v.setDetail_notes(IMG + SCRIPT);
		assertStripped(v.getDesc_notes());
		assertStripped(v.getRec_notes());
		assertStripped(v.getDetail_notes());
	}

	@Test
	public void commentSummaryNoteSettersSanitize() {
		Comment c = new Comment();
		c.setSummary1_notes(IMG);
		c.setSummary2_notes(SCRIPT);
		assertStripped(c.getSummary1_notes());
		assertStripped(c.getSummary2_notes());
	}

	@Test
	public void trackChangesMarkupSurvives() {
		Vulnerability v = new Vulnerability();
		v.setDesc_notes(TRACKED);
		assertTrue("track-changes span must be preserved: " + v.getDesc_notes(),
				v.getDesc_notes().contains("cts-1"));
	}

	@Test
	public void plainNotesUnchanged() {
		Vulnerability v = new Vulnerability();
		v.setDesc_notes("<p></p>");
		assertEquals("<p></p>", v.getDesc_notes());
	}
}
