package org.fuse.docx;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;

import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.relationships.Relationship;
import org.docx4j.wml.Tbl;
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
import com.fuse.utils.FSUtils;

/**
 * Issue #153: a finding's table and links came out broken in the DOCX
 * once its fields were served from the pre-compiled cache. The table's
 * cells were flattened into loose paragraphs, and every link pointed at a
 * relationship id that only existed in the precompiler's scratch package,
 * so Word asked to repair the file and the link went nowhere.
 */
public class DocxTablesAndLinksTest {

	private static final String URL = "https://docs.factionsecurity.com/";

	// the reporter's description, verbatim
	private static final String MARKDOWN = "| d |  |  |\n"
			+ "| --- | --- | --- |\n"
			+ "|  |  | ff |\n"
			+ "\n"
			+ "[link](" + URL + ")\n"
			+ "`code`\n";

	@Test
	public void cachedFieldKeepsItsTableAndLink() throws Exception {
		Assessment assessment = buildAssessment();
		Vulnerability v = assessment.getVulns().get(0);

		DocxPrecompiler pre = new DocxPrecompiler("Calibri", "p { margin: 0; }");
		assertTrue("precompiler should cache the description", pre.compile(v));
		assertNotNull(v.getCachedDescXml());
		// prove the report is built from the cache: the marker can only
		// reach the document through the cached path
		v.setCachedDescXml(v.getCachedDescXml().replace(">ff<", ">CACHEDff<"));

		WordprocessingMLPackage mlp = generate(assessment);
		String docXml = XmlUtils.marshaltoString(mlp.getMainDocumentPart().getContents(), true, true);
		assertTrue("cached XML was not used", docXml.contains("CACHEDff"));

		assertTableSurvives(mlp, "CACHEDff");
		assertLinkResolves(mlp, docXml);
		assertFalse("link token leaked into the document", docXml.contains("FCT-LINK"));
	}

	@Test
	public void liveConversionKeepsItsTableAndLink() throws Exception {
		Assessment assessment = buildAssessment();

		WordprocessingMLPackage mlp = generate(assessment);
		String docXml = XmlUtils.marshaltoString(mlp.getMainDocumentPart().getContents(), true, true);

		assertTableSurvives(mlp, "ff");
		assertLinkResolves(mlp, docXml);
	}

	@Test
	public void theSameLinkInTwoFindingsResolvesInBoth() throws Exception {
		Assessment assessment = buildAssessment();
		Vulnerability second = newVuln(2L, assessment.getVulns().get(0).getLevels());
		assessment.getVulns().add(second);

		DocxPrecompiler pre = new DocxPrecompiler("Calibri", "p { margin: 0; }");
		for (Vulnerability v : assessment.getVulns()) {
			pre.compile(v);
		}

		WordprocessingMLPackage mlp = generate(assessment);
		String docXml = XmlUtils.marshaltoString(mlp.getMainDocumentPart().getContents(), true, true);
		assertEquals("one link per finding", 2, assertLinkResolves(mlp, docXml));
	}

	// a w:tbl in the body whose cells hold the table's text — not the
	// cell text flattened into ordinary paragraphs
	private static void assertTableSurvives(WordprocessingMLPackage mlp, String lastCell) throws Exception {
		List<Object> tables = mlp.getMainDocumentPart().getJAXBNodesViaXPath("//w:tbl", true);
		Tbl found = null;
		for (Object o : tables) {
			Tbl t = (Tbl) XmlUtils.unwrap(o);
			String xml = XmlUtils.marshaltoString(t, true, true);
			if (xml.contains(">" + lastCell + "<") && xml.contains(">d<")) {
				found = t;
			}
		}
		assertNotNull("the finding's table is missing from the report", found);
		String xml = XmlUtils.marshaltoString(found, true, true);
		assertEquals("expected the header row and one body row", 2, count(xml, "<w:tr[ >]"));
		assertEquals("expected three cells per row", 6, count(xml, "<w:tc[ >]"));
	}

	// every hyperlink must reference an external relationship to the URL
	// that exists in the report package
	private static int assertLinkResolves(WordprocessingMLPackage mlp, String docXml) {
		Matcher m = Pattern.compile("<w:hyperlink [^>]*r:id=\"([^\"]+)\"").matcher(docXml);
		int links = 0;
		while (m.find()) {
			String id = m.group(1);
			Relationship rel = mlp.getMainDocumentPart().getRelationshipsPart().getRelationshipByID(id);
			assertNotNull("hyperlink references a relationship the report does not have: " + id, rel);
			assertEquals(URL, rel.getTarget());
			assertEquals("External", rel.getTargetMode());
			links++;
		}
		assertTrue("the finding's link is missing from the report", links > 0);
		return links;
	}

	private static int count(String s, String regex) {
		Matcher m = Pattern.compile(regex).matcher(s);
		int n = 0;
		while (m.find()) {
			n++;
		}
		return n;
	}

	// ============================================================
	// Fixture
	// ============================================================

	private static Assessment buildAssessment() {
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
		assessment.getVulns().add(newVuln(1L, levels));
		return assessment;
	}

	private static Vulnerability newVuln(long id, List<RiskLevel> levels) {
		Vulnerability v = new Vulnerability();
		v.setLevels(levels);
		v.setId(id);
		v.setName("Issue " + id);
		v.setImpact(3l);
		v.setLikelyhood(3l);
		v.setOverall(3l);
		v.setTracking("VID-" + id);
		v.setAssessmentId(1l);
		v.setCustomFields(new ArrayList<CustomField>());
		v.setDescription(FSUtils.convertFromMarkDown(MARKDOWN));
		v.setRecommendation("Rec" + id);
		return v;
	}

	private static WordprocessingMLPackage generate(Assessment assessment) throws Exception {
		WordprocessingMLPackage mlp = WordprocessingMLPackage.createPackage();
		mlp.getMainDocumentPart().addParagraphOfText("${fiBegin}");
		mlp.getMainDocumentPart().addParagraphOfText("${vulnName}");
		mlp.getMainDocumentPart().addParagraphOfText("${desc}");
		mlp.getMainDocumentPart().addParagraphOfText("${rec}");
		mlp.getMainDocumentPart().addParagraphOfText("${fiEnd}");
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

		// a save/reload round trip is what Word sees: it must not fail
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		mlp.save(out);
		WordprocessingMLPackage.load(new java.io.ByteArrayInputStream(out.toByteArray()));
		return mlp;
	}
}
