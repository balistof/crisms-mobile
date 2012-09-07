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
    
//    public void doLaunchContactPicker(View view) {
//    	Intent contactPickerIntent = new Intent(Intent.ACTION_PICK, Contacts.CONTENT_URI);
//    	startActivityForResult(contactPickerIntent, CONTACT_PICKER_RESULT);
//    }
//    
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if (resultCode == RESULT_OK) {
//            switch (requestCode) {
//            case CONTACT_PICKER_RESULT:
//            	final EditText phoneInput = (EditText) findViewById(R.id.toText);
//                Cursor cursor = null;  
//                String phoneNumber = "";
//                List<String> allNumbers = new ArrayList<String>();
//                int phoneIdx = 0;
//                try {  
//                    Uri result = data.getData();  
//                    String id = result.getLastPathSegment();  
//                    cursor = getContentResolver().query(Phone.CONTENT_URI, null, Phone.CONTACT_ID + "=?", new String[] { id }, null);  
//                    phoneIdx = cursor.getColumnIndex(Phone.DATA);
//                    if (cursor.moveToFirst()) {
//                        while (cursor.isAfterLast() == false) {
//                            phoneNumber = cursor.getString(phoneIdx);
//                            allNumbers.add(phoneNumber);
//                            cursor.moveToNext();
//                        }
//                    } else {
//                        //no results actions
//                    }  
//                } catch (Exception e) {  
//                   //error actions
//                } finally {  
//                    if (cursor != null) {  
//                        cursor.close();
//                    }
//
//                    final CharSequence[] items = allNumbers.toArray(new String[allNumbers.size()]);
//                    AlertDialog.Builder builder = new AlertDialog.Builder(Main.this);
//                    builder.setTitle("Choose a number");
//                    builder.setItems(items, new DialogInterface.OnClickListener() {
//                        public void onClick(DialogInterface dialog, int item) {
//                            String selectedNumber = items[item].toString();
//                            selectedNumber = selectedNumber.replace("-", "");
//                            phoneInput.setText(selectedNumber);
//                        }
//                    });
//                    AlertDialog alert = builder.create();
//                    if(allNumbers.size() > 1) {
//                        alert.show();
//                    } else {
//                        String selectedNumber = phoneNumber.toString();
//                        selectedNumber = selectedNumber.replace("-", "");
//                        phoneInput.setText(selectedNumber);
//                    }
//
//                    if (phoneNumber.length() == 0) {  
//                        //no numbers found actions  
//                    }  
//                } 
//                break;
//            }
//
//        } else {
//            // gracefully handle failure
//            // Log.w(DEBUG_TAG, "Warning: activity result not ok");
//        }
//    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
//    	switch (item.getItemId()) {
//        case android.R.id.:
    	// app icon in action bar clicked; go home
            showPreferences();
            return true;
//        default:
//            return super.onOptionsItemSelected(item);
//    	}
    }
    
    private void showPreferences() {
    	Intent intent = new Intent(this, Preferences.class);
        startActivity(intent);
    }

    private void sendMessage() {
    	SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
    	String userName = sharedPrefs.getString(Preferences.userNameKey, "");
    	String password = sharedPrefs.getString(Preferences.passwordKey, "");
    	String senderId = sharedPrefs.getString(Preferences.senderIdKey, "criSMS");
    	EditText messageEditText = (EditText) findViewById(R.id.messageText);
    	String message = messageEditText.getText().toString();
    	TextView toTextView = (TextView) findViewById(R.id.toTextView);
    	String to = toTextView.getText().toString().replace("+", "");
    	
    	if (to.contains("<"))
    		to = to.substring(to.lastIndexOf("<") + 1, to.lastIndexOf(">"));
    	
    		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
    		nameValuePairs.add(new BasicNameValuePair("user", userName));
    		nameValuePairs.add(new BasicNameValuePair("password", password));
    		nameValuePairs.add(new BasicNameValuePair("mobnumber", to));
    		nameValuePairs.add(new BasicNameValuePair("from", senderId));
    		nameValuePairs.add(new BasicNameValuePair("message", message));
    		new SendMessageTask().execute(nameValuePairs);
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

//    private void AddMessageToSentFolder(String number, String message) {
//    	ContentValues values = new ContentValues();
//
//    	values.put("address", number);
//    	values.put("body", message); 
//
//    	getApplicationContext().getContentResolver().insert(Uri.parse("content://sms/sent"), values);
//    }

	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.sendButton:
			sendMessage();
			break;
		}
	}
	
	class SendMessageTask extends AsyncTask<List<NameValuePair>, Void, Void> {

	    private Exception exception;
	    private String result;

	    protected Void doInBackground(List<NameValuePair>... data) {
	        try {
	            result = Main.HtmlPostToGateway("sendsms.php", data[0]);
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
			}
	    	else if (result.contains("ID")) {
	    		EditText messageEditText = (EditText) findViewById(R.id.messageText);
	    		messageEditText.setText("");
	        	TextView toTextView = (TextView) findViewById(R.id.toTextView);
	        	toTextView.setText("");
				Context context = getApplicationContext();
				int duration = Toast.LENGTH_SHORT;
				Toast.makeText(context, R.string.send_success, duration).show();
			}
			else {
				Context context = getApplicationContext();
				int duration = Toast.LENGTH_SHORT;
				Toast.makeText(context, result, duration).show();
			}
	    }
	 }
}
