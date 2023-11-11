package com.fuse.utils.reporttemplate;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public class LocalFile extends ReportTemplate {
	String path;

	public LocalFile() {
		this.path = "/opt/faction/templates/";
		if (System.getProperty("os.name").contains("Windows")) {
			this.path = "C:\\fusesoft\\templates\\";
		}

	}

	public void uploadTemplate(String templateName, byte[] templateBytes) {
		String filename = path + templateName;
		File outputFile = new File(filename);
		try {
			Files.write(outputFile.toPath(), templateBytes);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public InputStream getTemplate(String templateName) {
		String filename = path + templateName;
		File templateFile = new File(filename);
		try {
			return new FileInputStream(templateFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return this.getDefaultTemplate();
		}
	}

	public void deleteTemplate(String templateName) {
		String filename = path + templateName;
		File file = new File(filename);
		try {
			Files.deleteIfExists(file.toPath());
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
