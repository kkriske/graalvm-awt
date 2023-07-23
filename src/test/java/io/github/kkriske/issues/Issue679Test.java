package io.github.kkriske.issues;

import io.github.kkriske.HeadlessOnly;
import io.github.kkriske.HeadyOnly;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

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
    @EnabledOnOs(OS.LINUX)
    void testHeadyLinux() {
        assertFalse(Desktop.isDesktopSupported());
    }

    @Test
    @HeadyOnly
    @EnabledOnOs(OS.WINDOWS)
    void testHeadyWindows() {
        assertTrue(Desktop.isDesktopSupported());
    }
}
