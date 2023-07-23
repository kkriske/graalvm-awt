package io.github.kkriske.awt;

import com.oracle.svm.core.jdk.JNIRegistrationUtil;
import io.github.kkriske.awt.util.JUnitHelperFeature;
import io.github.kkriske.awt.util.JavaVersionUtil;
import org.graalvm.nativeimage.hosted.Feature;
import org.graalvm.nativeimage.hosted.RuntimeJNIAccess;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * TODO: Coverage of JNI calls required for AWT is incomplete.
 * Open questions:
 * - Host General AWT registrations and Heady AWT registrations in separate features?
 * - what JDK versions should be supported, just JDK 21 or both 17 and 21?
 * - Should xawt be hosted in a separate feature?
 */
public class JNIRegistrationAwt extends JNIRegistrationUtil implements Feature {

    private static final boolean SUPPORT_HEADY = !Boolean.getBoolean(JUnitHelperFeature.TEST_HEADLESS_PROPERTY);

    @Override
    public void beforeAnalysis(BeforeAnalysisAccess a) {
        // All places where System.loadLibrary("awt") is called
        List<Object> awtLoaders = new ArrayList<>();
        Collections.addAll(awtLoaders,
                method(a, "java.awt.Toolkit", "loadLibraries"),
                method(a, "java.awt.event.NativeLibLoader", "loadLibraries"),
                method(a, "java.awt.image.ColorModel", "loadLibraries"),
                method(a, "sun.awt.NativeLibLoader", "loadLibraries"),
                method(a, "sun.awt.image.NativeLibLoader", "loadLibraries"),
                clazz(a, "sun.font.FontManagerNativeLibrary"),
                clazz(a, "sun.java2d.Disposer"),
                method(a, "sun.java2d.cmm.lcms.LCMS", "getModule")
        );
        if (isWindows()) {
            Collections.addAll(awtLoaders,
                    clazz(a, "sun.print.PrintServiceLookupProvider"),
                    // TODO: method does not exist on JDK 17
                    // method(a, "sun.print.PrintServiceLookupProvider", "loadAWTLibrary"),
                    method(a, "sun.awt.windows.WToolkit", "loadLibraries")
            );
        }
        if (isLinux()) {
            Collections.addAll(awtLoaders,
                    clazz(a, "sun.awt.X11GraphicsEnvironment"),
                    // TODO: method does not exist on JDK 17
                    // method(a, "sun.awt.X11GraphicsEnvironment", "initStatic"),
                    clazz(a, "sun.print.CUPSPrinter")
                    // TODO: method does not exist on JDK 17
                    // method(a, "sun.print.CUPSPrinter", "initStatic")
            );
        }
        a.registerReachabilityHandler(JNIRegistrationAwt::registerAWTConfig, awtLoaders.toArray());
    }

