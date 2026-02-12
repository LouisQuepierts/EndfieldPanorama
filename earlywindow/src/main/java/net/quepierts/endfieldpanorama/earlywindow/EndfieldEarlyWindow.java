package net.quepierts.endfieldpanorama.earlywindow;

import joptsimple.OptionParser;
import lombok.Getter;
import net.neoforged.fml.loading.FMLConfig;
import net.neoforged.fml.loading.progress.StartupNotificationManager;
import net.neoforged.neoforgespi.earlywindow.ImmediateWindowProvider;
import net.quepierts.endfieldpanorama.earlywindow.render.RenderScene;
import net.quepierts.endfieldpanorama.earlywindow.render.pipeline.FrameBuffer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL31;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.tinyfd.TinyFileDialogs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.GLFW_CLIENT_API;
import static org.lwjgl.glfw.GLFW.GLFW_CONTEXT_CREATION_API;
import static org.lwjgl.glfw.GLFW.GLFW_CONTEXT_VERSION_MAJOR;
import static org.lwjgl.glfw.GLFW.GLFW_CONTEXT_VERSION_MINOR;
import static org.lwjgl.glfw.GLFW.GLFW_FALSE;
import static org.lwjgl.glfw.GLFW.GLFW_NATIVE_CONTEXT_API;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_API;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_CORE_PROFILE;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_FORWARD_COMPAT;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_PROFILE;
import static org.lwjgl.glfw.GLFW.GLFW_RESIZABLE;
import static org.lwjgl.glfw.GLFW.GLFW_TRUE;
import static org.lwjgl.glfw.GLFW.GLFW_VISIBLE;
import static org.lwjgl.glfw.GLFW.GLFW_X11_CLASS_NAME;
import static org.lwjgl.glfw.GLFW.GLFW_X11_INSTANCE_NAME;
import static org.lwjgl.glfw.GLFW.glfwCreateWindow;
import static org.lwjgl.glfw.GLFW.glfwGetFramebufferSize;
import static org.lwjgl.glfw.GLFW.glfwGetMonitorPos;
import static org.lwjgl.glfw.GLFW.glfwGetPrimaryMonitor;
import static org.lwjgl.glfw.GLFW.glfwGetVideoMode;
import static org.lwjgl.glfw.GLFW.glfwGetWindowAttrib;
import static org.lwjgl.glfw.GLFW.glfwGetWindowPos;
import static org.lwjgl.glfw.GLFW.glfwGetWindowSize;
import static org.lwjgl.glfw.GLFW.glfwMaximizeWindow;
import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwSetFramebufferSizeCallback;
import static org.lwjgl.glfw.GLFW.glfwSetWindowPos;
import static org.lwjgl.glfw.GLFW.glfwSetWindowPosCallback;
import static org.lwjgl.glfw.GLFW.glfwSetWindowSizeCallback;
import static org.lwjgl.glfw.GLFW.glfwShowWindow;
import static org.lwjgl.glfw.GLFW.glfwWindowHint;
import static org.lwjgl.glfw.GLFW.glfwWindowHintString;
import static org.lwjgl.opengl.GL.createCapabilities;
import static org.lwjgl.opengl.GL11C.*;

public final class EndfieldEarlyWindow implements ImmediateWindowProvider {

    private static final int[][] GL_VERSIONS = new int[][] { { 4, 6 }, { 4, 5 }, { 4, 4 }, { 4, 3 }, { 4, 2 }, { 4, 1 }, { 4, 0 }, { 3, 3 }, { 3, 2 } };

    private static final Logger LOGGER = LoggerFactory.getLogger("EARLYDISPLAY");

    private final ResourceManager manager = new ResourceManager();

    private long window;

    private ScheduledFuture<?> windowTick;
    private ScheduledFuture<?> initializationFuture;

    private ScheduledExecutorService renderScheduler;
    private int fbWidth;
    private int fbHeight;
    private int fbScale;
    private int winWidth;
    private int winHeight;
    private int winX;
    private int winY;

    private boolean maximized;

    private String glVersion;

    @Getter
    private RenderScene scene;
    private FrameBuffer mainTarget;

    private MinecraftProfile profile;

    private @NotNull Runnable ticker = () -> {};

    @Override
    public String name() {
        return "fmlearlywindow";
    }

