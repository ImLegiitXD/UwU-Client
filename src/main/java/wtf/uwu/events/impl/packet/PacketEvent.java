package wtf.uwu.events.impl.packet;

import lombok.AllArgsConstructor;
import lombok.Setter;
import net.minecraft.network.Packet;
import wtf.uwu.events.impl.CancellableEvent;

@AllArgsConstructor
public class PacketEvent extends CancellableEvent {

    @Setter
    private Packet<?> packet;
    private final State state;


    public Packet<?> getPacket() {
        return this.packet;
    }

    public State getState() {
        return this.state;
    }


    public enum State {
        INCOMING,
        OUTGOING
    }
}