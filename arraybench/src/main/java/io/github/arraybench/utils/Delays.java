package io.github.arraybench.utils;

public class Delays {
    public double getDisplayedDelay() { return 0; }
    public void setDisplayedDelay(double value) {}
    public void setCurrentDelay(double value) {}
    public void updateCurrentDelay(double oldRatio, double newRatio) {}
    public void updateDelayForTimeSort(double value) {}
    public double getSleepRatio() { return 0; }
    public void setSleepRatio(double sleepRatio) {}
    public boolean skipped() { return false; }
    public void changeSkipped(boolean Bool) {}
    public boolean paused() { return false; }
    public void changePaused(boolean Bool) {}
    public void togglePaused() {}
    public void sleep(double millis) {}
}
