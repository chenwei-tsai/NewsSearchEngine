package edu.nyu.cs.newssearchengine.indexer;

import edu.nyu.cs.newssearchengine.document.RawDocument;
import edu.nyu.cs.newssearchengine.utils.StopWords;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Chen-Wei Tsai on 12/2/16.
 */
public class NGramLanguageModel implements Serializable {

  private static final long serialVersionUID = 3L;

  private static final int N = 5;
  private static final int AUTO_COMPLETE_REPLY_LIST_LENGTH = 5;

  public Map<String, Integer>[] phraseOccurrenceMaps = new Map[N];
  private Map<String, PriorityQueue<HeapNode>>[] autoCompleteLibrary = new Map[N];

  class HeapNode implements Serializable {

    private static final long serialVersionUID = 3L;

    String prefix;
    String lastTerm;
    int count;

    public HeapNode(String prefix, String lastTerm, int count) {
      this.prefix = prefix;
      this.lastTerm = lastTerm;
      this.count = count;
    }

    public String getAutoCompleteString() {
      return prefix + " " + lastTerm;
    }
  }

  class MinHeapComparator implements Comparator<HeapNode>, Serializable {
    @Override
    public int compare(HeapNode o1, HeapNode o2) {
      return o1.count - o2.count;
    }
  }

  private Comparator<HeapNode> comparator = new MinHeapComparator();

  public NGramLanguageModel() {
    initiatePhraseOccurrenceMaps();
    initiateAutoCompleteLibrary();
  }

  private void initiatePhraseOccurrenceMaps() {
    for (int i = 0; i < N; ++i) {
      // phraseOccurrenceMaps[0] for 1-Gram model
      // phraseOccurrenceMaps[1] for 2-Gram model
      phraseOccurrenceMaps[i] = new ConcurrentHashMap<>();
    }
  }

  private void initiateAutoCompleteLibrary() {
    for (int i = 0; i < N; ++i) {
      // autoCompleteLibrary[0] for 1-Gram model
      // autoCompleteLibrary[1] for 2-Gram model
      autoCompleteLibrary[i] = new ConcurrentHashMap<>();
    }
  }

  public void handle(RawDocument document) {
    produceNGramStatistics(document.body);
  }

  private void produceNGramStatistics(String text) {

    String sterilizedText = text.toLowerCase().trim();

    sterilizedText = sterilizedText.replaceAll("\n", " ");
    sterilizedText = sterilizedText.replaceAll("\t", " ");
    sterilizedText = sterilizedText.replaceAll("mr\\.", "mr");
    sterilizedText = sterilizedText.replaceAll("ms\\.", "ms");
    sterilizedText = sterilizedText.replaceAll("dr\\.", "dr");
    sterilizedText = sterilizedText.replaceAll("j\\.", "j");
    sterilizedText = sterilizedText.replaceAll("u\\.s\\.", "us");
    sterilizedText = sterilizedText.replaceAll("b\\.c\\.", "bc");
    sterilizedText = sterilizedText.replaceAll("n\\.d\\.", "nd");
    sterilizedText = sterilizedText.replaceAll("\"", "");
    sterilizedText = sterilizedText.replaceAll(",", " ");

    String[] sentences = sterilizedText.split("\\.");

    for (String sentence : sentences) {
      sentence = sentence.trim();
      // remove all non-alphabet characters
      sentence = sentence.replaceAll("[^a-z]", " ").trim();
      sentence = sentence.replaceAll(" s ", " ");
      String[] words = sentence.split("\\s+");

//      Stemmer stemmer = new Stemmer();
      List<String> tokens = new ArrayList<>(Arrays.asList(words));
      StopWords.filterOutStopWords(tokens);
//      for (int i = 0; i < tokens.size(); i++) {
//        String token = tokens.get(i);
//        token = token.toLowerCase();
//        stemmer.add(token.toCharArray(), token.length());
//        stemmer.fullstem();
//        token = stemmer.toString();
//        tokens.set(i, token);
//      }
//      StopWords.filterOutStopWords(tokens);
      String[] newWords = new String[tokens.size()];
      words = tokens.toArray(newWords);

      for (int i = 0; i < words.length - 1; ++i) {
        StringBuilder stringBuilder = new StringBuilder();
//        if (words[i].equals("null")) {
//          System.out.println("here");
//        }
        stringBuilder.append(words[i]);
        for (int j = 1; i + j < words.length && j < N; ++j) {
          stringBuilder.append(" ");
          stringBuilder.append(words[i + j]);
          String stringToBeAdded = stringBuilder.toString().trim();
          if (phraseOccurrenceMaps[j].containsKey(stringToBeAdded)) {
            phraseOccurrenceMaps[j].put(stringToBeAdded, phraseOccurrenceMaps[j].get(stringToBeAdded) + 1);
          } else {
            phraseOccurrenceMaps[j].put(stringBuilder.toString().trim(), 1);
          }
        }
      }

    }
  }

