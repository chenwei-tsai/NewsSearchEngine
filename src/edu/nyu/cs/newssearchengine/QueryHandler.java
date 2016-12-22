package edu.nyu.cs.newssearchengine;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import edu.nyu.cs.newssearchengine.document.IndexedDocument;
import edu.nyu.cs.newssearchengine.document.ScoredDocument;
import edu.nyu.cs.newssearchengine.document.ScoredTerms;
import edu.nyu.cs.newssearchengine.query.QueryPhrase;
import edu.nyu.cs.newssearchengine.ranker.Ranker;
import edu.nyu.cs.newssearchengine.ranker.RankerFavorite;
import edu.nyu.cs.newssearchengine.utils.Logger;
import org.json.JSONArray;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 * Handles each incoming query, students do not need to change this class except
 * to provide more query time CGI arguments and the HTML output.
 * <p>
 * N.B. This class is not thread-safe.
 *
 * @author congyu
 * @author fdiaz
 */
public class QueryHandler implements HttpHandler {

  public static HtmlOutput htmlOutput = new HtmlOutput();

  private SearchEngine searchEngine;
  public QueryHandler(SearchEngine searchEngine) {
    this.searchEngine = searchEngine;
  }

  /**
   * CGI arguments provided by the user through the URL. This will determine
   * which Ranker to use and what output format to adopt. For simplicity, all
   * arguments are publicly accessible.
   */
  public static class CgiArguments {
    // The raw user query
    public String query = "";
    // How many results to return
    private int _numResults = 50;
    public int _numdocs;
    public int _numterms;

    // The type of the ranker we will be using.

    // The output format.
    public enum OutputFormat {
      TEXT,
      HTML,
    }

    public OutputFormat _outputFormat = OutputFormat.HTML;

    public CgiArguments(String uriQuery) {
      String[] params = uriQuery.split("&");
      for (String param : params) {
        String[] keyval = param.split("=", 2);
        if (keyval.length < 2) {
          continue;
        }
        String key = keyval[0].toLowerCase();
        String val = keyval[1];
        if (key.equals("query")) {
          query = val;
        } else if (key.equals("num")) {
          try {
            _numResults = Integer.parseInt(val);
          } catch (NumberFormatException e) {
            // Ignored, search engine should never fail upon invalid user input.
          }
        } else if (key.equals("format")) {
          try {
            _outputFormat = OutputFormat.valueOf(val.toUpperCase());
          } catch (IllegalArgumentException e) {
            // Ignored, search engine should never fail upon invalid user input.
          }
        } else if (key.equals("numdocs")) {
          try {
            _numdocs = Integer.parseInt(val);
          } catch (IllegalArgumentException e) {
            // Ignored, search engine should never fail upon invalid user input.
          }
        } else if (key.equals("numterms")) {
          try {
            _numterms = Integer.parseInt(val);
          } catch (IllegalArgumentException e) {
            // Ignored, search engine should never fail upon invalid user input.
          }
        }
      }  // End of iterating over params
    }
  }

  private void respondWithMsg(HttpExchange exchange, final String message)
      throws IOException {
    Headers responseHeaders = exchange.getResponseHeaders();
    responseHeaders.set("Content-Type", "text/plain");
    exchange.sendResponseHeaders(200, 0); // arbitrary number of bytes
    OutputStream responseBody = exchange.getResponseBody();
    responseBody.write(message.getBytes());
    responseBody.close();
  }

  private void respondWithHTML(HttpExchange exchange, final String message) throws IOException {
    Headers responseHeaders = exchange.getResponseHeaders();
    responseHeaders.set("Content-Type", "text/html");
    exchange.sendResponseHeaders(200, 0); // arbitrary number of bytes
    OutputStream responseBody = exchange.getResponseBody();
    responseBody.write(message.getBytes());
    responseBody.close();
  }

  private void constructTextOutput(
      final List<ScoredDocument> docs, StringBuffer response) {
    for (ScoredDocument doc : docs) {
      response.append(response.length() > 0 ? "\n" : "");
      response.append(doc.asTextResult());
    }
    response.append(response.length() > 0 ? "\n" : "");
  }

//  public static void constructHTMLOutput(
//      final List<ScoredDocument> docs, List<String> trends, StringBuffer response) {
//    htmlOutput.resultPage(docs, trends, response);
//  }

