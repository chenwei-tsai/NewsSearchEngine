package edu.nyu.cs.newssearchengine.crawler;

import edu.nyu.cs.newssearchengine.SearchEngine;
import edu.nyu.cs.newssearchengine.document.RawDocument;
import edu.nyu.cs.newssearchengine.document.DocumentMetadata;
import edu.nyu.cs.newssearchengine.utils.Logger;
import edu.nyu.cs.newssearchengine.utils.Utils;

import java.io.File;
import java.util.*;

public abstract class Crawler {
  public Crawler() {

    File archiveFolder = new File(SearchEngine.getDataFolder() + "/archive");
    int count = 0;
    if (archiveFolder.list().length == 0) {
      Logger.log("no file in archive folder");
      return ;
    }
    for (String docsInADayFolder : archiveFolder.list()) {
      File docsInADay = new File(archiveFolder+"/"+docsInADayFolder);
      for (File file: docsInADay.listFiles()) {
        RawDocument doc = RawDocument.load(file.getAbsolutePath());
        crawledURLs.add(doc.url);
        count ++;
      }
    }
    Logger.log("Add "+count+" docs as crawled.");
  }

  protected Date latestDate = new Date(0);
  protected Set<String> crawledURLs = new HashSet<>(); // set string crawledURLs init

  public void doCrawl() {
    Set<DocumentMetadata> metadata = findMetadata(); // set rawdoc... metadata init

    List<DocumentMetadata> list = new ArrayList<>(metadata);
    // sort the doc from old one to new one
    Collections.sort(list);
    Logger.log("find "+list.size()+ " urls.");
    for (DocumentMetadata data: metadata) {
      if (crawledURLs.contains(data.url)) {
        continue;
      }
      RawDocument doc = parseFromMetadata(data);
      if (doc == null) {
        Logger.log("can't parse from "+data.title);
        continue;
      }
      Utils.suspend(1);
      saveRawDoc(doc);
      latestDate = doc.date;
      crawledURLs.add(data.url);
    }
  }



  public abstract RawDocument parseFromMetadata(DocumentMetadata metadata);
  public abstract Set<DocumentMetadata> findMetadata();

  /**
   * Save it to data/docs/temp. Must make sure those file names do not collide
   * @param doc
   */
  public void saveRawDoc(RawDocument doc) {
    doc.save(SearchEngine.getDataFolder() + "/rawDocuments/" + doc.title);
  }

  public static void main(String[] args) {
    new NYTimesCrawler();
  }
}
