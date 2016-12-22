package edu.nyu.cs.newssearchengine.indexer;

import edu.nyu.cs.newssearchengine.SearchEngine;
import edu.nyu.cs.newssearchengine.document.IndexedDocument;
import edu.nyu.cs.newssearchengine.document.RawDocument;
import edu.nyu.cs.newssearchengine.query.IndexedQuery;
import edu.nyu.cs.newssearchengine.query.QueryPhrase;

import java.io.Serializable;
import java.util.*;


public abstract class Indexer  implements Serializable {

  private static final long serialVersionUID = 3L;

  protected List<IndexedDocument> documents = new ArrayList<>();
  public Map<String, List<Integer>> docsBySection = new HashMap<>();

  public TermHandler termHandler = new TermHandler();
  public OccurrenceHandler occurrenceHandler = new OccurrenceHandler();

  public NGramLanguageModel nGramLanguageModel = new NGramLanguageModel();

  public Indexer() {
    super();
  }

  public static String getIndexFileName() {
    return SearchEngine.getDataFolder() + "/index/corpus.idx";
  }

  public IndexedDocument getDoc(int id) {
    return documents.get(id);
  }

  public IndexedDocument nextDoc(IndexedQuery query, int docid) {
    Integer id = occurrenceHandler.nextDocID(query, docid);
    if (id == null) {
      return null;
    }
    return documents.get(id);
  }

  // Number of documents in the corpus.
  public final int numDocs() { return documents.size(); }
  // Number of term occurrences in the corpus. If a term appears 10 times, it
  // will be counted 10 times.
  public final long totalTermFrequency() { return termHandler.totalTermFrequency; }

  // Number of documents in which {@code term} appeared, over the full corpus.
  public int corpusDocFrequencyByTerm(String term) {
    Integer index = termHandler.dictionary.get(term);
    if (index == null) {
      return 0;
    }
    return occurrenceHandler.corpusDocFrequencyByTermIndex(index);
  }

  // Number of times {@code term} appeared in corpus.
  public int corpusTermFrequency(String term) {
    Integer count = termHandler.getTermFrequency(term);
    if (count == null) {
      return 0;
    }
    return count;
  }

  // Number of times {@code term} appeared in the document {@code docid}.
  // *** @CS2580: Note the function signature change from url to docid. ***
  public int documentTermFrequency(String term, int docid) {
    Integer index = termHandler.dictionary.get(term);
    if (index == null) {
      return 0;
    }
    return occurrenceHandler.documentTermFrequency(index, docid);
  }

  protected void processDocument(RawDocument document) {
    IndexedDocument invertedDoc = newDocumentIndexed(document);
    addDocBySection(invertedDoc);
    List<Integer> indexes = termHandler.handle(document);
    occurrenceHandler.handle(invertedDoc.getId(), indexes);
    nGramLanguageModel.handle(document);
    invertedDoc.setBodyLength(indexes.size());
  }

  private void addDocBySection(IndexedDocument document) {
    String section = document.section;
    if (!docsBySection.containsKey(section)) {
      docsBySection.put(section, new ArrayList<Integer>());
    }
    List<Integer> list = docsBySection.get(section);
    for (int i = 0; i < list.size(); i++) {
      if (document.date.after(documents.get(list.get(i)).date)) {
        list.add(i, document.getId());
        return ;
      }
    }
    list.add(document.getId());
  }

  protected void buildAutoCompleteLibrary() {
    nGramLanguageModel.buildAutoCompleteLibrary();
  }

  protected IndexedDocument newDocumentIndexed(RawDocument document) {
    IndexedDocument doc = new IndexedDocument(documents.size(), document);
    documents.add(doc);
    return doc;
  }

  public List<String> getTrendingWords(){
    return termHandler.trendingHandler.getTrendingWords();
  }

  public List<IndexedDocument> getDocsBySection(String section){
    List<Integer> list = docsBySection.get(section);

    List<IndexedDocument> result = new ArrayList<>();
    if (list == null) {
      return result;
    }
    for (int i = 0; i < 20 && i < list.size(); i++) {
      result.add(documents.get(list.get(i)));
    }
    return result;
  }

  public List<String> getSectionList() {
    List<String> list = new ArrayList<>(docsBySection.keySet());
    Collections.sort(list, new Comparator<String>() {
      @Override
      public int compare(String o1, String o2) {
        return - (docsBySection.get(o1).size() - docsBySection.get(o2).size());
      }
    });
    return list;
  }

  public List<String> getTrendingWordsBySection(String section){
    return termHandler.trendingHandler.getTrendingWordsBySection(section);
  }

  public static void main(String[] args) throws InterruptedException {

    DynamicIndexer indexer = new DynamicIndexer();
    indexer.constructIndexFromTemp();
    QueryPhrase query = new QueryPhrase("Sidney");
    IndexedQuery indexedQuery = new IndexedQuery(query, indexer);
    System.out.println(indexer.nextDoc(indexedQuery, -1));
    System.out.println(indexer.documentTermFrequency("sidnei", 0));

  }
}
