package com.rapleaf;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.Set;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import com.rapleaf.R;
import android.R.string;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.CommonDataKinds.*;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.net.Uri;
import android.os.Bundle;

public class RapleafActivity extends Activity {
    private static final String DEBUG_TAG = null;
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }

	
    final int CONTACT_PICKER_RESULT = 1001;
    public void doLaunchContactPicker(View view){
    	Intent contactPickerIntent = new Intent(Intent.ACTION_PICK, Contacts.CONTENT_URI);
    	startActivityForResult(contactPickerIntent, CONTACT_PICKER_RESULT);
    }
    public void submitOnClick(View view){
    	String getRapLeafRequest = getRapLeafRequest();
    	try {
    		JSONObject jsonobject = new JSONObject(getRapLeafRequest);
    		String resultString = jsonobject.toString();
    		resultString = resultString.substring(1, resultString.length()-2);
    		resultString = resultString.replace('"', ' ');
    		resultString = resultString.replaceAll(",", "\n");
    		TextView results = (TextView)findViewById(R.id.results);
			results.setText(resultString);
    		
    			Log.v(DEBUG_TAG, jsonobject.toString());
    		//}
    	} catch (Exception e){
    		e.printStackTrace();
    	}
    	
    	
    }
    public String getRapLeafRequest(){
    	String firstName = "";
    	String lastName = "";
    	String beforeAt = "";
    	String afterAt = "";
    	EditText setFirstName = (EditText)findViewById(R.id.name_form);
    	firstName = setFirstName.getEditableText().toString();
    	int spaceIndex = 0;
    	if (firstName.contains(" "))
    		spaceIndex = firstName.indexOf(" ");
    	Log.v(DEBUG_TAG, "spaceIdx: " + spaceIndex);
    	lastName = firstName.substring(spaceIndex+1);
    	firstName = firstName.substring(0, spaceIndex);
    	Log.v(DEBUG_TAG, "First Name = " + firstName);
    	Log.v(DEBUG_TAG, "Last Name = " + lastName);
    	EditText setEmail = (EditText)findViewById(R.id.email_form);
    	beforeAt= setEmail.getEditableText().toString();
    	int atIndex = 0;
    	if (beforeAt.contains("@"))
    		atIndex = beforeAt.indexOf("@");
    	else 
    		Log.v(DEBUG_TAG, "not a valid email address");
    	afterAt = beforeAt.substring(atIndex+1);
    	beforeAt = beforeAt.substring(0, atIndex);
    	Log.v(DEBUG_TAG, "beforeAt =" + beforeAt);
    	Log.v(DEBUG_TAG, "afterAt =" + afterAt);
    	String url = "https://personalize.rapleaf.com/v4/dr?first=" + firstName + "&last=" + lastName + "&email=" + beforeAt + "%40" + afterAt + "&api_key=7e67d0b8103f766d9333abd7e7ee6c5f";
    		
    	StringBuilder builder = new StringBuilder();
    	HttpClient httpclient = new DefaultHttpClient();
    	HttpGet httpGet = new HttpGet(url);
    	try {
    		HttpResponse response = httpclient.execute(httpGet);
    		StatusLine statusline = response.getStatusLine();
    		int statusCode = statusline.getStatusCode();
    		if (statusCode == 200){ //no error
    			HttpEntity entity = response.getEntity();
    			InputStream content = entity.getContent();
    			BufferedReader reader = new BufferedReader(new InputStreamReader(content));
    			String line;
    			while ((line = reader.readLine()) != null){
    				builder.append(line);
    			}
    		}
    		else {
    			Log.v(DEBUG_TAG, "Failed to download");
    		}
    		
    	}
    		 catch (ClientProtocolException e) {
    			 e.printStackTrace();
    	} catch(IOException e) {
    		e.printStackTrace();
    	}
    	return builder.toString();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
    	if (resultCode == RESULT_OK){
    		switch(requestCode){
    			case CONTACT_PICKER_RESULT:
    			
    			String email = "";
    			String name = "";
    			Cursor cursor = null;
    			
    			try{
    			Bundle extras = data.getExtras();
    			Set<String> keys = extras.keySet();
    			Iterator<String> it = keys.iterator();
    			while (it.hasNext()){
    				String key = it.next();
    				Log.v(DEBUG_TAG, key + "[" + extras.get(key) + "]");
    			}
    			Uri result = data.getData(); //get data returned from the contact picker
    			Log.v(DEBUG_TAG, "retrieved result: " + result.toString());
    			
    			String id = result.getLastPathSegment(); //get id for the contact
    			Log.v(DEBUG_TAG, "element: " + id.toString());
    			
    			cursor = getContentResolver().query(Email.CONTENT_URI, null, Email.CONTACT_ID + "=?", new String[]{id}, null);    			
    			cursor.moveToFirst();  
    			String columns[] = cursor.getColumnNames();  
    			for (String column : columns) {  
    			    int index = cursor.getColumnIndex(column);  
    			    Log.v(DEBUG_TAG, "Column: " + column + " == ["  
    			            + cursor.getString(index) + "]"); 
    			} 
    			if (cursor.moveToFirst()) {
    				int emailIdx = cursor.getColumnIndex(Email.DATA);//email addresses stored in column Email.DATA
    				int nameIdx = cursor.getColumnIndex("display_name");
    				email = cursor.getString(emailIdx);
    				Log.v(DEBUG_TAG, "nameIdx: " + StructuredName.DISPLAY_NAME);
    				name = cursor.getString(nameIdx);
    				
    				Log.v(DEBUG_TAG, "read email address: " + email);
    			}
    			else Log.w(DEBUG_TAG, "No results");
    			}
    			catch (Exception e) {
    				Log.e(DEBUG_TAG, "Failed to get email data");
    				Log.e(DEBUG_TAG, e.getMessage());
    			}
    			finally {
    				if (cursor != null)
    					cursor.close();
    			
    			EditText nameEntry = (EditText)findViewById(R.id.name_form);
    			nameEntry.setText(name);
        		if (name.length() == 0){
        			Toast.makeText(this, "I didn't detect a name :O", Toast.LENGTH_LONG).show();
        		} 
    			EditText emailEntry = (EditText)findViewById(R.id.email_form);
    			emailEntry.setText(email);
    			if (email.length() == 0){
    				Toast.makeText(this, "I didn't detect an email :O", Toast.LENGTH_LONG).show();
    			}
    			
    			
    			
    		
    		}
    			break;
    	}
    			   		
    		
    	}
 
    		else {
    			Log.w(DEBUG_TAG, "Warning: activity result is invalid");
    	}
    }

 }
