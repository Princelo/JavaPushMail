package com.mofirouz.javapushmail;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * @author Mo Firouz
 * @since 7/11/11
 */
public class JavaPushMailLogger {

    private static JavaPushMailLogger logger;
    private int level = 0; // 0=debug, 1=info 2=warn 3=error
    private boolean writeOut = false;
    private File writeFile = null;
    private BufferedWriter writer;
    private final String DATE_FORMAT_NOW = "yy/MM/dd HH:mm:ss(S)";
    public static String newline = System.getProperty("line.separator");

    private JavaPushMailLogger() {
    }

    private static void init() {
        if (logger == null)
            logger = new JavaPushMailLogger();
    }

    private void initWriter() throws IOException {
        if (writeFile == null)
            return;

        if (writeFile.exists())
            writeFile.delete();

        writer = new BufferedWriter(new FileWriter(writeFile));
    }

    private void write(String text, Exception e) {
        if (writeOut == false || writeFile == null || writer == null)
            return;

        try {
            writer.write(getCurrentTimestamp() + " @ " + text + newline);
            if (e != null) {
                writer.write(e.getMessage() + newline);
                StackTraceElement[] stack = e.getStackTrace();
                for (int i = 0; i < stack.length; i++) {
                    writer.write(stack[i].getClassName() + " @ " + stack[i].getMethodName() + ":" + stack[i].getLineNumber() + newline);
                }
            }

            writer.flush();

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    private void print(String text, Exception e, int messageLevel) {

        // if eg. level is set to INFO and message is DEBUG, don't show or print anything.
        if (level > messageLevel)
            return;

        System.err.println(getCurrentTimestamp() + " @ " + text);
        if (e != null)
            e.printStackTrace();

        if (writeOut)
            write(text, e);
    }

    private String getCurrentTimestamp() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
        return sdf.format(cal.getTime());

    }

    public static void setLevel(int level) {
        init();
        logger.level = level;
    }

    public static void setWriteFile(File f) {
        init();
        logger.writeFile = f;
        try {
            logger.initWriter();
        } catch (Exception e) {
        }
    }

    public static void setWriteToFile(boolean val) {
        init();
        logger.writeOut = val;
    }

    public static void debug(String text) {
        init();
        logger.print("[DEBUG]: " + text, null, 0);
    }

    public static void info(String text) {
        init();
        logger.print("[INFO]: " + text, null, 1);
    }

    public static void warn(String text) {
        init();
        logger.print("[WARN]: " + text, null, 2);
    }

    public static void error(String text) {
        init();
        logger.print("[ERROR]: " + text, null, 3);
    }

    public static void debug(String text, Exception e) {
        init();
        logger.print("[DEBUG]: " + text, e, 0);
    }

    public static void info(String text, Exception e) {
        init();
        logger.print("[INFO]: " + text, e, 1);
    }

    public static void warn(String text, Exception e) {
        init();
        logger.print("[WARN]: " + text, e, 2);
    }

    public static void error(String text, Exception e) {
        init();
        logger.print("[ERROR]: " + text, e, 3);
    }

    public static void debug(Exception e) {
        init();
        logger.print("[DEBUG]: ", e, 0);
    }

    public static void info(Exception e) {
        init();
        logger.print("[INFO]: ", e, 1);
    }

    public static void warn(Exception e) {
        init();
        logger.print("[WARN]: ", e, 2);
    }

    public static void error(Exception e) {
        init();
        logger.print("[ERROR]: ", e, 3);
    }

    public static boolean isWritingFile() {
        init();
        return logger.writeOut;
    }
}
