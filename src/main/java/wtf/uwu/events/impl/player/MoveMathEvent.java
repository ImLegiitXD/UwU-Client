
package wtf.uwu.events.impl.player;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import wtf.uwu.events.impl.CancellableEvent;

@Getter
@Setter
@AllArgsConstructor
public class MoveMathEvent extends CancellableEvent {

    public float strafe;
    public float forward;
}
