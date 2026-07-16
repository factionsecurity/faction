package org.fuse.docx;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.junit.Test;
import org.mockito.Mockito;

import com.fuse.dao.AppStore;
import com.fuse.dao.Assessment;
import com.fuse.dao.AssessmentType;
import com.fuse.dao.CustomField;
import com.fuse.dao.RiskLevel;
import com.fuse.dao.Teams;
import com.fuse.dao.Vulnerability;
import com.fuse.reporting.DocxPrecompiler;
import com.fuse.reporting.DocxUtils;
import com.fuse.reporting.GenerateReport;

/**
 * Guards the bullet-list numbering behavior: bullets from different
 * findings must never render as one continuing numbered sequence — not
 * from live batched conversion, and not from the pre-compiled cache
 * (whose v1 format stored bare scratch-package numIds that resolved
 * against the report template's decimal numbering).
 */
public class DocxListNumberingTest {

	@Test
	public void liveConversionKeepsBulletsBullets() throws Exception {
		Assessment assessment = buildAssessment();
		WordprocessingMLPackage mlp = generate(assessment);
		String docXml = org.docx4j.XmlUtils.marshaltoString(
				mlp.getMainDocumentPart().getContents(), true, true);
		int checked = verifyBullets(mlp, docXml);
		System.out.println("live: verified bullet items: " + checked);
		org.junit.Assert.assertTrue("expected many bullet items", checked >= 100);
	}

