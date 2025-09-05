
package wtf.uwu.events.impl.player;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import wtf.uwu.events.impl.CancellableEvent;

@Setter
@Getter
@AllArgsConstructor
public final class SlowDownEvent extends CancellableEvent {
    private float strafe;
    private float forward;
    private boolean sprinting;
}