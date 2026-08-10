package com.fuse.actions.retests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Date;

import org.junit.Test;

import com.fuse.dao.Vulnerability;

/**
 * Unit tests for the per-retest close-environment behavior added to
 * {@link VerificationQueue#completeVerification()}. The pure helper
 * {@link VerificationQueue#applyCloseEnvironment(Vulnerability, String)} is
 * exercised directly here so the branching (which takes precedence over the
 * system verificationOption) can be verified without a database or servlet
 * container.
 */
public class VerificationQueueCloseEnvTest {

	private static Vulnerability vuln() {
		Vulnerability v = new Vulnerability();
		v.setStatus(Vulnerability.StatusOpen);
		return v;
	}

	@Test
	public void closeInDevSetsDevClosedAndStatus() {
		Vulnerability v = vuln();
		String env = VerificationQueue.applyCloseEnvironment(v, "dev");
		assertEquals("Development", env);
		assertNotNull("devClosed should be set", v.getDevClosed());
		assertEquals(Vulnerability.StatusClosedInDev, v.getStatus());
	}

	@Test
	public void closeInStagingSetsStagingClosedAndStatus() {
		Vulnerability v = vuln();
		String env = VerificationQueue.applyCloseEnvironment(v, "staging");
		assertEquals("Staging", env);
		assertNotNull("stagingClosed should be set", v.getStagingClosed());
		assertEquals(Vulnerability.StatusClosedInStaging, v.getStatus());
	}

	@Test
	public void closeInProdSetsClosedAndStatus() {
		Vulnerability v = vuln();
		String env = VerificationQueue.applyCloseEnvironment(v, "prod");
		assertEquals("Production", env);
		assertNotNull("closed should be set", v.getClosed());
		assertEquals(Vulnerability.StatusClosed, v.getStatus());
	}

	@Test
	public void closeEnvIsCaseInsensitive() {
		Vulnerability v = vuln();
		String env = VerificationQueue.applyCloseEnvironment(v, "PROD");
		assertEquals("Production", env);
		assertNotNull("closed should be set", v.getClosed());
		assertEquals(Vulnerability.StatusClosed, v.getStatus());
	}

	@Test
	public void noCloseEnvLeavesVulnerabilityUntouched() {
		// When the assessor chooses "Don't close", the helper returns null so
		// completeVerification() falls back to the system verificationOption.
		Vulnerability v = vuln();
		assertNull(VerificationQueue.applyCloseEnvironment(v, ""));
		assertNull(VerificationQueue.applyCloseEnvironment(v, null));
		assertEquals("Status should be unchanged", Vulnerability.StatusOpen, v.getStatus());
		assertNull("closed should remain null", v.getClosed());
		assertNull("devClosed should remain null", v.getDevClosed());
		assertNull("stagingClosed should remain null", v.getStagingClosed());
	}

	@Test
	public void unknownCloseEnvIsIgnored() {
		Vulnerability v = vuln();
		assertNull(VerificationQueue.applyCloseEnvironment(v, "qa"));
		assertEquals("Status should be unchanged", Vulnerability.StatusOpen, v.getStatus());
	}

	@Test
	public void nullVulnerabilityIsSafe() {
		assertNull(VerificationQueue.applyCloseEnvironment(null, "prod"));
	}

	@Test
	public void onlyTheChosenEnvironmentIsClosed() {
		// Closing in prod must not also set dev/staging close dates.
		Date before = new Date();
		Vulnerability v = vuln();
		VerificationQueue.applyCloseEnvironment(v, "prod");
		assertNotNull(v.getClosed());
		assertNull("devClosed must not be set when closing in prod", v.getDevClosed());
		assertNull("stagingClosed must not be set when closing in prod", v.getStagingClosed());
		assertEquals(Vulnerability.StatusClosed, v.getStatus());

		// Sanity: a close date was actually recorded.
		assertNotNull(v.getClosed());
		assert (v.getClosed().getTime() >= before.getTime());
	}
}
