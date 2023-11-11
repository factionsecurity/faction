package com.fuse.utils;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import javax.persistence.EntityManager;

import org.apache.commons.codec.binary.Base64;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;

import com.fuse.dao.Assessment;
import com.fuse.dao.AssessmentType;
import com.fuse.dao.ExploitStep;
import com.fuse.dao.ReportOptions;
import com.fuse.dao.ReportTemplates;
import com.fuse.dao.RiskLevel;
import com.fuse.dao.Teams;
import com.fuse.dao.User;
import com.fuse.dao.Vulnerability;
import com.fuse.docx.DocxUtils;
import com.fuse.utils.reporttemplate.ReportTemplate;
import com.fuse.utils.reporttemplate.ReportTemplateFactory;

public class GenerateReport {

	private static String img = "data:image/png;base64,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";
	private String css = "body" + "{" + "   font-family: Arial;" + "}" + ".cke_editable" + "{" + "	font-size: 20px;"
			+ "	line-height: 1.2;" + "}" + "blockquote" + "{" + "	font-style: italic;" + "	font-family: Arial"
			+ "	padding: 2px 0;" + "	border-style: solid;" + "	border-color: #ccc;" + "	border-width: 0;" + "}"
			+ ".cke_contents_ltr blockquote" + "{" + "	padding-left: 20px;" + "	padding-right: 8px;"
			+ "	border-left-width: 5px;" + "}" + ".cke_contents_rtl blockquote" + "{" + "	padding-left: 8px;"
			+ "	padding-right: 20px;" + "	border-right-width: 5px;" + "}" + "a" + "{" + "	color: #0782C1;" + "}"
			+ "ol,ul,dl" + "{" + "	/* IE7: reset rtl list margin. (#7334) */" + "	*margin-right: 0px;"
			+ "	/* preserved spaces for list items with text direction other than the list. (#6249,#8049)*/"
			+ "	padding: 0 40px;" + "}" + "h1,h2,h3,h4,h5,h6" + "{" + "	font-weight: normal;" + "	line-height: 1.2;"
			+ "}" + "hr" + "{" + "	border: 0px;" + "	border-top: 1px solid #ccc;" + "}" + "img.right" + "{"
			+ "	border: 1px solid #ccc;" + "	float: right;" + "	margin-left: 15px;" + "	padding: 5px;" + "}"
			+ "img.left" + "{" + "	border: 1px solid #ccc;" + "	float: left;" + "	margin-right: 15px;"
			+ "	padding: 5px;" + "}" +
			/*
			 * "pre"+ "{"+ "	white-space: pre-wrap; "+ "	word-wrap: break-all; "+
			 * "	-moz-tab-size: 4;"+ "	tab-size: 4;"+ "}"+
			 */
			".marker" + "{" + "	background-color: Yellow;" + "}" + "span[lang]" + "{" + "	font-style: italic;" + "}"
			+ "figure" + "{" + "	text-align: center;" + "	border: solid 1px #ccc;" + "	border-radius: 2px;"
			+ "	background: rgba(0,0,0,0.05);" + "	padding: 10px;" + "	margin: 10px 20px;" + "	display: inline-block;"
			+ "}" + "figure > figcaption" + "{" + "	text-align: center;" + "	display: block; /* For IE8 */" + "}"
			+ "a > img {" + "	padding: 1px;" + "	margin: 1px;" + "	border: none;" + "	outline: 1px solid #0782C1;"
			+ "}" + "p{" + "word-wrap: break-word; " + "overflow:hidden;" + "white-space: wrap;" + "padding:0px;"
			+ "margin:0px" + "}";

