package sn.sn.Basic;

import static sn.sn.Basic.Other.sendDebug;
import static sn.sn.Sn.tps;

public class Lag implements Runnable {

    public static int TICK_COUNT = 0;
    public static final long[] TICKS = new long[600];
    public static long LAST_TICK = 0L;

    public static void getTPS()
    {
        tps = getTPS(100);
        sendDebug("tps = "+ tps);
    }

    public static double getTPS(int ticks)
    {
        if (TICK_COUNT< ticks) {
            return 20.0D;
        }
        int target = (TICK_COUNT- 1 - ticks) % TICKS.length;
        long elapsed = System.currentTimeMillis() - TICKS[target];

        return ticks / (elapsed / 1000.0D);
    }

    public void run()
    {
        TICKS[(TICK_COUNT% TICKS.length)] = System.currentTimeMillis();

        TICK_COUNT+= 1;
    }
}