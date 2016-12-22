package edu.nyu.cs.newssearchengine.document;

/**
 * Created by Vane on 11/10/16.
 */
public class ScoredTerms implements Comparable<ScoredTerms> {
  private String _term;
  private double _score;

  public ScoredTerms(String term, double score) {
    _term = term;
    _score = score;
  }

  public double getScore() {
    return _score;
  }

  public void changeScore(double score) {
    this._score = score;
  }

  public String asTextResult() {
    StringBuffer buf = new StringBuffer();
    buf.append(_term).append("\t");
    buf.append(_score);
    return buf.toString();
  }

  @Override
  public int compareTo(ScoredTerms o) {
    if (this._score == o._score) {
      return 0;
    }
    return (this._score > o._score) ? 1 : -1;
  }
}