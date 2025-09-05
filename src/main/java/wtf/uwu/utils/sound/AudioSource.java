package wtf.uwu.utils.sound;

import java.util.concurrent.atomic.AtomicInteger;

public class AudioSource {

    private static final AtomicInteger idCounter = new AtomicInteger(0);

    private final int sourceId;
    private float x, y, z;
    private float volume = 1.0f;
    private float pitch = 1.0f;
    private boolean looping = false;
    private boolean isPlaying = false;

    public AudioSource() {
        this.sourceId = idCounter.incrementAndGet();
        System.out.println("Allocated AudioSource with ID: " + this.sourceId);
    }

    public void setPosition(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void setVolume(float volume) {
        this.volume = Math.max(0.0f, Math.min(1.0f, volume));
    }

    public void setPitch(float pitch) {
        this.pitch = Math.max(0.5f, Math.min(2.0f, pitch));
    }

    public void setLooping(boolean looping) {
        this.looping = looping;
    }

    public void play() {
        this.isPlaying = true;
    }

    public void stop() {
        this.isPlaying = false;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public int getSourceId() {
        return sourceId;
    }

    public void release() {
        System.out.println("Released AudioSource with ID: " + this.sourceId);
    }
}