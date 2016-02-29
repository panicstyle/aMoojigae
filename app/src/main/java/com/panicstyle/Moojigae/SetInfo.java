package com.panicstyle.Moojigae;

import android.content.Context;
import android.util.Log;

import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;

/**
 * Created by david on 2015-07-04.
 */
public class SetInfo {
    public String m_userID;
    public String m_userPW;

    public Boolean CheckVersionInfo(Context context) {
        String fileName = "info.json";
        byte[] tmp = new byte[1024];

        FileInputStream fileos = null;
        try {
            fileos = context.openFileInput(fileName);
            fileos.read(tmp);
            fileos.close();
            String s = new String(tmp, 0, tmp.length);
            JSONObject obj = new JSONObject(s);
            String ver = (String)obj.get("ver");
            String currVer = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
            if (ver.equalsIgnoreCase(currVer)) {
                return true;
            } else {
                return false;
            }

        } catch(FileNotFoundException e){
            Log.e("FileNotFoundException", "can't create FileInputStream");
            System.out.println(e.getMessage());
            return false;
        } catch (Exception e) {
            Log.e("Exception","error occurred while creating xml file");
            return false;
        }

    }

    public Boolean SaveVersionInfo(Context context) {
        String fileName = "info.json";
        String currVer = "";

        JSONObject obj = new JSONObject();

        FileOutputStream fileos = null;
        try {
            currVer = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
            obj.put("ver", currVer);
            fileos = context.openFileOutput(fileName, Context.MODE_PRIVATE);
            fileos.write(obj.toString().getBytes());
            fileos.flush();
            fileos.close();
        } catch(FileNotFoundException e){
            Log.e("FileNotFoundException", "can't create FileOutputStream");
            System.out.println(e.getMessage());
        } catch (Exception e) {
            Log.e("Exception", "error occurred while creating xml file");
        }
        return true;
    }

    public Boolean GetUserInfoXML(Context context) {
        InputStream in = null;
        String fileName = "LoginInfo.xml";
        try {
            FileInputStream input = context.openFileInput(fileName);
            in = new BufferedInputStream(input);
            StringBuffer out = new StringBuffer();
            byte[] buffer = new byte[4094];
            int readSize;
            while ( (readSize = in.read(buffer)) != -1) {
                out.append(new String(buffer, 0, readSize));
            }
            String data = out.toString();

            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();

            xpp.setInput( new StringReader(data) );
            int eventType = xpp.getEventType();
            int type = 0;
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if(eventType == XmlPullParser.START_DOCUMENT) {
                    System.out.println("Start document");
                } else if(eventType == XmlPullParser.START_TAG) {
                    System.out.println("Start tag "+xpp.getName());
                    String strTag = xpp.getName();
                    if (strTag.equalsIgnoreCase("ID")) {
                        type = 1;
                    } else if (strTag.equalsIgnoreCase("Password")) {
                        type = 2;
                    } else {
                        type = 0;
                    }
                } else if (eventType == XmlPullParser.END_TAG) {
                    System.out.println("End tag "+xpp.getName());
                    type = 0;
                } else if (eventType == XmlPullParser.TEXT) {
                    System.out.println("Text "+xpp.getText());
                    if (type == 1) {
                        m_userID = xpp.getText();
                    } else if (type == 2) {
                        m_userPW = xpp.getText();
                    }
                }
                eventType = xpp.next();
            }
            System.out.println("End document");
            SaveUserInfo(context);
            return true;
        } catch( Exception e ) {
            System.out.println(e.getMessage());
            return false;
        } finally {
            if (in != null){
                try {
                    in.close();
                } catch( IOException ioe ) {
                }
            }
        }
    }

    public Boolean GetUserInfo(Context context) {
        String fileName = "login.json";
        byte[] tmp = new byte[1024];

        FileInputStream fileos = null;
        try {
            fileos = context.openFileInput(fileName);
            fileos.read(tmp);
            fileos.close();
            String s = new String(tmp, 0, tmp.length);
            JSONObject obj = new JSONObject(s);
            m_userID = (String)obj.get("id");
            m_userPW = (String)obj.get("pw");

        } catch(FileNotFoundException e){
            Log.e("FileNotFoundException", "can't create FileInputStream");
            System.out.println(e.getMessage());
            return false;
        } catch (Exception e) {
            Log.e("Exception","error occurred while creating xml file");
            return false;
        }
        return true;
    }

    public Boolean SaveUserInfo(Context context) {
        String fileName = "login.json";

        JSONObject obj = new JSONObject();

        FileOutputStream fileos = null;
        try {
            obj.put("id", m_userID);
            obj.put("pw", m_userPW);
            fileos = context.openFileOutput(fileName, Context.MODE_PRIVATE);
            fileos.write(obj.toString().getBytes());
            fileos.flush();
            fileos.close();
        } catch(FileNotFoundException e){
            Log.e("FileNotFoundException", "can't create FileOutputStream");
            System.out.println(e.getMessage());
        } catch (Exception e) {
            Log.e("Exception", "error occurred while creating xml file");
        }
        return true;
    }

}
