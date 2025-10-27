package com.fuse.utils;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.util.Iterator;

public class ImageBorderUtil {
    
    /**
     * Adds a border to image data of any supported format
     * 
     * @param imageData The raw image data as byte array
     * @param borderWidth The width of the border in pixels
     * @param borderColor The color of the border
     * @param outputFormat Output format (e.g., "PNG", "JPEG", "BMP", "GIF")
     * @return Image data with border as byte array
     * @throws IOException if image cannot be read or written
     */
    public static byte[] addBorder(byte[] imageData, 
                                   int borderWidth, 
                                   Color borderColor,
                                   String outputFormat) throws IOException {
        // Read image data into BufferedImage
        ByteArrayInputStream bais = new ByteArrayInputStream(imageData);
        BufferedImage sourceImage = ImageIO.read(bais);
        
        if (sourceImage == null) {
            throw new IOException("Invalid or unsupported image data");
        }
        
        // Calculate new dimensions
        int newWidth = sourceImage.getWidth() + (2 * borderWidth);
        int newHeight = sourceImage.getHeight() + (2 * borderWidth);
        
        // Determine image type based on output format and source image
        int imageType = determineImageType(outputFormat, sourceImage);
        
        // Create new image with border
        BufferedImage borderedImage = new BufferedImage(
            newWidth, 
            newHeight, 
            imageType
        );
        
        // Get graphics context
        Graphics2D g2d = borderedImage.createGraphics();
        
        // Enable antialiasing for better quality
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                            RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, 
                            RenderingHints.VALUE_RENDER_QUALITY);
        
        // Fill with border color
        g2d.setColor(borderColor);
        g2d.fillRect(0, 0, newWidth, newHeight);
        
        // Draw original image in center
        g2d.drawImage(sourceImage, borderWidth, borderWidth, null);
        g2d.dispose();
        
        // Convert back to specified format
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        boolean written = ImageIO.write(borderedImage, outputFormat, baos);
        
        if (!written) {
            throw new IOException("No writer found for format: " + outputFormat);
        }
        
        return baos.toByteArray();
    }
    
    /**
     * Auto-detects input format and uses same format for output
     */
    public static byte[] addBorder(byte[] imageData, 
                                   int borderWidth, 
                                   Color borderColor) throws IOException {
        String format = detectImageFormat(imageData);
        return addBorder(imageData, borderWidth, borderColor, format);
    }
    
    /**
     * Detects the image format from raw data
     */
    private static String detectImageFormat(byte[] imageData) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(imageData);
        ImageInputStream iis = ImageIO.createImageInputStream(bais);
        
        if (iis == null) {
            throw new IOException("Unable to create ImageInputStream");
        }
        
        Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
        
        if (!readers.hasNext()) {
            iis.close();
            throw new IOException("No reader found for image data");
        }
        
        ImageReader reader = readers.next();
        String format = reader.getFormatName().toUpperCase();
        
        reader.dispose();
        iis.close();
        
        return format;
    }
    
    /**
     * Determines the appropriate BufferedImage type based on output format
     */
    private static int determineImageType(String format, BufferedImage sourceImage) {
        String fmt = format.toUpperCase();
        
        // JPEG doesn't support transparency
        if (fmt.equals("JPEG") || fmt.equals("JPG")) {
            return BufferedImage.TYPE_INT_RGB;
        }
        
        // BMP typically doesn't support transparency
        if (fmt.equals("BMP")) {
            return BufferedImage.TYPE_INT_RGB;
        }
        
        // For formats that support transparency (PNG, GIF, WBMP, etc.)
        // Check if source image has transparency
        if (sourceImage.getColorModel().hasAlpha()) {
            return BufferedImage.TYPE_INT_ARGB;
        }
        
        // Default to RGB
        return BufferedImage.TYPE_INT_RGB;
    }
    
    /**
     * Lists all supported image format names
     */
    public static String[] getSupportedFormats() {
        return ImageIO.getWriterFormatNames();
    }
    
    // Convenience methods
    public static byte[] addBorder(byte[] imageData, int borderWidth) throws IOException {
        return addBorder(imageData, borderWidth, Color.BLACK);
    }
    
    public static byte[] addBorder(byte[] imageData) throws IOException {
        return addBorder(imageData, 1, Color.BLACK);
    }
    
    // InputStream version
    public static byte[] addBorder(InputStream imageStream, 
                                   int borderWidth, 
                                   Color borderColor,
                                   String outputFormat) throws IOException {
        BufferedImage sourceImage = ImageIO.read(imageStream);
        
        if (sourceImage == null) {
            throw new IOException("Invalid or unsupported image data");
        }
        
        int newWidth = sourceImage.getWidth() + (2 * borderWidth);
        int newHeight = sourceImage.getHeight() + (2 * borderWidth);
        
        int imageType = determineImageType(outputFormat, sourceImage);
        
        BufferedImage borderedImage = new BufferedImage(
            newWidth, 
            newHeight, 
            imageType
        );
        
        Graphics2D g2d = borderedImage.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                            RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, 
                            RenderingHints.VALUE_RENDER_QUALITY);
        
        g2d.setColor(borderColor);
        g2d.fillRect(0, 0, newWidth, newHeight);
        g2d.drawImage(sourceImage, borderWidth, borderWidth, null);
        g2d.dispose();
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        boolean written = ImageIO.write(borderedImage, outputFormat, baos);
        
        if (!written) {
            throw new IOException("No writer found for format: " + outputFormat);
        }
        
        return baos.toByteArray();
    }
}