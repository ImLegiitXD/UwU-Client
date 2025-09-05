package wtf.uwu.events.impl.player;

import wtf.uwu.events.impl.Event;

public final class UpdateEvent implements Event {

    public static final UpdateEvent INSTANCE = new UpdateEvent();

    public enum State {
        PRE, POST
    }

    private State state = State.PRE;

    private UpdateEvent() {}

    public UpdateEvent setState(State state) {
        this.state = state;
        return this;
    }

    public boolean isPre() {
        return state == State.PRE;
    }

    public boolean isPost() {
        return state == State.POST;
    }

    public State getState() {
        return state;
    }
}