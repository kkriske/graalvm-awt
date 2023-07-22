package io.github.kkriske;

import org.junit.jupiter.api.Test;

import java.awt.*;

import static org.junit.jupiter.api.Assertions.assertNotEquals;

class FontManagerTest {
    @Test
    void testGetAllFonts() {
        Font[] allFonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
        assertNotEquals(0, allFonts.length);
    }
}
