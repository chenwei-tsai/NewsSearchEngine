package edu.nyu.cs.newssearchengine.ranker;

import edu.nyu.cs.newssearchengine.query.IndexedQuery;
import edu.nyu.cs.newssearchengine.query.QueryPhrase;
import edu.nyu.cs.newssearchengine.document.ScoredDocument;
import edu.nyu.cs.newssearchengine.document.ScoredTerms;
import edu.nyu.cs.newssearchengine.document.IndexedDocument;
import edu.nyu.cs.newssearchengine.indexer.Indexer;

import java.util.*;

public class RankerFavorite extends Ranker {

  private static final double lambda = 0.5;

  public RankerFavorite(Indexer indexer) {
    super(indexer);
    System.out.println("Using Ranker: " + this.getClass().getSimpleName());
  }

  @Override
  public List<ScoredDocument> runQuery(QueryPhrase query, int numResults) {

    IndexedQuery indexedQuery = new IndexedQuery(query, indexer);
    Queue<ScoredDocument> rankQueue = new PriorityQueue<>();
    IndexedDocument doc;
    int docid = -1;
    Date now =new Date();
    while ((doc = indexer.nextDoc(indexedQuery, docid)) != null) {
      docid = doc.getId();
      Date date=doc.date;
      long differ=now.getTime()-date.getTime();
      long differhour=differ/(1000*60*60);
      int D = doc.getBodyLength();
      long C = indexer.totalTermFrequency();
      double score =0;
      for (String token : indexer.termHandler.getTermVector(indexedQuery.getIndexedTokenSet())) {
        double p_q_D = indexer.documentTermFrequency(token, docid);
        double p_q_C = indexer.corpusTermFrequency(token);
        score += Math.log(lambda * p_q_D / D + (1 - lambda) * p_q_C / C+3);
        if (doc.title.toLowerCase().contains(token.toLowerCase())) score=score+1;
        if (doc.summary.toLowerCase().contains(token.toLowerCase())) score=score+0.5;
      }
      differhour=(differhour/12+1)*12;
      score+=36.0/differhour;
      rankQueue.add(new ScoredDocument(doc, score));
      if (rankQueue.size() > numResults) {
        rankQueue.poll();
      }
    }

    List<ScoredDocument> results = new Vector<>();
    ScoredDocument scoredDoc;
    while ((scoredDoc = rankQueue.poll()) != null) {
      results.add(scoredDoc);
    }
    Collections.sort(results, Collections.reverseOrder());
    return results;
  }

  @Override
  public List<ScoredTerms> runPseudoQuery (QueryPhrase query, int numterms, int numdocs) {

    List<ScoredDocument> docResults = new ArrayList<>();
    docResults = runQuery(query, numdocs);

    List<ScoredTerms> results = new ArrayList<>();

    List<String> vacabulary = indexer.termHandler.uniqueTerms;

    PriorityQueue<ScoredTerms> pqScoredTerms = new PriorityQueue<ScoredTerms>();
    for (String word : vacabulary) {
      int countWord = 0;
      for (ScoredDocument document : docResults) {
        int documentId = document.getDoc().getId();
        countWord += indexer.documentTermFrequency(word, documentId);
      }
      countWord /= (indexer.corpusDocFrequencyByTerm(word));
      pqScoredTerms.add(new ScoredTerms(word, countWord));
      if (pqScoredTerms.size() > numterms) {
        pqScoredTerms.poll();
      }
    }

    while (!pqScoredTerms.isEmpty()) {
      results.add(pqScoredTerms.poll());
    }
    double sum = 0.0;
    for (ScoredTerms sTerm : results) {
      sum += sTerm.getScore();
    }
    for (ScoredTerms sTerm : results) {
      double score = sTerm.getScore();
      if (score == 0) {
        sTerm.changeScore(0);
      } else sTerm.changeScore(score / sum);
    }
    Collections.sort(results, Collections.reverseOrder());

    return results;
  }
}
