package com.fuse.actions.admin;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.Result;

import com.fuse.actions.FSActionSupport;
import com.fuse.dao.AssessmentType;
import com.fuse.dao.HibHelper;
import com.fuse.dao.Image;
import com.fuse.dao.ReportOptions;
import com.fuse.dao.ReportPage;
import com.fuse.dao.ReportTemplates;
import com.fuse.dao.Teams;
import com.fuse.utils.reporttemplate.ReportTemplate;
import com.fuse.utils.reporttemplate.ReportTemplateFactory;

@Namespace("/portal")
@Result(name = "success", location = "/WEB-INF/jsp/cms/ReportingTemplates.jsp")
public class CMS extends FSActionSupport {

	private String action = "";
	private Long id;
	private String document;
	private String header = "";
	private String footer = "";
	private List<ReportPage> fpages;
	private List<ReportPage> bpages;
	private List<Image> images;
	private String title;
	private boolean front;
	private String fontsize;
	private String fontname;
	private String css;
	private String headerSize;
	private String footerSize;
	private boolean footerCover = false;
	private boolean headerCover = false;
	private List<ReportTemplates> templates;
	private List<Teams> teams;
	private List<AssessmentType> types;
	private ReportTemplates selectedTemplate;
	private String name;
	private Long teamid;
	private Long typeid;
	private boolean retest;
	private File file_data;
	private String file_dataContentType;
	private String file_dataFilename;
	private String message;

