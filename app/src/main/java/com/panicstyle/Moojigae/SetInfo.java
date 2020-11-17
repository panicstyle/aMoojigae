package com.panicstyle.Moojigae;

import android.content.Context;
import android.util.Log;

import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

/**
 * Created by david on 2015-07-04.
 */
public class SetInfo {
    public String m_userID;
    public String m_userPW;
    public String m_regId;
    public boolean m_pushYN;

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
            m_userID = obj.getString("id");
            m_userPW = obj.getString("pw");

            if (obj.has("push_yn")) {
                m_pushYN = obj.getBoolean("push_yn");
            } else {
                m_pushYN = true;
            }
        } catch(FileNotFoundException e){
            Log.e("FileNotFoundException", "can't find FileInputStream");
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
            obj.put("push_yn", m_pushYN);
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