  private void constructTermOutput(
      final List<ScoredTerms> terms, StringBuffer response) {
    for (ScoredTerms term : terms) {
      response.append(response.length() > 0 ? "\n" : "");
      response.append(term.asTextResult());
    }
    response.append(response.length() > 0 ? "\n" : "");
  }

//  private void constructSearchBar(StringBuffer response) {
//    htmlOutput.homePage(response);
//  }

  public void handle(HttpExchange exchange) throws IOException {

    long threadId = Thread.currentThread().getId();
    System.out.println("[Server] Query hander thread Id " + threadId);

    String requestMethod = exchange.getRequestMethod();
    if (!requestMethod.equalsIgnoreCase("GET")) { // GET requests only.
      return;
    }

    // Print the user request header.
    Headers requestHeaders = exchange.getRequestHeaders();

    // Validate the incoming request.
    String uriQuery = exchange.getRequestURI().getQuery();
    String uriPath = exchange.getRequestURI().getPath();
    if (uriPath == null) {
      respondWithMsg(exchange, "Something wrong with the URI!");
    }

    if (uriPath.equals("/")) {
      StringBuffer response = new StringBuffer();
      htmlOutput.homePage(response);
      respondWithHTML(exchange, response.toString());
      return;
    }
    if (uriPath.equals("/auto")) {
      String[] queryString = uriQuery.split("=");
      if (queryString.length >= 2) {
        String autoCompleteQueryString = queryString[1];
        System.out.println("[QueryHandler] /auto " + autoCompleteQueryString);
        List<String> autoCompleteStrings = searchEngine.getAutoCompleteQueryStrings(autoCompleteQueryString);
        //System.out.println("[QueryHandler] /auto " + autoCompleteStrings.toString());
        if (autoCompleteStrings != null && autoCompleteStrings.size() != 0) {
          JSONArray jsonArray = new JSONArray();
          for (String string : autoCompleteStrings) {
            jsonArray.put(string);
          }
          String result = jsonArray.toString();
//          String testResult = "[\"new york\", \"new york city\", \"new york university\"]";
//          System.out.println(testResult);
          respondWithMsg(exchange, result);
        }
      }
      return;
    }
    System.out.println("[QueryHandler] /search");
    System.out.print("Incoming request: ");
    for (String key : requestHeaders.keySet()) {
      System.out.print(key + ":" + requestHeaders.get(key) + "; ");
    }
    System.out.println();
    System.out.println("Query: " + uriQuery);
    // Process the CGI arguments.
    CgiArguments cgiArgs = new CgiArguments(uriQuery);
    if (cgiArgs.query.isEmpty()) {
      respondWithMsg(exchange, "No query is given!");
    }
    StringBuffer response = new StringBuffer();
    // Processing the query.
    QueryPhrase processedQuery = new QueryPhrase(cgiArgs.query);
    List<String> trends = searchEngine.indexer.getTrendingWords();
    List<String> sections=searchEngine.indexer.getSectionList();

    try {
      if (uriPath.equals("/section")) {
        String section = uriQuery.split("=")[1];
        List<IndexedDocument> sectionDocs = searchEngine.indexer.getDocsBySection(section);

        List<String> sectionTrends = searchEngine.indexer.getTrendingWordsBySection(section);
        htmlOutput.sectionPage(sectionDocs, sectionTrends, sections, response, section);
      }


      if (uriPath.equals("/search")) {
        // Create the ranker.
        String queryString = uriQuery.split("=")[1];
        queryString = queryString.replaceAll("\\+", " ");
        Ranker ranker = new RankerFavorite(searchEngine.indexer);
        // Ranking.
        List<ScoredDocument> scoredDocs = ranker.runQuery(processedQuery, cgiArgs._numResults);
        switch (cgiArgs._outputFormat) {
          case TEXT:
            constructTextOutput(scoredDocs, response);
            break;
          case HTML:
            htmlOutput.resultPage(scoredDocs, trends, sections, response, queryString);
            break;
          default:
        }
      }
    } catch (Exception e) {
      Logger.log(e);
    }

    respondWithHTML(exchange, response.toString());
    System.out.println("Finished query: " + cgiArgs.query);
    return ;
  }
}