	@Action(value = "cms", results = {
			@Result(name = "templateUpload", location = "/WEB-INF/jsp/cms/TemplateUpload.jsp"),
			@Result(name = "reportJSON", location = "/WEB-INF/jsp/cms/reportJSON.jsp") })
	public String execute() throws IOException {

		if (!this.isAcadmin())
			return LOGIN;

		templates = em.createQuery("from ReportTemplates").getResultList();
		teams = em.createQuery("from Teams").getResultList();
		types = em.createQuery("from AssessmentType").getResultList();

		if (this.action != null && this.action.equals("updateCSS")) {
			HibHelper.getInstance().preJoin();
			em.joinTransaction();
			ReportOptions rpo = (ReportOptions) em.createQuery("from ReportOptions").getResultList().stream()
					.findFirst().orElse(null);
			if (rpo == null)
				rpo = new ReportOptions();

			if (this.css == null) {
				rpo.setFont(this.fontname);
				// rpo.setSize(this.fontsize);
			} else
				rpo.setBodyCss(this.css.trim());

			em.persist(rpo);
			HibHelper.getInstance().commit();
			return this.SUCCESSJSON;
		} else if (action.equals("templateUpload")) {
			templates = em.createQuery("from ReportTemplates").getResultList();
			teams = em.createQuery("from Teams").getResultList();
			types = em.createQuery("from AssessmentType").getResultList();
			if (id != null) {
				selectedTemplate = (ReportTemplates) em.createQuery("from ReportTemplates where id = :id")
						.setParameter("id", id).getResultList().stream().findFirst().orElse(null);
			}
			return "templateUpload";

		} else if (action.equals("templateCreate")) {
			Teams team = (Teams) em.find(Teams.class, teamid);
			AssessmentType type = (AssessmentType) em.find(AssessmentType.class, typeid);
			String query = "{ 'team_id' : " + team.getId() + ", " + " 'type_id' : " + type.getId() + ", "
					+ " 'retest' : " + retest + ", " + " '_id' : { $ne : " + id + "}}";
			List<ReportTemplates> testRport = (List<ReportTemplates>) em.createNativeQuery(query, ReportTemplates.class)
					.getResultList();

			if (testRport != null && testRport.size() > 0) {
				message = "Template with same settings already exits.";
				return "errorJson";
			}

			ReportTemplates template = new ReportTemplates();

			template.setName(name);
			template.setRetest(retest);
			template.setTeam(team);
			template.setType(type);
			HibHelper.getInstance().preJoin();
			em.joinTransaction();
			em.persist(template);
			HibHelper.getInstance().commit();
			return this.SUCCESSJSON;

		} else if (action.equals("templateDelete")) {
			if (id != null) {
				selectedTemplate = (ReportTemplates) em.createQuery("from ReportTemplates where id = :id")
						.setParameter("id", id).getResultList().stream().findFirst().orElse(null);
				// Lets delete our old file to make room for the new one.
				if (selectedTemplate.getFilename() != null && !selectedTemplate.getFilename().equals("")) {
					ReportTemplate report = (new ReportTemplateFactory()).getReportTemplate();
					report.deleteTemplate(selectedTemplate.getFilename());
					// File deleteme = new File(selectedTemplate.getFilename());
					// deleteme.delete();

				}
				// remove the DAO
				HibHelper.getInstance().preJoin();
				em.joinTransaction();
				em.remove(selectedTemplate);
				HibHelper.getInstance().commit();
				return SUCCESSJSON;

			}
		} else if (action.equals("templateSave")) {
			if (id != null) {
				selectedTemplate = (ReportTemplates) em.createQuery("from ReportTemplates where id = :id")
						.setParameter("id", id).getResultList().stream().findFirst().orElse(null);

				File test = this.getFile_data();
				if (file_data != null) {
					// Lets delete our old file to make room for the new one.
					if (selectedTemplate.getFilename() != null && !selectedTemplate.getFilename().equals("")) {

						ReportTemplate report = (new ReportTemplateFactory()).getReportTemplate();
						report.deleteTemplate(selectedTemplate.getFilename());
						// File deleteme = new File(selectedTemplate.getFilename());
						// deleteme.delete();

					}
					UUID guid = UUID.randomUUID();

					ReportTemplate report = (new ReportTemplateFactory()).getReportTemplate();
					String filename = guid.toString() + ".docx";
					FileInputStream fis = new FileInputStream(file_data);
					byte[] docx = new byte[(int) file_data.length()];
					fis.read(docx, 0, (int) file_data.length());
					report.uploadTemplate(filename, docx);

					/*
					 * String path = "/opt/fusesoft/templates/";
					 * if(System.getProperty("os.name").contains("Windows")) path = "C:\\tmp\\";
					 * String filename = path + guid.toString() + ".docx"; FileOutputStream fos =
					 * new FileOutputStream(new File(filename)); FileInputStream fis = new
					 * FileInputStream(file_data); byte [] docx = new byte[(int)file_data.length()];
					 * fis.read(docx, 0, (int)file_data.length()); fos.write(docx); fos.close();
					 * fis.close();
					 */
					selectedTemplate.setFilename(filename);
				} else {
					Teams team = (Teams) em.find(Teams.class, this.teamid);
					AssessmentType type = (AssessmentType) em.find(AssessmentType.class, this.typeid);
					String query = "{ 'team_id' : " + team.getId() + ", " + " 'type_id' : " + type.getId() + ", "
							+ " 'retest' : " + retest + ", " + " '_id' : { $ne : " + id + "}}";
					List<ReportTemplates> testRport = (List<ReportTemplates>) em
							.createNativeQuery(query, ReportTemplates.class).getResultList();

					if (testRport != null && testRport.size() > 0) {
						message = "Template with same settings already exits.";
						return "errorJson";
					}
					selectedTemplate.setName(name);
					selectedTemplate.setTeam(team);
					selectedTemplate.setType(type);
					selectedTemplate.setRetest(retest);
				}
				HibHelper.getInstance().preJoin();
				em.joinTransaction();
				em.persist(selectedTemplate);
				HibHelper.getInstance().commit();
				if (file_data != null) {

					ReportOptions RPO = (ReportOptions) em.createQuery("from ReportOptions").getResultList().stream()
							.findFirst().orElse(null);
					this.fontname = RPO.getFont();
					this.fontsize = RPO.getSize();
					this.css = RPO.getBodyCss();
					return SUCCESS; // need this since the 'upload' button will just submit the form.
				} else
					return this.SUCCESSJSON;
			}
		} else {

			ReportOptions RPO = (ReportOptions) em.createQuery("from ReportOptions").getResultList().stream()
					.findFirst().orElse(null);
			if (RPO == null) {
				HibHelper.getInstance().preJoin();
				em.joinTransaction();
				this.fontsize = "12px";
				this.fontname = "Calibri";
				RPO = new ReportOptions();
				RPO.setFont(this.fontname);
				RPO.setBodyCss("body{\r\n" 
									+ "font-size:15px;\r\n" 
									+ "}\r\n" 
								+ "div{\r\n" 
									+ "}\r\n" 
								+ "img{\r\n"
									+ "width:50% !important;\r\n" 
									+ "height: auto !important;\r\n"
									+ "display: block !important;\r\n" 
									+ "margin: auto !important;\r\n" 
								+ "}\r\n" 
								+ "p{\r\n"
									+ "margin-bottom:15px !important;\r\n" 
								+ "}\r\n" 
								+ ".code{\r\n"
									+ "}\r\n" 
								+ "pre{\r\n" 
									+ "background-color:#eeeeee !important;\r\n"
									+ "border:1px solid #cccccc !important;\r\n" 
									+ "font-size:15px;\r\n" 
									+ "padding: 10px 15px;\r\n"
								+ "}\r\n "
							+ "table {\n"
								+ "  font-family: Arial, Helvetica, sans-serif;\n"
								+ "  border-collapse: collapse;\n"
								+ "  width: 100%;\n"
							+ "}\n"
							+ "td, th {\n"
								+ "  border: 1px solid #ddd;\n"
								+ "  padding: 0px;\n"
							+ "}\n\n");
				
				em.persist(RPO);
				HibHelper.getInstance().commit();
				// session.getTransaction().begin();
				// session.save(RPO);
				// session.getTransaction().commit();

			} else {
				this.fontname = RPO.getFont();
				this.fontsize = RPO.getSize();
				this.css = RPO.getBodyCss();

			}
		}

		return SUCCESS;
	}