    private static void registerAWTConfig(DuringAnalysisAccess a) {
        registerOnLoad(a);
        a.registerReachabilityHandler(JNIRegistrationAwt::registerDisposerInitIDs,
                method(a, "sun.java2d.Disposer", "initIDs"));
        a.registerReachabilityHandler(JNIRegistrationAwt::registerSurfaceDataInitIDs,
                method(a, "sun.java2d.SurfaceData", "initIDs"));
        a.registerReachabilityHandler(JNIRegistrationAwt::registerColorModelInitIDs,
                method(a, "java.awt.image.ColorModel", "initIDs"));
        a.registerReachabilityHandler(JNIRegistrationAwt::registerIndexColorModelInitIDs,
                method(a, "java.awt.image.IndexColorModel", "initIDs"));
        a.registerReachabilityHandler(JNIRegistrationAwt::registerRegionInitIDs,
                method(a, "sun.java2d.pipe.Region", "initIDs"));
        a.registerReachabilityHandler(JNIRegistrationAwt::registerSpanClipRendererInitIDs,
                method(a, "sun.java2d.pipe.SpanClipRenderer", "initIDs", Class.class, Class.class));
        a.registerReachabilityHandler(JNIRegistrationAwt::registerGraphicsPrimitiveMgrInitIDs,
                method(a, "sun.java2d.loops.GraphicsPrimitiveMgr", "initIDs",
                        Class.class, Class.class, Class.class, Class.class, Class.class, Class.class,
                        Class.class, Class.class, Class.class, Class.class, Class.class));
        if (SUPPORT_HEADY) {
            a.registerReachabilityHandler(JNIRegistrationAwt::registerColorInitIDs,
                    method(a, "java.awt.Color", "initIDs"));
            a.registerReachabilityHandler(JNIRegistrationAwt::registerComponentInitIDs,
                    method(a, "java.awt.Component", "initIDs"));
        }

        if (isWindows()) {
            a.registerReachabilityHandler(JNIRegistrationAwt::registerToolkitInitIDs,
                    method(a, "java.awt.Toolkit", "initIDs"));
            a.registerReachabilityHandler(JNIRegistrationAwt::registerWToolkitInitIDs,
                    method(a, "sun.awt.windows.WToolkit", "initIDs"));
            a.registerReachabilityHandler(JNIRegistrationAwt::registerWToolkitInit,
                    method(a, "sun.awt.windows.WToolkit", "init"));
            a.registerReachabilityHandler(JNIRegistrationAwt::registerWToolkitEventLoop,
                    method(a, "sun.awt.windows.WToolkit", "eventLoop"));
            // WToolkit#getScreenInsets
            a.registerReachabilityHandler(JNIRegistrationAwt::registerFontInitIDs,
                    method(a, "java.awt.Font", "initIDs"));
            a.registerReachabilityHandler(JNIRegistrationAwt::registerWindowsFlagsInitNativeFlags,
                    method(a, "sun.java2d.windows.WindowsFlags", "initNativeFlags"));
            a.registerReachabilityHandler(JNIRegistrationAwt::registerWin32GraphicsEnvironmentInitDisplay,
                    method(a, "sun.awt.Win32GraphicsEnvironment", "initDisplay"));
            a.registerReachabilityHandler(JNIRegistrationAwt::registerWObjectPeerInitIDs,
                    method(a, "sun.awt.windows.WObjectPeer", "initIDs"));
            a.registerReachabilityHandler(JNIRegistrationAwt::registerPrintServiceLookupProviderNotifyRemotePrinterChange,
                    method(a, "sun.print.PrintServiceLookupProvider", "notifyRemotePrinterChange"));
            if (SUPPORT_HEADY) {
                a.registerReachabilityHandler(JNIRegistrationAwt::registerInsetsInitIDs,
                        method(a, "java.awt.Insets", "initIDs"));
                a.registerReachabilityHandler(JNIRegistrationAwt::registerInputEventInitIDs,
                        method(a, "java.awt.event.InputEvent", "initIDs"));
                a.registerReachabilityHandler(JNIRegistrationAwt::registerAWTEventInitIDs,
                        method(a, "java.awt.AWTEvent", "initIDs"));
            }
        }

        if (isLinux()) {
            a.registerReachabilityHandler(JNIRegistrationAwt::registerXRSurfaceDataInitIDs,
                    method(a, "sun.java2d.xr.XRSurfaceData", "initIDs"));
            if (SUPPORT_HEADY) {
                a.registerReachabilityHandler(JNIRegistrationAwt::registerX11GraphicsEnvironmentInitDisplay,
                        method(a, "sun.awt.X11GraphicsEnvironment", "initDisplay", boolean.class));
                a.registerReachabilityHandler(JNIRegistrationAwt::registerXToolkitInitIDs,
                        method(a, "sun.awt.X11.XToolkit", "initIDs"));
                a.registerReachabilityHandler(JNIRegistrationAwt::registerXToolkitWaitForEvents,
                        method(a, "sun.awt.X11.XToolkit", "initIDs"));
                a.registerReachabilityHandler(JNIRegistrationAwt::registerXDesktopPeerInit,
                        method(a, "sun.awt.X11.XDesktopPeer", "init",
                                int.class, boolean.class));
            }
        }
    }

    private static void registerOnLoad(DuringAnalysisAccess a) {
        if (isLinux()) {
            if (JavaVersionUtil.JAVA_SPEC <= 17) {
                RuntimeJNIAccess.register(method(a, "java.lang.System", "setProperty", String.class, String.class));
            }
            RuntimeJNIAccess.register(clazz(a, "java.awt.GraphicsEnvironment"));
            RuntimeJNIAccess.register(method(a, "java.awt.GraphicsEnvironment", "isHeadless"));
            RuntimeJNIAccess.register(method(a, "java.lang.System", "load", String.class));
        }
    }

