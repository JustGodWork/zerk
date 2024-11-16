package com.justgod.zerk.utils;

public class Timeout {

    public static Thread setTimeout(Runnable runnable, int delay) {
        Thread thread = new Thread(() -> {
            try {
                Thread.sleep(delay);
                runnable.run();
            } catch (Exception e) {
                System.err.println(e);
            }
        });
        thread.start();
        return thread;
    }

    public static void clearTimeout() {
        Thread thread = Thread.currentThread();
        if (thread != null) {
            thread.interrupt();
        }
    }

    public static void clearTimeout(Thread thread) {
        thread.interrupt();
    }

    public static Thread setInterval(Runnable runnable, int delay) {
        Thread thread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(delay);
                    runnable.run();
                } catch (Exception e) {
                    clearInterval();
                    break;
                }
            }
        });
        thread.start();
        return thread;
    }

    public static void clearInterval() {
        Thread thread = Thread.currentThread();
        if (thread != null) {
            thread.interrupt();
        }
    }

    public static void clearInterval(Thread thread) {
        thread.interrupt();
    }

};
