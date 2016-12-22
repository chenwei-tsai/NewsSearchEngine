package edu.nyu.cs.newssearchengine.query;

import edu.nyu.cs.newssearchengine.indexer.Indexer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class IndexedQuery {

  private List<List<Integer>> indexedPhrases = new ArrayList<>();
  private List<Integer> indexedTokens = new ArrayList<>();

  public IndexedQuery(QueryPhrase query, Indexer indexer) {
    for (List<String> phrase: query.getPhrases()) {
      List<Integer> list = new ArrayList<>();
      for (String token: phrase) {
        Integer index = indexer.termHandler.getIndexOfOriginalToken(token);
        if (index != null) {
          list.add(index);
        }
      }
      indexedPhrases.add(list);
    }
    for (String token: query.getTokens()) {
      indexedTokens.add(indexer.termHandler.getIndexOfOriginalToken(token));
    }
  }

  public Set<Integer> getIndexedTokenSet() {
    return new HashSet<>(indexedTokens);
  }

  public List<List<Integer>> getIndexedPhrases() {
    return new ArrayList<>(indexedPhrases);
  }
}
