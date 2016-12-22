package edu.nyu.cs.newssearchengine;

import edu.nyu.cs.newssearchengine.document.IndexedDocument;
import edu.nyu.cs.newssearchengine.document.ScoredDocument;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * Created by Vane on 11/28/16.
 */
public class HtmlOutput {

  public void resultHeader(StringBuffer response, String queryString) {
    response.append("<!DOCTYPE html>");
    response.append("<html>");
    response.append("<head>");
    response.append("<meta charset=\"UTF-8\">");
    response.append("<title>NYT Search Results</title>");
    resultStyle(response);
    response.append("</head>");
    response.append("<body>");
    response.append("<header>");
    response.append("<div class='bar'>");
    response.append("<div id=\"img\">");
    response.append("<a href=\"" + "http://" + SearchEngine.HOST + ":" + SearchEngine.PORT + "\">");
    response.append("<image src=\"http://www.simplynew.com/wordpress/wp-content/uploads/2013/05/nyt-logo.png\" width=\"85%\">" + "\n");
    response.append("</a>");
    response.append("</div>");
    response.append("<div id=\"bar\">");
    searchBar(response, queryString);
    response.append("</div>");
    response.append("</div>");
    response.append("</header>");
  }

  public void homeHeader(StringBuffer response){
    response.append("<!DOCTYPE html>");
    response.append("<html>");
    response.append("<head>");
    response.append("<meta charset=\"UTF-8\">");
    response.append("<title>NYT Search Engine</title>");
    homeStyle(response);
    response.append("</head>");
    response.append("<body style=\"margin:5%;padding-left:10%;padding-right:10%\">");
    response.append("<header>");
    response.append("<div style=\"text-align:center; margin-bottom:35px;\">");
    response.append("<image src=\"https://upload.wikimedia.org/wikipedia/commons/7/77/The_New_York_Times_logo.png\" width=\"50%\">" + "\n");
    response.append("</div>");
    response.append("</header>");
  }

  public void generateFooter(StringBuffer response){
    response.append("<footer>");
    response.append("<div>");
    response.append("<p>2016 &copy; Shaodong, Barry and You</p>");
    response.append("</div>");
    response.append("</footer>");
    response.append("</body>");
    response.append("</html>");
  }

  public void sectionPage(final List<IndexedDocument> docs, List<String> trends, List<String> sections, StringBuffer response, String sectionString) {
    resultHeader(response, "");
    response.append("<main>");
    response.append("<div>");
    response.append("<table>");
    response.append("  </tr>\n" +
        "  <tr valign=\"top\">\n" +
        "    <td width=\"20%\">\n");
    response.append("<div id=\"section\">");
    sectionOutput(sections, response);
    response.append("</div>");
    response.append("    </td>\n" +
        "    <td width=\"55%\">\n");
    sectionResultOutput(docs, response);
    response.append("    </td>\n" +
        "    <td width=\"25%\">\n");
    response.append("<div id=\"trend\">");
    trendOutput(trends, response);
    response.append("</div>");
    response.append("    </td>\n" +
        "  </tr>");
    response.append("</table>");
    response.append("</div>");
    response.append("</main>");
    generateFooter(response);
  }
  public void resultPage(final List<ScoredDocument> docs, List<String> trends, List<String> sections, StringBuffer response, String queryString) {
    resultHeader(response, queryString);
    response.append("<main>");
    response.append("<div>");
    response.append("<table>");
    response.append("  </tr>\n" +
        "  <tr valign=\"top\">\n" +
        "    <td width=\"20%\">\n");
    response.append("<div id=\"section\">");
    sectionOutput(sections, response);
    response.append("</div>");
    response.append("    </td>\n" +
        "    <td width=\"55%\">\n");
    resultOutput(docs, response);
    response.append("    </td>\n" +
        "    <td width=\"25%\">\n");
    response.append("<div id=\"trend\">");
    trendOutput(trends, response);
    response.append("</div>");
    response.append("    </td>\n" +
        "  </tr>");
    response.append("</table>");
    response.append("</div>");
    response.append("</main>");
    generateFooter(response);
  }

