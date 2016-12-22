package edu.nyu.cs.newssearchengine.ranker;

import edu.nyu.cs.newssearchengine.query.QueryPhrase;
import edu.nyu.cs.newssearchengine.document.ScoredDocument;
import edu.nyu.cs.newssearchengine.document.ScoredTerms;
import edu.nyu.cs.newssearchengine.indexer.Indexer;

import java.util.List;
import java.util.Vector;

/**
 * This is the abstract Ranker class for all concrete Ranker implementations.
 *
 *
 * 2013-02-16: The instructor's code went through substantial refactoring
 * between HW1 and HW2, students are expected to refactor code accordingly.
 * Refactoring is a common necessity in real world and part of the learning
 * experience.
 *
 * @author congyu
 * @author fdiaz
 */
public abstract class Ranker {

  // The Indexer via which documents are retrieved, see {@code IndexerFullScan}
  // for a concrete implementation. N.B. Be careful about thread safety here.
  protected Indexer indexer;

  /**
   * Constructor: the construction of the Ranker requires an Indexer.
   */
  protected Ranker(Indexer indexer) {
    this.indexer = indexer;
  }

  /**
   * Processes one query.
   * @param query the parsed user query
   * @param numResults number of results to return
   * @return Up to {@code numResults} scored documents in ranked order
   */
  public abstract List<ScoredDocument> runQuery(QueryPhrase query, int numResults);
  public abstract List<ScoredTerms> runPseudoQuery(QueryPhrase query, int numterms, int numdocs);
}
