package com.fuse.dao;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.CompletableFuture;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.TableGenerator;
import javax.persistence.Transient;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;

@Entity
public class ReportTemplates {
	
	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "repTemp")
    @TableGenerator(
        name = "repTemp",
        table = "repTempseq",
        pkColumnValue = "repTemp",
        valueColumnName = "nextrepTemp",
        initialValue = 1,
        allocationSize = 1
    )
	private Long id;
	private String name;
	private String variable;
	private String filename;
	@ManyToOne(fetch = FetchType.LAZY)
	private Teams team;
	@ManyToOne(fetch = FetchType.LAZY)
	private AssessmentType type;
	private boolean retest = false;
	private String base64EncodedTemplate;
	private Boolean saveInDB=false; //This is to support existing customers
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getFilename() {
		return filename;
	}
	public void setFilename(String filename) {
		this.filename = filename;
	}
	public Teams getTeam() {
		return team;
	}
	public void setTeam(Teams team) {
		this.team = team;
	}
	public AssessmentType getType() {
		return type;
	}
	public void setType(AssessmentType type) {
		this.type = type;
	}
	public boolean isRetest() {
		return retest;
	}
	public void setRetest(boolean retest) {
		this.retest = retest;
	}
	public String getVariable() {
		return variable;
	}
	public void setVariable(String variable) {
		this.variable = variable;
	}
	public String getBase64EncodedTemplate() {
		return base64EncodedTemplate;
	}
	public void setBase64EncodedTemplate(String base64EncodedTemplate) {
		this.base64EncodedTemplate = base64EncodedTemplate;
	}
	public Boolean getSaveInDB() {
		return saveInDB != null && saveInDB;
	}
	public void setSaveInDB(Boolean saveInDB) {
		this.saveInDB = saveInDB;
	}
	
	@Transient
	public InputStream getTemplate() {
		String b64Template = this.getBase64EncodedTemplate();
		byte [] docx = Base64.decodeBase64(b64Template);
		return new ByteArrayInputStream(docx);
	}
	
	@Transient
	public void initDefaultTemplate(Teams team, AssessmentType type) {
			CompletableFuture.supplyAsync( () -> {
				try {
					String defaultReportName = "default-report-template.docx";
					URL defaultTemplateURL = new URL(
							"https://github.com/factionsecurity/report_templates/raw/main/" + defaultReportName);
					URLConnection con = defaultTemplateURL.openConnection();
					con.setUseCaches(false);
					InputStream is = con.getInputStream();
					byte[] docxBytes = IOUtils.toByteArray(is);
					String docxB64 = Base64.encodeBase64String(docxBytes);
					this.setFilename(defaultReportName);
					this.saveInDB = true;
					this.base64EncodedTemplate = docxB64;
					this.team = team;
					this.type = type;
					this.name = "Sample Template";
					this.retest = false;
					EntityManager em = HibHelper.getInstance().getEM();
					HibHelper.getInstance().preJoin();
					em.joinTransaction();
					em.persist(this);
					HibHelper.getInstance().commit();
					
				} catch (MalformedURLException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
				return true;
			});
		
	}
	
	
	
	
	

}
