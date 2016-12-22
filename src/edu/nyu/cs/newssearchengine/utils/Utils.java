package edu.nyu.cs.newssearchengine.utils;

import edu.nyu.cs.newssearchengine.SearchEngine;
import edu.nyu.cs.newssearchengine.document.RawDocument;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class Utils {

  public static List<String> parseTokensFromHTML(File file) {
    try {
      String[] lines = new String(Files.readAllBytes(file.toPath())).split("\\n");

      int count = 0;
      List<String> linesToParse = new ArrayList<>();
      for (String line : lines) {
        if (line.startsWith("<p>")) {
          count++;
          linesToParse.add(line);
        }
      }
      return parseTokensFromHTMLString(linesToParse);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  public static List<String> parseTokensFromHTMLString(List<String> lines) {
    List<String> result = new ArrayList<>();
    for (String line : lines) {
      String[] arr = line.split("[^A-Za-z0-9]");
      for (String token : arr) {
        if (!token.isEmpty()) {
          result.add(token);
        }
      }
    }
    return result;
  }

  public static List<String> parseTokensFromString(String body) {
    List<String> result = new ArrayList<>();
    String[] arr = body.split("[^A-Za-z0-9]");
    for (String token : arr) {
      String processed = processTerm(token);
      if (processed != null && !processed.isEmpty()) {
        result.add(processed);
      }
    }
    return result;
  }

  public static String processTerm(String term) {
    term = term.toLowerCase();
    if (StopWords.isStopWord(term)) {
      return null;
    }
    Stemmer stemmer = new Stemmer();
    stemmer.add(term.toCharArray(), term.length());
    stemmer.fullstem();
    String stemed = stemmer.toString();
    if (StopWords.isStopWord(term)) {
      return null;
    }
    return stemed;
  }

  public static void archiveRawDocument(RawDocument document) {
    Path source = Paths.get(SearchEngine.getDataFolder()+"/rawDocuments/"+document.title);
    SimpleDateFormat dt1 = new SimpleDateFormat("yyyy-MM-dd");
    String dateString = dt1.format(document.date);
    File file = new File(SearchEngine.getDataFolder()+"/archive/"+dateString);
    if (!file.exists()) {
      file.mkdir();
    }
    Path target = Paths.get(SearchEngine.getDataFolder()+"/archive/"+dateString+"/"+document.title);

    try {
      //if (SearchEngine.TESTING) {
     //   Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
      //} else {
        Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
      //}
    } catch (IOException e) {
      Logger.log(e);
    }

  }

  public static void suspend(int second) {
    try {
      Thread.sleep(second * 1000);
    } catch (InterruptedException e) {
      Logger.log(e);
    }
  }
}
