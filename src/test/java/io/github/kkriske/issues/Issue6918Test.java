package io.github.kkriske.issues;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.font.TextAttribute;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * https://github.com/oracle/graal/issues/6918
 */
class Issue6918Test {
    @Test
    @EnabledOnOs(OS.WINDOWS)
    void testWindows() throws IOException {
        test("issue6918win.png");
    }

    @Test
    @EnabledOnOs(OS.LINUX)
    void testLinux() throws IOException {
        test("issue6918unix.png");
    }

    private void test(String imgResource) throws IOException {
        Font font = new Font(Map.of(
                TextAttribute.SIZE, 50,
                TextAttribute.WEIGHT, TextAttribute.WEIGHT_REGULAR,
                TextAttribute.TRACKING, TextAttribute.TRACKING_TIGHT
        ));
        BufferedImage image = new BufferedImage(512, 512, BufferedImage.TYPE_INT_RGB);
        Graphics2D pen = image.createGraphics();
        pen.setFont(font);
        pen.setColor(Color.WHITE);
        pen.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        pen.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        pen.drawString("12345678", 150, 100);
        pen.drawString("12345678", 150, 250);
        pen.drawString("12345678", 150, 400);
        image.flush();

        try (InputStream in = Issue6918Test.class.getResourceAsStream(imgResource);
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", out);
            Assertions.assertArrayEquals(in.readAllBytes(), out.toByteArray());
        }
    }
}