	public File getFile_data() {
		return file_data;
	}

	public String getFile_dataContentType() {
		return file_dataContentType;
	}

	public String getFile_dataFilename() {
		return file_dataFilename;
	}

	public String getActiveCms() {
		return "active";
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getDocument() {
		return document;
	}

	public void setDocument(String document) {
		this.document = document;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public List<ReportPage> getFpages() {
		return fpages;
	}

	public void setFpages(List<ReportPage> fpages) {
		this.fpages = fpages;
	}

	public List<ReportPage> getBpages() {
		return bpages;
	}

	public void setBpages(List<ReportPage> bpages) {
		this.bpages = bpages;
	}

	public boolean isFront() {
		return front;
	}

	public void setFront(boolean front) {
		this.front = front;
	}

	public List<Image> getImages() {
		return images;
	}

	public void setImages(List<Image> images) {
		this.images = images;
	}

	public String getFontsize() {
		return fontsize;
	}

	public void setFontsize(String fontsize) {
		this.fontsize = fontsize;
	}

	public String getFontname() {
		return fontname;
	}

	public void setFontname(String fontname) {
		this.fontname = fontname;
	}

	public String getCss() {
		return css;
	}

	public void setCss(String css) {
		this.css = css;
	}

	public String getHeader() {
		return header;
	}

	public void setHeader(String header) {
		this.header = header;
	}

	public String getFooter() {
		return footer;
	}

	public void setFooter(String footer) {
		this.footer = footer;
	}

	public String getHeaderSize() {
		return headerSize;
	}

	public void setHeaderSize(String headerSize) {
		this.headerSize = headerSize;
	}

	public String getFooterSize() {
		return footerSize;
	}

	public void setFooterSize(String footerSize) {
		this.footerSize = footerSize;
	}

	public boolean getFooterCover() {
		return footerCover;
	}

	public void setFooterCover(boolean footerCover) {
		this.footerCover = footerCover;
	}

	public boolean getHeaderCover() {
		return headerCover;
	}

	public void setHeaderCover(boolean headerCover) {
		this.headerCover = headerCover;
	}

	public List<ReportTemplates> getTemplates() {
		return templates;
	}

	public void setTemplates(List<ReportTemplates> templates) {
		this.templates = templates;
	}

	public List<Teams> getTeams() {
		return teams;
	}

	public ReportTemplates getSelectedTemplate() {
		return selectedTemplate;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setTeamid(Long teamid) {
		this.teamid = teamid;
	}

	public void setTypeid(Long typeid) {
		this.typeid = typeid;
	}

	public void setRetest(boolean retest) {
		this.retest = retest;
	}

	public List<AssessmentType> getTypes() {
		return types;
	}

	public void setFile_data(File file_data) {
		this.file_data = file_data;
	}

	public void setFile_dataContentType(String file_dataContentType) {
		this.file_dataContentType = file_dataContentType;
	}

	public void setFile_dataFilename(String file_dataFilename) {
		this.file_dataFilename = file_dataFilename;
	}

	public String getMessage() {
		return message;
	}

}
