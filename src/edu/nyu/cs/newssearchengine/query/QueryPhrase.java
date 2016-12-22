package edu.nyu.cs.newssearchengine.query;

import edu.nyu.cs.newssearchengine.utils.Utils;

import java.io.IOException;
import java.util.*;

/**
 * @CS2580: implement this class for HW2 to handle phrase. If the raw query is
 * ["new york city"], the presence of the phrase "new york city" must be
 * recorded here and be used in indexing and ranking.
 * <p>
 * Supposing input query is
 * "the \"new york university\" is the \"best university\" in us"
 * <p>
 * After processed, the phrases should be:
 * Set {
 * [the],
 * [new, york, university],
 * [is],
 * [best, university],
 * [in],
 * [us],
 * }
 */
public class QueryPhrase {
  private List<List<String>> phrases = new ArrayList<>();
  private String query;
  private List<String> tokens = new ArrayList<>();

  public QueryPhrase(String query) {
    this.query = query;
    processQuery();
  }

  public List<List<String>> getPhrases() {
    return new ArrayList<>(phrases);
  }

  private void processQuery() {
    if (query == null) {
      return;
    }
    int current = -1;
    int last = -1;
    boolean isInPhrase = true;
    while ((current = query.indexOf('\"', current + 1)) != -1) {
      String content = query.substring(last + 1, current).trim();

      if (isInPhrase) {
        addPhrase(content);
      } else {
        addTokens(content);
      }
      last = current;
    }
    String content = query.substring(last + 1).trim();
    addTokens(content);
  }

  private void addTokens(String content) {
    if (content.isEmpty()) return;
    List<String> arr = Utils.parseTokensFromString(content);
    for (String token: arr) {
      addPhrase(token);
    }
  }

  private void addPhrase(String content) {
    if (content.isEmpty()) return;
    List<String> arr = Utils.parseTokensFromString(content);
    List<String> list = new ArrayList<>();
    for (String token : arr) {
      list.add(token);
      tokens.add(token);
    }
    phrases.add(list);
  }


  public List<String> getTokens() {
    return new ArrayList<>(tokens);
  }

  public Set<String> getTokenSet() {
    return new HashSet<>(tokens);
  }

  public static void main(String[] args) {
    QueryPhrase queryPhrase = new QueryPhrase("solution is");
    System.out.println(queryPhrase.getPhrases());
    System.out.println(queryPhrase.getTokens());
  }
}