    private static void registerDisposerInitIDs(DuringAnalysisAccess a) {
        RuntimeJNIAccess.register(method(a, "sun.java2d.Disposer", "addRecord",
                Object.class, long.class, long.class));
    }

    private static void registerSurfaceDataInitIDs(DuringAnalysisAccess a) {
        RuntimeJNIAccess.register(clazz(a, "sun.java2d.InvalidPipeException"));
        RuntimeJNIAccess.register(clazz(a, "sun.java2d.NullSurfaceData"));
        RuntimeJNIAccess.register(fields(a, "sun.java2d.SurfaceData", "pData", "valid"));
        RuntimeJNIAccess.register(clazz(a, "java.awt.image.IndexColorModel"));
        RuntimeJNIAccess.register(fields(a, "java.awt.image.IndexColorModel", "allgrayopaque"));
    }

    private static void registerColorModelInitIDs(DuringAnalysisAccess a) {
        if (JavaVersionUtil.JAVA_SPEC <= 20) {
            RuntimeJNIAccess.register(fields(a, "java.awt.image.ColorModel", "pData"));
        }
        RuntimeJNIAccess.register(fields(a, "java.awt.image.ColorModel",
                "nBits", "colorSpace", "numComponents", "supportsAlpha",
                "isAlphaPremultiplied", "transparency", "colorSpaceType", "is_sRGB"));
        RuntimeJNIAccess.register(method(a, "java.awt.image.ColorModel", "getRGBdefault"));
    }

    private static void registerIndexColorModelInitIDs(DuringAnalysisAccess a) {
        RuntimeJNIAccess.register(fields(a, "java.awt.image.IndexColorModel",
                "transparent_index", "map_size", "rgb"));
    }

    private static void registerRegionInitIDs(DuringAnalysisAccess a) {
        RuntimeJNIAccess.register(fields(a, "sun.java2d.pipe.Region",
                "endIndex", "bands", "lox", "loy", "hix", "hiy"));
    }

    private static void registerSpanClipRendererInitIDs(DuringAnalysisAccess a) {
        RuntimeJNIAccess.register(fields(a, "sun.java2d.pipe.Region",
                "bands", "endIndex"));
        RuntimeJNIAccess.register(fields(a, "sun.java2d.pipe.RegionIterator",
                "region", "curIndex", "numXbands"));
    }

