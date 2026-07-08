package org.fuse.docx;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Base64;

import javax.imageio.ImageIO;

import org.junit.Test;

import com.fuse.utils.FSUtils;
import com.fuse.utils.ReportImageScaler;

public class ReportImageScalerTest {

	private String makePng(int w, int h) throws Exception {
		BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ImageIO.write(img, "png", out);
		return "data:image/png;base64," + Base64.getEncoder().encodeToString(out.toByteArray());
	}

	@Test
	public void downscalesWidePng() throws Exception {
		String uri = makePng(2400, 1400);
		String scaled = ReportImageScaler.downscaleDataUri(uri, 1600);
		assertNotEquals("should have been rewritten", uri, scaled);
		byte[] bytes = Base64.getMimeDecoder().decode(scaled.substring(scaled.indexOf(',') + 1));
		BufferedImage img = ImageIO.read(new ByteArrayInputStream(bytes));
		assertEquals(1600, img.getWidth());
	}

	@Test
	public void leavesSmallPngAlone() throws Exception {
		String uri = makePng(800, 500);
		assertEquals(uri, ReportImageScaler.downscaleDataUri(uri, 1600));
	}

	@Test
	public void inlineUriSurvivesJtidyUnchanged() throws Exception {
		String uri = makePng(1200, 700);
		String content = "Details text<br/><img src='" + uri + "'></img><br/>";
		String tidied = FSUtils.jtidy(content);
		assertTrue("jtidy must preserve the data URI byte-for-byte, or the "
				+ "warm-cache hash lookup misses", tidied.contains(uri));
	}

	@Test
	public void megabyteUriSurvivesJtidyUnchanged() throws Exception {
		// realistic screenshot-scale URI (~1MB+); jtidy behavior can differ
		// with huge attribute values
		java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(2400, 1400,
				java.awt.image.BufferedImage.TYPE_INT_RGB);
		java.awt.Graphics2D g = img.createGraphics();
		g.setColor(java.awt.Color.WHITE);
		g.fillRect(0, 0, 2400, 1400);
		java.util.Random rnd = new java.util.Random(7);
		g.setFont(new java.awt.Font("Monospaced", java.awt.Font.PLAIN, 14));
		for (int y = 14; y < 1400; y += 16) {
			g.setColor(new java.awt.Color(rnd.nextInt(64), rnd.nextInt(64), rnd.nextInt(64)));
			StringBuilder line = new StringBuilder();
			while (line.length() < 300) {
				line.append(Integer.toHexString(rnd.nextInt())).append(' ');
			}
			g.drawString(line.toString(), 4, y);
		}
		g.dispose();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ImageIO.write(img, "png", out);
		String uri = "data:image/png;base64," + Base64.getEncoder().encodeToString(out.toByteArray());
		System.out.println("URI length: " + uri.length());

		String scaled = ReportImageScaler.downscaleDataUri(uri, 1600);
		System.out.println("scaled length: " + scaled.length() + " changed=" + !scaled.equals(uri)
				+ " mime=" + scaled.substring(0, scaled.indexOf(';')));

		// exercise the jpeg encoder directly via reflection to see its size
		java.lang.reflect.Method m = ReportImageScaler.class.getDeclaredMethod("encodeJpeg",
				java.awt.image.BufferedImage.class, float.class);
		m.setAccessible(true);
		byte[] raw = Base64.getMimeDecoder().decode(uri.substring(uri.indexOf(',') + 1));
		java.awt.image.BufferedImage src = ImageIO.read(new ByteArrayInputStream(raw));
		byte[] jpg = (byte[]) m.invoke(null, src, 0.85f);
		System.out.println("direct jpeg bytes: " + (jpg == null ? "null" : jpg.length)
				+ " original bytes: " + raw.length);
		assertNotEquals("scaler should shrink a text-style screenshot", uri, scaled);

		String content = "Details text<br/><img src='" + uri + "'></img><br/>";
		String tidied = FSUtils.jtidy(content);
		assertTrue("jtidy altered a megabyte data URI", tidied.contains(uri));
	}
}
