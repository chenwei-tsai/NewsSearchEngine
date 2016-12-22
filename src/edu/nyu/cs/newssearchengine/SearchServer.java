package edu.nyu.cs.newssearchengine;

import com.sun.net.httpserver.HttpServer;
import edu.nyu.cs.newssearchengine.utils.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SearchServer extends ControlledThread {

  SearchEngine searchEngine;

  public SearchServer(SearchEngine searchEngine) {
    this.searchEngine = searchEngine;
  }

  @Override
  public void run() {

    QueryHandler handler = new QueryHandler(searchEngine);
    InetSocketAddress addr = new InetSocketAddress(searchEngine.PORT);
    HttpServer server;
    try {
      server = HttpServer.create(addr, -1);
    } catch (IOException e) {
      Logger.log(e);
      return;
    }
    server.createContext("/", handler);
    ExecutorService executorService = Executors.newFixedThreadPool(16);
    server.setExecutor(executorService);
    server.start();
    System.out.println("Listening on port: " + Integer.toString(searchEngine.PORT));
  }
}
