package de.fabiansiemens.hyperion.core.util;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import javax.imageio.ImageIO;

import org.springframework.stereotype.Service;

@Service
public class ImageCompressionService {

	//TODO IMPROVE
    public byte[] compressImage(byte[] imageData, String fileExtension, int maxWidth, int maxHeight) throws Exception {
    	
    	
        // Bild aus Byte-Array laden
        ByteArrayInputStream inputStream = new ByteArrayInputStream(imageData);
        BufferedImage originalImage = ImageIO.read(inputStream);

        // Ursprüngliche Bildmaße
        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();

        // Seitenverhältnis beibehalten und neue Breite/Höhe berechnen
        double aspectRatio = (double) originalWidth / originalHeight;
        int newWidth = originalWidth;
        int newHeight = originalHeight;

        if (originalWidth > maxWidth || originalHeight > maxHeight) {
            if (aspectRatio > 1) { // Breiter als hoch
                newWidth = maxWidth;
                newHeight = (int) (maxWidth / aspectRatio);
            } else { // Höher als breit
                newHeight = maxHeight;
                newWidth = (int) (maxHeight * aspectRatio);
            }
        }

        // Skaliertes Bild erzeugen
        Image scaledImage = originalImage.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
        BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, originalImage.getType());

        Graphics2D g2d = resizedImage.createGraphics();
        g2d.drawImage(scaledImage, 0, 0, null);
        g2d.dispose();

        // Zurück in ein Byte-Array konvertieren
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(resizedImage, fileExtension, outputStream); // "jpg" oder das entsprechende Format des Originals
        return outputStream.toByteArray();
    }
}
