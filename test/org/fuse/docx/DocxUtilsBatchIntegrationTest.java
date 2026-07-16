package org.fuse.docx;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;

import org.docx4j.TextUtils;
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
import com.fuse.reporting.DocxUtils;
import com.fuse.reporting.GenerateReport;

/**
 * Exercises the batched XHTML conversion in setFindings end-to-end: many
 * findings, multiple conversion chunks, and verifies every field lands in
 * the generated document with no leaked markers or placeholders.
 */
public class DocxUtilsBatchIntegrationTest {

	@Test
	public void batchedFindingsConversionProducesEveryField() throws Exception {
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

		// 10 sections x 3 vulns = 30 findings -> 90+ fields -> several chunks
		String[] sections = new String[10];
		for (int i = 0; i < sections.length; i++) {
			sections[i] = "S" + i;
		}
		Assessment assessment = GenerateReport.createTestAssessment(team, type, levels, sections);
		assessment.setGuid("test-guid");
		int i = 1;
		for (Vulnerability v : assessment.getVulns()) {
			v.setCustomFields(new ArrayList<CustomField>());
			v.setDescription("MarkerDescription" + i + " with a <a href='https://example.com'>link</a>");
			v.setRecommendation("MarkerRecommendation" + i + "<ul><li>item one</li><li>item two</li></ul>");
			v.setDetails("MarkerDetails" + i + " <b>bold</b><pre class='code'>code block</pre>");
			i++;
		}
		int vulnCount = assessment.getVulns().size();

		WordprocessingMLPackage mlp = WordprocessingMLPackage.createPackage();
		mlp.getMainDocumentPart().addParagraphOfText("${fiBegin}");
		mlp.getMainDocumentPart().addParagraphOfText("${vulnName}");
		mlp.getMainDocumentPart().addParagraphOfText("${desc}");
		mlp.getMainDocumentPart().addParagraphOfText("${rec}");
		mlp.getMainDocumentPart().addParagraphOfText("${details}");
		mlp.getMainDocumentPart().addParagraphOfText("${fiEnd}");
		// setFindings' template extraction consumes the element following
		// ${fiEnd}; real templates always have trailing content
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

		StringWriter text = new StringWriter();
		TextUtils.extractText(mlp.getMainDocumentPart().getContents(), text);
		String docText = text.toString();

		for (int n = 1; n <= vulnCount; n++) {
			assertTrue("missing vuln name " + n, docText.contains("Test Issue " + n));
			assertTrue("missing desc " + n, docText.contains("MarkerDescription" + n));
			assertTrue("missing rec " + n, docText.contains("MarkerRecommendation" + n));
			assertTrue("missing details " + n, docText.contains("MarkerDetails" + n));
		}
		assertFalse("split marker leaked into document", docText.contains("FCT-FIELD-SPLIT"));
		assertFalse("unresolved rec placeholder", docText.contains("${rec"));
		assertFalse("unresolved desc placeholder", docText.contains("${desc"));
		assertFalse("unresolved details placeholder", docText.contains("${details"));
	}
}