    private static void registerGraphicsPrimitiveMgrInitIDs(DuringAnalysisAccess a) {
        String[] primitiveTypes = {
                "sun.java2d.loops.Blit",
                "sun.java2d.loops.BlitBg",
                "sun.java2d.loops.ScaledBlit",
                "sun.java2d.loops.FillRect",
                "sun.java2d.loops.FillSpans",
                "sun.java2d.loops.FillParallelogram",
                "sun.java2d.loops.DrawParallelogram",
                "sun.java2d.loops.DrawLine",
                "sun.java2d.loops.DrawRect",
                "sun.java2d.loops.DrawPolygons",
                "sun.java2d.loops.DrawPath",
                "sun.java2d.loops.FillPath",
                "sun.java2d.loops.MaskBlit",
                "sun.java2d.loops.MaskFill",
                "sun.java2d.loops.DrawGlyphList",
                "sun.java2d.loops.DrawGlyphListAA",
                "sun.java2d.loops.DrawGlyphListLCD",
                "sun.java2d.loops.TransformHelper"
        };
        Class<?>[] primitiveTypeInitSignature = {
                long.class,
                clazz(a, "sun.java2d.loops.SurfaceType"),
                clazz(a, "sun.java2d.loops.CompositeType"),
                clazz(a, "sun.java2d.loops.SurfaceType")
        };
        for (String primitiveType : primitiveTypes) {
            RuntimeJNIAccess.register(clazz(a, primitiveType));
            RuntimeJNIAccess.register(constructor(a, primitiveType, primitiveTypeInitSignature));
        }

        RuntimeJNIAccess.register(fields(a, "sun.java2d.loops.SurfaceType",
                "OpaqueColor", "AnyColor", "AnyByte", "ByteBinary1Bit", "ByteBinary2Bit", "ByteBinary4Bit",
                "ByteIndexed", "ByteIndexedBm", "ByteGray", "Index8Gray", "Index12Gray", "AnyShort", "Ushort555Rgb",
                "Ushort555Rgbx", "Ushort565Rgb", "Ushort4444Argb", "UshortGray", "UshortIndexed", "Any3Byte",
                "ThreeByteBgr", "AnyInt", "IntArgb", "IntArgbPre", "IntArgbBm", "IntRgb", "IntBgr", "IntRgbx",
                "Any4Byte", "FourByteAbgr", "FourByteAbgrPre"));

        RuntimeJNIAccess.register(fields(a, "sun.java2d.loops.CompositeType",
                "SrcNoEa", "SrcOverNoEa", "Src", "SrcOver", "Xor", "AnyAlpha"));

        RuntimeJNIAccess.register(method(a, "sun.java2d.loops.GraphicsPrimitiveMgr", "register",
                clazz(a, "[Lsun.java2d.loops.GraphicsPrimitive;")));
        RuntimeJNIAccess.register(fields(a, "sun.java2d.loops.GraphicsPrimitive", "pNativePrim"));
        RuntimeJNIAccess.register(fields(a, "sun.java2d.SunGraphics2D",
                "pixel", "eargb", "clipRegion", "composite", "lcdTextContrast", "strokeHint"));
        RuntimeJNIAccess.register(method(a, "java.awt.Color", "getRGB"));
        RuntimeJNIAccess.register(fields(a, "sun.java2d.loops.XORComposite",
                "xorPixel", "xorColor", "alphaMask"));
        RuntimeJNIAccess.register(fields(a, "java.awt.AlphaComposite",
                "rule", "extraAlpha"));
        RuntimeJNIAccess.register(fields(a, "java.awt.geom.AffineTransform",
                "m00", "m01", "m02", "m10", "m11", "m12"));
        RuntimeJNIAccess.register(fields(a, "java.awt.geom.Path2D",
                "pointTypes", "numTypes", "windingRule"));
        RuntimeJNIAccess.register(fields(a, "java.awt.geom.Path2D$Float",
                "floatCoords"));
        RuntimeJNIAccess.register(fields(a, "sun.awt.SunHints",
                "INTVAL_STROKE_PURE"));
    }

    private static void registerColorInitIDs(DuringAnalysisAccess a) {
        if (isLinux()) {
            RuntimeJNIAccess.register(fields(a, "java.awt.Color", "value"));
        }
    }

    private static void registerComponentInitIDs(DuringAnalysisAccess a) {
        if (isWindows()) {
            RuntimeJNIAccess.register(method(a, "java.awt.event.InputEvent", "getButtonDownMasks"));
            RuntimeJNIAccess.register(clazz(a, "sun.awt.windows.WComponentPeer"));
            RuntimeJNIAccess.register(fields(a, "sun.awt.windows.WComponentPeer", "winGraphicsConfig", "hwnd"));
            RuntimeJNIAccess.register(method(a, "sun.awt.windows.WComponentPeer", "replaceSurfaceData"));
            RuntimeJNIAccess.register(method(a, "sun.awt.windows.WComponentPeer", "replaceSurfaceDataLater"));
            RuntimeJNIAccess.register(method(a, "sun.awt.windows.WComponentPeer", "disposeLater"));
            RuntimeJNIAccess.register(fields(a, "java.awt.Component",
                    "peer", "x", "y", "height", "width", "visible", "background", "foreground",
                    "enabled", "parent", "graphicsConfig", "focusable", "appContext", "cursor"));
            RuntimeJNIAccess.register(method(a, "java.awt.Component", "getFont_NoClientCode"));
            RuntimeJNIAccess.register(method(a, "java.awt.Component", "getToolkitImpl"));
            RuntimeJNIAccess.register(method(a, "java.awt.Component", "isEnabledImpl"));
            RuntimeJNIAccess.register(method(a, "java.awt.Component", "getLocationOnScreen_NoTreeLock"));
        }
        if (isLinux()) {
            RuntimeJNIAccess.register(fields(a, "java.awt.Component",
                    "x", "y", "width", "height", "isPacked", "peer",
                    "background", "foreground", "graphicsConfig", "name", "appContext"));
            RuntimeJNIAccess.register(method(a, "java.awt.Component", "getFont_NoClientCode"));
            RuntimeJNIAccess.register(method(a, "java.awt.Component", "getLocationOnScreen_NoTreeLock"));
            RuntimeJNIAccess.register(clazz(a, "java.awt.event.KeyEvent"));
            RuntimeJNIAccess.register(fields(a, "java.awt.event.KeyEvent", "isProxyActive"));
        }
    }

