package edu.nyu.cs.newssearchengine.utils;

import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.crf.*;
import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CoreAnnotations;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static edu.stanford.nlp.ie.crf.CRFClassifier.getClassifier;


/** This is a demo of calling CRFClassifier programmatically.
 *  <p>
 *  Usage: {@code java -mx400m -cp "stanford-ner.jar:." NERDemo [serializedClassifier [fileName]] }
 *  <p>
 *  If arguments aren't specified, they default to
 *  classifiers/english.all.3class.distsim.crf.ser.gz and some hardcoded sample text.
 *  <p>
 *  To use CRFClassifier from the command line:
 *  </p><blockquote>
 *  {@code java -mx400m edu.stanford.nlp.ie.crf.CRFClassifier -loadClassifier [classifier] -textFile [file] }
 *  </blockquote><p>
 *  Or if the file is already tokenized and one word per line, perhaps in
 *  a tab-separated value format with extra columns for part-of-speech tag,
 *  etc., use the version below (note the 's' instead of the 'x'):
 *  </p><blockquote>
 *  {@code java -mx400m edu.stanford.nlp.ie.crf.CRFClassifier -loadClassifier [classifier] -testFile [file] }
 *  </blockquote>
 *
 *  @author Jenny Finkel
 *  @author Christopher Manning
 */

public class NERParser {

  static String serializedClassifier = "classifiers/english.all.3class.distsim.crf.ser.gz";

  static AbstractSequenceClassifier<CoreLabel> classifier;
  static {
    try {
      classifier = getClassifier(serializedClassifier);
    } catch (IOException e) {
      e.printStackTrace();
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
  }

  public static void main(String[] args) {
      String[] example = {"Good afternoon Rajat Raina, how are you today?",
        "I go to school at Stanford University, which is located in California." };

    for (String str : example) {
      List<String> entities = getNEs(str);
      for (String token: entities) {
        System.out.println(token);
      }
    }
  }

  public static List<String> getNEs(String input) {
    return getEntities(classifier.classifyWithInlineXML(input));
  }

  private static List<String> getEntities(String output) {
    List<String> list = new ArrayList<>();
    Pattern pattern = Pattern.compile("<[A-Z]+>([^<>]*)</[A-Z]+>");
    Matcher m = pattern.matcher(output);
    while(m.find()) {
      list.add(m.group(1));
    }
    return list;
  }

}
