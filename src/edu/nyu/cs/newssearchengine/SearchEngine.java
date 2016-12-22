package edu.nyu.cs.newssearchengine;

import edu.nyu.cs.newssearchengine.indexer.AutoCompleteLibrary;
import edu.nyu.cs.newssearchengine.indexer.DynamicIndexer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SearchEngine {

  public static boolean TESTING = false;
  public static boolean REAL_Time = true;

  public static String SERVER_HOST = "newyorktimes.eastus.cloudapp.azure.com";
  public static String LOCAL_HOST = "localhost";

  public static String HOST;
  public static int PORT = 23456;

  static {
    HOST = TESTING ? LOCAL_HOST : SERVER_HOST;
  }

  public static String getDataFolder() {
    return SearchEngine.TESTING ? "testData" : "data";
  }

  public DynamicIndexer indexer;

  AutoCompleteLibrary autoCompleteLibrary;

  /**
   * Prints {@code msg} and exits the program if {@code condition} is false.
   */
  public void Check(boolean condition, String msg) {
    if (!condition) {
      System.err.println("Fatal error: " + msg);
      System.exit(-1);
    }
  }

  public List<String> getAutoCompleteQueryStrings(String query) {
    if (autoCompleteLibrary != null) {
      return autoCompleteLibrary.getAutoCompleteStrings(query);
    }
    return new ArrayList<>();
  }

  private void parseCommandLine(String[] args) {
    for (String arg : args) {
      if (arg.equals("--test")) {
        SearchEngine.TESTING = true;
      }

      if (arg.equals("--offline")) {
        SearchEngine.REAL_Time = false;
      }
    }
    HOST = TESTING ? LOCAL_HOST : SERVER_HOST;

  }

  private void startCrawlingAndIndexing() {
    IndexerThread indexerThread = new IndexerThread(this);
    indexerThread.start();
  }

  private void startServing() {
    SearchServer server = new SearchServer(this);
    server.start();
  }

  private void loadIndex() throws IOException, ClassNotFoundException {
    indexer = DynamicIndexer.load();
  }
  
  public static void main(String[] args) throws IOException,
    ClassNotFoundException {
    SearchEngine searchEngine = new SearchEngine();
    searchEngine.parseCommandLine(args);

    searchEngine.startCrawlingAndIndexing();
    //searchEngine.loadIndex();
    searchEngine.startServing();
  }

}