  public void sectionOutput(List<String> sections, final StringBuffer response){
    response.append("<nav class=\"sections\">");
    response.append("<div style=\"text-align:left\">");
    response.append("<h1>Sections</h1>");
    response.append("<ol class=\"nobullet\">");
    for(String section : sections) {
      response.append("<li>");
      response.append("<a id=\"sec\" href=\"" + "http://" + SearchEngine.HOST + ":" + SearchEngine.PORT + "/section?query=" + section + "\">");
      response.append(section);
      response.append("</a>");
      response.append("</li>");
    }
    response.append("</ol>");
    response.append("</div>");
    response.append("</nav>");
  }

  public void trendOutput(List<String> trends, final StringBuffer response){
    response.append("<nav class=\"trends\">");
    response.append("<div style=\"text-align:left\">");
    response.append("<h1>Hot Topics</h1>");
    response.append("<ol>");
    for(String trend : trends){
      response.append("<li>");
      response.append("<a href=\"" + "http://" + SearchEngine.HOST + ":" + SearchEngine.PORT + "/search?query=" + trend+"\">");
      response.append(trend);
      response.append("</a>");
      response.append("</li>");
    }
    response.append("</ol>");
    response.append("</div>");
    response.append("</nav>");
  }

  public void resultOutput(final List<ScoredDocument> docs, StringBuffer response){
    if (docs == null || docs.size() == 0) {
      response.append("<p>");
      response.append("<h2>");
      response.append("Sorry! We can't find anything!");
      response.append("</h2>");
      response.append("<p>");
    } else {
      for (ScoredDocument doc : docs) {
        response.append("<p>");
        response.append(response.length() > 0 ? "" : "");
        response.append(doc.asHtmlResult());
        response.append("</p>");
      }
    }
  }

  public void sectionResultOutput(final List<IndexedDocument> docs, StringBuffer response){
    for (IndexedDocument doc : docs) {
      response.append("<p>");
      response.append(response.length() > 0 ? "" : "");
      response.append(doc.asHtmlResult());
      response.append("</p>");
    }
  }

  public void barSection(StringBuffer response){
    response.append("<nav>");
    response.append("<a href=\"http://"+ SearchEngine.HOST + ":" + SearchEngine.PORT + "/section?query=U.S.\"><h3>U.S.</h3></a>&nbsp;");
    response.append("<a href=\"http://"+ SearchEngine.HOST + ":" + SearchEngine.PORT + "/section?query=World\"><h3>World</h3></a>&nbsp;");
    response.append("<a href=\"http://"+ SearchEngine.HOST + ":" + SearchEngine.PORT + "/section?query=Technology\"><h3>Technology</h3></a>&nbsp;");
    response.append("<a href=\"http://"+ SearchEngine.HOST + ":" + SearchEngine.PORT + "/section?query=Arts\"><h3>Arts</h3></a>&nbsp;");
    response.append("<a href=\"http://"+ SearchEngine.HOST + ":" + SearchEngine.PORT + "/section?query=Sports\"><h3>Sports</h3></a>&nbsp;");
    response.append("<a href=\"http://"+ SearchEngine.HOST + ":" + SearchEngine.PORT + "/section?query=Opinion\"><h3>Opinion</h3></a>&nbsp;");
    response.append("<a href=\"http://"+ SearchEngine.HOST + ":" + SearchEngine.PORT + "/section?query=Food\"><h3>Food</h3></a>");
    response.append("</nav>");
  }

  public void homePage(StringBuffer response){
    homeHeader(response);
    barSection(response);
    searchBar(response, null);
    generateFooter(response);
  }

