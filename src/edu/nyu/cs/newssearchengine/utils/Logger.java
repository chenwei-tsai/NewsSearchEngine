package edu.nyu.cs.newssearchengine.utils;

import org.apache.commons.io.FileUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Logger {

  private static String filePath = "log.txt";

  static {
    File file = new File(filePath);
    if (!file.exists()) {
      try {
        file.createNewFile();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  private static void log(List<String> list) {
    list.add(0, new Date().toString());
    list.add("");
    try {
      FileUtils.writeLines(new File(filePath), list, true);
      for (String line : list) {
        System.err.println(line);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }


  public static void log(String message) {
    List<String> list = new ArrayList<>();
    list.add(message);
    try {
      FileUtils.writeLines(new File(filePath), list, true);
      for (String line : list) {
        System.out.println(line);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void log(Exception exception) {
    List<String> list = new ArrayList<>();
    list.add(exception.toString());
    for (StackTraceElement element : exception.getStackTrace()) {
      list.add(element.toString());
    }
    log(list);
  }
}