    // Windows registration calls

    private static void registerToolkitInitIDs(DuringAnalysisAccess a) {
        RuntimeJNIAccess.register(method(a, "java.awt.Toolkit", "getDefaultToolkit"));
        RuntimeJNIAccess.register(method(a, "java.awt.Toolkit", "getFontMetrics", clazz(a, "java.awt.Font")));
        RuntimeJNIAccess.register(clazz(a, "java.awt.Insets"));
        RuntimeJNIAccess.register(constructor(a, "java.awt.Insets", int.class, int.class, int.class, int.class));
    }

    private static void registerWToolkitInitIDs(DuringAnalysisAccess a) {
        RuntimeJNIAccess.register(method(a, "sun.awt.windows.WToolkit", "windowsSettingChange"));
        RuntimeJNIAccess.register(method(a, "sun.awt.windows.WToolkit", "displayChanged"));

        RuntimeJNIAccess.register(clazz(a, "sun.java2d.SurfaceData"));
        RuntimeJNIAccess.register(fields(a, "sun.java2d.SurfaceData", "pData"));

        RuntimeJNIAccess.register(clazz(a, "sun.awt.image.SunVolatileImage"));
        RuntimeJNIAccess.register(fields(a, "sun.awt.image.SunVolatileImage", "volSurfaceManager"));

        RuntimeJNIAccess.register(clazz(a, "sun.awt.image.VolatileSurfaceManager"));
        RuntimeJNIAccess.register(fields(a, "sun.awt.image.VolatileSurfaceManager", "sdCurrent"));

        RuntimeJNIAccess.register(clazz(a, "java.awt.Component"));

        RuntimeJNIAccess.register(clazz(a, "sun.awt.windows.WDesktopPeer"));
        RuntimeJNIAccess.register(method(a, "sun.awt.windows.WDesktopPeer", "userSessionCallback",
                boolean.class, clazz(a, "java.awt.desktop.UserSessionEvent$Reason")));
        RuntimeJNIAccess.register(method(a, "sun.awt.windows.WDesktopPeer", "systemSleepCallback", boolean.class));

        RuntimeJNIAccess.register(fields(a, "java.awt.desktop.UserSessionEvent$Reason",
                "UNSPECIFIED", "CONSOLE", "REMOTE", "LOCK"));
    }

    private static void registerWToolkitInit(DuringAnalysisAccess a) {
        RuntimeJNIAccess.register(clazz(a, "sun.awt.SunToolkit"));
        RuntimeJNIAccess.register(method(a, "sun.awt.SunToolkit", "isTouchKeyboardAutoShowEnabled"));
    }

    private static void registerWToolkitEventLoop(DuringAnalysisAccess a) {
        RuntimeJNIAccess.register(clazz(a, "sun.awt.AWTAutoShutdown"));
        RuntimeJNIAccess.register(method(a, "sun.awt.AWTAutoShutdown", "notifyToolkitThreadBusy"));
        RuntimeJNIAccess.register(method(a, "sun.awt.AWTAutoShutdown", "notifyToolkitThreadFree"));
    }

    private static void registerFontInitIDs(DuringAnalysisAccess a) {
        RuntimeJNIAccess.register(method(a, "java.awt.Font", "getFontPeer"));
        RuntimeJNIAccess.register(fields(a, "java.awt.Font", "pData", "name", "size", "style"));
        RuntimeJNIAccess.register(method(a, "java.awt.Font", "getFont", String.class));
    }

    private static void registerWindowsFlagsInitNativeFlags(DuringAnalysisAccess a) {
        RuntimeJNIAccess.register(fields(a, "sun.java2d.windows.WindowsFlags",
                "d3dEnabled", "d3dSet", "offscreenSharingEnabled", "setHighDPIAware"));
    }

