package oauth.googleapi.ui;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import oauth.googleapi.authentication.R;
import oauth.googleapi.helper.AuthConstants;
import oauth.googleapi.helper.ConversionHelper;
import oauth.googleapi.helper.GoogleCalendarDataStore;
import oauth.googleapi.helper.ServiceHelper;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

public class InsertEventActivity extends GoogleCalendarBase {
	
	static String TAG="InsertEventActivity";
	String calendarId;
	GoogleCalendarDataStore dataStore;
	ConversionHelper conversionHelper;
	 int status_code=0;
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle bundle=getIntent().getExtras();
		setContentView(R.layout.insert_event);
		
		if (bundle==null  || ! bundle.containsKey("calendarId")){
			Log.d(TAG,"OnCreate: This Activity does have the Calendar Id");
			return;
		}
		calendarId=bundle.getString("calendarId");
		conversionHelper=new ConversionHelper();
	}

	protected void onResume() {
		super.onResume();
		dataStore=GoogleCalendarDataStore.getInstance();
		
		if(dataStore==null)
			return;
		
	}
	public void insert_event_btn_clicked(View view){
		
		EditText edit=(EditText)findViewById(R.id.summary_editText);
		final String summary=conversionHelper.tryParseString(edit.getText().toString());
		
		edit=(EditText)findViewById(R.id.startdate_edit);
		final String startDate=conversionHelper.tryParseString(edit.getText().toString());
		
		edit=(EditText)findViewById(R.id.enddate_edit);
		final String endDate=conversionHelper.tryParseString(edit.getText().toString());
		
		edit=(EditText)findViewById(R.id.description_edit);
		final String description=conversionHelper.tryParseString(edit.getText().toString());
		
		edit=(EditText)findViewById(R.id.location_edit);
		final String location=conversionHelper.tryParseString(edit.getText().toString());
		
		final String accessToken=dataStore.getAccessToken();
		
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("access_token", accessToken));
	    String paramString = URLEncodedUtils.format(params, "utf-8");
	    
	    final String url = String.format(AuthConstants.CALENDAR_INSERT_EVENT_URI,URLEncoder.encode(calendarId))+"?"+ paramString;
	    
	    Thread t=new Thread(new Runnable(){
	    	public void run(){
	    		
	    		JSONObject mainobject=new JSONObject();
	    		try {
	    			JSONObject object=new JSONObject();
	    			object.put("date", startDate);
					mainobject.put("start",object);
					
					object=new JSONObject();
					object.put("date", endDate);
					mainobject.put("end",object);
					
		    		mainobject.put("summary",summary);
		    		mainobject.put("description", description);
		    		mainobject.put("location",location);
		    		
		    		//object.put("attendees[].email","khushboo@3pillarglobal.com");
		    		//object.put("reminders.overrides[].method","sms");
		    		//object.put("reminders.overrides[].minutes","10");
				} catch (JSONException e1) {
					Log.d(TAG,"InsertEvent()="+e1.toString());
				}
	    		
	    		JSONObject result=new ServiceHelper().getHttpPostResponse(url, null,mainobject,AuthConstants.CONTENT_TYPE_JSON);
				//final Message message=handler.obtainMessage();
				
				//message.obj=result;
				//message.arg1=2;
	    		
				if(result==null){
					Log.d(TAG,"Http response of CalendarEvents (JSONObject) is null");
					//message.what=400;			//error condition
				}
				else{
					try {
						if(result.has("tpg_status_code"))
							
							status_code=result.getInt("tpg_status_code");
					} catch (JSONException e) {
						
						Log.d(TAG,"Http status code is unknown");	//Handle 401(Login Required) error code, 404 (Not Found)
					}
				}
				
				runOnUiThread(new Runnable(){
					public void run(){
						cancelProgressDialog();
						if(status_code==200){
							showOkAlert(getResources().getString(R.string.inserted_the_event), true);
						}
						else {
							showOkAlert(getResources().getString(R.string.error_inserting_event), true);
						}
						
					}
				});
	    	}
	    });
	    showProgressDialog();
	    t.start();
	}
}
