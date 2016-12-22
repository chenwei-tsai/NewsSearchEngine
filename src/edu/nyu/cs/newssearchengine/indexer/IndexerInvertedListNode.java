package edu.nyu.cs.newssearchengine.indexer;

import java.io.Serializable;

/**
 * Created by Chen-Wei Tsai on 10/26/16.
 */
public class IndexerInvertedListNode implements Comparable<IndexerInvertedListNode>, Serializable {

  private static final long serialVersionUID = 3L;

  int _documentId;
  int _position;

  public IndexerInvertedListNode(int documentId, int position) {
    this._documentId = documentId;
    this._position = position;
  }

  @Override
  public int compareTo(IndexerInvertedListNode node) {
    // ascending order
    if (this._documentId > node._documentId) {
      return 1;
    } else if (this._documentId < node._documentId) {
      return -1;
    } else if (this._position > node._position) {
      return 1;
    } else if (this._position < node._position) {
      return -1;
    } else {
      return 0;
    }
  }

  @Override
  public boolean equals(Object object) {
    if (object instanceof IndexerInvertedListNode) {
      IndexerInvertedListNode node = (IndexerInvertedListNode) object;
      if (this._documentId == node._documentId && this._position == node._position) {
        return true;
      } else {
        return false;
      }
    } else {
      return false;
    }
  }

}