	@Test
	public void cachedFieldsKeepBulletsBullets() throws Exception {
		Assessment assessment = buildAssessment();

		// one vuln gets an inline screenshot so the cached-image path runs:
		// precompile embeds it as a data URI, report time must create a real
		// image part (probe-free) and a valid relationship
		Vulnerability v3 = assessment.getVulns().get(2);
		v3.setDetails(v3.getDetails() + "<img src='" + makePng(300, 180) + "'></img>");

		// pre-compile every vuln the way the save hooks do
		DocxPrecompiler pre = new DocxPrecompiler("Calibri", "p { margin: 0; }");
		int compiled = 0;
		for (Vulnerability v : assessment.getVulns()) {
			if (pre.compile(v)) {
				compiled++;
			}
		}
		org.junit.Assert.assertTrue("precompiler should compile the fixture vulns", compiled >= 25);

		for (Vulnerability v : assessment.getVulns()) {
			if (v.getId() == 1L) {
				// prove the cache is actually served: tamper with the cached
				// text (hash still matches the entity content) — the marker
				// can only reach the document via the cached path
				org.junit.Assert.assertNotNull(v.getCachedDetailsXml());
				v.setCachedDetailsXml(v.getCachedDetailsXml().replace("DTest1", "CACHEDMARK1"));
			}
			if (v.getId() == 2L) {
				// v1-format poison: bare scratch-package numId with no
				// carried definitions, hash forged to match. The legacy
				// guard must refuse it and convert live.
				v.setCachedDetailsXml("<w:p xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\">"
						+ "<w:pPr><w:numPr><w:ilvl w:val=\"0\"/><w:numId w:val=\"1\"/></w:numPr></w:pPr>"
						+ "<w:r><w:t>POISON</w:t></w:r></w:p>");
				v.setCachedDetailsHash(DocxPrecompiler.contentHash("Calibri",
						v.getDetails() != null ? v.getDetails() : ""));
			}
		}

		WordprocessingMLPackage mlp = generate(assessment);
		String docXml = org.docx4j.XmlUtils.marshaltoString(
				mlp.getMainDocumentPart().getContents(), true, true);

		org.junit.Assert.assertTrue("cached XML was not used (tamper marker missing)",
				docXml.contains("CACHEDMARK1"));
		java.io.StringWriter sw = new java.io.StringWriter();
		org.docx4j.TextUtils.extractText(mlp.getMainDocumentPart().getContents(), sw);
		String docText = sw.toString();
		org.junit.Assert.assertTrue("colon text corrupted (':8080' lost)",
				docText.contains("connect to host :8080 as user : admin"));
		org.junit.Assert.assertFalse("namespace prefix leaked into text: 'w:8080'",
				docText.contains("w:8080"));
		org.junit.Assert.assertFalse("namespace prefix leaked into text: 'user w:'",
				docText.contains("user w:"));
		org.junit.Assert.assertFalse("legacy v1 cache format must not be used",
				docXml.contains("POISON"));
		org.junit.Assert.assertFalse("numbering tokens leaked into the document",
				docXml.contains("FCT-NUM-"));

		int checked = verifyBullets(mlp, docXml);
		System.out.println("cached: verified bullet items: " + checked);
		org.junit.Assert.assertTrue("expected many bullet items", checked >= 100);

		// the cached image must have become a real, wired-up package part:
		// a media entry, a relationship pointing at it, a registered content
		// type, and an r:embed in the document referencing that relationship
		ByteArrayOutputStream packageBytes = new ByteArrayOutputStream();
		mlp.save(packageBytes);
		java.util.Set<String> zipEntries = new java.util.TreeSet<>();
		java.util.zip.ZipInputStream zin = new java.util.zip.ZipInputStream(
				new java.io.ByteArrayInputStream(packageBytes.toByteArray()));
		String contentTypesXml = null;
		String relsXml = null;
		java.util.zip.ZipEntry ze;
		while ((ze = zin.getNextEntry()) != null) {
			zipEntries.add(ze.getName());
			if (ze.getName().equals("[Content_Types].xml") || ze.getName().equals("word/_rels/document.xml.rels")) {
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				byte[] buf = new byte[8192];
				int n;
				while ((n = zin.read(buf)) > 0) {
					out.write(buf, 0, n);
				}
				if (ze.getName().startsWith("[Content_Types]")) {
					contentTypesXml = out.toString("UTF-8");
				} else {
					relsXml = out.toString("UTF-8");
				}
			}
		}
		String mediaEntry = null;
		for (String name : zipEntries) {
			if (name.startsWith("word/media/fctimage")) {
				mediaEntry = name;
			}
		}
		org.junit.Assert.assertNotNull("cached image part missing from package: " + zipEntries, mediaEntry);
		org.junit.Assert.assertTrue("png content type not registered",
				contentTypesXml != null && contentTypesXml.contains("image/png"));
		org.junit.Assert.assertTrue("no relationship to the cached image part",
				relsXml != null && relsXml.contains(mediaEntry.substring("word/".length())));
		org.junit.Assert.assertTrue("document does not embed the cached image",
				docXml.contains("r:embed"));
	}

	// small screenshot-like PNG as a data URI
	private static String makePng(int w, int h) throws Exception {
		java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(w, h,
				java.awt.image.BufferedImage.TYPE_INT_RGB);
		java.awt.Graphics2D g = img.createGraphics();
		g.setColor(java.awt.Color.WHITE);
		g.fillRect(0, 0, w, h);
		g.setColor(java.awt.Color.DARK_GRAY);
		g.drawString("evidence", 10, h / 2);
		g.dispose();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		javax.imageio.ImageIO.write(img, "png", out);
		return "data:image/png;base64," + java.util.Base64.getEncoder().encodeToString(out.toByteArray());
	}

