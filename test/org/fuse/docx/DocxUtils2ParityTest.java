package org.fuse.docx;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Random;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;

import org.docx4j.TextUtils;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.junit.Assume;
import org.junit.Test;
import org.mockito.Mockito;

import com.fuse.dao.AppStore;
import com.fuse.dao.Assessment;
import com.fuse.dao.AssessmentType;
import com.fuse.dao.CustomField;
import com.fuse.dao.RiskLevel;
import com.fuse.dao.Teams;
import com.fuse.dao.User;
import com.fuse.dao.Vulnerability;
import com.fuse.reporting.DocxUtils;
import com.fuse.reporting.DocxUtils2;
import com.fuse.reporting.DocxPrecompiler;
import com.fuse.utils.MethodProfiler;

/**
 * Parity test harness for DocxUtils vs DocxUtils2.
 *
 * Runs both implementations against an identical template-package +
 * assessment fixture and asserts that every vulnerability name and every
 * HTML field (desc/rec/details) lands in the generated document with no
 * leaked template markers.
 *
 * The default run uses a small fixture (6 vulns) so it stays under a second
 * and exercises the core code paths. Set -Dfaction.parity.harness=true to
 * also run a 400-vuln comparison that prints MethodProfiler reports side by
 * side — this is how to compare wall time end-to-end before flipping
 * traffic.
 *
 * Images are inline PNG data URIs (no `getImage?id=` references) so neither
 * path touches MongoDB: image-resolving code is exercised against real
 * bytes, but the test stays hermetic.
 */
public class DocxUtils2ParityTest {

	// ============================================================
	// Default test: small fixture, parity assertions
	// ============================================================

