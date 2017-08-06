package omtteam.openmodularlighting.handler;

/**
 * Created by Keridos on 02/05/17.
 * This class is the listener for all the Events we need to watch & modify.
 */
public class EventHandler {
    private static EventHandler instance;

    private EventHandler() {
    }

    public static EventHandler getInstance() {
        if (instance == null) {
            instance = new EventHandler();
        }
        return instance;
    }
}
