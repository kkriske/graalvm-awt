package io.github.kkriske.issues;

import io.github.kkriske.HeadlessOnly;
import io.github.kkriske.HeadyOnly;
import org.junit.jupiter.api.Test;

import java.awt.*;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * https://github.com/oracle/graal/issues/679
 */
class Issue679Test {
    @Test
    @HeadlessOnly
    void testHeadless() {
        assertFalse(Desktop.isDesktopSupported());
    }

    @Test
    @HeadyOnly
    void testHeady() {
        assertTrue(Desktop.isDesktopSupported());
    }
}
