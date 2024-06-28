package rahulstech.jfx.balancesheet.util;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.FileAppender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.time.LocalDate;
import java.util.logging.Level;

public class Log {

    public static void init(File dir, boolean development) {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        ch.qos.logback.classic.Level level = development ? ch.qos.logback.classic.Level.TRACE : ch.qos.logback.classic.Level.INFO;
        logger.setLevel(level);
        logger.addAppender(createFileAppender(context,dir));
        if (development) {
            logger.addAppender(createConsoleAppender(context));
        }
    }

    private static Appender<ILoggingEvent> createConsoleAppender(LoggerContext context) {
        // Create a pattern layout encoder
        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(context);
        encoder.setPattern("[%d{HH:mm:ss.SSS}] [%thread] [%-5level] [%logger{36}]: %msg%n");
        encoder.start();

        // Create appender
        ConsoleAppender<ILoggingEvent> appender = new ConsoleAppender<>();
        appender.setEncoder(encoder);
        appender.setName("CONSOLE");
        appender.start();

        return appender;
    }

    private static Appender<ILoggingEvent> createFileAppender(LoggerContext context, File dir) {
        // Create a pattern layout encoder
        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(context);
        encoder.setPattern("[%d{yyyy-MM-dd HH:mm:ss.SSS}] [%-5level] [%logger{36}]: %msg%n");
        encoder.start();

        // Create appender
        FileAppender<ILoggingEvent> appender = new FileAppender<>();
        appender.setContext(context);
        appender.setName("FILE");
        appender.setEncoder(encoder);
        appender.setAppend(true);
        appender.setFile(new File(dir,"log_"+ LocalDate.now() +".log").toString());
        appender.start();

        return appender;
    }

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
