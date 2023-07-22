package io.github.kkriske.awt;

import com.oracle.svm.core.annotate.Alias;
import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;
import org.graalvm.nativeimage.Platform;
import org.graalvm.nativeimage.Platforms;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@TargetClass(className = "sun.awt.FontConfiguration")
final class Target_sun_awt_FontConfiguration {

    @Alias
    private static Target_sun_util_logging_PlatformLogger logger;

    @Alias
    protected static boolean isProperties;

    @Alias
    protected Target_sun_font_SunFontManager fontManager;

    @Alias
    private File fontConfigFile;

    @Alias
    private boolean foundOsSpecificFile;

    @Substitute
    private void findFontConfigFile() {
        foundOsSpecificFile = true;
        String userConfigFile = System.getProperty("sun.awt.fontconfig");
        if (userConfigFile != null) {
            fontConfigFile = new File(userConfigFile);
        } else {
            foundOsSpecificFile = false;
            isProperties = false;
        }
    }

    @Substitute
    private void readFontConfigFile(File f) {
        if (f != null) {
            try (FileInputStream in = new FileInputStream(f.getPath())) {
                if (isProperties) {
                    loadProperties(in);
                } else {
                    loadBinary(in);
                }
                if (Target_sun_font_FontUtilities.debugFonts()) {
                    logger.config("Read logical font configuration from " + f);
                }
            } catch (IOException e) {
                if (Target_sun_font_FontUtilities.debugFonts()) {
                    logger.config("Failed to read logical font configuration from " + f);
                }
            }
        } else {
            try (InputStream in = Target_sun_awt_FontConfiguration.class
                    .getResourceAsStream(FontManagerSubstitutions.FONTCONFIG_RESOURCE_ROOT)) {
                loadBinary(in);
                if (Target_sun_font_FontUtilities.debugFonts()) {
                    logger.config("Read logical font configuration from internal fontconfig.bfc");
                }
            } catch (IOException e) {
                if (Target_sun_font_FontUtilities.debugFonts()) {
                    logger.config("Failed to read logical font configuration from internal fontconfig.bfc");
                }
            }
        }
        String version = getVersion();
        if (!"1".equals(version) && Target_sun_font_FontUtilities.debugFonts()) {
            logger.config("Unsupported fontconfig version: " + version);
        }
    }

    @Alias
    public static native void loadProperties(InputStream in) throws IOException;

    @Alias
    public static native void loadBinary(InputStream inStream) throws IOException;

    @Alias
    public native String getVersion();

    @Alias
    protected native void setFontConfiguration();
}

@Platforms(Platform.LINUX.class)
@TargetClass(className = "sun.font.FcFontConfiguration")
final class Target_sun_font_FcFontConfiguration {

    @Alias
    private Target_sun_font_FontConfigManager_FcCompFont[] fcCompFonts;

    @Substitute
    public synchronized boolean init() {
        if (fcCompFonts != null) {
            return true;
        }

        Target_sun_awt_FontConfiguration.class.cast(this).setFontConfiguration();
        readFcInfo();
        Target_sun_awt_FcFontManager fm = Target_sun_awt_FcFontManager.class
                .cast(Target_sun_awt_FontConfiguration.class.cast(this).fontManager);
        Target_sun_font_FontConfigManager fcm = fm.getFontConfigManager();
        if (fcCompFonts == null) {
            fcCompFonts = fcm.loadFontConfig();
            if (fcCompFonts != null) {
                try {
                    writeFcInfo();
                } catch (Exception e) {
                    if (Target_sun_font_FontUtilities.debugFonts()) {
                        warning("Exception writing fcInfo " + e);
                    }
                }
            } else if (Target_sun_font_FontUtilities.debugFonts()) {
                warning("Failed to get info from libfontconfig");
            }
        } else {
            fcm.populateFontConfig(fcCompFonts);
        }

        if (fcCompFonts == null) {
            return false; // couldn't load fontconfig.
        }

        /*
        Do not load fallback fonts

        // NB already in a privileged block from SGE
        String javaHome = System.getProperty("java.home");
        if (javaHome == null) {
            throw new Error("java.home property not set");
        }
        String javaLib = javaHome + File.separator + "lib";
        getInstalledFallbackFonts(javaLib);
         */

        return true;
    }

    @Alias
    private native void readFcInfo();

    @Alias
    private native void writeFcInfo();

    @Alias
    private static native void warning(String msg);
}

@Platforms(Platform.LINUX.class)
@TargetClass(className = "sun.font.FontConfigManager")
final class Target_sun_font_FontConfigManager {
    @Alias
    native Target_sun_font_FontConfigManager_FcCompFont[] loadFontConfig();

    @Alias
    native void populateFontConfig(Target_sun_font_FontConfigManager_FcCompFont[] fcInfo);
}

@Platforms(Platform.LINUX.class)
@TargetClass(className = "sun.font.FontConfigManager", innerClass = "FcCompFont")
final class Target_sun_font_FontConfigManager_FcCompFont {
}

@Platforms(Platform.LINUX.class)
@TargetClass(className = "sun.awt.FcFontManager")
final class Target_sun_awt_FcFontManager {

    @Alias
    public synchronized native Target_sun_font_FontConfigManager getFontConfigManager();

}

@TargetClass(className = "sun.font.SunFontManager")
final class Target_sun_font_SunFontManager {
}

@TargetClass(className = "sun.font.FontUtilities")
final class Target_sun_font_FontUtilities {
    @Alias
    public static native boolean debugFonts();
}

@TargetClass(className = "sun.util.logging.PlatformLogger")
final class Target_sun_util_logging_PlatformLogger {
    @Alias
    public native void config(String msg);
}

public final class FontManagerSubstitutions {
    public static final String FONTCONFIG_RESOURCE = "fontconfig.bfc";
    public static final String FONTCONFIG_RESOURCE_ROOT = '/' + FONTCONFIG_RESOURCE;
}
