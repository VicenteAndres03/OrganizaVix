import java.util.ArrayList;
import java.util.List;

public class ThemeManager {

    private static boolean dark = true;
    private static final List<Runnable> listeners = new ArrayList<>();

    public static boolean isDark() {
        return dark;
    }

    public static void toggle() {
        dark = !dark;
        for (Runnable r : listeners)
            r.run();
    }

    public static void addListener(Runnable r) {
        listeners.add(r);
    }
}