	@Test
	public void parityBothImplementationsProduceAllFields() throws Exception {
		int vulnCount = 6;
		Assessment assessment = buildAssessment(vulnCount);
		WordprocessingMLPackage template = buildTemplatePackage();

		// snapshot the template to bytes so DocxUtils2 gets a byte-for-byte
		// identical template after going through ZIP round-trip
		ByteArrayOutputStream templateBytes = new ByteArrayOutputStream();
		template.save(templateBytes);

		// ----- run DocxUtils (the original) -----
		WordprocessingMLPackage mlpOrig = WordprocessingMLPackage.load(new ByteArrayInputStream(templateBytes.toByteArray()));
		EntityManagerFactory emf = stubEntityManagerFactory();
		DocxUtils orig = new DocxUtils(emf, mlpOrig, assessment);
		orig.FONT = "Calibri";
		MethodProfiler.setEnabled(true);
		MethodProfiler.clearStats();
		long origStart = System.currentTimeMillis();
		mlpOrig = orig.generateDocx("p { margin: 0; }");
		long origElapsed = System.currentTimeMillis() - origStart;
		ByteArrayOutputStream origBytes = new ByteArrayOutputStream();
		mlpOrig.save(origBytes);
		String origText = extractText(mlpOrig);

		// ----- run DocxUtils2 (raw-XML path) -----
		DocxUtils2 gen2 = new DocxUtils2(emf, assessment);
		gen2.FONT = "Calibri";
		MethodProfiler.clearStats();
		long gen2Start = System.currentTimeMillis();
		byte[] gen2Output = gen2.generateReport(new ByteArrayInputStream(templateBytes.toByteArray()), "p { margin: 0; }");
		long gen2Elapsed = System.currentTimeMillis() - gen2Start;
		assertNotNull("DocxUtils2 output was null", gen2Output);
		assertTrue("DocxUtils2 output was empty", gen2Output.length > 0);
		WordprocessingMLPackage mlp2 = WordprocessingMLPackage.load(new ByteArrayInputStream(gen2Output));
		String gen2Text = extractText(mlp2);

		System.out.println("\n=== Parity test (vulns=" + vulnCount + ") ===");
		System.out.println("DocxUtils wall:  " + origElapsed + "ms, size=" + origBytes.size() + "B, " + origText.length() + " chars");
		System.out.println("DocxUtils2 wall: " + gen2Elapsed + "ms, size=" + gen2Output.length + "B, " + gen2Text.length() + " chars");

		// ----- assertions on every field per vulnerability -----
		for (int n = 1; n <= vulnCount; n++) {
			assertTrue("DocxUtils missing vuln name " + n, origText.contains("Parity Issue " + n));
			assertTrue("DocxUtils missing desc " + n, origText.contains("ParityDesc" + n));
			assertTrue("DocxUtils missing rec " + n, origText.contains("ParityRec" + n));
			assertTrue("DocxUtils missing details " + n, origText.contains("ParityDetails" + n));
		}
		for (int n = 1; n <= vulnCount; n++) {
			assertTrue("DocxUtils2 missing vuln name " + n, gen2Text.contains("Parity Issue " + n));
			assertTrue("DocxUtils2 missing desc " + n, gen2Text.contains("ParityDesc" + n));
			assertTrue("DocxUtils2 missing rec " + n, gen2Text.contains("ParityRec" + n));
			assertTrue("DocxUtils2 missing details " + n, gen2Text.contains("ParityDetails" + n));
		}

		// ----- no leaked template markers in either output -----
		assertFalse("DocxUtils leaked ${fiBegin}", origText.contains("${fiBegin"));
		assertFalse("DocxUtils leaked ${fiEnd}", origText.contains("${fiEnd"));
		assertFalse("DocxUtils leaked ${vulnName}", origText.contains("${vulnName}"));
		assertFalse("DocxUtils leaked ${desc}", origText.contains("${desc}"));
		assertFalse("DocxUtils leaked ${rec}", origText.contains("${rec}"));
		assertFalse("DocxUtils leaked ${details}", origText.contains("${details}"));
		assertFalse("DocxUtils leaked ${loop", origText.contains("${loop"));
		assertFalse("DocxUtils leaked ${asmtName}", origText.contains("${asmtName}"));
		assertFalse("DocxUtils leaked split marker", origText.contains("FCT-FIELD-SPLIT"));

		assertFalse("DocxUtils2 leaked ${fiBegin}", gen2Text.contains("${fiBegin"));
		assertFalse("DocxUtils2 leaked ${fiEnd}", gen2Text.contains("${fiEnd"));
		assertFalse("DocxUtils2 leaked ${vulnName}", gen2Text.contains("${vulnName}"));
		assertFalse("DocxUtils2 leaked ${desc}", gen2Text.contains("${desc}"));
		assertFalse("DocxUtils2 leaked ${rec}", gen2Text.contains("${rec}"));
		assertFalse("DocxUtils2 leaked ${details}", gen2Text.contains("${details}"));
		assertFalse("DocxUtils2 leaked ${loop", gen2Text.contains("${loop"));
		assertFalse("DocxUtils2 leaked ${asmtName}", gen2Text.contains("${asmtName}"));
		assertFalse("DocxUtils2 leaked split marker", gen2Text.contains("FCT-FIELD-SPLIT"));

		// ----- assessment-level variables land in both outputs -----
		assertTrue("DocxUtils missing asmt name", origText.contains("Test PCI Assessment"));
		assertTrue("DocxUtils2 missing asmt name", gen2Text.contains("Test PCI Assessment"));
		assertTrue("DocxUtils missing asmt id", origText.contains("1337"));
		assertTrue("DocxUtils2 missing asmt id", gen2Text.contains("1337"));
		assertTrue("DocxUtils missing assessors", origText.contains("Bob Dobbs"));
		assertTrue("DocxUtils2 missing assessors", gen2Text.contains("Bob Dobbs"));
		assertTrue("DocxUtils missing riskTotal", origText.contains("6"));
		assertTrue("DocxUtils2 missing riskTotal", gen2Text.contains("6"));
	}

	// ============================================================
	// Perf harness: gated, 400-vuln comparison with profiler
	// output for both implementations side by side. Run with
	// -Dfaction.parity.harness=true -Dfaction.parity.vulns=400
	// ============================================================

