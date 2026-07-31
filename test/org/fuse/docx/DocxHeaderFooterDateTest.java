package org.fuse.docx;

import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;

import org.docx4j.TextUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.HeaderPart;
import org.docx4j.relationships.Relationship;
import org.docx4j.wml.HeaderReference;
import org.docx4j.wml.Hdr;
import org.docx4j.wml.HdrFtrRef;
import org.docx4j.wml.ObjectFactory;
import org.docx4j.wml.SectPr;
import org.junit.Test;
import org.mockito.Mockito;

import com.fuse.dao.AppStore;
import com.fuse.dao.Assessment;
import com.fuse.dao.AssessmentType;
import com.fuse.dao.RiskLevel;
import com.fuse.dao.Teams;
import com.fuse.reporting.DocxUtils;
import com.fuse.reporting.GenerateReport;

/**
 * Regression test for factionsecurity/faction#143 — ${today} (and other
 * date variables) were resolved in the document body but left as literal
 * text in headers/footers, because the Map<String,Date> of date values
 * never reached replaceHeaderAndFooter(). This guards that the header
 * now resolves both the default date format and a custom-format
 * variant (${today <pattern>}).
 */
public class DocxHeaderFooterDateTest {

	@Test
	public void headerDateVariableResolves() throws Exception {
		Teams team = new Teams();
		team.setId(1L);
		team.setTeamName("Hacking Team");
		AssessmentType type = new AssessmentType();
		type.setId(1L);
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
		assessment.getVulns().clear();

		WordprocessingMLPackage mlp = WordprocessingMLPackage.createPackage();
		mlp.getMainDocumentPart().addParagraphOfText("End of report");

		// Wire up a default header containing both the default-format and a
		// custom-format date placeholder, per docx4j's header creation pattern.
		ObjectFactory factory = Context.getWmlObjectFactory();
		HeaderPart headerPart = new HeaderPart();
		Relationship headerRel = mlp.getMainDocumentPart().addTargetPart(headerPart);

		Hdr hdr = factory.createHdr();
		headerPart.setJaxbElement(hdr);

		org.docx4j.wml.P headerPara = factory.createP();
		org.docx4j.wml.R headerRun = factory.createR();
		org.docx4j.wml.Text headerTextRun = factory.createText();
		headerTextRun.setValue("today=${today} custom=${today MM-dd-yyyy}");
		headerTextRun.setSpace("preserve");
		headerRun.getContent().add(headerTextRun);
		headerPara.getContent().add(headerRun);
		hdr.getContent().add(headerPara);

		HeaderReference headerRef = factory.createHeaderReference();
		headerRef.setType(HdrFtrRef.DEFAULT);
		headerRef.setId(headerRel.getId());

		SectPr sectPr = mlp.getMainDocumentPart().getJaxbElement().getBody().getSectPr();
		if (sectPr == null) {
			sectPr = factory.createSectPr();
			mlp.getMainDocumentPart().getJaxbElement().getBody().setSectPr(sectPr);
		}
		sectPr.getEGHdrFtrReferences().add(headerRef);

		EntityManagerFactory emf = Mockito.mock(EntityManagerFactory.class);
		EntityManager em = Mockito.mock(EntityManager.class);
		Query query = Mockito.mock(Query.class);
		Mockito.when(emf.createEntityManager()).thenReturn(em);
		Mockito.when(em.createQuery("from AppStore order by order")).thenReturn(query);
		Mockito.when(query.getResultList()).thenReturn(new ArrayList<AppStore>());

		DocxUtils genDoc = new DocxUtils(emf, mlp, assessment);
		genDoc.FONT = "Calibri";
		mlp = genDoc.generateDocx("p { margin: 0; }");

		StringWriter sw = new StringWriter();
		TextUtils.extractText(
				mlp.getHeaderFooterPolicy().getDefaultHeader().getContents(), sw);
		String headerText = sw.toString();

		String defaultFormatted = new SimpleDateFormat("MM/dd/yyyy").format(new Date());

		org.junit.Assert.assertFalse("literal ${today} leaked into header",
				headerText.contains("${today"));
		org.junit.Assert.assertTrue("default-format date not resolved in header, got: " + headerText,
				headerText.contains("today=" + defaultFormatted));

		String customFormatted = new SimpleDateFormat("MM-dd-yyyy").format(new Date());
		org.junit.Assert.assertTrue("custom-format date not resolved in header, got: " + headerText,
				headerText.contains("custom=" + customFormatted));
	}
}
