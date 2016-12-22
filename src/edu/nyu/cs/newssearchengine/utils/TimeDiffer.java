package edu.nyu.cs.newssearchengine.utils;

import java.util.Date;

/**
 * Created by Vane on 12/11/16.
 */
public class TimeDiffer {
  public static String getTimeDiffer(Date date){
    Date now = new Date();
    long diff= now.getTime()-date.getTime();
    long minutes = diff/(60*1000);
    long hours = diff/(60*1000*60);
    long days = diff/(60*1000*60*24);
    if (minutes<60) return minutes+"min";
    if (hours<24) return hours+"h";
    return days+"d";
  }
}
