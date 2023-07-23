package io.github.kkriske.issues;

import org.junit.jupiter.api.Test;

import java.awt.*;

import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * https://github.com/oracle/graal/issues/2729
 */
class Issue2729Test {
    @Test
    void test() {
        String[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        assertNotEquals(0, fonts.length);
    }
}
