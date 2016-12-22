package edu.nyu.cs.newssearchengine.indexer;

import edu.nyu.cs.newssearchengine.document.IndexedDocument;
import edu.nyu.cs.newssearchengine.query.IndexedQuery;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class OccurrenceHandler implements Serializable {

  private static final long serialVersionUID = 3L;
  // Maps each term to their inverted lists
  protected Map<Integer, List<IndexerInvertedListNode>> bodyInvertedIndex = new ConcurrentHashMap<>();

  public OccurrenceHandler() {
  }

  protected void handle(int docid, List<Integer> tokens) {
    // Constructs the inverted index lists
    int bodyPosition = 1;
    for (Integer index : tokens) {
      if (bodyInvertedIndex.containsKey(index)) {
        List<IndexerInvertedListNode> nodeList = bodyInvertedIndex.get(index);
        nodeList.add(new IndexerInvertedListNode(docid, bodyPosition));
      } else {
        List<IndexerInvertedListNode> nodeList = new ArrayList<>();
        nodeList.add(new IndexerInvertedListNode(docid, bodyPosition));
        bodyInvertedIndex.put(index, nodeList);
      }
      bodyPosition++;
    }
    // Sorts all the inverted lists.
    // Might be spared if the lists are constructed in order.
    for (Map.Entry<Integer, List<IndexerInvertedListNode>> entry :
      bodyInvertedIndex.entrySet()) {
      List<IndexerInvertedListNode> list = entry.getValue();
      Collections.sort(list);
    }
  }

  /**
   * In HW2, you should be using {@link IndexedDocument}.
   */
  public Integer nextDocID(IndexedQuery query, int docid) {

    List<Integer> documentList = produceQueryDocumentList(query);

    if (documentList != null) {
      for (Integer index : documentList) {
        if (index > docid) {
          return index;
        }
      }
    }
    return null;

  }

  private List<Integer> produceQueryDocumentList(IndexedQuery query) {
    if (query == null) {
      return null;
    }
    Set<Integer> commonIDs = null;
    List<List<Integer>> indexedPhrases = query.getIndexedPhrases();
    if (indexedPhrases == null || indexedPhrases.isEmpty()) {
      return new ArrayList<>();
    }
    for (List<Integer> phrase : indexedPhrases) {
      Set<Integer> phraseSet = new HashSet<>(phrase);
      Set<Integer> set = searchTokensFromInvertedIndex(phraseSet, bodyInvertedIndex);
      if (commonIDs == null) {
        commonIDs = set;
      } else {
        commonIDs.retainAll(set);
      }
    }

    ArrayList<Integer> documentList = new ArrayList<>(commonIDs);
    Collections.sort(documentList);

    return documentList;
  }

  private Set<Integer> searchTokensFromInvertedIndex(
    Set<Integer> tokens,
    Map<Integer, List<IndexerInvertedListNode>> invertedIndex
  ) {
    Set<Integer> documentSet = new HashSet<>();
    if (tokens == null || tokens.size() == 0 || invertedIndex == null ||
      documentSet == null) {
      return documentSet;
    }

    List<List<IndexerInvertedListNode>> listList = new ArrayList<>();
    for (Integer token : tokens) {
      if (invertedIndex.containsKey(token)) {
        listList.add(invertedIndex.get(token));
      }
    }

    if (listList.size() < tokens.size()) {
      return documentSet;
    }

    List<IndexerInvertedListNode> firstList = listList.get(0);
    for (IndexerInvertedListNode node : firstList) {
      int documentId = node._documentId;
      int position = node._position;
      boolean keepMatching = true;
      for (int i = 0; i < listList.size() - 1 && keepMatching; ++i) {
        List<IndexerInvertedListNode> list = listList.get(i + 1);
        for (int j = 0; j < list.size(); ++j) {
          if (list.get(j)._documentId == documentId && list.get(j)._position
            == position + i + 1) {
            break;
          }
          if (j == list.size() - 1) {
            keepMatching = false;
          }
        }
      }
      if (keepMatching) {
        documentSet.add(documentId);
      }
    }
    return documentSet;
  }

  public int corpusDocFrequencyByTermIndex(Integer index) {
    if (!bodyInvertedIndex.containsKey(index)) {
      return 0;
    }
    Set<Integer> docs = new HashSet<>();
    List<IndexerInvertedListNode> list = bodyInvertedIndex.get(index);
    for (IndexerInvertedListNode node : list) {
      docs.add(node._documentId);
    }
    return docs.size();
  }

  public int documentTermFrequency(Integer termIndex, int docid) {
    if (bodyInvertedIndex.containsKey(termIndex)) {
      List<IndexerInvertedListNode> list = bodyInvertedIndex.get(termIndex);
      int count = 0;
      for (IndexerInvertedListNode node : list) {
        if (node._documentId == docid) {
          count++;
        }
      }
      return count;
    } else {
      return 0;
    }
  }
}