	public String generateRetestDocxReport(Long id, EntityManager em, String host) {
		String guid = UUID.randomUUID().toString();

		Properties props = System.getProperties();
		String debug = (String) props.get("fusevt.debug");

		// String debug = "false";

		ReportOptions RPO = (ReportOptions) em.createQuery("from ReportOptions").getSingleResult();

		String customCSS = css + (RPO == null ? "" : RPO.getBodyCss());

		try {

			Assessment a = (Assessment) em.createQuery("from Assessment where id = :id").setParameter("id", id)
					.getResultList().stream().findFirst().orElse(null);
			for (Vulnerability v : a.getVulns()) {
				v.updateRiskLevels(em);
			}
			//TODO: Not sure why this is added... debugging?
			/*for (Vulnerability v : a.getVulns()) {
				System.out.println(v.getOverallStr());
			}*/

			String mongoQuery = "{\"type_id\" : " + a.getType().getId() + ", \"team_id\" : "
					+ a.getAssessor().get(0).getTeam().getId() + ", \"retest\" : true }";
			ReportTemplates base = (ReportTemplates) em.createNativeQuery(mongoQuery, ReportTemplates.class)
					.getSingleResult();

			ReportTemplate report = (new ReportTemplateFactory()).getReportTemplate();
			InputStream is = report.getTemplate(base.getFilename());

			WordprocessingMLPackage mlp = WordprocessingMLPackage.load(is);

			DocxUtils genDoc = new DocxUtils();
			genDoc.FONT = RPO.getFont();
			genDoc.generateDocx(mlp, a, customCSS);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			mlp.save(baos);
			byte[] finalReport = baos.toByteArray();
			String docx = Base64.encodeBase64String(finalReport);
			return docx;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	public String generateDocxReport(Long id, EntityManager em) {

		Properties props = System.getProperties();

		String debug = (String) props.get("fusevt.debug");

		ReportOptions RPO = (ReportOptions) em.createQuery("from ReportOptions").getSingleResult();

		String customCSS = css + (RPO == null ? "" : RPO.getBodyCss());

		try {

			Assessment a = (Assessment) em.createQuery("from Assessment where id = :id").setParameter("id", id)
					.getResultList().stream().findFirst().orElse(null);

			for (Vulnerability v : a.getVulns()) {
				v.updateRiskLevels(em);
			}
			for (Vulnerability v : a.getVulns()) {
				System.out.println(v.getOverallStr());
			}

			String mongoQuery = "{ 'type_id' : " + a.getType().getId() + ", 'team_id' : "
					+ a.getAssessor().get(0).getTeam().getId() + ", 'retest' : false }";

			ReportTemplates base = (ReportTemplates) em.createNativeQuery(mongoQuery, ReportTemplates.class)
					.getSingleResult();

			ReportTemplate report = (new ReportTemplateFactory()).getReportTemplate();
			InputStream is = report.getTemplate(base.getFilename());

			// WordprocessingMLPackage mlp = WordprocessingMLPackage.load(new
			// File(base.getFilename()));
			WordprocessingMLPackage mlp = WordprocessingMLPackage.load(is);

			DocxUtils gendoc = new DocxUtils();
			gendoc.FONT = RPO.getFont();
			mlp = gendoc.generateDocx(mlp, a, customCSS);
			gendoc.tocGenerator(mlp);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			mlp.save(baos);
			// byte[] report = DocxUtils.updateTOC(baos.toByteArray());
			byte[] finalReport = baos.toByteArray();

			String docx = Base64.encodeBase64String(finalReport);
			return docx;

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	public static Assessment createTestAssessment(Teams t, AssessmentType type, List<RiskLevel> riskLevels) {

		Assessment a = new Assessment();
		User u = new User();
		u.setFname("Bob");
		u.setLname("Dobbs");
		u.setEmail("bdobs@supersecure.com");
		u.setTeam(t);
		a.setName("Test PCI Assessment");
		a.setId(1337l);
		a.setEngagement(u);
		List<User> hacker = new ArrayList<User>();
		hacker.add(u);
		hacker.add(u);
		a.setAssessor(hacker);
		a.setRemediation(u);
		a.setAppId("1337");
		a.setRiskAnalysis("This is a test report.");
		a.setSummary("This is a test report.");
		Vulnerability v0 = new Vulnerability();
		v0.setLevels(riskLevels);
		Vulnerability v1 = new Vulnerability();
		v1.setLevels(riskLevels);
		Vulnerability v2 = new Vulnerability();
		v2.setLevels(riskLevels);
		v0.setName("Test Issue 1");
		v0.setImpact(5l);
		v0.setLikelyhood(5l);
		v0.setOverall(5l);
		v0.setRecommendation("Test Recommendation");
		v0.setDescription("Test Description");
		v0.setTracking("VID-1234");
		v1.setName("Test Issue 2");
		v1.setImpact(4l);
		v1.setLikelyhood(4l);
		v1.setOverall(4l);
		v1.setRecommendation("Test Recommendation");
		v1.setDescription("Test Description");
		v1.setTracking("VID-1235");
		v2.setName("Test Issue 3");
		v2.setImpact(3l);
		v2.setLikelyhood(3l);
		v2.setOverall(3l);
		v2.setRecommendation("Test Recommendation");
		v2.setDescription("Test Description");
		v2.setTracking("VID-1236");
		a.setVulns((new ArrayList<Vulnerability>()));
		a.getVulns().add(v0);
		a.getVulns().add(v1);
		a.getVulns().add(v2);
		a.setStart(new Date());
		a.setEnd(new Date());
		a.setType(type);
		ExploitStep ex = new ExploitStep();
		ex.setDescription("<pre class='code'>Code Example</pre><br/>" + "Image Shown below<br/>" + "<img src='" + img
				+ "' style='width:20%' ></img><br/>" + "");
		ex.setStepNum(1);
		v0.setSteps(new ArrayList<ExploitStep>());
		v1.setSteps(new ArrayList<ExploitStep>());
		v2.setSteps(new ArrayList<ExploitStep>());
		v0.getSteps().add(ex);
		v1.getSteps().add(ex);
		v2.getSteps().add(ex);
		return a;

	}

	public byte[] testDocxPage(EntityManager em, Long teamId, Long typeId, boolean retest) {

		Properties props = System.getProperties();

		String debug = (String) props.get("fusevt.debug");

		String guid = UUID.randomUUID().toString();

		ReportOptions RPO = (ReportOptions) em.createQuery("from ReportOptions").getSingleResult();

		String customCSS = this.css + (RPO == null ? "" : RPO.getBodyCss());

		try {

			Teams t = em.find(Teams.class, teamId);
			AssessmentType type = em.find(AssessmentType.class, typeId);

			Vulnerability v0 = new Vulnerability();
			v0.updateRiskLevels();

			Assessment a = this.createTestAssessment(t, type, v0.getLevels());

			String mongoQuery = "{\"type_id\" : " + a.getType().getId() + ", \"team_id\" : "
					+ a.getAssessor().get(0).getTeam().getId() + ", \"retest\" : " + retest + "}";

			ReportTemplates base = (ReportTemplates) em.createNativeQuery(mongoQuery, ReportTemplates.class)
					.getSingleResult();

			ReportTemplate report = (new ReportTemplateFactory()).getReportTemplate();
			InputStream is = report.getTemplate(base.getFilename());

			WordprocessingMLPackage mlp = WordprocessingMLPackage.load(is);

			DocxUtils genDoc = new DocxUtils();
			genDoc.FONT = RPO.getFont();
			mlp = genDoc.generateDocx(mlp, a, customCSS);
			genDoc.tocGenerator(mlp);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			mlp.save(baos);

			// byte[] report = DocxUtils.updateTOC(baos.toByteArray());
			byte[] finalReport = (baos.toByteArray());
			return finalReport;

		} catch (Exception ex) {
			ex.printStackTrace();
			return new byte[0];
		}
	}

}
