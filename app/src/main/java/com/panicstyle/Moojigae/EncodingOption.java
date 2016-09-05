package com.panicstyle.Moojigae;

import android.content.Context;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;

/**
 * Created by dykim on 2016-08-30.
 */
public class EncodingOption {

    public String getEncodingOption(Context context, HttpRequest httpRequest) {

        String url = GlobalConst.m_strServer + "/encoding.info";

        String result = httpRequest.requestGet(url, "", "utf-8");

        if (result.contains("euc-kr")) {
            return "euc-kr";
        } else {
            return "utf-8";
        }
    }

}
