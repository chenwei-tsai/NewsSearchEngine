package edu.nyu.cs.newssearchengine.indexer;

import edu.nyu.cs.newssearchengine.document.RawDocument;
import edu.nyu.cs.newssearchengine.utils.Counter;
import edu.nyu.cs.newssearchengine.utils.CounterMap;
import edu.nyu.cs.newssearchengine.utils.FastPriorityQueue;
import edu.nyu.cs.newssearchengine.utils.NERParser;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class TrendingHandler implements Serializable {

  private static final int MILLIS_IN_HOUR = 1000 * 60 * 60;
  private static final int N_TRENDING = 10;
  private Date lastUpdateTime = null;

  Counter<Date> docCounter = new Counter<>();
  Map<Date, CounterMap<String, String>> map = new ConcurrentHashMap<>();
  private List<String> trendingWords = new ArrayList<>();
  private Map<String, Vector<String>> trendingWordsBySection = new ConcurrentHashMap<>();

  public void addDocument(RawDocument document) {
    Date date = keepHoursOnly(document.date);
    if (!map.containsKey(date)) {
      map.put(date, new CounterMap<String, String>());
    }
    CounterMap<String, String> counter = map.get(date);
    Set<String> set = new HashSet<>(NERParser.getNEs(document.title + ". " + document.body));
    String section = document.section;
    for (String uniqueToken: set) {
      counter.incrementCount(section, uniqueToken, 1);
    }
    docCounter.incrementCount(date, 1);
  }

  public void update() {
    Date current = keepHoursOnly(new Date());
    if (current == lastUpdateTime) {
      return;
    }
    int docCount = 0;
    Counter<String> totalCounter = new Counter();
    Map<String, Counter<String>> counterBySection = new HashMap<>();
    for (int i = 0; i < 24; i++) {
      Date date = new Date(current.getTime() - i * MILLIS_IN_HOUR);
      CounterMap<String, String> counter = map.get(date);
      if (counter == null) {
        continue;
      }
      for (String section: counter.keySet()) {
        Counter<String> counterForThisSection = counter.getCounter(section);

        totalCounter.incrementAll(counterForThisSection);

        if (!counterBySection.containsKey(section)) {
          counterBySection.put(section, new Counter<String>());
        }
        Counter<String> sectionCounter = counterBySection.get(section);
        sectionCounter.incrementAll(counterForThisSection);
      }
      docCount += docCounter.getCount(date);
    }

    Vector<String> temp = getTrendingVector(totalCounter);
    trendingWords = temp;

    Map<String, Vector<String>> newTrendingMapBySection = new HashMap<>();
    for (String section: counterBySection.keySet()) {
      Vector trendingVector = getTrendingVector(counterBySection.get(section));
      newTrendingMapBySection.put(section, trendingVector);
    }
    trendingWordsBySection = newTrendingMapBySection;

    lastUpdateTime = current;
  }

  private Vector<String> getTrendingVector(Counter<String> totalCounter) {
    FastPriorityQueue<String> pq = new FastPriorityQueue<>();
    for (Map.Entry<String, Double> entry: totalCounter.getEntrySet()) {
      pq.setPriority(entry.getKey(), entry.getValue());
    }
    Vector temp = new Vector<>();
    for (int i = 0; i < N_TRENDING && pq.hasNext(); i++) {
      temp.add(pq.next());
    }
    return temp;
  }

  public List<String> getTrendingWords() {
    return new ArrayList<>(trendingWords);
  }

  public List<String> getTrendingWordsBySection(String section) {
    if (!trendingWordsBySection.containsKey(section)) {
      return new ArrayList<>();
    }
    return new ArrayList<>(trendingWordsBySection.get(section));
  }

  private static Date keepHoursOnly(Date date) {
    return new Date(date.getTime() / MILLIS_IN_HOUR * MILLIS_IN_HOUR);
  }

  public static void main(String[] args) throws InterruptedException {
    Date date = new Date();
    Set<Date> set = new HashSet<>();
    set.add(keepHoursOnly(date));

    Thread.sleep(134);
    set.add(keepHoursOnly(new Date()));
    Thread.sleep(134);
    set.add(keepHoursOnly(new Date()));
    System.out.println(set.size());
    System.out.println(keepHoursOnly(new Date()));
  }
}
