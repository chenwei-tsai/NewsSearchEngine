package edu.nyu.cs.newssearchengine.document;


import edu.nyu.cs.newssearchengine.utils.Logger;
import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * Raw document with all information
 */
public class RawDocument extends DocumentMetadata implements Serializable {

  public String body="";

  public RawDocument(){
    super();
  }

  public RawDocument(DocumentMetadata metadata){
    this.date=metadata.date;
    this.url=metadata.url;
    this.section=metadata.section;
    this.summary=metadata.summary;
    this.title=metadata.title;
  }

  public RawDocument (String fileName){

    RawDocument rawDocument =this.load(fileName);
//    System.out.println(rawDocument.title);
    this.date=rawDocument.date;
    this.url=rawDocument.url;
    this.section=rawDocument.section;
    this.summary=rawDocument.summary;
    this.title=rawDocument.title;
    this.body=rawDocument.body;
  }

  public static RawDocument load(String fileName) {
    RawDocument rawDocument = new RawDocument();
    File file = new File(fileName);
    if (file.exists()) {
      try {
        InputStream is = new FileInputStream(fileName);
        String jsonText = IOUtils.toString(is);
        JSONObject json = new JSONObject(jsonText);
        rawDocument.body = json.getString("body");
        String tempDate = json.getString("date");
        SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd hh:mm:ss z yyyy");
        try {
          rawDocument.date = sdf.parse(tempDate);
        } catch (ParseException e) {
          e.printStackTrace();
        }
        rawDocument.title = json.getString("title");
        rawDocument.summary = json.getString("abstract");
        rawDocument.url = json.getString("url");
        rawDocument.section = json.getString("section");
        /*
      } catch (JSONException e) {
        System.err.println("Failed to read the document " + fileName);
        Logger.log(e);
        */
      } catch (IOException e){
        Logger.log(e);
      }
    }
    return rawDocument;
  }

  public void save(String fileName) {
    JSONObject document = new JSONObject();
    document.put("date", this.date);
    document.put("title", this.title);
//    System.out.println(this.title);
    document.put("abstract", this.summary);
    document.put("url", this.url);
    document.put("section", this.section);
    document.put("body", this.body);

    try{
      File file = new File(fileName);
      file.createNewFile();
      FileWriter fileWriter= new FileWriter(file);
      fileWriter.write(document.toString());
      fileWriter.flush();
      fileWriter.close();
    }catch (IOException e){
      Logger.log(e);
    }
  }
}
