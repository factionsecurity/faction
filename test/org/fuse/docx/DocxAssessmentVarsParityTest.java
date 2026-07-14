package org.fuse.docx;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
import com.fuse.dao.CustomType;
import com.fuse.dao.RiskLevel;
import com.fuse.dao.Teams;
import com.fuse.dao.Vulnerability;
import com.fuse.reporting.DocxPrecompiler;
import com.fuse.reporting.DocxUtils;
import com.fuse.reporting.GenerateReport;

/**
 * Guards the ordering change that runs replaceAssessment BEFORE the
 * findings are inserted (so its whole-document marshal covers only the
 * template): every assessment-level variable that used to be resolved by
 * the document-wide pass must now be resolved inside finding content by
 * the per-field paths — replacement() for live conversion and
 * applyAssessmentVarsToXml for the pre-compiled cache. A variable that
 * leaks through either path shows up here as a literal ${...} in the
 * output.
 */
public class DocxAssessmentVarsParityTest {

	@Test
	public void assessmentVariablesResolveInsideFindings() throws Exception {
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
		Date start = new java.util.GregorianCalendar(2026, 0, 5).getTime();
		Date end = new java.util.GregorianCalendar(2026, 0, 23).getTime();
		assessment.setStart(start);
		assessment.setEnd(end);

		// assessment-level TEXT custom field (fieldType < 3)
		CustomType engagementType = new CustomType();
		engagementType.setVariable("engagement");
		engagementType.setFieldType(1);
		CustomField engagement = new CustomField();
		engagement.setType(engagementType);
		engagement.setValue("ENG-42");
		List<CustomField> asmtCfs = new ArrayList<>();
		asmtCfs.add(engagement);
		assessment.setCustomFields(asmtCfs);

		// every assessment-scoped token the doc-wide pass used to resolve
		// inside findings, in both cached and live fields
		String varSoup = "name=${asmtName} start=${asmtStart} end=${asmtEnd} eng=${cfengagement}"
				+ " team=${asmtTeam} type=${asmtType} open=${totalOpenVulns}";
		assessment.getVulns().clear();
		for (int i = 1; i <= 4; i++) {
			Vulnerability v = new Vulnerability();
			v.setLevels(levels);
			v.setId((long) i);
			v.setName("VarIssue " + i);
			v.setImpact(3l);
			v.setLikelyhood(3l);
			v.setOverall(3l);
			v.setCvssScore("8.3");
			v.setCvssString("CVSS:4.0/AV:N/AC:L/AT:P/PR:N/UI:N/VC:H/VI:L/VA:L/SC:N/SI:N/SA:N");
			v.setTracking("VID-" + i);
			v.setAssessmentId(1l);
			v.setCustomFields(new ArrayList<CustomField>());
			v.setDescription("Desc" + i + " " + varSoup);
			v.setRecommendation("Rec" + i + " <ul><li>fix ${asmtName}</li></ul>");
			v.setDetails("Details" + i + " " + varSoup);
			assessment.getVulns().add(v);
		}

		// half cached, half live — both substitution paths must resolve
		DocxPrecompiler pre = new DocxPrecompiler("Calibri", "p { margin: 0; }");
		pre.compile(assessment.getVulns().get(0));
		pre.compile(assessment.getVulns().get(1));

		WordprocessingMLPackage mlp = WordprocessingMLPackage.createPackage();
		mlp.getMainDocumentPart().addParagraphOfText("Template header: ${asmtName} ${asmtStart} ${cfengagement}");
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

		DocxUtils genDoc = new DocxUtils(emf, mlp, assessment);
		genDoc.FONT = "Calibri";
		mlp = genDoc.generateDocx("p { margin: 0; }");

		java.io.StringWriter sw = new java.io.StringWriter();
		org.docx4j.TextUtils.extractText(mlp.getMainDocumentPart().getContents(), sw);
		String docText = sw.toString();

		String startStr = new SimpleDateFormat("MM/dd/yyyy").format(start);
		String endStr = new SimpleDateFormat("MM/dd/yyyy").format(end);

		for (int i = 1; i <= 4; i++) {
			org.junit.Assert.assertTrue("missing vuln " + i, docText.contains("VarIssue " + i));
			org.junit.Assert.assertTrue("missing desc " + i, docText.contains("Desc" + i));
		}
		// no unresolved assessment tokens anywhere — template or findings
		org.junit.Assert.assertFalse("leaked ${asmt token: " + snippetAround(docText, "${asmt"),
				docText.contains("${asmt"));
		org.junit.Assert.assertFalse("leaked ${cfengagement token",
				docText.contains("${cfengagement"));
		org.junit.Assert.assertFalse("leaked ${totalOpenVulns token",
				docText.contains("${totalOpenVulns"));
		// resolved values present in finding content (name= prefix pins the
		// occurrence to the varSoup inside desc/details, not the template)
		org.junit.Assert.assertTrue("asmtName not resolved in findings",
				docText.contains("name=Test PCI Assessment"));
		org.junit.Assert.assertTrue("asmtStart not resolved in findings",
				docText.contains("start=" + startStr));
		org.junit.Assert.assertTrue("asmtEnd not resolved in findings",
				docText.contains("end=" + endStr));
		org.junit.Assert.assertTrue("assessment custom field not resolved in findings",
				docText.contains("eng=ENG-42"));
		org.junit.Assert.assertTrue("asmtTeam not resolved in findings",
				docText.contains("team=Hacking Team"));
		org.junit.Assert.assertTrue("totalOpenVulns not resolved in findings",
				docText.contains("open=4"));
		// template paragraph resolved by replaceAssessment as before
		org.junit.Assert.assertTrue("template header not resolved",
				docText.contains("Template header: Test PCI Assessment " + startStr + " ENG-42"));
	}

	private static String snippetAround(String text, String needle) {
		int idx = text.indexOf(needle);
		if (idx < 0)
			return "";
		return text.substring(Math.max(0, idx - 40), Math.min(text.length(), idx + 60));
	}
}
