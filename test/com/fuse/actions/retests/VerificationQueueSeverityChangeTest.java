package com.fuse.actions.retests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.fuse.dao.RiskLevel;
import com.fuse.dao.Vulnerability;

/**
 * Unit tests for the per-retest severity-change behavior. The pure helpers
 * {@link VerificationQueue#validateSeverityChange} and
 * {@link VerificationQueue#applySeverityChange} are exercised directly so
 * the validation + application logic can be verified without a DB or servlet
 * container.
 */
public class VerificationQueueSeverityChangeTest {

	private static List<RiskLevel> testLevels() {
		List<RiskLevel> levels = new ArrayList<>();
		levels.add(makeLevel(0, "Informational"));
		levels.add(makeLevel(3, "Low"));
		levels.add(makeLevel(5, "Medium"));
		levels.add(makeLevel(7, "High"));
		levels.add(makeLevel(9, "Critical"));
		return levels;
	}

	private static RiskLevel makeLevel(int riskId, String name) {
		RiskLevel rl = new RiskLevel();
		rl.setRiskId(riskId);
		rl.setRisk(name);
		return rl;
	}

	private static Vulnerability vuln(Long overall) {
		Vulnerability v = new Vulnerability();
		v.setOverall(overall);
		return v;
	}

	// --- validateSeverityChange ---

	@Test
	public void noChangeWhenNewOverallIsNull() {
		assertNull(VerificationQueue.validateSeverityChange(5L, null, ""));
	}

	@Test
	public void noChangeWhenNewOverallEqualsOriginal() {
		assertNull(VerificationQueue.validateSeverityChange(5L, 5L, ""));
	}

	@Test
	public void changedWithoutAnnotationIsRejected() {
		String error = VerificationQueue.validateSeverityChange(5L, 3L, "");
		assertNotNull(error);
		assertTrue("error should mention annotation", error.toLowerCase().contains("annotation"));
	}

	@Test
	public void changedWithBlankAnnotationIsRejected() {
		assertNotNull(VerificationQueue.validateSeverityChange(5L, 7L, "   "));
	}

	@Test
	public void changedWithAnnotationPasses() {
		assertNull(VerificationQueue.validateSeverityChange(5L, 3L, "Reduced impact after retest."));
	}

	// --- applySeverityChange ---

	@Test
	public void nullNewOverallIsNoOp() {
		Vulnerability v = vuln(5L);
		assertNull(VerificationQueue.applySeverityChange(v, 5L, null, testLevels(), "note"));
		assertEquals(Long.valueOf(5L), v.getOverall());
	}

	@Test
	public void sameOverallIsNoOp() {
		Vulnerability v = vuln(5L);
		assertNull(VerificationQueue.applySeverityChange(v, 5L, 5L, testLevels(), "note"));
		assertEquals(Long.valueOf(5L), v.getOverall());
	}

	@Test
	public void downgradeSetsOverallAndReturnsTable() {
		Vulnerability v = vuln(7L);
		String note = VerificationQueue.applySeverityChange(v, 7L, 3L, testLevels(),
				"Lower exploitation likelihood confirmed.");
		assertNotNull(note);
		assertEquals(Long.valueOf(3L), v.getOverall());
		assertTrue("note should contain chSevTable", note.contains("chSevTable"));
		assertTrue("note should contain Previous header", note.contains("Previous"));
		assertTrue("note should contain New header", note.contains("New"));
		assertTrue("note should contain old severity", note.contains("High"));
		assertTrue("note should contain new severity", note.contains("Low"));
		assertTrue("note should include annotation", note.contains("Lower exploitation likelihood confirmed."));
	}

	@Test
	public void upgradeSetsOverallAndReturnsTable() {
		Vulnerability v = vuln(3L);
		String note = VerificationQueue.applySeverityChange(v, 3L, 9L, testLevels(),
				"Exploit confirmed in production.");
		assertNotNull(note);
		assertEquals(Long.valueOf(9L), v.getOverall());
		assertTrue("note should contain chSevTable", note.contains("chSevTable"));
		assertTrue("note should contain old severity", note.contains("Low"));
		assertTrue("note should contain new severity", note.contains("Critical"));
		assertTrue("note should include annotation", note.contains("Exploit confirmed in production."));
	}

	@Test
	public void nullVulnerabilityIsSafe() {
		assertNull(VerificationQueue.applySeverityChange(null, 5L, 9L, testLevels(), "note"));
	}

	@Test
	public void nullLevelsFallbackToUnassigned() {
		Vulnerability v = vuln(5L);
		String note = VerificationQueue.applySeverityChange(v, 5L, 9L, null, "note");
		assertNotNull(note);
		assertEquals(Long.valueOf(9L), v.getOverall());
		assertTrue("note should contain chSevTable", note.contains("chSevTable"));
		assertTrue("note should contain Unassigned fallback", note.contains("Unassigned"));
	}
}