	@Test
	public void parity400VulnsTimingComparison() throws Exception {
		Assume.assumeTrue("parity perf harness disabled; pass -Dfaction.parity.harness=true to enable",
				"true".equals(System.getProperty("faction.parity.harness")));
		int vulnCount = Integer.parseInt(System.getProperty("faction.parity.vulns", "400"));

		Assessment assessment = buildAssessment(vulnCount);
		WordprocessingMLPackage template = buildTemplatePackage();
		ByteArrayOutputStream templateBytes = new ByteArrayOutputStream();
		template.save(templateBytes);
		EntityManagerFactory emf = stubEntityManagerFactory();

		// ----- DocxUtils (original) -----
		WordprocessingMLPackage mlpOrig = WordprocessingMLPackage.load(new ByteArrayInputStream(templateBytes.toByteArray()));
		DocxUtils orig = new DocxUtils(emf, mlpOrig, assessment);
		orig.FONT = "Calibri";
		MethodProfiler.setEnabled(true);
		MethodProfiler.clearStats();
		long origStart = System.currentTimeMillis();
		mlpOrig = orig.generateDocx("p { margin: 0; } img { width: 50% !important; height: auto !important; }");
		long origElapsed = System.currentTimeMillis() - origStart;
		long origSize = sizeOf(mlpOrig);
		System.out.println("\n=== DocxUtils (original): vulns=" + vulnCount + " wall=" + origElapsed + "ms size=" + origSize + "B ===");
		MethodProfiler.printReport();
		MethodProfiler.clearStats();

		// ----- DocxUtils2 (raw XML) -----
		DocxUtils2 gen2 = new DocxUtils2(emf, assessment);
		gen2.FONT = "Calibri";
		MethodProfiler.setEnabled(true);
		MethodProfiler.clearStats();
		long gen2Start = System.currentTimeMillis();
		byte[] gen2Output = gen2.generateReport(new ByteArrayInputStream(templateBytes.toByteArray()),
				"p { margin: 0; } img { width: 50% !important; height: auto !important; }");
		long gen2Elapsed = System.currentTimeMillis() - gen2Start;
		System.out.println("\n=== DocxUtils2 (raw XML): vulns=" + vulnCount + " wall=" + gen2Elapsed + "ms size=" + gen2Output.length + "B ===");
		MethodProfiler.printReport();
		MethodProfiler.clearStats();

		System.out.println("\n=== Speedup: " + String.format("%.2fx", (double) origElapsed / Math.max(gen2Elapsed, 1)) + " ===");

		// spot-check that the first and last vuln fields are present
		String gen2Text;
		{
			WordprocessingMLPackage mlp2 = WordprocessingMLPackage.load(new ByteArrayInputStream(gen2Output));
			gen2Text = extractText(mlp2);
		}
		assertTrue("DocxUtils2 missing first vuln name", gen2Text.contains("Parity Issue 1"));
		assertTrue("DocxUtils2 missing last vuln name", gen2Text.contains("Parity Issue " + vulnCount));
		assertFalse("DocxUtils2 leaked ${fiBegin", gen2Text.contains("${fiBegin"));
		assertFalse("DocxUtils2 leaked ${desc", gen2Text.contains("${desc"));
	}

	// ============================================================
	// Perf harness with pre-compiled cache: compares live vs cached
	// report generation. Run with:
	//   -Dfaction.parity.harness=true -Dfaction.parity.vulns=500
	// ============================================================

