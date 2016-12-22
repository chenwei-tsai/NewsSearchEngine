package edu.nyu.cs.newssearchengine.document;

import edu.nyu.cs.newssearchengine.utils.TimeDiffer;

import java.util.Date;

/**
 * Document with score.
 * 
 * @author fdiaz
 * @author congyu
 */
public class ScoredDocument implements Comparable<ScoredDocument> {
  private IndexedDocument _doc;
  private double _score;

  public ScoredDocument(IndexedDocument doc, double score) {
    _doc = doc;
    _score = score;
  }

  public String asTextResult() {

    StringBuffer buf = new StringBuffer();
    buf.append(_doc.getId()).append("\t");
    buf.append(_doc.title).append("\t");
    buf.append(_score).append("\t");
    return buf.toString();
  }

  public IndexedDocument getDoc(){
    return _doc;
  }

  /**
   * @CS2580: Student should implement {@code asHtmlResult} for final project.
   */
  public String asHtmlResult() {
    StringBuffer buf = new StringBuffer();
    Date date=_doc.date;
    String timeDiffer = TimeDiffer.getTimeDiffer(date);
    buf.append("<br>").append("<a href=\"").append(_doc.url).append("\" target=\"_blank\">");
    buf.append(_doc.title).append("</a>");
    buf.append("<br>").append("<font size=\"2\" color=\"#006621\">");
    buf.append(_doc.url).append("</font>");
//    buf.append("</b>");
    buf.append("<br>");
    buf.append("<span class=\"time\">"+timeDiffer+" ago - </span>");
    buf.append(_doc.summary);
//    buf.append("</b>");
    return buf.toString();
  }

//  String getTimeDiffer(Date date){
//    Date now = new Date();
//    long diff= now.getTime()-date.getTime();
//    long minutes = diff/(60*1000);
//    long hours = diff/(60*1000*60);
//    long days = diff/(60*1000*60*24);
//    if (minutes<60) return minutes+"min";
//    if (hours<24) return hours+"h";
//    return days+"d";
//  }

  @Override
  public int compareTo(ScoredDocument o) {
    if (this._score == o._score) {
      return 0;
    }
    return (this._score > o._score) ? 1 : -1;
  }
}
