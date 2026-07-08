package org.fuse.docx;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;

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
import com.fuse.dao.Vulnerability;
import com.fuse.reporting.DocxUtils;
import com.fuse.reporting.GenerateReport;
import com.fuse.utils.MethodProfiler;

/**
 * Manual performance harness for report generation. Not part of the normal
 * test run — enable with -Dfaction.perf.harness=true.
 *
 * Generates a 400-finding assessment where each finding's details embeds a
 * unique screenshot-sized PNG, mirroring the shape of real large reports,
 * and prints the MethodProfiler report. Run with -Dfaction.perf.images=false
 * to compare the same document without images.
 */
public class DocxPerfHarness {

	@Test
	public void profileLargeAssessment() throws Exception {
		Assume.assumeTrue("perf harness disabled", "true".equals(System.getProperty("faction.perf.harness")));
		boolean withImages = !"false".equals(System.getProperty("faction.perf.images"));
		int vulnCount = Integer.parseInt(System.getProperty("faction.perf.vulns", "400"));

		Teams team = new Teams();
		team.setId(123l);
		team.setTeamName("Hacking Team");
		AssessmentType type = new AssessmentType();
		type.setId(1235l);
		type.setType("Assessment Type");

		List<RiskLevel> levels = new ArrayList<>();
		String[] risk = { "Informational", "Recommended", "Low", "Medium", "High", "Critical" };
		for (int i = 0; i < 10; i++) {
			RiskLevel level = new RiskLevel();
			level.setRiskId(i);
			if (i < risk.length)
				level.setRisk(risk[i]);
			levels.add(level);
		}

		Assessment assessment = GenerateReport.createTestAssessment(team, type, levels, new String[] { "seed" });
		assessment.setGuid("perf-guid");
		assessment.getVulns().clear();

		Random rnd = new Random(42);
		for (int i = 1; i <= vulnCount; i++) {
			Vulnerability v = new Vulnerability();
			v.setLevels(levels);
			v.setId((long) i);
			v.setName("Perf Issue " + i);
			v.setImpact((long) (i % 5) + 1);
			v.setLikelyhood((long) (i % 5) + 1);
			v.setOverall((long) (i % 5) + 1);
			v.setCvssScore("8.3");
			v.setCvssString("CVSS:4.0/AV:N/AC:L/AT:P/PR:N/UI:N/VC:H/VI:L/VA:L/SC:N/SI:N/SA:N");
			v.setTracking("VID-" + i);
			v.setAssessmentId(1l);
			v.setCustomFields(new ArrayList<CustomField>());
			v.setDescription("Description for issue " + i
					+ " with a <a href='https://example.com'>link</a> and <b>markup</b>.");
			v.setRecommendation("Recommendation for issue " + i + "<ul><li>fix it</li><li>test it</li></ul>");
			if (withImages) {
				int imgW = Integer.parseInt(System.getProperty("faction.perf.imgw", "800"));
				int imgH = Integer.parseInt(System.getProperty("faction.perf.imgh", "500"));
				v.setDetails("Details for issue " + i + "<pre class='code'>payload</pre><br/>"
						+ "<img src='" + makePng(rnd, imgW, imgH) + "'></img><br/>");
			} else {
				v.setDetails("Details for issue " + i + "<pre class='code'>payload</pre><br/>plain text details");
			}
			assessment.getVulns().add(v);
		}

		WordprocessingMLPackage mlp = WordprocessingMLPackage.createPackage();
		mlp.getMainDocumentPart().addParagraphOfText("Report intro");
		mlp.getMainDocumentPart().addParagraphOfText("${fiBegin}");
		mlp.getMainDocumentPart().addParagraphOfText("${vulnName}");
		mlp.getMainDocumentPart().addParagraphOfText("${desc}");
		mlp.getMainDocumentPart().addParagraphOfText("${rec}");
		mlp.getMainDocumentPart().addParagraphOfText("${details}");
		mlp.getMainDocumentPart().addParagraphOfText("${fiEnd}");
		mlp.getMainDocumentPart().addParagraphOfText("");
		mlp.getMainDocumentPart().addParagraphOfText("End of report");

		EntityManagerFactory emf = Mockito.mock(EntityManagerFactory.class);
		EntityManager em = Mockito.mock(EntityManager.class);
		Query query = Mockito.mock(Query.class);
		Mockito.when(emf.createEntityManager()).thenReturn(em);
		Mockito.when(em.createQuery("from AppStore order by order")).thenReturn(query);
		Mockito.when(query.getResultList()).thenReturn(new ArrayList<AppStore>());

		MethodProfiler.setEnabled(true);
		MethodProfiler.clearStats();

		long start = System.currentTimeMillis();
		DocxUtils genDoc = new DocxUtils(emf, mlp, assessment);
		genDoc.FONT = "Calibri";
		mlp = genDoc.generateDocx("p { margin: 0; } img { width: 50% !important; height: auto !important; }");
		long elapsed = System.currentTimeMillis() - start;

		System.out.println("\n=== Perf harness: vulns=" + vulnCount + " images=" + withImages
				+ " generateDocx wall=" + elapsed + "ms ===");
		MethodProfiler.printReport();
		MethodProfiler.clearStats();

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		mlp.save(baos);
		System.out.println("Saved docx size: " + (baos.size() / 1024 / 1024) + " MB");
	}

	// screenshot-like PNG: dense text lines and UI blocks — compresses like
	// a real terminal/browser capture and downscales the way real
	// screenshots do (unlike random noise, which is incompressible)
	private String makePng(Random rnd, int w, int h) throws Exception {
		BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = img.createGraphics();
		g.setColor(java.awt.Color.WHITE);
		g.fillRect(0, 0, w, h);
		for (int i = 0; i < 30; i++) {
			g.setColor(new java.awt.Color(rnd.nextInt(0xFFFFFF)));
			g.fillRect(rnd.nextInt(w), rnd.nextInt(h), rnd.nextInt(400) + 40, rnd.nextInt(60) + 10);
		}
		g.setFont(new java.awt.Font("Monospaced", java.awt.Font.PLAIN, 14));
		g.setColor(new java.awt.Color(40, 40, 40));
		for (int y = 14; y < h; y += 16) {
			// realistic text coverage: most rows have a line of text of
			// varying length, not wall-to-wall characters
			if (rnd.nextInt(10) < 7) {
				StringBuilder line = new StringBuilder();
				int len = rnd.nextInt(w / 16) + 8;
				while (line.length() < len) {
					line.append(Integer.toHexString(rnd.nextInt())).append(' ');
				}
				g.drawString(line.toString(), 8 + rnd.nextInt(40), y);
			}
		}
		g.dispose();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ImageIO.write(img, "png", out);
		return "data:image/png;base64," + Base64.getEncoder().encodeToString(out.toByteArray());
	}
}
