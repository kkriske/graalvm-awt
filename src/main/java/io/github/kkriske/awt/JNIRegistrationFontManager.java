package io.github.kkriske.awt;

import com.oracle.svm.core.jdk.JNIRegistrationUtil;
import com.oracle.svm.core.util.VMError;
import io.github.kkriske.awt.util.JavaVersionUtil;
import org.graalvm.nativeimage.hosted.Feature;
import org.graalvm.nativeimage.hosted.RuntimeJNIAccess;
import org.graalvm.nativeimage.hosted.RuntimeReflection;
import org.graalvm.nativeimage.hosted.RuntimeResourceAccess;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class JNIRegistrationFontManager extends JNIRegistrationUtil implements Feature {

    @Override
    public void beforeAnalysis(BeforeAnalysisAccess a) {
        // All places where System.loadLibrary("fontmanager") is called
        a.registerReachabilityHandler(JNIRegistrationFontManager::registerFontManagerConfig,
                clazz(a, "sun.font.FontManagerNativeLibrary"));
    }

    private static void registerFontManagerConfig(DuringAnalysisAccess a) {
        a.registerReachabilityHandler(JNIRegistrationFontManager::registerFontManagerFactoryGetInstance,
                method(a, "sun.font.FontManagerFactory", "getInstance"));
        a.registerReachabilityHandler(JNIRegistrationFontManager::registerSunFontManagerInitIDs,
                method(a, "sun.font.SunFontManager", "initIDs"));
        a.registerReachabilityHandler(JNIRegistrationFontManager::registerSunLayoutEngineShape,
                method(a, "sun.font.SunLayoutEngine", "shape",
                        clazz(a, "sun.font.Font2D"), clazz(a, "sun.font.FontStrike"), float.class, float[].class,
                        long.class, char[].class, clazz(a, "sun.font.GlyphLayout$GVData"),
                        int.class, int.class, int.class,
                        int.class, clazz(a, "java.awt.geom.Point2D$Float"), int.class, int.class));
        a.registerReachabilityHandler(JNIRegistrationFontManager::registerFreetypeFontScalerInitIDs,
                method(a, "sun.font.FreetypeFontScaler", "initIDs", Class.class));

        if (isLinux()) {
            // These native methods live in the awt library, but ultimately are fontmanager specific
            a.registerReachabilityHandler(JNIRegistrationFontManager::registerFcFontManagerGetFontPathNative,
                    method(a, "sun.awt.FcFontManager", "getFontPathNative", boolean.class, boolean.class));
            a.registerReachabilityHandler(JNIRegistrationFontManager::registerFontConfigManagerGetFontConfig,
                    method(a, "sun.font.FontConfigManager", "getFontConfig",
                            String.class, clazz(a, "sun.font.FontConfigManager$FontConfigInfo"), clazz(a, "[Lsun.font.FontConfigManager$FcCompFont;"), boolean.class));
        }
        if (isWindows()) {
            a.registerReachabilityHandler(JNIRegistrationFontManager::addFontConfigResource,
                    constructor(a, "sun.awt.FontConfiguration", clazz(a, "sun.font.SunFontManager")));
            a.registerReachabilityHandler(JNIRegistrationFontManager::registerWin32FontManagerPopulateFontFileNameMap0,
                    method(a, "sun.awt.Win32FontManager", "populateFontFileNameMap0",
                            clazz(a, "java.util.HashMap"),
                            clazz(a, "java.util.HashMap"),
                            clazz(a, "java.util.HashMap"),
                            clazz(a, "java.util.Locale")));
        }
    }

    private static void addFontConfigResource(DuringAnalysisAccess a) {
        Path fontconfig = Path.of(System.getProperty("java.home"), "lib", "fontconfig.bfc");
        Module javaDesktopModule = clazz(a, "sun.awt.FontConfiguration").getModule();
        try {
            RuntimeResourceAccess.addResource(javaDesktopModule, FontManagerSubstitutions.FONTCONFIG_RESOURCE, Files.readAllBytes(fontconfig));
        } catch (IOException e) {
            VMError.shouldNotReachHere(e);
        }
    }

    // shared registration calls

    private static void registerFontManagerFactoryGetInstance(DuringAnalysisAccess a) {
        if (JavaVersionUtil.JAVA_SPEC <= 17) {
            if (isWindows()) {
                RuntimeReflection.register(clazz(a, "sun.awt.Win32FontManager"));
                RuntimeReflection.register(constructor(a, "sun.awt.Win32FontManager"));
            } else if (isLinux()) {
                RuntimeReflection.register(clazz(a, "sun.awt.X11FontManager"));
                RuntimeReflection.register(constructor(a, "sun.awt.X11FontManager"));
            }
        }
    }

    private static void registerSunFontManagerInitIDs(DuringAnalysisAccess a) {
        RuntimeJNIAccess.register(clazz(a, "sun.font.TrueTypeFont"));
        RuntimeJNIAccess.register(method(a, "sun.font.TrueTypeFont", "readBlock",
                clazz(a, "java.nio.ByteBuffer"), int.class, int.class));
        RuntimeJNIAccess.register(method(a, "sun.font.TrueTypeFont", "readBytes",
                int.class, int.class));
        RuntimeJNIAccess.register(clazz(a, "sun.font.Type1Font"));
        RuntimeJNIAccess.register(method(a, "sun.font.Type1Font", "readFile",
                clazz(a, "java.nio.ByteBuffer")));
        RuntimeJNIAccess.register(clazz(a, "java.awt.geom.Point2D$Float"));
        RuntimeJNIAccess.register(constructor(a, "java.awt.geom.Point2D$Float", float.class, float.class));
        RuntimeJNIAccess.register(fields(a, "java.awt.geom.Point2D$Float", "x", "y"));
        RuntimeJNIAccess.register(clazz(a, "sun.font.StrikeMetrics"));
        RuntimeJNIAccess.register(constructor(a, "sun.font.StrikeMetrics",
                float.class, float.class, float.class, float.class, float.class,
                float.class, float.class, float.class, float.class, float.class));
        RuntimeJNIAccess.register(clazz(a, "java.awt.geom.Rectangle2D$Float"));
        RuntimeJNIAccess.register(constructor(a, "java.awt.geom.Rectangle2D$Float"));
        RuntimeJNIAccess.register(constructor(a, "java.awt.geom.Rectangle2D$Float",
                float.class, float.class, float.class, float.class));
        RuntimeJNIAccess.register(fields(a, "java.awt.geom.Rectangle2D$Float",
                "x", "y", "width", "height"));
        RuntimeJNIAccess.register(clazz(a, "java.awt.geom.GeneralPath"));
        RuntimeJNIAccess.register(constructor(a, "java.awt.geom.GeneralPath",
                int.class, byte[].class, int.class, float[].class, int.class));
        RuntimeJNIAccess.register(constructor(a, "java.awt.geom.GeneralPath"));
        RuntimeJNIAccess.register(clazz(a, "sun.font.Font2D"));
        RuntimeJNIAccess.register(method(a, "sun.font.Font2D", "charToGlyph", int.class));
        RuntimeJNIAccess.register(method(a, "sun.font.Font2D", "charToVariationGlyph", int.class, int.class));
        RuntimeJNIAccess.register(method(a, "sun.font.Font2D", "getMapper"));
        RuntimeJNIAccess.register(method(a, "sun.font.Font2D", "getTableBytes", int.class));
        RuntimeJNIAccess.register(method(a, "sun.font.Font2D", "canDisplay", char.class));
        RuntimeJNIAccess.register(clazz(a, "sun.font.CharToGlyphMapper"));
        RuntimeJNIAccess.register(method(a, "sun.font.CharToGlyphMapper", "charToGlyph", int.class));
        RuntimeJNIAccess.register(clazz(a, "sun.font.PhysicalStrike"));
        RuntimeJNIAccess.register(method(a, "sun.font.FontStrike", "getGlyphMetrics", int.class));
        RuntimeJNIAccess.register(method(a, "sun.font.PhysicalStrike", "getGlyphPoint", int.class, int.class));
        RuntimeJNIAccess.register(method(a, "sun.font.PhysicalStrike", "adjustPoint",
                clazz(a, "java.awt.geom.Point2D$Float")));
        RuntimeJNIAccess.register(fields(a, "sun.font.PhysicalStrike", "pScalerContext"));
        RuntimeJNIAccess.register(clazz(a, "sun.font.GlyphList"));
        RuntimeJNIAccess.register(fields(a, "sun.font.GlyphList",
                "gposx", "gposy", "len", "images", "usePositions", "positions", "lcdRGBOrder", "lcdSubPixPos"));
    }

    private static void registerSunLayoutEngineShape(DuringAnalysisAccess a) {
        RuntimeJNIAccess.register(clazz(a, "sun.font.GlyphLayout$GVData"));
        RuntimeJNIAccess.register(fields(a, "sun.font.GlyphLayout$GVData",
                "_count", "_flags", "_glyphs", "_positions", "_indices"));
        RuntimeJNIAccess.register(method(a, "sun.font.GlyphLayout$GVData", "grow"));
    }

    private static void registerFreetypeFontScalerInitIDs(DuringAnalysisAccess a) {
        RuntimeJNIAccess.register(method(a, "sun.font.FreetypeFontScaler", "invalidateScaler"));
        if (JavaVersionUtil.JAVA_SPEC >= 20) {
            RuntimeJNIAccess.register(method(a, "sun.font.FontUtilities", "debugFonts"));
        }
    }

    // linux registration calls

    private static void registerFcFontManagerGetFontPathNative(DuringAnalysisAccess a) {
        RuntimeJNIAccess.register(clazz(a, "java.awt.GraphicsEnvironment"));
        RuntimeJNIAccess.register(method(a, "java.awt.GraphicsEnvironment", "getLocalGraphicsEnvironment"));
        RuntimeJNIAccess.register(clazz(a, "sun.java2d.SunGraphicsEnvironment"));
        RuntimeJNIAccess.register(method(a, "sun.java2d.SunGraphicsEnvironment", "isDisplayLocal"));
    }

    private static void registerFontConfigManagerGetFontConfig(DuringAnalysisAccess a) {
        RuntimeJNIAccess.register(clazz(a, "sun.font.FontConfigManager$FontConfigInfo"));
        RuntimeJNIAccess.register(clazz(a, "sun.font.FontConfigManager$FcCompFont"));
        RuntimeJNIAccess.register(clazz(a, "sun.font.FontConfigManager$FontConfigFont"));
        RuntimeJNIAccess.register(fields(a, "sun.font.FontConfigManager$FontConfigInfo",
                "fcVersion", "cacheDirs"));
        RuntimeJNIAccess.register(fields(a, "sun.font.FontConfigManager$FcCompFont",
                "fcName", "firstFont", "allFonts"));
        RuntimeJNIAccess.register(constructor(a, "sun.font.FontConfigManager$FontConfigFont"));
        RuntimeJNIAccess.register(fields(a, "sun.font.FontConfigManager$FontConfigFont",
                "familyName", "styleStr", "fullName", "fontFile"));
    }

    // windows registrations calls

    private static void registerWin32FontManagerPopulateFontFileNameMap0(DuringAnalysisAccess a) {
        RuntimeJNIAccess.register(clazz(a, "java.util.HashMap"));
        RuntimeJNIAccess.register(method(a, "java.util.HashMap", "put", Object.class, Object.class));
        RuntimeJNIAccess.register(method(a, "java.util.HashMap", "containsKey", Object.class));
        RuntimeJNIAccess.register(clazz(a, "java.util.ArrayList"));
        RuntimeJNIAccess.register(constructor(a, "java.util.ArrayList", int.class));
        RuntimeJNIAccess.register(method(a, "java.util.ArrayList", "add", Object.class));
        RuntimeJNIAccess.register(clazz(a, "java.lang.String"));
        RuntimeJNIAccess.register(method(a, "java.lang.String", "toLowerCase", clazz(a, "java.util.Locale")));
    }
}
