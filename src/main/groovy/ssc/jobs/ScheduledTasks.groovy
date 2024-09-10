package ssc.jobs

import java.sql.Time
import java.text.SimpleDateFormat
import java.util.TimerTask
import java.text.SimpleDateFormat

/**
 * Created by schohan on 11/9/2015.
 */
public class ScheduledTask extends TimerTask {
    private Closure runClosure
    private Timer timer

    /* avoid construction of this object using default constructor*/
    private ScheduledTask() {
        throw new Exception("Cannot use this constructor")
    }

    /* Construct a timer with the execution closure passed in as argument */
    public ScheduledTask(String name, long delay, long interval, Closure executionClosure) {
        timer = new Timer(name, true) // Create all timers as Daemons
        runClosure = executionClosure
        timer.schedule(this, delay, interval)
    }

    /* Purge timer */
    public void stop() {
        timer.cancel()
        timer.purge()
    }

    /* Execute the closure that was passed while constructing this object */
    public void run() {
        runClosure.call()
    }





    // Test driver
    public static void main(String[] args) {
        ScheduledTask st = new ScheduledTask("test1",0,2000, {println("test1")}); // Instantiate SheduledTask class
        ScheduledTask st2 = new ScheduledTask("test2",0,2000, {println("test2")});

        try {Thread.sleep(10000)}catch (Exception e) {e.printStackTrace()}
        print("Stopping st")
        st.stop()
        print("st Stopped")
        try {Thread.sleep(50000)}catch (Exception e) {e.printStackTrace()}
    }
}