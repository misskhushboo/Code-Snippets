package oauth.googleapi.ui;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import oauth.googleapi.adapters.CalendarEventListAdapter;
import oauth.googleapi.authentication.R;
import oauth.googleapi.helper.AuthConstants;
import oauth.googleapi.helper.ConversionHelper;
import oauth.googleapi.helper.GoogleCalendarDataStore;
import oauth.googleapi.helper.ServiceHelper;
import oauth.googleapi.objects.CalendarEventItems;
import oauth.googleapi.objects.CalendarEvents;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

public class CalendarEventsActivity extends GoogleCalendarBase{

	GoogleCalendarDataStore dataStore;
	static String TAG="CalendarEventsActivity";
	String calendarId;
	ListView listview;
	ConversionHelper conversionHelper;
	CalendarEvents events;
	ArrayAdapter<String> adapter;

	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle bundle=getIntent().getExtras();
		setContentView(R.layout.events_listview);
		
		if (bundle==null  || ! bundle.containsKey("calendarId")){
			Log.d(TAG,"OnCreate: This Activity does have the Calendar Id");
			return;
		}
		calendarId=bundle.getString("calendarId");
		listview=(ListView)findViewById(R.id.mainlistview);
		adapter=new CalendarEventListAdapter(getBaseContext(),calendarId);
		conversionHelper=new ConversionHelper();
	}

	protected void onResume() {
		super.onResume();
		dataStore=GoogleCalendarDataStore.getInstance();
		
		if(dataStore==null)
			return;
		final String accessToken=dataStore.getAccessToken();
		
		List<NameValuePair> params = new LinkedList<NameValuePair>();
		params.add(new BasicNameValuePair("access_token", accessToken));
	    String paramString = URLEncodedUtils.format(params, "utf-8");
	    
	    final String url = String.format(AuthConstants.CALENDAR_EVENTS_URI,URLEncoder.encode(calendarId))+"?"+ paramString;
		Thread t =new Thread (new Runnable() {
			
			public void run() {
				
				JSONObject result=new ServiceHelper().getHttpGetResponse(url, getBaseContext());
				Message message=handler.obtainMessage();
				
				if(result==null){
					Log.d(TAG,"Http response of CalendarEvents (JSONObject) is null");
					return;
				}
				
				message.obj=result;
				message.arg1=1;
				try {
					if(result.has("tpg_status_code"))
						message.what=result.getInt("tpg_status_code");
						
				} catch (JSONException e) {
					message.what=0;
					Log.d(TAG,"Http status code is unknown");	//Handle 401(Login Required) error code, 404 (Not Found)
				}
				handler.handleMessage(message);
			}
		});
		showProgressDialog();
		t.start();
		//new AsynchronousTaskLoader(this, handler, null).execute(url);
	}
	
	Handler handler=new Handler(){
		public void handleMessage(final Message msg) {
			super.handleMessage(msg);
			cancelProgressDialog();
			if(msg.what==403){
				Log.d(TAG,"403 error. Aborting process. Daily Limit for Unauthenticated use exceeded. Continued use requires signup");//Read the message from response.
				return;
			}
			if(msg.arg1==2){								//Handling the insertion of the Events in this present calendar
				runOnUiThread(new Runnable(){
					public void run(){
						if(msg.what==200){
							showOkAlert(getResources().getString(R.string.inserted_the_event), false);
						}
						else {
							showOkAlert(getResources().getString(R.string.error_inserting_event), false);
						}
					}
				});
				
			}
			else if(msg.arg1==1){							//handling the display of Events in this present calendar
				JSONObject result=(JSONObject)msg.obj;
				if(result==null){
					Log.d(TAG,"JSONn object in response is null. Aborting process");
					return;
				}
				try{
					events=new CalendarEvents();

					String summary=conversionHelper.tryParseString(result.opt("summary"));
					String time_zone=conversionHelper.tryParseString(result.opt("timeZone"));
					events.setSummary(summary);
					events.setTimezone(time_zone);

					JSONArray items=result.optJSONArray("items");
					if(items==null){
						Log.d(TAG,"Handler: There are no events in this calendar.");
						runOnUiThread(new Runnable(){
							public void run(){
								showOkAlert(getResources().getString(R.string.no_events), false);
							}
						});
						return;
					}
					for(int index=0;  index<items.length();	index++){
						JSONObject parts=items.getJSONObject(index);
						CalendarEventItems event_items=new CalendarEventItems();

						String id=conversionHelper.tryParseString(parts.opt("id"));
						String summary1=conversionHelper.tryParseString(parts.opt("summary"));
						String location=conversionHelper.tryParseString(parts.opt("location"));

						event_items.setId(id);
						event_items.setSummary(summary1);
						event_items.setLocation(location);

						JSONObject obj=parts.optJSONObject("start");
						if(obj!=null){
							String startDateTime=conversionHelper.tryParseString(obj.opt("dateTime"));
							if(startDateTime==null || startDateTime.length()==0){
								startDateTime=conversionHelper.tryParseString(obj.opt("date"));
							}
							event_items.setStartDateTime(startDateTime);
						}

						obj=parts.optJSONObject("end");
						if(obj!=null){
							String endDateTime=conversionHelper.tryParseString(obj.opt("dateTime"));
							if(endDateTime==null || endDateTime.length()==0){
								endDateTime=conversionHelper.tryParseString(obj.opt("date"));
							}
							event_items.setEndDateTime(endDateTime);
						}
						obj=parts.optJSONObject("organizer");
						if(obj!=null){
							String organizer=conversionHelper.tryParseString(obj.opt("displayName"));
							event_items.setOrganizer(organizer);
						}
						obj=parts.optJSONObject("creator");
						if(obj!=null){
							String creator=conversionHelper.tryParseString(obj.opt("displayName"));
							event_items.setCreator(creator);
						}
						JSONArray arr=parts.optJSONArray("attendees");
						if(arr!=null){
							int number=arr.length();
							event_items.setNumber_attendees(number);
						}
						events.getItems().add(event_items);
					}
					GoogleCalendarDataStore.getInstance().setCalendarEventslist(events);
					runOnUiThread(new Runnable(){
						public void run(){
							listview.setAdapter(adapter);
							//((Button)findViewById(R.id.insert_event_btn)).setVisibility(View.VISIBLE);
						}
					});
					listview.setOnItemClickListener(rowclick);
					
				}catch (JSONException e) {
					Log.d(TAG, "Handler: "+e.toString());
				}
			}
		}
	};
	
	OnItemClickListener rowclick=new OnItemClickListener() {
		
    	public void onItemClick(AdapterView<?> arg0, View v, int arg2,long arg3) {
			Object obj=v.getTag();
			
			if(obj!=null || obj instanceof CalendarEventItems ){
				
				CalendarEventItems items=(CalendarEventItems)obj;
				Log.d(TAG,items.getCreator()+"  "+items.getLocation()+"   "+items.getStartDateTime()+"   "+items.getEndDateTime()+"    "+items.getOrganizer()+"   "+items.getNumber_attendees());
				
				LayoutInflater inflater=getLayoutInflater();
				View view=inflater.inflate(R.layout.event_details, null);
				
				TextView tv=(TextView)view.findViewById(R.id.row1);
				tv.setText(items.getCreator());
				
				tv=(TextView)view.findViewById(R.id.row2);
				tv.setText(items.getLocation());
				tv=(TextView)view.findViewById(R.id.row3);
				tv.setText(items.getStartDateTime());
				tv=(TextView)view.findViewById(R.id.row4);
				tv.setText(items.getEndDateTime());
				tv=(TextView)view.findViewById(R.id.row5);
				tv.setText(items.getOrganizer());
				tv=(TextView)view.findViewById(R.id.row6);
				tv.setText(items.getNumber_attendees()+"");
				((TextView)view.findViewById(R.id.header)).setText(items.getSummary());
				
				final AlertDialog.Builder builder=new AlertDialog.Builder(CalendarEventsActivity.this);
				builder.setView(view);
				
				builder.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
			           }
			       });
				runOnUiThread(new Runnable(){
					public void run(){
						AlertDialog dialog = builder.create();
						Log.d(TAG,"Setting the background theme of the dialog.");
						dialog.getWindow().setTitle(null);
						dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
						dialog.show();
					}
				});
			}
			else
				Log.d(TAG,"OnClickListener on Event: Could not retrieve the CalendarEventItems instance="+obj);
		}
	};
	
	public void insert_event_btn_clicked(View view){
		Intent intent=new Intent(this,InsertEventActivity.class);
		intent.putExtra("calendarId", calendarId);
		startActivity(intent);
	}
}
