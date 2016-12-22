package edu.nyu.cs.newssearchengine.utils;

public class Timer {
  private long last;

  public Timer() {
    reset();
  }

  public long timeInMillis() {
    return System.currentTimeMillis() - last;
  }

  public String getTime() {
    return "" + (System.currentTimeMillis() - last) / 1000.0 + "s";
  }

  public void reset() {
    this.last = System.currentTimeMillis();
  }
}
