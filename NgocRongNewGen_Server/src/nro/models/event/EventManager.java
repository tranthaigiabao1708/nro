package nro.models.event;

import nro.models.event_list.Default;

public class EventManager {

    private static EventManager instance;

    public static EventManager gI() {
        if (instance == null) {
            instance = new EventManager();
        }
        return instance;
    }

    public void init() {
        new Default().init();
    }
}
