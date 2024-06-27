package rahulstech.jfx.balancesheet.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.logging.Level;

public class Log {

    public static void warn(String tag, String message) {
        log(Level.WARNING,tag,message,null);
    }

    public static void warn(String tag, String message, Throwable reason) {
        log(Level.WARNING,tag,message,reason);
    }

    public static void error(String tag, String message) {
        log(Level.SEVERE,tag,message,null);
    }

    public static void error(String tag, String message, Throwable reason) {
        log(Level.SEVERE,tag,message,reason);
    }

    public static void info(String tag, String message) {
        log(Level.INFO,tag,message,null);
    }

    public static void info(String tag, String message, Throwable reason) {
        log(Level.INFO,tag,message,reason);
    }

    public static void debug(String tag, String message) {
        log(Level.FINE,tag,message,null);
    }

    public static void debug(String tag, String message, Throwable reason) {
        log(Level.FINE,tag,message,reason);
    }

    public static void trace(String tag, String message, Throwable reason) {
        log(Level.FINER,tag,message,reason);
    }

    public static void trace(String tag, String message) {
        log(Level.FINER,tag,message,null);
    }

    public static void log(Level level, String tag, String message, Throwable reason) {
        Logger logger = LoggerFactory.getLogger(tag);
        if (level==Level.FINE) {
            logger.debug(message,reason);
        }
        else if (level==Level.FINER) {
            logger.trace(message,reason);
        }
        else if (level==Level.WARNING) {
            logger.warn(message,reason);
        }
        else if (level==Level.SEVERE) {
            logger.error(message,reason);
        }
        else {
            logger.info(message,reason);
        }
    }
}