	@Test
	public void cachedVsLivePerformanceComparison() throws Exception {
		Assume.assumeTrue("cached perf harness disabled; pass -Dfaction.parity.harness=true to enable",
				"true".equals(System.getProperty("faction.parity.harness")));
		int vulnCount = Integer.parseInt(System.getProperty("faction.parity.vulns", "500"));
		String customCSS = "p { margin: 0; } img { width: 50% !important; height: auto !important; }";

		Assessment assessment = buildAssessment(vulnCount);

		// pre-compile all vulnerabilities' HTML fields
		long precompileStart = System.currentTimeMillis();
		DocxPrecompiler pre = new DocxPrecompiler("Calibri", customCSS);
		int compiled = 0;
		for (Vulnerability v : assessment.getVulns()) {
			if (pre.compile(v)) compiled++;
		}
		long precompileElapsed = System.currentTimeMillis() - precompileStart;
		System.out.println("\n=== Pre-compile: " + compiled + "/" + vulnCount + " vulns compiled in "
				+ precompileElapsed + "ms ===");

		// build a fresh template for each run
		EntityManagerFactory emf = stubEntityManagerFactory();

		// ----- LIVE (no cache) -----
		// create a fresh assessment with no cached XML
		Assessment liveAssessment = buildAssessment(vulnCount);
		WordprocessingMLPackage liveTemplate = buildTemplatePackage();
		ByteArrayOutputStream liveTemplateBytes = new ByteArrayOutputStream();
		liveTemplate.save(liveTemplateBytes);
		DocxUtils liveGen = new DocxUtils(emf, WordprocessingMLPackage.load(
				new ByteArrayInputStream(liveTemplateBytes.toByteArray())), liveAssessment);
		liveGen.FONT = "Calibri";
		MethodProfiler.setEnabled(true);
		MethodProfiler.clearStats();
		long liveStart = System.currentTimeMillis();
		liveGen.generateDocx(customCSS);
		long liveElapsed = System.currentTimeMillis() - liveStart;
		System.out.println("\n=== DocxUtils LIVE (no cache): vulns=" + vulnCount
				+ " wall=" + liveElapsed + "ms ===");
		MethodProfiler.printReport();
		MethodProfiler.clearStats();

		// ----- CACHED -----
		WordprocessingMLPackage cachedTemplate = buildTemplatePackage();
		ByteArrayOutputStream cachedTemplateBytes = new ByteArrayOutputStream();
		cachedTemplate.save(cachedTemplateBytes);
		DocxUtils cachedGen = new DocxUtils(emf, WordprocessingMLPackage.load(
				new ByteArrayInputStream(cachedTemplateBytes.toByteArray())), assessment);
		cachedGen.FONT = "Calibri";
		MethodProfiler.setEnabled(true);
		MethodProfiler.clearStats();
		long cachedStart = System.currentTimeMillis();
		cachedGen.generateDocx(customCSS);
		long cachedElapsed = System.currentTimeMillis() - cachedStart;
		System.out.println("\n=== DocxUtils CACHED (precompiled): vulns=" + vulnCount
				+ " wall=" + cachedElapsed + "ms ===");
		MethodProfiler.printReport();
		MethodProfiler.clearStats();

		System.out.println("\n=== Speedup from cache: "
				+ String.format("%.2fx", (double) liveElapsed / Math.max(cachedElapsed, 1)) + " ===");
		System.out.println("=== Pre-compile cost: " + precompileElapsed + "ms (amortized across reports) ===");
	}

	// ============================================================
	// Fixtures
	// ============================================================

	private Assessment buildAssessment(int vulnCount) {
		Teams team = new Teams();
		team.setId(123L);
		team.setTeamName("Hacking Team");
		AssessmentType type = new AssessmentType();
		type.setId(1235L);
		type.setType("Assessment Type");

		List<RiskLevel> levels = new ArrayList<>();
		String[] riskNames = { "Informational", "Recommended", "Low", "Medium", "High", "Critical" };
		for (int i = 0; i < 10; i++) {
			RiskLevel level = new RiskLevel();
			level.setRiskId(i);
			if (i < riskNames.length) {
				level.setRisk(riskNames[i]);
			}
			levels.add(level);
		}

		Assessment a = new Assessment();
		User u = new User();
		u.setId(1L);
		u.setFname("Bob");
		u.setLname("Dobbs");
		u.setEmail("bdobs@supersecure.com");
		u.setTeam(team);
		a.setName("Test PCI Assessment");
		a.setId(1337L);
		a.setEngagement(u);
		List<User> hackers = new ArrayList<>();
		hackers.add(u);
		hackers.add(u);
		a.setType(type);
		a.setAssessor(hackers);
		a.setRemediation(u);
		a.setAppId("1337");
		a.setRiskAnalysis("Risk analysis text content for parity check");
		a.setSummary("Summary section content for parity check");
		a.setVulns(new ArrayList<>());
		a.setStart(new Date());
		a.setEnd(new Date());
		a.setGuid("parity-guid");

		Random rnd = new Random(42);
		for (int i = 1; i <= vulnCount; i++) {
			Vulnerability v = new Vulnerability();
			v.setLevels(levels);
			v.setId((long) i);
			v.setName("Parity Issue " + i);
			v.setImpact((long) (i % 5) + 1);
			v.setLikelyhood((long) (i % 5) + 1);
			v.setOverall((long) (i % 5) + 1);
			v.setCvssScore("8.3");
			v.setCvssString("CVSS:4.0/AV:N/AC:L/AT:P/PR:N/UI:N/VC:H/VI:L/VA:L/SC:N/SI:N/SA:N");
			v.setTracking("VID-" + i);
			v.setAssessmentId(1337L);
			v.setCustomFields(new ArrayList<CustomField>());
			// Section is null so vulns land in Default — both implementations
			// handle Default identically and resolvePlaceholderSpec in
			// DocxUtils2 only knows about Default today
			v.setSection(null);
			// Distinct content per vuln so HTML conversion can't dedup
			v.setDescription("ParityDesc" + i + " with a <a href='https://example.com'>link</a> and <b>markup</b>.");
			v.setRecommendation("ParityRec" + i + " <ul><li>fix it</li><li>test it</li></ul>");
			// Half of the vulns embed a small inline PNG so the image path
			// executes against real bytes without touching MongoDB
			if (i % 2 == 0) {
				v.setDetails("ParityDetails" + i + " <pre class='code'>payload</pre><br/>"
						+ "<img src='" + makePng(rnd, 200, 120) + "'></img><br/>");
			} else {
				v.setDetails("ParityDetails" + i + " <pre class='code'>payload</pre><br/>plain text only");
			}
			a.getVulns().add(v);
		}
		return a;
	}

