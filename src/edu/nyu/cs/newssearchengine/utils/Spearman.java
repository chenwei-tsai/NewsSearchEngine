package edu.nyu.cs.newssearchengine.utils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * Created by Barry on 11/17/16.
 */
public class Spearman {

  private static final String SEPERATOR = "\t";
  private static final String README_FILE = "readme_spearman.txt";

  private static final boolean IS_DEBUG = false;

  public static void main(String[] args) {

    Map<String, DocumentItem> documentItemMap = new HashMap<>();
    List<DocumentItem> documentItemList = new ArrayList<>();

    check(args.length == 2, "the correct usage is: java edu.nyu.cs.newssearchengine.Spearman <PATH-TO-PAGERANKS> <PATH-TO-NUMVIEWS>");
    String pageRankPath = args[0];
    String numViewPath = args[1];

    try {
      List<String> pageRankLines = Files.readAllLines(Paths.get(pageRankPath), Charset.defaultCharset());
      for (String line : pageRankLines) {
        String[] arr = line.split(SEPERATOR);
        documentItemMap.put(arr[0], new DocumentItem(arr[0], Float.parseFloat(arr[1]), 0));
      }

      List<String> numViewLines = Files.readAllLines(Paths.get(numViewPath), Charset.defaultCharset());
      for (String line : numViewLines) {
        String[] arr = line.split(SEPERATOR);
        DocumentItem documentItem = documentItemMap.get(arr[0]);
        if (documentItem != null) {
          documentItem.numView = Integer.parseInt(arr[1]);
        }
      }

      for (Map.Entry<String, DocumentItem> entry : documentItemMap.entrySet()) {
        documentItemList.add(entry.getValue());
      }

      Collections.sort(documentItemList, pageRankComparator);

      int i = 1;
      for (DocumentItem documentItem : documentItemList) {
        documentItem.x = i;
        i++;
        debugPrint(documentItem.documentName + " " + documentItem.pageRank + " " + documentItem.x);
      }

      Collections.sort(documentItemList, numViewComparator);

      i = 1;
      for (DocumentItem documentItem : documentItemList) {
        documentItem.y = i;
        i++;
        debugPrint(documentItem.documentName + " " + documentItem.numView + " " + documentItem.y);
      }

      int n = documentItemList.size();
      double xSum = 0.0d;
      for (DocumentItem documentItem : documentItemList) {
        xSum = xSum + documentItem.x;
      }
      double z = xSum / n;

      double numerator = 0.0d;
      double denominatorX = 0.0d;
      double denominatorY = 0.0d;

      for (DocumentItem documentItem : documentItemList) {
        numerator += (documentItem.x - z) * (documentItem.y - z);
        denominatorX += (documentItem.x - z) * (documentItem.x - z);
        denominatorY += (documentItem.y - z) * (documentItem.y - z);
      }

      double coefficient = numerator / Math.sqrt(denominatorX * denominatorY);
      System.out.println("The Spearman's rank correlation coefficient is " + coefficient);

      List<String> outputLines = new ArrayList<>();
      outputLines.add(String.valueOf(coefficient));
      Files.write(Paths.get(README_FILE), outputLines, Charset.defaultCharset());

    } catch (IOException ex) {
      System.err.println("Fatal error: " + "the file path you provided is not correct");
      System.exit(-1);
    } catch (NumberFormatException ex) {
      System.err.println("Fatal error: " + "your input file format is not correct");
      System.exit(-1);
    }

  }

  public static void check(boolean condition, String msg) {
    if (!condition) {
      System.err.println("Fatal error: " + msg);
      System.exit(-1);
    }
  }

  static class DocumentItem {

    String documentName;
    float pageRank;
    int numView;
    int x;
    int y;

    DocumentItem(String documentName, float pageRank, int numView) {
      this.documentName = documentName;
      this.pageRank = pageRank;
      this.numView = numView;
    }
  }

  static Comparator<DocumentItem> pageRankComparator = new Comparator<DocumentItem>() {
    @Override
    public int compare(DocumentItem item1, DocumentItem item2) {
      if (item1.pageRank > item2.pageRank) {
        return -1;
      } else if (item1.pageRank == item2.pageRank) {
        return 0;
      } else{
        return 1;
      }
    }
  };

  static Comparator<DocumentItem> numViewComparator = new Comparator<DocumentItem>() {
    @Override
    public int compare(DocumentItem item1, DocumentItem item2) {
      if (item1.numView > item2.numView) {
        return -1;
      } else if (item1.numView == item2.numView) {
        return 0;
      } else {
        return 1;
      }
    }
  };

  static void debugPrint(String debugMessage) {
    if (IS_DEBUG) {
      System.out.println(debugMessage);
    }
  }

}