  public void resultStyle(StringBuffer response){
    try{
      String easyjs=new String(Files.readAllBytes(Paths.get("include/jquery.easy-autocomplete.min.js")));
      String easycss=new String(Files.readAllBytes(Paths.get("include/easy-autocomplete.min.css")));
      String easycssthemes=new String(Files.readAllBytes(Paths.get("include/easy-autocomplete.themes.min.css")));

      response.append("<style>");
      response.append(easycss);
      response.append(easycssthemes);
      response.append("input[type=text] {\n" +
        "    width: 100%;\n" +
        "    box-sizing: border-box;\n" +
        "    border: 1px solid #7590f5;\n" +
        "    border-radius: 4px;\n" +
        "    font-size: 16px;\n" +
        "    background-color: white;\n" +
        "    background-position: 10px 10px;\n" +
        "    background-repeat: no-repeat;\n" +
        "    padding: 12px 12px 12px 12px;\n" +
        "}\n");
      response.append("ol.nobullet{list-style-type:none}");
      response.append("a:link{color:#1a0dab;text-decoration:none;}");
      response.append("a:visited{color:#609}");
      response.append("a:hover{color:#ff6600;text-decoration:underline;}");
      response.append(".bar{width: 100%;}");
      response.append("#sec:hover{color:#ff6600}");
      response.append("#bar{float:left;width:55%;padding-top:25px;}");
      response.append("#img{float:left;width:20%;}");
      response.append("main table{width:100%; margin-left:0%;margin-right:0%}");
      response.append("#trend{padding-left:10%;padding-top:10%}");
      response.append("#section{padding-top:10%}");
      response.append("nav h1{font-size:20px;padding-left:25px;color:#000}");
      response.append("nav div{line-height: 2em}");
      response.append("nav.sections a{text-decoration:none;color:#1a0dab}");
      response.append("nav.trends {border:1px solid #e2e2e6}");
      response.append("nav.trends ol{padding-left:55px;}");
      response.append("nav.trends li a{font-size: 16px;padding-left:15px}");
      response.append("main form{text-align: center}");
      response.append("main p a{font-size:17px}");
      response.append("main p{font-size:15px}");
      response.append("main table p span.time{font-size: small; color: #808080}");
      response.append("footer div{text-align: left; color:#777; padding-top:20px;padding-left:25%}");
      response.append("</style>");
    response.append("<script src=\"//code.jquery.com/jquery-1.11.2.min.js\"></script>\n");
    response.append("<script>");
    response.append(easyjs);
    response.append("</script>");
    }catch (IOException e){
      e.printStackTrace();
    }
  }

  public void homeStyle(StringBuffer response){
    try{
      String easyjs=new String(Files.readAllBytes(Paths.get("include/jquery.easy-autocomplete.min.js")));
      String easycss=new String(Files.readAllBytes(Paths.get("include/easy-autocomplete.min.css")));
      String easycssthemes=new String(Files.readAllBytes(Paths.get("include/easy-autocomplete.themes.min.css")));
      response.append("<style>");
      response.append(easycss);
      response.append(easycssthemes);
      response.append("input[type=text] {\n" +
          "    width: 100%;\n" +
          "    box-sizing: border-box;\n" +
          "    border: 1px solid #7590f5;\n" +
          "    border-radius: 4px;\n" +
          "    font-size: 16px;\n" +
          "    padding: 12px 12px 12px 12px;\n" +
          "}\n");
      response.append("body form{text-align:left;margin-left: 20%;margin-right:20%; padding-bottom:5%;}");
      response.append("body footer div p{text-align: center; color:#777; padding-top:20px;}");
      response.append("nav{text-align: center; padding-bottom: 15px}");
      response.append("nav h3{display:inline}");
      response.append("a:link{color:#1a0dab;}");
      response.append("a:visited{color:#609}");
      response.append("a:hover{color:#ff6600;text-decoration:underline;}");
      response.append("</style>");
      response.append("<script src=\"//code.jquery.com/jquery-1.11.2.min.js\"></script>\n");
      response.append("<script>");
      response.append(easyjs);
      response.append("</script>");}catch (IOException e){
      e.printStackTrace();
    }
  }

  public void searchBoxScript(StringBuffer response){
    response.append("<script>");
    response.append("var options = {\n" +
        "\turl: function(phrase) {\n" +
        "\t\treturn \"http://" + SearchEngine.HOST + ":" + SearchEngine.PORT + "/auto?phrase=\" + phrase ;\n" +
        "\t},\n" +
        "};\n" +
        "$(\"#autocomplete\").easyAutocomplete(options);");
    response.append("</script>");
  }

  public void searchBar(StringBuffer response, String queryString) {
    response.append("<form ; action=\"http://"+ SearchEngine.HOST +":" + SearchEngine.PORT + "/search?\" method=\"get\">" + "\n");
    if (queryString == null || queryString.length() == 0) {
      response.append("<input type=\"text\" name=\"query\" placeholder=\"Search here...\" id=\"autocomplete\">");
    } else {
      response.append("<input type=\"text\" name=\"query\" placeholder=\"Search here...\" id=\"autocomplete\" value=\"" + queryString + "\">");
    }
    searchBoxScript(response);
    response.append("</form>" + "\n");
  }
}