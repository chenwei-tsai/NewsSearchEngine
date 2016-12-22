package edu.nyu.cs.newssearchengine.utils;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

/**
 * Created by Vane on 11/11/16.
 */
public class Bhattacharyya {
  private static String prfPath;
  private static String outputPath;
  private static HashMap<String, HashMap<String, Double>> queryTermScore= new HashMap<String, HashMap<String, Double>>();
  private static HashMap<String, String> queryPath=new HashMap<String, String>();
  private static Vector<String> queries = new Vector<String>();


  public void Process(String prfPath) throws IOException{
    BufferedReader prfFile= new BufferedReader(new FileReader(prfPath));
    String line = prfFile.readLine();
    while(line!=null){
      String[] lineSplit=line.split(":");
      queries.add(lineSplit[0]);
      queryPath.put(lineSplit[0],lineSplit[1]);
      line=prfFile.readLine();
    }
    prfFile.close();
    for(String query:queries){
      HashMap<String, Double> temp = new HashMap<String, Double>();
      BufferedReader bufReader = new BufferedReader(new FileReader(queryPath.get(query)));
      String newline = "";
      while((newline=bufReader.readLine())!=null){
        String[] newlinesplit=newline.split("\\s+");
        temp.put(newlinesplit[0], Double.parseDouble(newlinesplit[1]));
      }
      queryTermScore.put(query,temp);
      bufReader.close();
    }
    return;
  }

  private static Set<String> Intersection (String q1, String q2) throws IOException{
    Set<String> q1Terms = new HashSet<String>();
    Set<String> q2Terms = new HashSet<String>();
    String q1Path = queryPath.get(q1);
    String q2Path = queryPath.get(q2);
    BufferedReader bufReader = new BufferedReader(new FileReader(q1Path));
    String lineOne = "";
    while((lineOne=bufReader.readLine())!=null){
      q1Terms.add(lineOne.split("\\s+")[0]);
    }
    bufReader=new BufferedReader(new FileReader(q2Path));
    String lineTwo = "";
    while((lineTwo=bufReader.readLine())!=null){
      q2Terms.add(lineTwo.split("\\s+")[0]);
    }
    bufReader.close();
    q1Terms.retainAll(q2Terms);
    return q1Terms;
  }

  public static void Write(String outputPath) throws IOException{
    File output = new File(outputPath);
    if (!output.exists()) output.createNewFile();
    BufferedWriter bufferedWriter=new BufferedWriter(new FileWriter(outputPath, true));
    for(int i=0;i<queries.size();i++){
      for(int j=i+1;j<queries.size();j++){
        String queryOne = queries.elementAt(i);
        String queryTwo = queries.elementAt(j);
        Set<String> common = Intersection(queryOne,queryTwo);
        double sum=0.0;
        for(String commonTerm:common){
          double probsQueryOne = queryTermScore.get(queryOne).get(commonTerm);
          double probsQueryTwo = queryTermScore.get(queryTwo).get(commonTerm);
          sum+=Math.sqrt(probsQueryOne*probsQueryTwo);
        }
        bufferedWriter.write(queryOne+"\t"+queryTwo+"\t"+sum);
        bufferedWriter.newLine();
      }
    }
    bufferedWriter.close();
    return;
  }

  public static void main(String args[]) throws IOException{

    Bhattacharyya bhat=new Bhattacharyya();
    bhat.prfPath=args[0];
    bhat.outputPath=args[1];
    bhat.Process(prfPath);
    bhat.Write(outputPath);
    System.out.println("Results saved to " + args[1]);
  }
}
