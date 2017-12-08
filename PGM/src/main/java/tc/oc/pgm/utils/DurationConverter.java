package tc.oc.pgm.utils;

import com.sun.istack.internal.Nullable;
import net.md_5.bungee.api.ChatColor;

import java.time.Duration;

public class DurationConverter {

    public static String getReadableFormat(Duration duration, @Nullable ChatColor start, @Nullable ChatColor end,
                                           Boolean inSeconds) {
        Long seconds = duration.getSeconds();
        Long minutes = seconds / 60;
        Long secondsRemaining = seconds % 60;
        ChatColor startColor = ChatColor.RESET;
        ChatColor endColor = ChatColor.RESET;
        String minutes1, secondsRemaining1;
        startColor = (start != null) ? start : startColor;
        endColor = (end != null) ? end : endColor;
        minutes1 = (minutes < 10) ? ("0" + minutes) : ("" + minutes);
        secondsRemaining1 = (secondsRemaining < 10) ? ("0" + secondsRemaining) : ("" + secondsRemaining);
        if(inSeconds) {
            return secondsRemaining1 + "";
        } else if(minutes > 0) {
            return startColor + "" + minutes1 + ":" + secondsRemaining1 + endColor;
        } else {
            return startColor + "" + secondsRemaining1 + "" + endColor;
        }
    }
}
