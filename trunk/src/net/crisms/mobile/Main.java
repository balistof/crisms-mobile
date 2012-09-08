package net.crisms.mobile;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class Main extends Activity implements OnClickListener {
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        if (!isLoginSet()) {
        	showPreferences();
        }
        
        AutoCompleteTextView toAutoComplete = (AutoCompleteTextView)findViewById(R.id.toTextView);
        //toAutoComplete.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
        FillAutocompleteTextView(toAutoComplete);
        
        Button sendButton = (Button)findViewById(R.id.sendButton);
        sendButton.setOnClickListener(this);
    }
    
    private boolean isLoginSet() {
    	SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
    	String userName = sharedPrefs.getString(Preferences.userNameKey, "");
    	String password = sharedPrefs.getString(Preferences.passwordKey, "");
    	
    	return userName != "" && password != "";
    }
    
    private void FillAutocompleteTextView(AutoCompleteTextView toAutoComplete) {
    	ArrayList<String> items= new ArrayList<String>();
    	// iterate over all contacts with numbers
    	Cursor cursor = getContentResolver().query(Phone.CONTENT_URI, null, null, null, null);
    	int phoneIdx = cursor.getColumnIndex(Phone.DATA);
    	int nameIdx = cursor.getColumnIndex(Contacts.DISPLAY_NAME_PRIMARY);
    	int hasPhoneNumberIdx = cursor.getColumnIndex(Contacts.HAS_PHONE_NUMBER);
    	if (cursor.moveToFirst()) {
            while (cursor.isAfterLast() == false) {
            	if (cursor.getInt(hasPhoneNumberIdx) > 0)
            	{
	                String phoneNumber = cursor.getString(phoneIdx);
	                String displayName = cursor.getString(nameIdx);
	                items.add(displayName + " <" + phoneNumber + ">");
            	}
                cursor.moveToNext();
            }
        } else {
            //no results actions
        } 
    	
    	String item[] = {};
    	toAutoComplete.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, items.toArray(item)));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
	    	case R.id.menu_settings:
	            showPreferences();
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
    	}
    }
    
    private void showPreferences() {
    	Intent intent = new Intent(this, Preferences.class);
        startActivity(intent);
    }

    private void sendMessage() {
    	SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
    	String userName = sharedPrefs.getString(Preferences.userNameKey, "");
    	String password = sharedPrefs.getString(Preferences.passwordKey, "");
    	String senderId = sharedPrefs.getString(Preferences.senderIdKey, "");
    	EditText messageEditText = (EditText) findViewById(R.id.messageText);
    	String message = messageEditText.getText().toString();
    	TextView toTextView = (TextView) findViewById(R.id.toTextView);
    	String to = toTextView.getText().toString().replace("+", "");
    	
		if (to.contains("<"))
		{
    		int start = to.lastIndexOf("<");
    		to = to.substring(start + 1, to.indexOf(">", start));
		}
    	
		// make a send message view and show this view after the to field
		TextView sent_message_text = (TextView) findViewById(R.id.sent_message_text);
		TextView sent_message_status = (TextView) findViewById(R.id.sent_message_status);
		sent_message_text.setText(message);
		sent_message_status.setText(R.string.sending);
		sent_message_text.setVisibility(View.VISIBLE);
		sent_message_status.setVisibility(View.VISIBLE);
		
    	new SendMessageTask().execute(userName, password, senderId, to, message);
    }
    
    public static String HtmlPostToGateway(String serverFile, List<NameValuePair> nameValuePairs) throws IOException
    {
    	HttpURLConnection urlConnection = null;
    	try {
    		URL url = new URL("http://gateway.crisms.net/" + serverFile);
        	urlConnection = (HttpURLConnection) url.openConnection();
    		urlConnection.setDoOutput(true);

    		StringBuilder params = new StringBuilder();
    		boolean first = true;
    		for (NameValuePair item : nameValuePairs) {
    			if (first)
    				first = false;
    			else {
    				params.append("&");
    			}
    			params.append(item.getName());
    			params.append("=");
    			params.append(URLEncoder.encode(item.getValue(), "UTF-8"));
    		}
    		
    		String postData = params.toString();
    		urlConnection.setFixedLengthStreamingMode(postData.getBytes().length);
    		urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
    		PrintWriter out = new PrintWriter(urlConnection.getOutputStream());
    		out.print(postData);
    		out.close();

    		//build the string to store the response text from the server
    		StringBuilder response = new StringBuilder();;

    		//start listening to the stream
    		Scanner inStream = new Scanner(urlConnection.getInputStream());

    		//process the stream and store it in StringBuilder
    		while(inStream.hasNextLine())
    			response.append(inStream.nextLine());
            
            return response.toString();
    	}
		finally {
			if (urlConnection != null)
				urlConnection.disconnect();
		}
    }
    
    private void AddMessageToSentFolder(String number, String message) {
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		if (!sharedPrefs.getBoolean(Preferences.addToOutboxKey, false))
			return;
    	
    	ContentValues values = new ContentValues();

    	values.put("address", number);
    	values.put("body", message); 

    	getApplicationContext().getContentResolver().insert(Uri.parse("content://sms/sent"), values);
    }

	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.sendButton:
			sendMessage();
			break;
		}
	}
	
	class SendMessageTask extends AsyncTask<String, Void, Void> {

	    private Exception exception;
	    private String result;
	    private String to;
	    private String message;

	    protected Void doInBackground(String... data) {
	        try {        	
	        	to = data[3];
	        	message = data[4];
	        	List<NameValuePair> postData = new ArrayList<NameValuePair>();
	        	postData.add(new BasicNameValuePair("user", data[0]));
	        	postData.add(new BasicNameValuePair("password", data[1]));
	        	postData.add(new BasicNameValuePair("from", data[2]));
	        	postData.add(new BasicNameValuePair("mobnumber", data[3]));
	        	postData.add(new BasicNameValuePair("message", data[4]));
	        	
	            result = Main.HtmlPostToGateway("sendsms.php", postData);
	        } catch (Exception e) {
	            this.exception = e;
	        }
			return null;
	    }

	    protected void onPostExecute(Void res) {
	    	if (exception != null) {
				Context context = getApplicationContext();
				int duration = Toast.LENGTH_SHORT;
				Toast.makeText(context, R.string.send_general_failure, duration).show();
				TextView sent_message_text = (TextView) findViewById(R.id.sent_message_text);
				TextView sent_message_status = (TextView) findViewById(R.id.sent_message_status);
				sent_message_text.setVisibility(View.GONE);
				sent_message_status.setVisibility(View.GONE);
			}
	    	else if (result.contains("ID")) {
	    		EditText messageEditText = (EditText) findViewById(R.id.messageText);
	    		messageEditText.setText("");
	        	TextView toTextView = (TextView) findViewById(R.id.toTextView);
	        	toTextView.setText("");
				Context context = getApplicationContext();
				int duration = Toast.LENGTH_SHORT;
				Toast.makeText(context, R.string.send_success, duration).show();
				TextView sent_message_status = (TextView) findViewById(R.id.sent_message_status);
				sent_message_status.setText(R.string.sent);
				AddMessageToSentFolder(to, message);
			}
			else {
				Context context = getApplicationContext();
				int duration = Toast.LENGTH_SHORT;
				Toast.makeText(context, result, duration).show();
				TextView sent_message_text = (TextView) findViewById(R.id.sent_message_text);
				TextView sent_message_status = (TextView) findViewById(R.id.sent_message_status);
				sent_message_text.setVisibility(View.GONE);
				sent_message_status.setVisibility(View.GONE);
			}
	    }
	 }
}