	@Test
	public void vulnTablePathKeepsBulletsBullets() throws Exception {
		Assessment assessment = buildAssessment();
		// pre-compile only half the vulns so both the cache-hit branch and
		// the batched-conversion branch of the table path run in one report
		DocxPrecompiler pre = new DocxPrecompiler("Calibri", "p { margin: 0; }");
		for (Vulnerability v : assessment.getVulns()) {
			if (v.getId() % 2 == 0) {
				pre.compile(v);
			}
		}

		// production-shaped template: a ${vulnTable} marker row, a ${loop-1}
		// row, and a second row carrying the HTML field placeholders
		WordprocessingMLPackage mlp = WordprocessingMLPackage.createPackage();
		org.docx4j.wml.Tbl table = org.docx4j.model.table.TblFactory.createTable(3, 1, 4000);
		setCellParagraphs(table, 0, "${vulnTable}");
		setCellParagraphs(table, 1, "${loop-1}", "${vulnName}");
		setCellParagraphs(table, 2, "${desc}", "${rec}", "${details}", "${cfpoc}");
		mlp.getMainDocumentPart().addObject(table);
		mlp.getMainDocumentPart().addParagraphOfText("End of report");

		// round-trip through bytes so JAXB parent pointers are set the way
		// a real loaded template's are (indexOfRow walks parents)
		ByteArrayOutputStream tpl = new ByteArrayOutputStream();
		mlp.save(tpl);
		mlp = WordprocessingMLPackage.load(new java.io.ByteArrayInputStream(tpl.toByteArray()));

		EntityManagerFactory emf = Mockito.mock(EntityManagerFactory.class);
		EntityManager em = Mockito.mock(EntityManager.class);
		Query query = Mockito.mock(Query.class);
		Mockito.when(emf.createEntityManager()).thenReturn(em);
		Mockito.when(em.createQuery("from AppStore order by order")).thenReturn(query);
		Mockito.when(query.getResultList()).thenReturn(new ArrayList<AppStore>());

		DocxUtils genDoc = new DocxUtils(emf, mlp, assessment);
		genDoc.FONT = "Calibri";
		mlp = genDoc.generateDocx("p { margin: 0; }");

		java.io.StringWriter sw = new java.io.StringWriter();
		org.docx4j.TextUtils.extractText(mlp.getMainDocumentPart().getContents(), sw);
		String docText = sw.toString();
		for (int i = 1; i <= 30; i++) {
			org.junit.Assert.assertTrue("table missing vuln name " + i, docText.contains("Issue " + i));
			org.junit.Assert.assertTrue("table missing desc " + i, docText.contains("Desc" + i));
			org.junit.Assert.assertTrue("table missing rec " + i, docText.contains("Rec" + i));
			org.junit.Assert.assertTrue("table missing details " + i, docText.contains("bullet DTest" + i));
		}
		org.junit.Assert.assertFalse("leaked field placeholder", docText.contains("${rec"));
		org.junit.Assert.assertFalse("leaked field placeholder", docText.contains("${desc"));
		org.junit.Assert.assertFalse("leaked field placeholder", docText.contains("${details"));
		org.junit.Assert.assertFalse("leaked table marker", docText.contains("${vulnTable"));
		org.junit.Assert.assertFalse("leaked loop marker", docText.contains("${loop"));

		String docXml = org.docx4j.XmlUtils.marshaltoString(
				mlp.getMainDocumentPart().getContents(), true, true);
		org.junit.Assert.assertFalse("numbering tokens leaked into the document",
				docXml.contains("FCT-NUM-"));
		int checked = verifyBullets(mlp, docXml);
		System.out.println("vulnTable: verified bullet items: " + checked);
		org.junit.Assert.assertTrue("expected many bullet items", checked >= 100);
	}

	// replaces the single cell's content in the given table row with one
	// paragraph per text value
	private static void setCellParagraphs(org.docx4j.wml.Tbl table, int rowIndex, String... texts) {
		org.docx4j.wml.Tr row = (org.docx4j.wml.Tr) table.getContent().get(rowIndex);
		org.docx4j.wml.Tc cell = (org.docx4j.wml.Tc) org.docx4j.XmlUtils.unwrap(row.getContent().get(0));
		cell.getContent().clear();
		org.docx4j.wml.ObjectFactory factory = org.docx4j.jaxb.Context.getWmlObjectFactory();
		for (String text : texts) {
			org.docx4j.wml.P p = factory.createP();
			org.docx4j.wml.R r = factory.createR();
			org.docx4j.wml.Text t = factory.createText();
			t.setValue(text);
			r.getContent().add(t);
			p.getContent().add(r);
			cell.getContent().add(p);
		}
	}

