package com.fuse.utils;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

import javax.imageio.ImageIO;

/**
 * Downscales oversized report images before they are embedded into
 * documents. Full-resolution screenshots are the dominant cost of report
 * generation — XHTML conversion time, docx size, heap and LibreOffice/PDF
 * time all scale with pixel count — while the rendered image is page width
 * or narrower anyway.
 *
 * The maximum width is configurable with the FACTION_REPORT_IMAGE_MAX_WIDTH
 * environment variable (or system property). A value of 0 or less disables
 * downscaling entirely.
 */
public class ReportImageScaler {

	public static final int DEFAULT_MAX_WIDTH = 1600;

	// images larger than this many bytes are re-encoded even when their
	// width already fits the cap
	private static final int REENCODE_BYTE_THRESHOLD = 512 * 1024;

	public static int configuredMaxWidth() {
		String conf = System.getProperty("FACTION_REPORT_IMAGE_MAX_WIDTH");
		if (conf == null || conf.trim().isEmpty()) {
			conf = System.getenv("FACTION_REPORT_IMAGE_MAX_WIDTH");
		}
		if (conf == null || conf.trim().isEmpty()) {
			return DEFAULT_MAX_WIDTH;
		}
		try {
			return Integer.parseInt(conf.trim());
		} catch (NumberFormatException e) {
			System.err.println("Invalid FACTION_REPORT_IMAGE_MAX_WIDTH '" + conf + "', using "
					+ DEFAULT_MAX_WIDTH);
			return DEFAULT_MAX_WIDTH;
		}
	}

	/**
	 * Data URI in, data URI out. Returns the input unchanged when the image
	 * is already narrow enough, is not a re-encodable format (gif/webp/svg),
	 * or anything at all goes wrong — a full-size image is always preferable
	 * to a broken report.
	 */
	public static String downscaleDataUri(String dataUri, int maxWidth) {
		if (maxWidth <= 0 || dataUri == null) {
			return dataUri;
		}
		try {
			if (!dataUri.startsWith("data:image/")) {
				return dataUri;
			}
			int comma = dataUri.indexOf(',');
			if (comma < 0) {
				return dataUri;
			}
			String header = dataUri.substring(0, comma);
			String format;
			if (header.contains("image/png")) {
				format = "png";
			} else if (header.contains("image/jpeg") || header.contains("image/jpg")) {
				format = "jpg";
			} else {
				return dataUri;
			}
			byte[] bytes = Base64.getMimeDecoder().decode(dataUri.substring(comma + 1));
			BufferedImage img = ImageIO.read(new ByteArrayInputStream(bytes));
			if (img == null) {
				return dataUri;
			}
			boolean needsResize = img.getWidth() > maxWidth;
			// heavy-but-narrow images (large screenshots that fit the width
			// cap) still bloat the document; re-encode them at original size
			boolean heavy = bytes.length > REENCODE_BYTE_THRESHOLD;
			if (!needsResize && !heavy) {
				return dataUri;
			}
			int w = needsResize ? maxWidth : img.getWidth();
			int h = needsResize
					? Math.max(1, (int) Math.round((double) img.getHeight() * maxWidth / img.getWidth()))
					: img.getHeight();
			boolean opaque = img.getTransparency() == Transparency.OPAQUE || "jpg".equals(format);
			BufferedImage scaled = new BufferedImage(w, h,
					opaque ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB);
			Graphics2D g = scaled.createGraphics();
			g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.drawImage(img, 0, 0, w, h, null);
			g.dispose();

			// keep whichever encoding is smallest: downscaled PNG keeps
			// crisp text, JPEG wins by a wide margin on dense content that
			// PNG cannot compress once scaling has smoothed it
			byte[] best = null;
			String bestMime = null;
			ByteArrayOutputStream png = new ByteArrayOutputStream();
			if (ImageIO.write(scaled, "png", png) && png.size() < bytes.length) {
				best = png.toByteArray();
				bestMime = "image/png";
			}
			if (opaque) {
				byte[] jpg = encodeJpeg(scaled, 0.85f);
				if (jpg != null && jpg.length < bytes.length && (best == null || jpg.length < best.length)) {
					best = jpg;
					bestMime = "image/jpeg";
				}
			}
			if (best == null) {
				// nothing beat the original bytes; keep it
				return dataUri;
			}
			if (!needsResize && best.length > bytes.length * 0.8) {
				// a pure re-encode must earn its (possibly lossy) keep
				return dataUri;
			}
			return "data:" + bestMime + ";base64," + Base64.getEncoder().encodeToString(best);
		} catch (Throwable t) {
			// never fail a report over an image
			return dataUri;
		}
	}

	private static byte[] encodeJpeg(BufferedImage img, float quality) {
		javax.imageio.ImageWriter writer = null;
		try {
			writer = ImageIO.getImageWritersByFormatName("jpg").next();
			javax.imageio.ImageWriteParam param = writer.getDefaultWriteParam();
			param.setCompressionMode(javax.imageio.ImageWriteParam.MODE_EXPLICIT);
			param.setCompressionQuality(quality);
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			javax.imageio.stream.ImageOutputStream ios = ImageIO.createImageOutputStream(out);
			writer.setOutput(ios);
			writer.write(null, new javax.imageio.IIOImage(img, null, null), param);
			ios.close();
			return out.toByteArray();
		} catch (Throwable t) {
			return null;
		} finally {
			if (writer != null) {
				writer.dispose();
			}
		}
	}

	/** Cheap stable key for caching downscale results of large URI strings. */
	public static String hash(String value) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] digest = md.digest(value.getBytes(StandardCharsets.UTF_8));
			StringBuilder hex = new StringBuilder();
			for (byte b : digest) {
				String h = Integer.toHexString(0xff & b);
				if (h.length() == 1) {
					hex.append('0');
				}
				hex.append(h);
			}
			return hex.toString();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
