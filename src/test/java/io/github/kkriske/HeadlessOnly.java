package io.github.kkriske;

import io.github.kkriske.awt.util.JUnitHelperFeature;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@EnabledIfSystemProperty(named = JUnitHelperFeature.JAVA_HEADLESS_PROPERTY, matches = "true",
        disabledReason = "Disabled during heady execution")
public @interface HeadlessOnly {
}
