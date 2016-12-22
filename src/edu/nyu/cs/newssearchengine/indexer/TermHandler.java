package edu.nyu.cs.newssearchengine.indexer;

import edu.nyu.cs.newssearchengine.document.RawDocument;
import edu.nyu.cs.newssearchengine.utils.StopWords;
import edu.nyu.cs.newssearchengine.utils.Utils;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class TermHandler implements Serializable {

  private static final long serialVersionUID = 3L;

  protected long totalTermFrequency = 0;
  protected Map<Integer, Integer> termCorpusFrequency = new ConcurrentHashMap<>();
  protected Map<String, Integer> dictionary = new ConcurrentHashMap<>();

  public TrendingHandler trendingHandler = new TrendingHandler();
  public List<String> uniqueTerms = new ArrayList<>();

  public TermHandler() {
  }

  public List<Integer> handle(RawDocument document) {
    List<String> tokens = Utils.parseTokensFromString(document.body);
    tokens.addAll(Utils.parseTokensFromString(document.title));
    trendingHandler.addDocument(document);

    StopWords.filterOutStopWords(tokens);
    for (int i = 0; i < tokens.size(); i++) {
      String token = tokens.get(i);
      tokens.set(i, Utils.processTerm(token));
    }
    StopWords.filterOutStopWords(tokens);
    List<Integer> indexes = readTermVector(tokens);
    for (Integer idx : indexes) {
      termCorpusFrequency.put(idx, termCorpusFrequency.get(idx) + 1);
      ++totalTermFrequency;
    }
    return indexes;
  }

  public int getTermFrequency(String term) {
    return termCorpusFrequency.containsKey(term)
      ? termCorpusFrequency.get(term) : 0;
  }

  private List<Integer> readTermVector(List<String> tokens) {
    List<Integer> tokenIndexes = new ArrayList<>();
    for (String token : tokens) {
      token = token.toLowerCase();
      int index;
      if (dictionary.containsKey(token)) {
        index = dictionary.get(token);
      } else {
        index = uniqueTerms.size();
        uniqueTerms.add(token);
        dictionary.put(token, index);
        termCorpusFrequency.put(index, 0);
      }
      tokenIndexes.add(index);
    }
    return tokenIndexes;
  }

  /**
   * Convert from term index to term string
   * @param tokens
   * @return
   */
  public Set<String> getTermVector(Set<Integer> tokens) {
    Set<String> retval = new HashSet<>();
    for (int idx : tokens) {
      retval.add(uniqueTerms.get(idx));
    }
    return retval;
  }

  public Set<Integer> getIndexes(Set<String> tokens) {
    Set<Integer> indexes = new HashSet<>();
    for (String token : tokens) {
      indexes.add(dictionary.get(token));
    }
    return indexes;
  }


  public Integer getIndex(String processed) {
    return dictionary.get(processed);
  }

  public Integer getIndexOfOriginalToken(String token) {
    return dictionary.get(Utils.processTerm(token));
  }
}
