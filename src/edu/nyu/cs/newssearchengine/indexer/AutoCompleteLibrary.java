package edu.nyu.cs.newssearchengine.indexer;

import edu.nyu.cs.newssearchengine.utils.Timer;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static edu.nyu.cs.newssearchengine.utils.Configuration.AUTO_COMPLETE_REPLY_LIST_LENGTH;

/**
 * Created by Chen-Wei Tsai on 12/8/16.
 */
public class AutoCompleteLibrary {

  private ConcurrentHashMap<String, List<String>> autoCompleteLibrary = createNewAutoCompleteLibrary();

  private Comparator<HeapNode> minHeapComparator = new MinHeapComparator();

  NGramLanguageModel nGramLanguageModel;

  public AutoCompleteLibrary(NGramLanguageModel nGramLanguageModel) {
    this.nGramLanguageModel = nGramLanguageModel;
  }

  class HeapNode implements Serializable, Comparable<HeapNode> {

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

    @Override
    public int compareTo(HeapNode heapNode) {
      return - (this.count - heapNode.count);
    }
  }

  class MinHeapComparator implements Comparator<HeapNode>, Serializable {
    @Override
    public int compare(HeapNode o1, HeapNode o2) {
      return o1.count - o2.count;
    }
  }

  private void initiateAutoCompleteLibrary(Map<String, PriorityQueue<HeapNode>>[] autoCompleteMaps) {
    for (int i = 0; i < AUTO_COMPLETE_REPLY_LIST_LENGTH; ++i) {
      // autoCompleteMaps[0] for 1-Gram model
      // autoCompleteMaps[1] for 2-Gram model
      autoCompleteMaps[i] = new ConcurrentHashMap<>();
    }
  }

  private ConcurrentHashMap<String, List<String>> createNewAutoCompleteLibrary() {
    return new ConcurrentHashMap<>(524288, 0.75f, 128);
  }

  public void buildAutoCompleteLibrary() {
    System.out.println("[Indexer] Build auto-complete library");
    Timer timer = new Timer();
    timer.reset();

    ConcurrentHashMap<String, List<String>> newAutoCompleteLibrary = createNewAutoCompleteLibrary();
    Map<String, PriorityQueue<HeapNode>>[] autoCompleteMaps = new Map[AUTO_COMPLETE_REPLY_LIST_LENGTH];
    initiateAutoCompleteLibrary(autoCompleteMaps);

    for (int nGram = 2; nGram <= 5; ++nGram) {
      Map<String, Integer> phraseOccurrenceMap = nGramLanguageModel.phraseOccurrenceMaps[nGram - 1];
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

        if (autoCompleteMaps[nGram - 1].containsKey(prefix)) {
          PriorityQueue<HeapNode> minHeap = autoCompleteMaps[nGram - 1].get(prefix);
          maintainTopK(minHeap, AUTO_COMPLETE_REPLY_LIST_LENGTH, heapNode);
        } else {
          PriorityQueue<HeapNode> minHeap = new PriorityQueue<>(AUTO_COMPLETE_REPLY_LIST_LENGTH, minHeapComparator);
          maintainTopK(minHeap, AUTO_COMPLETE_REPLY_LIST_LENGTH, heapNode);
          autoCompleteMaps[nGram - 1].put(prefix, minHeap);
        }
      }
    }

    for (int i = 1; i < AUTO_COMPLETE_REPLY_LIST_LENGTH; ++i) {
      Map<String, PriorityQueue<HeapNode>> currentMap = autoCompleteMaps[i];
      for (Map.Entry<String, PriorityQueue<HeapNode>> entry : currentMap.entrySet()) {
        PriorityQueue<HeapNode> minHeap = entry.getValue();
        List<HeapNode> nodeList = new ArrayList<>();
        while (minHeap.size() != 0) {
          nodeList.add(minHeap.poll());
        }
        Collections.reverse(nodeList);
        List<String> stringList = new ArrayList<>();
        for (HeapNode heapNode : nodeList) {
          stringList.add(new String(heapNode.getAutoCompleteString()));
        }
        newAutoCompleteLibrary.put(entry.getKey(), stringList);
      }
    }

    this.autoCompleteLibrary = newAutoCompleteLibrary;
    System.out.println("[Indexer] Auto-complete library built: " + timer.getTime());
  }

  private void maintainTopK(PriorityQueue<HeapNode> minHeap, final int k, HeapNode newNode) {
    if (minHeap == null || newNode == null) {
      return;
    }
    if (minHeap.size() < k) {
      minHeap.add(newNode);
    } else {
      HeapNode lowestValueNode = minHeap.peek();
      if (minHeapComparator.compare(newNode, lowestValueNode) > 0) {
        minHeap.poll();
        minHeap.add(newNode);
      }
    }
  }

  public List<String> getAutoCompleteStrings(String query) {
    if (query == null || query.length() == 0) {
      return new ArrayList<>();
    }
    String newQueryString = query.trim();
    newQueryString = newQueryString.replaceAll("\\s+", " ");
    return new ArrayList<>(autoCompleteLibrary.get(newQueryString));
  }

}
