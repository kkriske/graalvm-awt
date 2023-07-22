package io.github.kkriske.awt.util;

import org.graalvm.nativeimage.hosted.Feature;
import org.graalvm.nativeimage.hosted.RuntimeSystemProperties;

public class JUnitHelperFeature implements Feature {

    public static final String TEST_HEADLESS_PROPERTY = "test.headless";
    public static final String JAVA_HEADLESS_PROPERTY = "java.awt.headless";

    @Override
    public void beforeAnalysis(BeforeAnalysisAccess access) {
        RuntimeSystemProperties.register(JAVA_HEADLESS_PROPERTY, System.getProperty(TEST_HEADLESS_PROPERTY));
    }
}
