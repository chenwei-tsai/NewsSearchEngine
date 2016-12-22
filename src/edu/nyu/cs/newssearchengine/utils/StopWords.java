package edu.nyu.cs.newssearchengine.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Vane on 11/18/16.
 */
public class StopWords {
  public static Set<String> stopWords = new HashSet<>();

  static {
    try {
      BufferedReader bufferedReader = new BufferedReader(new FileReader("corpus/stoplist.txt"));
      String word = "";
      while ((word = bufferedReader.readLine()) != null) {
        stopWords.add(word.trim());
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void filterOutStopWords(List<String> vocabulary) {
    vocabulary.removeAll(stopWords);
  }

  public static boolean isStopWord(String word) {
    return stopWords.contains(word);
  }
}
