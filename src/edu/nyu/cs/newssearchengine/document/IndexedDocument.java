package edu.nyu.cs.newssearchengine.document;

import edu.nyu.cs.newssearchengine.utils.TimeDiffer;

import java.io.Serializable;
import java.util.Date;

/**
 * @CS2580: implement this class for HW2 to incorporate any additional
 * information needed for your favorite ranker.
 */
public class IndexedDocument extends DocumentMetadata implements Serializable{

  private static final long serialVersionUID = 3452345345L;
  private int id;

  private int bodyLength = 0;

  public IndexedDocument(int id, RawDocument document) {
    this.id = id;
    title = document.title;
    url = document.url;
    date = document.date;
    summary = document.summary;
    section = document.section;
  }

  public int getBodyLength() {
    return this.bodyLength;
  }

  public void setBodyLength(int bodyLength) {
    this.bodyLength = bodyLength;
  }

  public int getId() {
    return id;
  }

  public String asHtmlResult() {
    StringBuffer buf = new StringBuffer();
    Date date=this.date;
    String timeDiffer = TimeDiffer.getTimeDiffer(date);
    buf.append("<br>").append("<a href=\"").append(this.url).append("\" target=\"_blank\">");
    buf.append(this.title).append("</a>");
    buf.append("<br>").append("<font size=\"2\" color=\"#006621\">");
    buf.append(this.url).append("</font>");
//    buf.append("</b>");
    buf.append("<br>");
    buf.append("<span class=\"time\">"+timeDiffer+" ago - </span>");
    buf.append(this.summary);
//    buf.append("</b>");
    return buf.toString();
  }
}
