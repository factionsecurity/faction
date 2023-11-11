package com.fuse.utils.reporttemplate;

import java.io.InputStream;

public interface ReportTemplateInterface {
	public void uploadTemplate(String templateName, byte[] templateBytes);

	public InputStream getTemplate(String fileName);

	public InputStream getDefaultTemplate();

	public void deleteTemplate(String fileName);

	public String setup();

}
