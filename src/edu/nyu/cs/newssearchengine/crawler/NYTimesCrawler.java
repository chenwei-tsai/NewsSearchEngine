package edu.nyu.cs.newssearchengine.crawler;

import edu.nyu.cs.newssearchengine.document.RawDocument;
import edu.nyu.cs.newssearchengine.document.DocumentMetadata;
import edu.nyu.cs.newssearchengine.utils.Logger;
import org.json.JSONObject;

import org.json.JSONArray;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.HashSet;
import java.util.Set;
import java.util.ArrayList;


public class NYTimesCrawler extends Crawler {
  private String baseUrl= "https://api.nytimes.com/svc/news/v3/content/all/all.json";
  private String apiKey = "8b235646237947ba999735baaae6122b";
//  private JSONFactory jsonFactory = new JSONFactory();

  @Override
  public RawDocument parseFromMetadata(DocumentMetadata metadata) {
    RawDocument rawDocument = new RawDocument(metadata);
    try {
      Document doc = Jsoup.connect(metadata.url).get();
      Elements contents = doc.select("div.story-body-supplemental p.story-body-text");
//    sometimes the article can have no body, only summary
//    for example http://www.nytimes.com/interactive/2016/11/22/us/politics/document-Trump-Foundation-Tax-Returns.html
      for (Element content : contents) {
        String text = content.text();
        if (!text.equals("null")) {
          rawDocument.body = rawDocument.body + " " + text;
        }
      }
      if (rawDocument.body.equals("") || rawDocument.body.isEmpty()) {
        throw new Exception("can't find body for document: " + metadata.title);
      }
    } catch (Exception e) {
      // Doc with problems will be ignored.
      Logger.log("[Crawler] Failed to parse the document: " + rawDocument.title);
      Logger.log(e);
      return null;
    }
    return rawDocument;
  }


  private String readAll(Reader bReader) throws IOException{
    StringBuilder stringBuilder =  new StringBuilder();
    int cp;
    while ((cp = bReader.read()) != -1){
      stringBuilder.append((char) cp);
    }
    return stringBuilder.toString();
  }

  ///http://developer.nytimes.com/timeswire_v3.json#/Console/GET/%7Bsource%7D/%7Bsection%7D.json
  @Override
  public Set<DocumentMetadata> findMetadata() {

    String request = baseUrl + "?api-key=" + apiKey;
    String jsonString = null;
    try {
      URL url = new URL(request);
      try {
        InputStream inputStream = url.openStream();
        InputStreamReader isReader =  new InputStreamReader(inputStream, Charset.forName("UTF-8"));
        BufferedReader bReader = new BufferedReader(isReader);
        jsonString = readAll(bReader);
      } catch (IOException e) {
        Logger.log("open stream of url wrong");
      }
    } catch (MalformedURLException e) {
      Logger.log("request url wrong");
    }
    return parseFromJson(jsonString);
  }

  private Set<DocumentMetadata> parseFromJson(String jsonString){
    Set<DocumentMetadata> set= new HashSet<>();
    if (jsonString == null || jsonString.isEmpty()) {
      return set;
    }
    JSONObject jsonObject = new JSONObject(jsonString);
    JSONArray array = jsonObject.getJSONArray("results");
    for (int i = 0; i < array.length(); i++) {
      JSONObject singleDoc = array.getJSONObject(i);
      DocumentMetadata doc = new DocumentMetadata();
      try {
        set.add(doc.parseJsonObject(singleDoc));
      } catch (ParseException e) {
        Logger.log(e);
      }
    }
    return set;
  }

  public static void main(String[] args){
    NYTimesCrawler crawler = new NYTimesCrawler();
    crawler.doCrawl();
  }
}