    private static void registerWin32GraphicsEnvironmentInitDisplay(DuringAnalysisAccess a) {
        RuntimeJNIAccess.register(method(a, "sun.awt.Win32GraphicsEnvironment", "dwmCompositionChanged", boolean.class));
    }

    private static void registerWObjectPeerInitIDs(DuringAnalysisAccess a) {
        RuntimeJNIAccess.register(fields(a, "sun.awt.windows.WObjectPeer",
                "pData", "destroyed", "target", "createError"));
        RuntimeJNIAccess.register(method(a, "sun.awt.windows.WObjectPeer", "getPeerForTarget", Object.class));
    }

    private static void registerPrintServiceLookupProviderNotifyRemotePrinterChange(DuringAnalysisAccess a) {
        RuntimeJNIAccess.register(method(a, "sun.print.PrintServiceLookupProvider", "refreshServices"));
    }

    // Windows registration calls only required for heady execution

    private static void registerInsetsInitIDs(DuringAnalysisAccess a) {
        RuntimeJNIAccess.register(fields(a, "java.awt.Insets", "left", "right", "top", "bottom"));
    }

    private static void registerInputEventInitIDs(DuringAnalysisAccess a) {
        RuntimeJNIAccess.register(fields(a, "java.awt.event.InputEvent", "modifiers"));
    }

    private static void registerAWTEventInitIDs(DuringAnalysisAccess a) {
        RuntimeJNIAccess.register(fields(a, "java.awt.AWTEvent", "bdata", "id", "consumed"));
    }

    // Linux registration calls

    private static void registerXRSurfaceDataInitIDs(DuringAnalysisAccess a) {
        RuntimeJNIAccess.register(fields(a, "sun.java2d.xr.XRSurfaceData",
                "picture", "xid"));
    }

    // Linux registration calls only required for heady execution

    private static void registerX11GraphicsEnvironmentInitDisplay(DuringAnalysisAccess a) {
        RuntimeJNIAccess.register(clazz(a, "sun.awt.SunToolkit"));
        RuntimeJNIAccess.register(method(a, "sun.awt.SunToolkit", "awtLock"));
        RuntimeJNIAccess.register(method(a, "sun.awt.SunToolkit", "awtUnlock"));
        RuntimeJNIAccess.register(method(a, "sun.awt.SunToolkit", "awtLockWait", long.class));
        RuntimeJNIAccess.register(method(a, "sun.awt.SunToolkit", "awtLockNotify"));
        RuntimeJNIAccess.register(method(a, "sun.awt.SunToolkit", "awtLockNotifyAll"));
        registerForThrowNew(a, "java.awt.AWTError");
        RuntimeJNIAccess.register(method(a, "sun.awt.X11.XErrorHandlerUtil", "init", long.class));
    }

    private static void registerXToolkitInitIDs(DuringAnalysisAccess a) {
        RuntimeJNIAccess.register(fields(a, "sun.awt.X11.XToolkit",
                "numLockMask", "modLockIsShiftLock"));
    }

    private static void registerXToolkitWaitForEvents(DuringAnalysisAccess a) {
        RuntimeJNIAccess.register(clazz(a, "java.lang.Thread"));
        RuntimeJNIAccess.register(method(a, "java.lang.Thread", "yield"));
    }

    private static void registerXDesktopPeerInit(DuringAnalysisAccess a) {
        RuntimeJNIAccess.register(clazz(a, "java.awt.Desktop$Action"));
        RuntimeJNIAccess.register(clazz(a, "sun.awt.X11.XDesktopPeer"));
        RuntimeJNIAccess.register(fields(a, "sun.awt.X11.XDesktopPeer", "supportedActions"));
        RuntimeJNIAccess.register(clazz(a, "java.util.ArrayList"));
        RuntimeJNIAccess.register(method(a, "java.util.ArrayList", "add", Object.class));
        RuntimeJNIAccess.register(method(a, "java.util.ArrayList", "clear"));
        RuntimeJNIAccess.register(fields(a, "java.awt.Desktop$Action",
                "OPEN", "BROWSE", "MAIL"));
    }
}
