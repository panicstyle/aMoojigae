package com.panicstyle.Moojigae;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Xml;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class SetView extends Activity {
    static final int SETUP_CODE = 1;
	private String userID;
	private String userPW;

	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setview);    

        if (GetUserInfoXML(this)) {
	        EditText tID = (EditText)findViewById(R.id.editID); 
	        tID.setText(userID);        
	        EditText tPW = (EditText)findViewById(R.id.editPW); 
	        tPW.setText(userPW);        
		}

        findViewById(R.id.okbtn).setOnClickListener(mClickListener);
        findViewById(R.id.cancelbtn).setOnClickListener(mClickListener);
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
	        			userID = xpp.getText();
	        		} else if (type == 2) {
	        			userPW = xpp.getText();
	        		}
	        	}
	        	eventType = xpp.next();
	        }
	        System.out.println("End document");
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
    
    public void SaveData() {
    	
    	EditText textID = (EditText)findViewById(R.id.editID);
    	EditText textPW = (EditText)findViewById(R.id.editPW);

    	String userID = textID.getText().toString();    	
    	String userPW = textPW.getText().toString();    	

    	String fileName = "LoginInfo.xml";
/*    	
        File newxmlfile = new File("LoginInfo.xml");
        try{
                newxmlfile.createNewFile();
        }catch(IOException e){
                Log.e("IOException", "exception in createNewFile() method");
                System.out.println(e.getMessage());
        }
        //we have to bind the new file with a FileOutputStream
       
*/
        FileOutputStream fileos = null;        
        try{
                fileos = openFileOutput(fileName, Context.MODE_PRIVATE);
        }catch(FileNotFoundException e){
                Log.e("FileNotFoundException", "can't create FileOutputStream");
                System.out.println(e.getMessage());
        }
        //we create a XmlSerializer in order to write xml data
        XmlSerializer serializer = Xml.newSerializer();
        try 
        {
      
        	// we set the FileOutputStream as output for the serializer, using UTF-8 encoding
        	serializer.setOutput(fileos, "UTF-8");
            //Write <?xml declaration with encoding (if encoding not null) and standalone flag (if standalone not null)
            serializer.startDocument(null, Boolean.valueOf(true));
           //set indentation option
            serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
            //start a tag called "root"
            serializer.startTag(null, "LoginInfo");
            //i indent code just to have a view similar to xml-tree
   
            serializer.startTag(null, "ID");
            //set an attribute called "attribute" with a "value" for 
//            serializer.attribute(null, "data", userID);
            serializer.text(userID);
            serializer.endTag(null, "ID");            
            
            serializer.startTag(null, "Password");
            //set an attribute called "attribute" with a "value" for 
//            serializer.attribute(null, "data", userPW);
            serializer.text(userPW);
            serializer.endTag(null, "Password");            
            
            serializer.endTag(null, "LoginInfo");
            serializer.endDocument();
            //write xml data into the FileOutputStream
            serializer.flush();
            //finally we close the file stream
            fileos.close();
            Toast.makeText(this,  "Save Data", Toast.LENGTH_SHORT).show();
                       
        } catch (Exception e) {
         	Log.e("Exception","error occurred while creating xml file");
        }
    }
    
    public void CancelData() {
    	Toast.makeText(this,  "Cancel Data", Toast.LENGTH_SHORT).show();
    }
    
    Button.OnClickListener mClickListener = new View.OnClickListener()
    {
      public void onClick(View v)
      {
          switch (v.getId())
          {
          case R.id.okbtn:
               if (getParent() == null) {
               	setResult(Activity.RESULT_OK, new Intent());
               } else {
               	getParent().setResult(Activity.RESULT_OK, new Intent());
               }
               SaveData();
               break;
          case R.id.cancelbtn:
               if (getParent() == null) {
               	setResult(Activity.RESULT_CANCELED, new Intent());
               } else {
               	getParent().setResult(Activity.RESULT_CANCELED, new Intent());
               }
               CancelData();
               break;
          }
          finish();
      }
    };
}
