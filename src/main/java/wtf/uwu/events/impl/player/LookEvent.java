
package wtf.uwu.events.impl.player;

import lombok.AllArgsConstructor;
import wtf.uwu.events.impl.Event;

@AllArgsConstructor
public class LookEvent implements Event {
    public float yaw;
    public float pitch;
    public float prevYaw;
    public float prevPitch;
}