	// ============================================================
	// Fixture
	// ============================================================

	private Assessment buildAssessment() {
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

		Assessment assessment = GenerateReport.createTestAssessment(team, type, levels, new String[] { "S1" });
		assessment.setGuid("test-guid");
		assessment.getVulns().clear();
		// enough findings that convertFieldsBatched uses several chunks
		// (25 fields per chunk), so cross-chunk numId collisions surface
		for (int i = 1; i <= 30; i++) {
			Vulnerability v = new Vulnerability();
			v.setLevels(levels);
			v.setId((long) i);
			v.setName("Issue " + i);
			v.setImpact(3l);
			v.setLikelyhood(3l);
			v.setOverall(3l);
			v.setCvssScore("8.3");
			v.setCvssString("CVSS:4.0/AV:N/AC:L/AT:P/PR:N/UI:N/VC:H/VI:L/VA:L/SC:N/SI:N/SA:N");
			v.setTracking("VID-" + i);
			v.setAssessmentId(1l);
			v.setCustomFields(new ArrayList<CustomField>());
			// a type-3 (HTML) custom field with a bullet list — cached via
			// cachedCfXml, so its numbering must survive the splice too
			com.fuse.dao.CustomType pocType = new com.fuse.dao.CustomType();
			pocType.setVariable("poc");
			pocType.setFieldType(3);
			CustomField poc = new CustomField();
			poc.setType(pocType);
			poc.setValue("<ul><li>bullet CF" + i + "-a</li><li>bullet CF" + i + "-b</li></ul>");
			v.getCustomFields().add(poc);
			// colon-adjacent text: the precompiler's namespace normalization
			// must never touch text content (" :80" once became " w:80")
			v.setDescription("Desc" + i + " connect to host :8080 as user : admin"
					+ "<ul><li>bullet A" + i + "</li><li>bullet B" + i + "</li></ul>");
			// production failure shapes: a numbered list followed by a bullet
			// list in the same field, in both valid and editor-mangled
			// (ul nested directly inside ol) markup
			if (i % 2 == 0) {
				v.setRecommendation("Rec" + i
						+ "<ol><li><p>step one</p></li><li><p>step two</p></li><li><p>step three</p></li></ol>"
						+ "<ul><li>bullet Test" + i + "</li><li>bullet Test2-" + i + "</li></ul>");
			} else {
				v.setRecommendation("Rec" + i
						+ "<ol><li>step one</li><li>step two</li><li>step three</li>"
						+ "<ul><li>bullet Test" + i + "</li><li>bullet Test2-" + i + "</li></ul></ol>");
			}
			// SunEditor wraps every list item's content in <p>; the ol lives
			// in the rec field, the ul in details — the production bug had
			// the ul numbered as a continuation of the previous field's ol
			v.setDetails("<ul><li><p>bullet DTest" + i + "</p></li><li><p>bullet DTest2-" + i
					+ "</p></li><li><p><br /></p></li></ul>");
			assessment.getVulns().add(v);
		}
		return assessment;
	}

