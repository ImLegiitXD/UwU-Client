package wtf.uwu.utils.sound;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class AudioBuffer {

    private final int bufferId;
    private final ByteBuffer data;
    private final int sampleRate;
    private final int bitsPerSample;
    private final int channels;

    public AudioBuffer(byte[] pcmData, int sampleRate, int bitsPerSample, int channels) {
        this.sampleRate = sampleRate;
        this.bitsPerSample = bitsPerSample;
        this.channels = channels;

        this.bufferId = (int) (Math.random() * 10000);
        this.data = ByteBuffer.allocateDirect(pcmData.length).order(ByteOrder.nativeOrder());
        this.data.put(pcmData).flip();
    }

    public int getBufferId() {
        return bufferId;
    }

    public int getSampleRate() {
        return sampleRate;
    }

    public int getSize() {
        return data.capacity();
    }

    public void dispose() {
        // System.out.println("Disposing audio buffer " + bufferId);
    }
}