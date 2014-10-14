package soa.speech.evaluator.monitor;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.joda.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessingTimeReviewer {

    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    Map<String, FileTimer> processTime = new HashMap<>();
    Map<String, Double> overalTimeMap = Collections.synchronizedMap(new HashMap<String, Double>());

    public Map<String, Double> getOveralTimeMap() {
        return overalTimeMap;
    }

    public double getOveralTimer(String timerName) {
        return overalTimeMap.get(timerName);
    }

    public void startProcessing(String identifier, String timerName) {

        long time = System.currentTimeMillis();
        FileTimer t = new FileTimer();
        t.setStart(time);
        processTime.put(identifier, t);
    }

    public void startProcessing(String identifier, String timerName, String size) {

        long time = System.currentTimeMillis();
        FileTimer t = new FileTimer();
        t.setStart(time);
        t.setSize(Long.getLong(size));
        processTime.put(identifier, t);
    }

    public void endProcessing(String identifier, String timerName) {

        long end = System.currentTimeMillis();
        FileTimer timing = processTime.get(identifier);
        timing.setEnd(end);
        long durationInMillis = timing.durationInMillis();

        synchronized (overalTimeMap) {
            if (!overalTimeMap.containsKey(timerName)) {
                overalTimeMap.put(timerName, Double.valueOf(""+durationInMillis));
            } else {
                double sum = overalTimeMap.get(timerName) + durationInMillis;
                overalTimeMap.put(timerName, sum);
            }
        }
        logger.info(identifier + " took " + durationInMillis + " millis. Overal " + timerName + " process time " + overalTimeMap.get(timerName));
    }

    abstract class Timer {
        Long start;
        Long end;

        public long durationInMillis() {
            if (end == null || start == null) {
                return 0L;
            }
            Duration d = new Duration(start, end);
            return d.getMillis();
        }

        public Long getStart() {
            return start;
        }

        public void setStart(Long start) {
            this.start = start;
        }

        public Long getEnd() {
            return end;
        }

        public void setEnd(Long end) {
            this.end = end;
        }
    }

    class FileTimer extends Timer {

        Long size;

        public Long getSize() {
            return size;
        }

        public void setSize(Long filesize) {
            this.size = filesize;
        }
    }
}
