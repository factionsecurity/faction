package com.fuse.utils.reporttemplate;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.io.IOUtils;

public class ReportTemplate {
	URL defaultTemplateURL;
	String defaultReportName = "default-report-template.docx";


	public ReportTemplate() {
		try {
			this.defaultTemplateURL = new URL(
					"https://github.com/factionsecurity/report_templates/raw/main/" + this.defaultReportName);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}

	}

	public InputStream getDefaultTemplate() {
		try {
			URLConnection con = this.defaultTemplateURL.openConnection();
			con.setUseCaches(false);
			return con.getInputStream();
		} catch (IOException e) {
			return null;
		}
	}

	public void uploadTemplate(String templateName, byte[] templateBytes) {
	}

	public InputStream getTemplate(String fileName) {
		return null;
	}

	public void deleteTemplate(String fileName) {
	}

	public String setup() {
		InputStream is = this.getDefaultTemplate();
		try {
			byte[] byteArray = IOUtils.toByteArray(is);
			this.uploadTemplate(this.defaultReportName, byteArray);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return this.defaultReportName;

	}

}