    @Override
    public Runnable initialize(String[] arguments) {
        final OptionParser parser = new OptionParser();
        var mcVersionOpt = parser.accepts("fml.mcVersion").withRequiredArg().ofType(String.class);
        var forgeVersionOpt = parser.accepts("fml.neoForgeVersion").withRequiredArg().ofType(String.class);
        var widthOpt = parser.accepts("width")
                .withRequiredArg().ofType(Integer.class)
                .defaultsTo(FMLConfig.getIntConfigValue(FMLConfig.ConfigValue.EARLY_WINDOW_WIDTH));
        var heightOpt = parser.accepts("height")
                .withRequiredArg().ofType(Integer.class)
                .defaultsTo(FMLConfig.getIntConfigValue(FMLConfig.ConfigValue.EARLY_WINDOW_HEIGHT));
        var maximizedOpt = parser.accepts("earlywindow.maximized");
        var usernameOpt = parser.accepts("username").withOptionalArg().ofType(String.class);
        parser.allowsUnrecognizedOptions();
        var parsed = parser.parse(arguments);
        winWidth = parsed.valueOf(widthOpt);
        winHeight = parsed.valueOf(heightOpt);
        FMLConfig.updateConfig(FMLConfig.ConfigValue.EARLY_WINDOW_WIDTH, winWidth);
        FMLConfig.updateConfig(FMLConfig.ConfigValue.EARLY_WINDOW_HEIGHT, winHeight);
        fbScale = FMLConfig.getIntConfigValue(FMLConfig.ConfigValue.EARLY_WINDOW_FBSCALE);

        this.maximized = parsed.has(maximizedOpt) || FMLConfig.getBoolConfigValue(FMLConfig.ConfigValue.EARLY_WINDOW_MAXIMIZED);

        var forgeVersion = parsed.valueOf(forgeVersionOpt);
        StartupNotificationManager.modLoaderConsumer().ifPresent(c -> c.accept("NeoForge loading " + forgeVersion));

        renderScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            final var thread = Executors.defaultThreadFactory().newThread(r);
            thread.setDaemon(true);
            return thread;
        });

        var mcVersion = parsed.valueOf(mcVersionOpt);
        var username = parsed.has(usernameOpt) ? parsed.valueOf(usernameOpt) : "Louis_Quepierts";

        this.profile = new MinecraftProfile(username);

        initWindow(mcVersion);
        this.initializationFuture = renderScheduler.schedule(() -> initRender(mcVersion, forgeVersion), 1, TimeUnit.MILLISECONDS);
        return this::periodicTick;
    }

    @Override
    public void updateFramebufferSize(IntConsumer width, IntConsumer height) {
        width.accept(this.fbWidth);
        height.accept(this.fbHeight);
    }

    @Override
    public long setupMinecraftWindow(IntSupplier width, IntSupplier height, Supplier<String> title, LongSupplier monitor) {

        try {
            this.initializationFuture.get(30, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        } catch (TimeoutException e) {
            Thread.dumpStack();
            crash("We seem to be having trouble initializing the window, waited for 30 seconds");
        }

        while (!this.windowTick.isDone()) {
            this.windowTick.cancel(false);
        }

        var tries = 0;
        var renderlockticket = false;
        do {
            try {
                renderlockticket = renderLock.tryAcquire(100, TimeUnit.MILLISECONDS);
                if (++tries > 9) {
                    Thread.dumpStack();
                    crash("We seem to be having trouble handing off the window, tried for 1 second");
                }
            } catch (InterruptedException e) {
                Thread.interrupted();
            }
        } while (!renderlockticket);
        renderLock.release();

        glfwMakeContextCurrent(window);
        // Set the title to what the game wants
        glfwSetWindowTitle(window, title.get());
        glfwSwapInterval(0);
        // Clean up our hooks
        glfwSetFramebufferSizeCallback(window, null).free();
        glfwSetWindowPosCallback(window, null).free();
        glfwSetWindowSizeCallback(window, null).free();
        this.ticker = this::renderThreadFunc;
        this.windowTick = null; // this tells the render thread that the async ticker is done

        return this.window;
    }

    @Override
    public boolean positionWindow(Optional<Object> monitor, IntConsumer widthSetter, IntConsumer heightSetter, IntConsumer xSetter, IntConsumer ySetter) {
        widthSetter.accept(this.winWidth);
        heightSetter.accept(this.winHeight);
        xSetter.accept(this.winX);
        ySetter.accept(this.winY);
        return true;
    }

    private Constructor<?> overlayConstructor;

    @Override
    @SuppressWarnings("unchecked")
    public <T> Supplier<T> loadingOverlay(Supplier<?> mc, Supplier<?> ri, Consumer<Optional<Throwable>> ex, boolean fade) {
        return () -> {
            try {
                var overlay = overlayConstructor.newInstance(mc.get(), ri.get(), ex, fade, this);
                return (T) overlay;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

    @Override
    public void updateModuleReads(ModuleLayer layer) {
        try {
            ClassLoader loader = Thread.currentThread().getContextClassLoader();

            final var overlayClass      = Class.forName("net.quepierts.endfieldpanorama.neoforge.Overlay", false, loader);
            final var minecraftClass    = Class.forName("net.minecraft.client.Minecraft", false, loader);
            final var reloadClass       = Class.forName("net.minecraft.server.packs.resources.ReloadInstance", false, loader);
            this.overlayConstructor     = overlayClass.getConstructor(
                    minecraftClass,
                    reloadClass,
                    Consumer.class,
                    boolean.class,
                    EndfieldEarlyWindow.class
            );

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void periodicTick() {
        glfwPollEvents();
        this.ticker.run();
    }

    @Override
    public String getGLVersion() {
        return "3.3";
    }

    private final ReentrantLock crashLock = new ReentrantLock();

    @Override
    public void crash(String message) {
        crashLock.lock();

        StringBuilder msgBuilder = new StringBuilder(2000);
        msgBuilder.append("Failed to initialize the mod loading system and display.\n");
        msgBuilder.append("\n\n");
        msgBuilder.append("Failure details:\n");
        msgBuilder.append(message);
        msgBuilder.append("\n\n");
        LOGGER.error("ERROR DISPLAY\n{}", msgBuilder);
        // we show the display on a new dedicated thread
        var thread = new Thread(() -> {
            TinyFileDialogs.tinyfd_messageBox("Minecraft: NeoForge", msgBuilder.toString(), "yesno", "error", false);
        }, "crash-report");
        thread.setDaemon(true);
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException ignored) {}

        System.exit(1);
    }

    public void initWindow(@Nullable String mcVersion) {
        long glfwInitBegin = System.nanoTime();
        if (!glfwInit()) {
            crash("We are unable to initialize the graphics system.\nglfwInit failed.\n");
            throw new IllegalStateException("Unable to initialize GLFW");
        }
        long glfwInitEnd = System.nanoTime();

        if (glfwInitEnd - glfwInitBegin > 1e9) {
            LOGGER.error("WARNING : glfwInit took {} seconds to start.", (glfwInitEnd - glfwInitBegin) / 1.0e9);
        }

        // Clear the Last Exception (#7285 - Prevent Vanilla throwing an IllegalStateException due to invalid controller mappings)
        handleLastGLFWError((error, description) -> LOGGER.error(String.format("Suppressing Last GLFW error: [0x%X]%s", error, description)));

        // Set window hints for the new window we're gonna create.
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_CLIENT_API, GLFW_OPENGL_API);
        glfwWindowHint(GLFW_CONTEXT_CREATION_API, GLFW_NATIVE_CONTEXT_API);
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        if (mcVersion != null) {
            // this emulates what we would get without early progress window
            // as vanilla never sets these, so GLFW uses the first window title
            // set them explicitly to avoid it using "FML early loading progress" as the class
            String vanillaWindowTitle = "Minecraft* " + mcVersion;
            glfwWindowHintString(GLFW_X11_CLASS_NAME, vanillaWindowTitle);
            glfwWindowHintString(GLFW_X11_INSTANCE_NAME, vanillaWindowTitle);
        }

        long primaryMonitor = glfwGetPrimaryMonitor();
        if (primaryMonitor == 0) {
            LOGGER.error("Failed to find a primary monitor - this means LWJGL isn't working properly");
            crash("Failed to locate a primary monitor.\nglfwGetPrimaryMonitor failed.\n");
            throw new IllegalStateException("Can't find a primary monitor");
        }
        GLFWVidMode vidmode = glfwGetVideoMode(primaryMonitor);

        if (vidmode == null) {
            LOGGER.error("Failed to get the current display video mode.");
            crash("Failed to get current display resolution.\nglfwGetVideoMode failed.\n");
            throw new IllegalStateException("Can't get a resolution");
        }
        long window = 0;
        var successfulWindow = new AtomicBoolean(false);
        var windowFailFuture = renderScheduler.schedule(() -> {
            if (!successfulWindow.get()) crash("Timed out trying to setup the Game Window.");
        }, 30, TimeUnit.SECONDS);
        int versidx = 0;
        var skipVersions = FMLConfig.<String>getListConfigValue(FMLConfig.ConfigValue.EARLY_WINDOW_SKIP_GL_VERSIONS);
        final String[] lastGLError = new String[GL_VERSIONS.length];
        do {
            final var glVersionToTry = GL_VERSIONS[versidx][0] + "." + GL_VERSIONS[versidx][1];
            if (skipVersions.contains(glVersionToTry)) {
                LOGGER.info("Skipping GL version " + glVersionToTry + " because of configuration");
                versidx++;
                continue;
            }
            LOGGER.info("Trying GL version " + glVersionToTry);
            glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, GL_VERSIONS[versidx][0]); // we try our versions one at a time
            glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, GL_VERSIONS[versidx][1]);
            glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
            glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE);
            window = glfwCreateWindow(winWidth, winHeight, "Minecraft: NeoForge Loading...", 0L, 0L);
            var erridx = versidx;
            handleLastGLFWError((error, description) -> lastGLError[erridx] = String.format("Trying %d.%d: GLFW error: [0x%X]%s", GL_VERSIONS[erridx][0], GL_VERSIONS[erridx][1], error, description));
            if (lastGLError[versidx] != null) {
                LOGGER.trace(lastGLError[versidx]);
            }
            versidx++;
        } while (window == 0 && versidx < GL_VERSIONS.length);
//        LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(12));
        if (versidx == GL_VERSIONS.length && window == 0) {
            LOGGER.error("Failed to find any valid GLFW profile. " + lastGLError[0]);

            crash("Failed to find a valid GLFW profile.\nWe tried " +
                    Arrays.stream(GL_VERSIONS).map(p -> p[0] + "." + p[1]).filter(o -> !skipVersions.contains(o))
                            .collect(Collector.of(() -> new StringJoiner(", ").setEmptyValue("no versions"), StringJoiner::add, StringJoiner::merge, StringJoiner::toString))
                    +
                    " but none of them worked.\n" + Arrays.stream(lastGLError).filter(Objects::nonNull).collect(Collectors.joining("\n")));
            throw new IllegalStateException("Failed to create a GLFW window with any profile");
        }
        successfulWindow.set(true);
        if (!windowFailFuture.cancel(true)) throw new IllegalStateException("We died but didn't somehow?");
        var requestedVersion = GL_VERSIONS[versidx - 1][0] + "." + GL_VERSIONS[versidx - 1][1];
        var maj = glfwGetWindowAttrib(window, GLFW_CONTEXT_VERSION_MAJOR);
        var min = glfwGetWindowAttrib(window, GLFW_CONTEXT_VERSION_MINOR);
        var gotVersion = maj + "." + min;
        LOGGER.info("Requested GL version " + requestedVersion + " got version " + gotVersion);
        this.glVersion = gotVersion;
        this.window = window;

        int[] x = new int[1];
        int[] y = new int[1];
        glfwGetMonitorPos(primaryMonitor, x, y);
        int monitorX = x[0];
        int monitorY = y[0];
//        glfwSetWindowSizeLimits(window, 854, 480, GLFW_DONT_CARE, GLFW_DONT_CARE);
        if (this.maximized) {
            glfwMaximizeWindow(window);
        }

        glfwGetWindowSize(window, x, y);
        this.winWidth = x[0];
        this.winHeight = y[0];

        glfwSetWindowPos(window, (vidmode.width() - this.winWidth) / 2 + monitorX, (vidmode.height() - this.winHeight) / 2 + monitorY);
        glfwSetFramebufferSizeCallback(window, this::fbResize);
        glfwSetWindowPosCallback(window, this::winMove);
        glfwSetWindowSizeCallback(window, this::winResize);

        // Show the window
        glfwShowWindow(window);
        glfwGetWindowPos(window, x, y);
        handleLastGLFWError((error, description) -> LOGGER.debug(String.format("Suppressing GLFW get window position error: [0x%X]%s", error, description)));
        this.winX = x[0];
        this.winY = y[0];
        glfwGetFramebufferSize(window, x, y);
        this.fbWidth = x[0];
        this.fbHeight = y[0];
        glfwPollEvents();
    }

    private void winResize(long window, int width, int height) {
        if (window == this.window && width != 0 && height != 0) {
            this.winWidth = width;
            this.winHeight = height;
        }
    }

    private void fbResize(long window, int width, int height) {
        if (window == this.window && width != 0 && height != 0) {
            this.fbWidth = width;
            this.fbHeight = height;
        }
    }

    private void winMove(long window, int x, int y) {
        if (window == this.window) {
            this.winX = x;
            this.winY = y;
        }
    }

    private void handleLastGLFWError(BiConsumer<Integer, String> handler) {
        try (MemoryStack memorystack = MemoryStack.stackPush()) {
            PointerBuffer pointerbuffer = memorystack.mallocPointer(1);
            int error = glfwGetError(pointerbuffer);
            if (error != GLFW_NO_ERROR) {
                long pDescription = pointerbuffer.get();
                String description = pDescription == 0L ? "" : MemoryUtil.memUTF8(pDescription);
                handler.accept(error, description);
            }
        }
    }

    private void initRender(final @Nullable String mcVersion, final String forgeVersion) {
        glfwMakeContextCurrent(window);
        // Wait for one frame to be complete before swapping; enable vsync in other words.
        glfwSwapInterval(1);
        createCapabilities();
        LOGGER.info("GL info: " + glGetString(GL_RENDERER) + " GL version " + glGetString(GL_VERSION) + ", " + glGetString(GL_VENDOR));

        this.mainTarget = new FrameBuffer(true);
        this.mainTarget.resize(this.fbWidth, this.fbHeight);
        this.mainTarget.clearColor(1.0f, 0.0f, 0.0f, 1.0f);

        this.manager.register(this.mainTarget);

        this.scene      = new RenderScene(this.manager, this.profile);
        this.scene.resize(this.fbWidth, this.fbHeight);

        // clear color
        GL11.glClearColor(1.0f, 0.0f, 0.0f, 1.0f);
        GL11.glClear(GL_COLOR_BUFFER_BIT);

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glfwMakeContextCurrent(0);
        this.windowTick = renderScheduler.scheduleAtFixedRate(this::renderThreadFunc, 50, 50, TimeUnit.MILLISECONDS);
//        this.performanceTick = renderScheduler.scheduleAtFixedRate(performanceInfo::update, 0, 500, TimeUnit.MILLISECONDS);
        // schedule a 50 ms ticker to try and smooth out the rendering
//        renderScheduler.scheduleAtFixedRate(() -> animationTimerTrigger.set(true), 1, 50, TimeUnit.MILLISECONDS);
    }

    private final Semaphore     renderLock      = new Semaphore(1);

    private static final long   MINFRAMETIME    = TimeUnit.MILLISECONDS.toNanos(10);
    private long                nextFrameTime   = 0;

    private void renderThreadFunc() {
        if (!renderLock.tryAcquire()) {
            return;
        }

        try {
            long nt;
            if ((nt = System.nanoTime()) < nextFrameTime) {
                return;
            }
            this.nextFrameTime = nt + MINFRAMETIME;

            glfwMakeContextCurrent(window);

            this.mainTarget.resize(this.fbWidth, this.fbHeight);
            GL31.glViewport(0, 0, this.fbWidth, this.fbHeight);

            this.mainTarget.clear();
            this.mainTarget.bind();
            this.draw(0.1f);
            this.mainTarget.unbind();
            this.mainTarget.draw(this.fbWidth, this.fbHeight);

            glfwSwapBuffers(window);
        } catch (Exception e) {
            LOGGER.error("Error during rendering", e);
        } finally {
            if (this.windowTick != null) {
                glfwMakeContextCurrent(0);
            }
            renderLock.release();
        }
    }

    public void draw(float delta) {

        // render
        this.scene.render(delta);

    }

    public void close() {
        renderScheduler.shutdown();
        this.manager.free();
    }
}