  public void buildAutoCompleteLibrary() {

    System.err.println("Build auto complete library");
    initiateAutoCompleteLibrary();
    for (int nGram = 2; nGram <= 5; ++nGram) {
      Map<String, Integer> phraseOccurrenceMap = phraseOccurrenceMaps[nGram - 1];
      for (Map.Entry<String, Integer> entry : phraseOccurrenceMap.entrySet()) {
        String[] terms = entry.getKey().trim().split(" ");
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(terms[0]);
        for (int i = 1; i < terms.length - 1; ++i) {
          stringBuilder.append(" ");
          stringBuilder.append(terms[i]);
        }
        String prefix = stringBuilder.toString().trim();
        String lastTerm = terms[terms.length - 1];
        HeapNode heapNode = new HeapNode(prefix, lastTerm, entry.getValue());

        if (autoCompleteLibrary[nGram - 1].containsKey(prefix)) {
          PriorityQueue<HeapNode> minHeap = autoCompleteLibrary[nGram - 1].get(prefix);
          maintainTopK(minHeap, AUTO_COMPLETE_REPLY_LIST_LENGTH, heapNode);
        } else {
          PriorityQueue<HeapNode> minHeap = new PriorityQueue<>(AUTO_COMPLETE_REPLY_LIST_LENGTH, comparator);
          maintainTopK(minHeap, AUTO_COMPLETE_REPLY_LIST_LENGTH, heapNode);
          autoCompleteLibrary[nGram - 1].put(prefix, minHeap);
        }
      }
    }
  }

  private void maintainTopK(PriorityQueue<HeapNode> minHeap, final int k, HeapNode newNode) {
    if (minHeap == null || newNode == null) {
      return;
    }
    if (minHeap.size() < k) {
      minHeap.add(newNode);
    } else {
      HeapNode lowestValueNode = minHeap.peek();
      if (comparator.compare(newNode, lowestValueNode) > 0) {
        minHeap.poll();
        minHeap.add(newNode);
      }
    }
  }


  public List<String> getAutoCompleteStrings(String query) {
    List<String> returnList = new ArrayList<>();
    if (query == null || query.length() == 0) {
      return returnList;
    }
    String[] words = query.trim().split("\\s+");
    if (words.length >= N) {
      return returnList;
    } else {
      int nGram = words.length;
      StringBuilder stringBuilder = new StringBuilder();
      for (int i = 0; i < nGram; ++i) {
        stringBuilder.append(words[i]);
        stringBuilder.append(" ");
      }
      String prefix = stringBuilder.toString().trim();
      if (autoCompleteLibrary[nGram].containsKey(prefix)) {
        PriorityQueue<HeapNode> minHeap = autoCompleteLibrary[nGram].get(prefix);
        List<HeapNode> tempNodeList = new ArrayList<>();
        while (minHeap.size() != 0) {
          HeapNode node = minHeap.poll();
          returnList.add(node.getAutoCompleteString());
          tempNodeList.add(node);
        }
        Collections.reverse(returnList);
        for (HeapNode heapNode : tempNodeList) {
          minHeap.add(heapNode);
        }
        return returnList;
      } else {
        return returnList;
      }
    }
  }

}
