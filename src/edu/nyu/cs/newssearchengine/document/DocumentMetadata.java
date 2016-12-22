package edu.nyu.cs.newssearchengine.document;

import org.json.JSONObject;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DocumentMetadata implements Comparable<DocumentMetadata>, Serializable {
  public Date date;
  public String title;
  public String summary;
  public String url;
  public String section;

  @Override
  public int compareTo(DocumentMetadata o) {
    return date.compareTo(o.date);
  }

  public DocumentMetadata parseJsonObject (JSONObject jsonObject) throws ParseException{
    String tempDate = jsonObject.getString("updated_date");
    DocumentMetadata data= new DocumentMetadata();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss-'05:00'");
    data.date = sdf.parse(tempDate);
    data.title = jsonObject.getString("title");
    data.summary = jsonObject.getString("abstract");
    data.url = jsonObject.getString("url");
    data.section = jsonObject.getString("section");
    return data;
  }
}
