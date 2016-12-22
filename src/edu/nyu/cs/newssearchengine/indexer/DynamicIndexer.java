package edu.nyu.cs.newssearchengine.indexer;

import edu.nyu.cs.newssearchengine.SearchEngine;
import edu.nyu.cs.newssearchengine.document.RawDocument;
import edu.nyu.cs.newssearchengine.utils.Logger;
import edu.nyu.cs.newssearchengine.utils.Utils;

import java.io.*;
import java.util.*;

public class DynamicIndexer extends Indexer implements Serializable {

  private static final long serialVersionUID = 3L;

  public DynamicIndexer() {
  }

  public boolean constructIndexFromTemp() {
    String corpusFolder = SearchEngine.getDataFolder() + "/rawDocuments";
    return constructIndexFromFolder(corpusFolder, true);
  }

  private boolean constructIndexFromFolder(String corpusFolder, boolean archiveIt) {
    System.out.println("Construct index from: " + corpusFolder);

    boolean newDocument = false;

    File[] files = new File(corpusFolder).listFiles();
    List<String> fileNames = new ArrayList<>();
    for (File file : files) {
      if (file.getName().startsWith(".")) {
        continue;
      }
      fileNames.add(file.getAbsolutePath());
    }
    if (fileNames.size() != 0) {
      newDocument = true;
    }
    for (String fileName : fileNames) {
      RawDocument document = RawDocument.load(fileName);
      processDocument(document);
      if (archiveIt) {
        Utils.archiveRawDocument(document);
      }
    }
    System.out.println("Indexed " + Integer.toString(numDocs()) + " docs with" +
      " " + Long.toString(termHandler.totalTermFrequency) + " terms.");
    return newDocument;
  }

  public static DynamicIndexer load() throws IOException,
    ClassNotFoundException {
    String indexFile = getIndexFileName();
    System.out.println("Load index from: " + indexFile);

    ObjectInputStream reader = new ObjectInputStream(new FileInputStream
      (indexFile));
    DynamicIndexer loaded = (DynamicIndexer) reader.readObject();
    reader.close();
    return loaded;
  }

  public void dump() throws IOException {
    String indexFile = getIndexFileName();
    System.out.println("Store index to: " + indexFile);
    ObjectOutputStream writer =
      new ObjectOutputStream(new FileOutputStream(indexFile));
    writer.writeObject(this);
    writer.close();
    Logger.log("Indexer stored");
  }

  public static DynamicIndexer loadFromArchive() {
    DynamicIndexer indexer = new DynamicIndexer();

    File archiveFolder = new File(SearchEngine.getDataFolder() + "/archive");
    for (String docsInADayFolder : archiveFolder.list()) {
      indexer.constructIndexFromFolder(archiveFolder+"/"+docsInADayFolder, false);
    }
    return indexer;
  }

  public static void main(String[] args) {
    DynamicIndexer indexer = DynamicIndexer.loadFromArchive();
    System.out.println(indexer.documents.size());
  }
}
