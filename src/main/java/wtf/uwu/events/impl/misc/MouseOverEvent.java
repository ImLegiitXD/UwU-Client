package wtf.uwu.events.impl.misc;

import wtf.uwu.events.impl.Event;

public class MouseOverEvent implements Event {
    private double range;

    public MouseOverEvent(double range) {
        this.range = range;
    }

    public double getRange() {
        return range;
    }

    public void setRange(double range) {
        this.range = range;
    }
}