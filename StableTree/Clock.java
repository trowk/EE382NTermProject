package StableTree;

import java.io.Serializable;

public class Clock implements Serializable {
    int pid;
    int clock;

    public Clock(int _pid, int _clock) {
        pid = _pid;
        clock = _clock;
    }

    public int compareTo(Clock c) {
        return clock == c.clock ? pid - c.pid : clock - c.clock;
    }
}
