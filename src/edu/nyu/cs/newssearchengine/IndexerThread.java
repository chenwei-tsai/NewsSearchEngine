package edu.nyu.cs.newssearchengine;

import edu.nyu.cs.newssearchengine.crawler.Crawler;
import edu.nyu.cs.newssearchengine.crawler.NYTimesCrawler;
import edu.nyu.cs.newssearchengine.indexer.AutoCompleteLibrary;
import edu.nyu.cs.newssearchengine.indexer.DynamicIndexer;
import edu.nyu.cs.newssearchengine.indexer.Indexer;
import edu.nyu.cs.newssearchengine.query.IndexedQuery;
import edu.nyu.cs.newssearchengine.query.QueryPhrase;
import edu.nyu.cs.newssearchengine.utils.Configuration;
import edu.nyu.cs.newssearchengine.utils.Logger;
import edu.nyu.cs.newssearchengine.utils.Timer;
import edu.nyu.cs.newssearchengine.utils.Utils;

public class IndexerThread extends ControlledThread {

  private SearchEngine searchEngine;

  private Crawler crawler;

  private Timer timer = new Timer();

  public IndexerThread(SearchEngine searchEngine) {
    this.searchEngine = searchEngine;
    crawler = new NYTimesCrawler();
  }

  @Override
  public void run() {

    if (Configuration.BUILD_INDEXER_FROM_ARCHIVE) {
      Logger.log("[Indexer] Build indexer from archive");
      timer.reset();
      searchEngine.indexer = DynamicIndexer.loadFromArchive();
      Logger.log("[Indexer] Indexer built from archive " + timer.getTime());
    } else
    try {
      // Loads the previous version indexer
      System.out.println("[Indexer] Load the indexer from previous saved version");
      timer.reset();
      searchEngine.indexer = DynamicIndexer.load();
      Logger.log("[Indexer] Indexer loaded from previous saved version " + timer.getTime());
    } catch (Exception e) {
      Logger.log(e);
      Logger.log("[Indexer] Failed to load indexer from previous object files, build the indexer from archive");
      // Rebuilds the indexer from the archived documents
      timer.reset();
      searchEngine.indexer = DynamicIndexer.loadFromArchive();
      Logger.log("[Indexer] Indexer built from archive " + timer.getTime());
    }

    searchEngine.autoCompleteLibrary = new AutoCompleteLibrary(searchEngine.indexer.nGramLanguageModel);
    searchEngine.autoCompleteLibrary.buildAutoCompleteLibrary();
    searchEngine.indexer.termHandler.trendingHandler.update();

    long roundCount = 1;
    while (searchEngine.REAL_Time) {
      try {
        Logger.log("[Indexer] Indexer thread round " + roundCount);
        crawler.doCrawl();
        boolean shouldUpdate = searchEngine.indexer.constructIndexFromTemp();
        if (shouldUpdate) {
          // todo: adjust the dump position
          searchEngine.indexer.dump();
          searchEngine.indexer.termHandler.trendingHandler.update();
          searchEngine.autoCompleteLibrary.buildAutoCompleteLibrary();
        }
        Utils.suspend(120);
        roundCount++;
      } catch (Exception e) {
        Logger.log("[Indexer] Indexer thread crash; keep looping");
        Logger.log(e);
      }
    }
  }

  public static void main(String[] args) throws InterruptedException {

    IndexerThread indexerThread = new IndexerThread(new SearchEngine());
    indexerThread.start();

    indexerThread.join();
    Indexer indexer = indexerThread.searchEngine.indexer;
    QueryPhrase query = new QueryPhrase("France");
    IndexedQuery indexedQuery = new IndexedQuery(query, indexer);
    System.out.println(indexer.nextDoc(indexedQuery, -1));
  }
}
