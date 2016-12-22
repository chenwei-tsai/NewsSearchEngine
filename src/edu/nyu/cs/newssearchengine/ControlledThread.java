package edu.nyu.cs.newssearchengine;

public class ControlledThread extends Thread {
  protected boolean running = true;

  public ControlledThread() {
    super();
  }

  public void stopRunning() {
    running = false;
  }
}
