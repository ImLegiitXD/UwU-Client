package wtf.uwu;

import com.google.gson.*;
import de.florianmichael.viamcp.ViaMCP;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundCategory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjglx.Sys;
import org.lwjglx.opengl.Display;
import wtf.uwu.events.EventManager;
import wtf.uwu.features.command.CommandManager;
import wtf.uwu.features.config.ConfigManager;
import wtf.uwu.features.friend.FriendManager;
import wtf.uwu.features.modules.ModuleManager;
import wtf.uwu.features.modules.impl.visual.ScaffoldCounter;
import wtf.uwu.gui.altmanager.repository.AltRepositoryGUI;
import wtf.uwu.gui.click.astolfo.AstolfoGui;
import wtf.uwu.gui.click.dropdown.DropdownGUI;
import wtf.uwu.gui.click.neverlose.NeverLose;
import wtf.uwu.gui.notification.NotificationManager;
import wtf.uwu.gui.notification.NotificationType;
import wtf.uwu.gui.widget.WidgetManager;
import wtf.uwu.utils.discord.DiscordInfo;
import wtf.uwu.utils.misc.SpoofSlotUtils;
import wtf.uwu.utils.packet.BadPacketsComponent;
import wtf.uwu.utils.packet.BlinkComponent;
import wtf.uwu.utils.packet.PingSpoofComponent;
import wtf.uwu.utils.player.FallDistanceComponent;
import wtf.uwu.utils.player.RotationUtils;
import wtf.uwu.utils.sound.SoundUtil;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

@Getter
public class UwU {

    public static final Logger LOGGER = LogManager.getLogger(UwU.class);
    public static final UwU INSTANCE = new UwU();

    public final String clientName = "UwU Client";
    public final String version = "Pre release 4";
    public final String clientCloud = "none";

    private final File mainDir = new File(Minecraft.getMinecraft().mcDataDir, clientName);

    private EventManager eventManager;
    private NotificationManager notificationManager;
    private ModuleManager moduleManager;
    private ConfigManager configManager;
    private WidgetManager widgetManager;
    private CommandManager commandManager;
    private FriendManager friendManager;
    private NeverLose neverLose;
    private DropdownGUI dropdownGUI;
    private AstolfoGui astolfoGui;
    private AltRepositoryGUI altRepositoryGUI;
    private DiscordInfo discordRP;

    private TrayIcon trayIcon;

    private int startTime;
    private long startTimeLong;

    private boolean loaded;
    private Path dataFolder;

    public void init() {
        loaded = false;

        setupMainDirectory();
        setupDisplayTitle();
        initializeManagers();
        registerEventHandlers();
        initializeStartTime();
        initializeViaMCP();
        setupDiscordRPC();
        setupSystemTray();
        handleFastRender();
        initializeStartupSound();

        loaded = true;

        dataFolder = Paths.get(Minecraft.getMinecraft().mcDataDir.getAbsolutePath()).resolve(clientName);
        LOGGER.info("{} {} initialized successfully.", clientName, version);
    }

    private void setupMainDirectory() {
        if (!mainDir.exists()) {
            if (mainDir.mkdir()) {
                LOGGER.info("Created main directory at {}", mainDir.getAbsolutePath());
            } else {
                LOGGER.warn("Failed to create main directory at {}", mainDir.getAbsolutePath());
            }
            Minecraft.getMinecraft().gameSettings.setSoundLevel(SoundCategory.MUSIC, 0);
        } else {
            LOGGER.info("Main directory already exists at {}", mainDir.getAbsolutePath());
        }
        this.dataFolder = Paths.get(Minecraft.getMinecraft().mcDataDir.getAbsolutePath()).resolve(clientName);
    }

    private void setupDisplayTitle() {
        String osVersion = Sys.getVersion();
        String title = String.format("%s %s | %s", clientName, version, osVersion);
        Display.setTitle(title);
        LOGGER.info("Display title set to: {}", title);
    }

