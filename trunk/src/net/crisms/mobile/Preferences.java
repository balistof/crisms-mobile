package net.crisms.mobile;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

@TargetApi(11)
public class Preferences extends Activity implements OnClickListener {

	public static final String userNameKey = "username";
	public static final String passwordKey = "password";
	private static final String addToOutboxKey = "addtooutbox";
	public static final String senderIdKey = "senderid";
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        
        LoadSettings();
        
        Button loginButton = (Button)findViewById(R.id.login_button);
        loginButton.setOnClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_preferences, menu);
        return true;
    }

    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
            	SaveSettings();
            	// app icon in action bar clicked; go home
                Intent intent = new Intent(this, Main.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    public void onBackPressed() {
    	SaveSettings();
    	super.onBackPressed();
    }
    
    private void SaveSettings()
    {
    	SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
    	Editor prefEditor = sharedPrefs.edit();
    	TextView userNameTextView = (TextView) findViewById(R.id.username_text_box);
    	prefEditor.putString(userNameKey, userNameTextView.getText().toString());
    	TextView passwordTextView = (TextView) findViewById(R.id.password_text_box);
    	prefEditor.putString(passwordKey, passwordTextView.getText().toString());
    	CheckBox addToOutboxCheckBox = (CheckBox) findViewById(R.id.add_to_outbox_check_box);
    	prefEditor.putBoolean(addToOutboxKey, addToOutboxCheckBox.isChecked());
    	TextView senderIdTextView = (TextView) findViewById(R.id.sender_id_text_box);
    	prefEditor.putString(senderIdKey, senderIdTextView.getText().toString());
    	
    	prefEditor.commit();
    }
    
    private void LoadSettings()
    {
    	SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
    	String userName = sharedPrefs.getString(userNameKey, "");
    	TextView userNameTextView = (TextView) findViewById(R.id.username_text_box);
    	userNameTextView.setText(userName);

    	String password = sharedPrefs.getString(passwordKey, "");
    	TextView passwordTextView = (TextView) findViewById(R.id.password_text_box);
    	passwordTextView.setText(password);
    	
    	boolean addToOutbox = sharedPrefs.getBoolean(addToOutboxKey, true); 
    	CheckBox addToOutboxCheckBox = (CheckBox) findViewById(R.id.add_to_outbox_check_box);
    	addToOutboxCheckBox.setChecked(addToOutbox);
    	
    	String senderId = sharedPrefs.getString(senderIdKey, "");
    	TextView senderIdTextView = (TextView) findViewById(R.id.sender_id_text_box);
    	senderIdTextView.setText(senderId);
    }

	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.login_button:
			// test connection
			TextView usernameTextView = (TextView) findViewById(R.id.username_text_box);
			
	    	TextView passwordTextView = (TextView) findViewById(R.id.password_text_box);
	    	
				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
				NameValuePair userName = new BasicNameValuePair("user", usernameTextView.getText().toString());
				nameValuePairs.add(userName);
				NameValuePair password = new BasicNameValuePair("password", passwordTextView.getText().toString());
				nameValuePairs.add(password);
				
				new ConnectTask().execute(nameValuePairs);
				
			break;
		}
	}
	
	class ConnectTask extends AsyncTask<List<NameValuePair>, Void, Void> {

	    private Exception exception;
	    private String result;

	    protected Void doInBackground(List<NameValuePair>... data) {
	        try {
	            result = Main.HtmlPostToGateway("connect.php", data[0]);
	        } catch (Exception e) {
	            this.exception = e;
	        }
			return null;
	    }

	    protected void onPostExecute(Void res) {
	    	if (exception != null) {
				Context context = getApplicationContext();
				int duration = Toast.LENGTH_SHORT;
				Toast.makeText(context, R.string.login_general_failure, duration).show();
			}
	    	else if (result.contains("OK")) {
				Context context = getApplicationContext();
				int duration = Toast.LENGTH_SHORT;
				Toast.makeText(context, R.string.login_success, duration).show();
			}
			else {
				Context context = getApplicationContext();
				int duration = Toast.LENGTH_SHORT;
				Toast.makeText(context, R.string.login_auth_failure, duration).show();
			}
	    }
	 }
}
