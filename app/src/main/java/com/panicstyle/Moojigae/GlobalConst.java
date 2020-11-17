package com.panicstyle.Moojigae;

/**
 * Created by dykim on 2016. 3. 1..
 */
public class GlobalConst {
    public static String m_strServerName = "jumin.moojigae.or.kr";
    public static String m_strServer = "http://" + m_strServerName;

    public static final String SET_COOKIE_KEY = "Set-Cookie";
    public static final String COOKIE_KEY = "Cookie";
    public static final String SESSION_COOKIE = "sessionid";

    public static final int NAVI_RECENT = 1;
    public static final int NAVI_BOARD = 2;
    public static final int NAVI_SITE = 3;
    public static final int NAVI_SETUP = 4;

    public static final int REQUEST_VIEW = 1;
    public static final int REQUEST_WRITE = 2;
    public static final int REQUEST_MODIFY = 3;
    public static final int REQUEST_COMMENT_WRITE = 4;
    public static final int REQUEST_COMMENT_MODIFY = 5;
    public static final int REQUEST_COMMENT_REPLY_VIEW = 6;
    public static final int REQUEST_COMMENT_MODIFY_VIEW = 7;
    public static final int REQUEST_COMMENT_DELETE_VIEW = 8;

    public static final int RESULT_DELETE = 2;
}