    private void initializeManagers() {
        try {
            LOGGER.info("Initializing EventManager...");
            eventManager = new EventManager();
        } catch (Throwable t) {
            LOGGER.error("Failed to initialize EventManager!", t);
        }

        try {
            LOGGER.info("Initializing NotificationManager...");
            notificationManager = new NotificationManager();
        } catch (Throwable t) {
            LOGGER.error("Failed to initialize NotificationManager!", t);
        }

        try {
            LOGGER.info("Initializing ModuleManager...");
            moduleManager = new ModuleManager();
        } catch (Throwable t) {
            LOGGER.error("CRITICAL: Failed to initialize ModuleManager! Modules will not work.", t);
        }

        try {
            LOGGER.info("Initializing WidgetManager...");
            widgetManager = new WidgetManager();
        } catch (Throwable t) {
            LOGGER.error("Failed to initialize WidgetManager!", t);
        }

        try {
            LOGGER.info("Initializing ConfigManager...");
            configManager = new ConfigManager();
        } catch (Throwable t) {
            LOGGER.error("Failed to initialize ConfigManager!", t);
        }

        try {
            LOGGER.info("Initializing CommandManager...");
            commandManager = new CommandManager();
        } catch (Throwable t) {
            LOGGER.error("Failed to initialize CommandManager!", t);
        }

        try {
            LOGGER.info("Initializing FriendManager...");
            friendManager = new FriendManager();
        } catch (Throwable t) {
            LOGGER.error("Failed to initialize FriendManager!", t);
        }

        try {
            LOGGER.info("Initializing NeverLose GUI...");
            neverLose = new NeverLose();
        } catch (Throwable t) {
            LOGGER.error("Failed to initialize NeverLose GUI!", t);
        }

        try {
            LOGGER.info("Initializing DropdownGUI...");
            dropdownGUI = new DropdownGUI();
        } catch (Throwable t) {
            LOGGER.error("Failed to initialize DropdownGUI!", t);
        }

        try {
            LOGGER.info("Initializing AstolfoGUI...");
            astolfoGui = new AstolfoGui();
        } catch (Throwable t) {
            LOGGER.error("Failed to initialize AstolfoGUI!", t);
        }

        try {
            LOGGER.info("Initializing AltRepositoryGUI...");
            altRepositoryGUI = new AltRepositoryGUI(this);
        } catch (Throwable t) {
            LOGGER.error("Failed to initialize AltRepositoryGUI!", t);
        }
    }

    private void registerEventHandlers() {
        try {
            eventManager.register(new ScaffoldCounter());
            eventManager.register(new RotationUtils());
            eventManager.register(new FallDistanceComponent());
            eventManager.register(new BadPacketsComponent());
            eventManager.register(new PingSpoofComponent());
            eventManager.register(new BlinkComponent());
            eventManager.register(new SpoofSlotUtils());
            LOGGER.info("Event handlers registered.");
        } catch (Throwable t) {
            LOGGER.error("Failed to register event handlers!", t);
        }
    }

    private void initializeStartTime() {
        startTime = (int) System.currentTimeMillis();
        startTimeLong = System.currentTimeMillis();
        LOGGER.info("Start time initialized: {} ms", startTime);
    }

    private void initializeViaMCP() {
        try {
            ViaMCP.create();
            ViaMCP.INSTANCE.initAsyncSlider();
            LOGGER.info("ViaMCP initialized.");
        } catch (Throwable t) {
            LOGGER.error("Failed to initialize ViaMCP!", t);
        }
    }

    private void setupDiscordRPC() {
        try {
            discordRP = new DiscordInfo();
            discordRP.init();
            LOGGER.info("Discord Rich Presence initialized.");
        } catch (Throwable t) {
            LOGGER.error("Failed to set up Discord RPC.", t);
        }
    }

    private void setupSystemTray() {
        if (isWindows() && SystemTray.isSupported()) {
            try {
                Image trayImage = ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream("/assets/minecraft/uwu client/img/logo.png")));
                trayIcon = new TrayIcon(trayImage, clientName);
                trayIcon.setImageAutoSize(true);
                trayIcon.setToolTip(clientName);

                SystemTray.getSystemTray().add(trayIcon);
                trayIcon.displayMessage(clientName, "Client started successfully.", TrayIcon.MessageType.INFO);

                LOGGER.info("System tray icon added.");
            } catch (IOException | AWTException | NullPointerException e) {
                LOGGER.error("Failed to create or add TrayIcon.", e);
            }
        } else {
            LOGGER.warn("System tray not supported or not running on Windows.");
        }
    }

    private void handleFastRender() {
        if (Minecraft.getMinecraft().gameSettings.ofFastRender) {
            if (notificationManager != null) {
                notificationManager.post(NotificationType.WARNING, "Fast Rendering has been disabled", "due to compatibility issues");
            }
            Minecraft.getMinecraft().gameSettings.ofFastRender = false;
            LOGGER.info("Fast Rendering was disabled due to compatibility issues.");
        }
    }

    private void initializeStartupSound() {
        try {
            SoundUtil.initializeAndPlayStartupSound();
            LOGGER.info("Initializing startup sound subsystem...");
        } catch (Throwable t) {
            LOGGER.warn("Could not initialize startup sound subsystem. This is not critical.", t);
        }
    }

    private boolean isWindows() {
        String osName = System.getProperty("os.name").toLowerCase();
        return osName.contains("windows");
    }

    public void onStop() {
        if (discordRP != null) {
            discordRP.stop();
            LOGGER.info("Discord Rich Presence stopped.");
        }
        if (configManager != null) {
            configManager.saveConfigs();
            LOGGER.info("All configurations saved.");
        }
    }

    public ModuleManager getModuleManager() {
        return moduleManager;
    }
}