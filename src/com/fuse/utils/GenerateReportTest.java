package com.fuse.utils;

import static org.junit.Assert.*;

import java.awt.Desktop;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.persistence.EntityManager;

import org.docx4j.org.apache.xml.security.exceptions.Base64DecodingException;
import org.docx4j.org.apache.xml.security.utils.Base64;
import org.junit.Test;

import com.fuse.dao.HibHelper;
import com.fuse.docx.DocxUtils;

public class GenerateReportTest {

	@Test
	public void generateNormalReport() {
		EntityManager em = HibHelper.getInstance().getEMF().createEntityManager();
		GenerateReport report = new GenerateReport();
		
		String b64report = report.generateDocxReport(28l, em);
		assertNotNull(b64report);
		try {
			byte [] bytereport = Base64.decode(b64report);
			assertNotNull(bytereport);

		} catch (Base64DecodingException e) {
			fail(e.getMessage());
		}
		
		em.close();
	}

}
