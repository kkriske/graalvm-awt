package io.github.kkriske.issues;

import org.junit.jupiter.api.Test;

import javax.print.PrintServiceLookup;

/**
 * https://github.com/oracle/graal/issues/3484
 */
class Issue3484Test {
    @Test
    void testPrintService() {
        PrintServiceLookup.lookupDefaultPrintService();
    }
}