	private WordprocessingMLPackage generate(Assessment assessment) throws Exception {
		// use the real sample template when available — it ships a numbering
		// part (decimal heading numbering on numIds 1-4) that a fresh
		// package lacks, which is where allocation collisions surface
		WordprocessingMLPackage mlp;
		java.io.File template = new java.io.File(System.getProperty("user.dir") + "/src/test/sampletemplate.docx");
		if (template.exists()) {
			mlp = WordprocessingMLPackage.load(template);
		} else {
			mlp = WordprocessingMLPackage.createPackage();
		}
		mlp.getMainDocumentPart().addParagraphOfText("${fiBegin}");
		mlp.getMainDocumentPart().addParagraphOfText("${vulnName}");
		mlp.getMainDocumentPart().addParagraphOfText("${desc}");
		mlp.getMainDocumentPart().addParagraphOfText("${rec}");
		mlp.getMainDocumentPart().addParagraphOfText("${details}");
		mlp.getMainDocumentPart().addParagraphOfText("${cfpoc}");
		mlp.getMainDocumentPart().addParagraphOfText("${fiEnd}");
		mlp.getMainDocumentPart().addParagraphOfText("");
		mlp.getMainDocumentPart().addParagraphOfText("End of report");

		EntityManagerFactory emf = Mockito.mock(EntityManagerFactory.class);
		EntityManager em = Mockito.mock(EntityManager.class);
		Query query = Mockito.mock(Query.class);
		Mockito.when(emf.createEntityManager()).thenReturn(em);
		Mockito.when(em.createQuery("from AppStore order by order")).thenReturn(query);
		Mockito.when(query.getResultList()).thenReturn(new ArrayList<AppStore>());

		DocxUtils genDoc = new DocxUtils(emf, mlp, assessment);
		genDoc.FONT = "Calibri";
		mlp = genDoc.generateDocx("p { margin: 0; }");

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		mlp.save(baos);
		java.nio.file.Files.write(java.nio.file.Paths.get("/tmp/list-numbering-test.docx"), baos.toByteArray());
		return mlp;
	}

	// ============================================================
	// Verification: resolve every "bullet ..." list item through
	// numId -> num -> abstractNum -> numFmt and require "bullet"
	// ============================================================

	private static int verifyBullets(WordprocessingMLPackage mlp, String docXml) throws Exception {
		org.junit.Assert.assertNotNull("document has no numbering part",
				mlp.getMainDocumentPart().getNumberingDefinitionsPart());
		String numberingXml = org.docx4j.XmlUtils.marshaltoString(
				mlp.getMainDocumentPart().getNumberingDefinitionsPart().getContents(), true, true);

		java.util.Map<String, String> abstractFmt = new java.util.HashMap<>();
		java.util.regex.Matcher am = java.util.regex.Pattern.compile(
				"<w:abstractNum [^>]*w:abstractNumId=\"(\\d+)\"[^>]*>(.*?)</w:abstractNum>",
				java.util.regex.Pattern.DOTALL).matcher(numberingXml);
		while (am.find()) {
			java.util.regex.Matcher f = java.util.regex.Pattern.compile("w:numFmt w:val=\"(\\w+)\"")
					.matcher(am.group(2));
			abstractFmt.put(am.group(1), f.find() ? f.group(1) : "?");
		}
		java.util.Map<String, String> numToAbstract = new java.util.HashMap<>();
		java.util.regex.Matcher nm = java.util.regex.Pattern.compile(
				"<w:num w:numId=\"(\\d+)\"[^>]*>\\s*<w:abstractNumId w:val=\"(\\d+)\"").matcher(numberingXml);
		while (nm.find()) {
			numToAbstract.put(nm.group(1), nm.group(2));
		}

		int checked = 0;
		java.util.regex.Matcher pm = java.util.regex.Pattern.compile("<w:p\\b.*?</w:p>",
				java.util.regex.Pattern.DOTALL).matcher(docXml);
		while (pm.find()) {
			String p = pm.group(0);
			StringBuilder text = new StringBuilder();
			java.util.regex.Matcher tm = java.util.regex.Pattern.compile("<w:t[^>]*>([^<]*)</w:t>").matcher(p);
			while (tm.find()) {
				text.append(tm.group(1));
			}
			if (text.toString().contains("bullet ") || text.toString().contains("bullet A")
					|| text.toString().contains("bullet B") || text.toString().contains("bullet Test")) {
				java.util.regex.Matcher nid = java.util.regex.Pattern.compile("<w:numId w:val=\"(\\d+)\"").matcher(p);
				org.junit.Assert.assertTrue("list item lost its numbering: " + text, nid.find());
				String fmt = abstractFmt.get(numToAbstract.get(nid.group(1)));
				org.junit.Assert.assertEquals("item '" + text + "' (numId " + nid.group(1)
						+ ") must be a bullet", "bullet", fmt);
				checked++;
			}
		}
		return checked;
	}
}
