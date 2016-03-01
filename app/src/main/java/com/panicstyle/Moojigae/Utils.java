package com.panicstyle.Moojigae;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by dykim on 2016-02-13.
 */
public class Utils {
    public static String getMatcherFirstString(String strPattern, String strContent) {
        Matcher m = getMatcher(strPattern, strContent);

        if (m.find()) { // Find each match in turn; String can't do this.
            return m.group(0);
        } else {
            return "";
        }
    }

    public static String getMatcherString(String strPattern, String strContent, int nCount) {
        Matcher m = getMatcher(strPattern, strContent);

        if (m.find()) { // Find each match in turn; String can't do this.
            if (m.groupCount() > nCount - 1) {
                return m.group(nCount);
            } else {
                return "";
            }
        } else {
            return "";
        }
    }

    public static Matcher getMatcher(String strPattern, String strContent) {
        Pattern p = Pattern.compile(strPattern, Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(strContent);

        return m;
    }

    public static String repalceHtmlSymbol(String strSrc) {
        String strDest = strSrc;
        strDest=strDest.replaceAll("\n","");
        strDest=strDest.replaceAll("\r","");
        strDest=strDest.replaceAll("<div><br></div>","\n");
        strDest=strDest.replaceAll("<br>","\n");
        strDest=strDest.replaceAll("<br/>","\n");
        strDest=strDest.replaceAll("<br />","\n");
        strDest=strDest.replaceAll("<div><div><div>","<div>");
        strDest=strDest.replaceAll("<div>","\n");
        strDest=strDest.replaceAll("&nbsp;"," ");
        strDest=strDest.replaceAll("&lt;","<");
        strDest=strDest.replaceAll("&gt;",">");
        strDest=strDest.replaceAll("&amp;","&");
        strDest=strDest.replaceAll("&quot;","\"");
        strDest=strDest.replaceAll("&apos;","'");
        strDest=strDest.replaceAll("&#4514;",":");
//        strDest=strDest.replaceAll("(<div>)(.|\\n)*?(</div>)","\n(.|\n)*?");
        strDest=strDest.replaceAll("(<b>\\[)\\d+(\\]</b>)", "");
        strDest=strDest.replaceAll("(<!--)(.|\\n)*?(-->)", "");
        strDest=strDest.replaceAll("(<style)(.|\\n)*?(/style>)", "");
        strDest=strDest.replaceAll("(<script)(.|\\n)*?(/script>)", "");
        strDest=strDest.replaceAll("(<)(.|\\n)*?(>)","");

        strDest=strDest.trim();

        return strDest;
    }

    public static String repalceTag(String strSrc) {
        String strDest = strSrc;
        strDest=strDest.replaceAll("(<)(.|\\n)*?(>)","");

        strDest=strDest.trim();

        return strDest;
    }
}