	/**
	 * Builds the template package using docx4j programmatic API. The
	 * template mirrors what real report templates contain: a findings
	 * block delimited by ${fiBegin}/${fiEnd} with per-vuln placeholders,
	 * followed by an assessment-level variable.
	 *
	 * Both implementations receive an identical byte-stream copy of this
	 * template — DocxUtils gets a freshly loaded WordprocessingMLPackage,
	 * DocxUtils2 gets the saved bytes back as an InputStream.
	 */
	private WordprocessingMLPackage buildTemplatePackage() throws Exception {
		WordprocessingMLPackage mlp = WordprocessingMLPackage.createPackage();
		mlp.getMainDocumentPart().addParagraphOfText("Report intro");
		mlp.getMainDocumentPart().addParagraphOfText("${asmtName} assessment (${asmtId}) by ${asmtAssessor}");
		mlp.getMainDocumentPart().addParagraphOfText("${fiBegin}");
		mlp.getMainDocumentPart().addParagraphOfText("${vulnName}");
		mlp.getMainDocumentPart().addParagraphOfText("${desc}");
		mlp.getMainDocumentPart().addParagraphOfText("${rec}");
		mlp.getMainDocumentPart().addParagraphOfText("${details}");
		mlp.getMainDocumentPart().addParagraphOfText("${fiEnd}");
		mlp.getMainDocumentPart().addParagraphOfText("");
		mlp.getMainDocumentPart().addParagraphOfText("End of report");
		return mlp;
	}

	private EntityManagerFactory stubEntityManagerFactory() {
		EntityManagerFactory emf = Mockito.mock(EntityManagerFactory.class);
		EntityManager em = Mockito.mock(EntityManager.class);
		Query query = Mockito.mock(Query.class);
		Mockito.when(emf.createEntityManager()).thenReturn(em);
		Mockito.when(em.createQuery("from AppStore order by order")).thenReturn(query);
		Mockito.when(query.getResultList()).thenReturn(new ArrayList<AppStore>());
		return emf;
	}

	private static String extractText(WordprocessingMLPackage mlp) {
		StringWriter sw = new StringWriter();
		try {
			TextUtils.extractText(mlp.getMainDocumentPart().getContents(), sw);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
		return sw.toString();
	}

	private static long sizeOf(WordprocessingMLPackage mlp) {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			mlp.save(baos);
			return baos.size();
		} catch (Exception ex) {
			return -1;
		}
	}

	// tiny screenshot-like PNG so the image pipeline handles real bytes
	private String makePng(Random rnd, int w, int h) {
		try {
			java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(w, h, java.awt.image.BufferedImage.TYPE_INT_RGB);
			java.awt.Graphics2D g = img.createGraphics();
			g.setColor(java.awt.Color.WHITE);
			g.fillRect(0, 0, w, h);
			g.setColor(new java.awt.Color(40, 40, 40));
			g.setFont(new java.awt.Font("Monospaced", java.awt.Font.PLAIN, 12));
			int y = 14;
			while (y < h) {
				StringBuilder line = new StringBuilder();
				int len = rnd.nextInt(w / 8) + 4;
				while (line.length() < len) {
					line.append(Integer.toHexString(rnd.nextInt())).append(' ');
				}
				g.drawString(line.toString(), 6, y);
				y += 16;
			}
			g.dispose();
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			javax.imageio.ImageIO.write(img, "png", out);
			return "data:image/png;base64," + Base64.getEncoder().encodeToString(out.toByteArray());
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}
}
