package com.fuse.utils.reporttemplate;

import org.apache.commons.lang3.SystemUtils;

public class ReportTemplateFactory {

	public ReportTemplate getReportTemplate() {
		String reportStorage = SystemUtils.getEnvironmentVariable("REPORT_STORAGE", "");
		if (reportStorage.equals("aws")) {
			return new S3();
		} else {
			return new LocalFile();
		}
	}

}
