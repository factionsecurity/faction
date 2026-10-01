package com.fuse.unittests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.junit.Test;

import com.fuse.utils.FSUtils;

/** FSUtils.checkUploadedFile must accept Struts multipart temp files and reject anything else. */
public class UploadedFileGuardTest {

	@Test
	public void acceptsFileInsideTempDir() throws Exception {
		File f = Files.createTempFile("faction-upload", ".bin").toFile();
		try {
			File ok = FSUtils.checkUploadedFile(f);
			assertEquals(f.getCanonicalPath(), ok.getCanonicalPath());
		} finally {
			f.delete();
		}
	}

	@Test
	public void rejectsFileOutsideTempDir() throws Exception {
		File outside = new File("pom.xml").getAbsoluteFile(); // exists, but not an upload
		try {
			FSUtils.checkUploadedFile(outside);
			fail("file outside the upload directory must be rejected");
		} catch (IOException expected) {
		}
	}

	@Test
	public void rejectsMissingOrNull() throws Exception {
		try { FSUtils.checkUploadedFile(null); fail("null must be rejected"); } catch (IOException expected) {}
		try { FSUtils.checkUploadedFile(new File(System.getProperty("java.io.tmpdir"), "does-not-exist-" + System.nanoTime())); fail("missing file must be rejected"); } catch (IOException expected) {}
	}
}
