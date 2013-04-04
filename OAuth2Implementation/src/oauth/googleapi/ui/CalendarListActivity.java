package oauth.googleapi.ui;

import java.util.LinkedList;
import java.util.List;

import oauth.googleapi.adapters.CalendarListAdapter;
import oauth.googleapi.authentication.R;
import oauth.googleapi.helper.AuthConstants;
import oauth.googleapi.helper.ConversionHelper;
import oauth.googleapi.helper.GoogleCalendarDataStore;
import oauth.googleapi.helper.ServiceHelper;
import oauth.googleapi.objects.Calendar;
import oauth.googleapi.objects.CalendarList;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class CalendarListActivity extends GoogleCalendarBase {

	GoogleCalendarDataStore dataStore;
	static String TAG="CalendarListActivity";
	ListView listview;
	CalendarListAdapter adapter;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.calendarlist_activity);
		listview=(ListView)findViewById(R.id.listView1);
		adapter=new CalendarListAdapter(getBaseContext());
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

	    final String url = AuthConstants.CALENDAR_LIST_URI+"?"+ paramString;
		Thread t =new Thread (new Runnable() {
			
			public void run() {
				
				JSONObject result=new ServiceHelper().getHttpGetResponse(url, getBaseContext());
				Message message=handler.obtainMessage();
				
				if(result==null){
					Log.d(TAG,"Http response of CalendarList (JSONObject) is null");
					return;
				}
				
				message.obj=result;
				
				try {
					if(result.has("tpg_status_code"))
						message.what=result.getInt("tpg_status_code");
				} catch (JSONException e) {
					message.what=0;
					Log.d(TAG,"Http status code is unknown");
				}
				handler.handleMessage(message);
			}
		});
		t.start();
		//new AsynchronousTaskLoader(this, handler, null).execute(url);
	}
	Handler handler=new Handler(){
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			
			JSONObject result=(JSONObject)msg.obj;
			
			try {
				//Check for the status code and then proceed to parse the json response
				JSONArray items=result.getJSONArray("items");
				if(items==null){
					Log.d(TAG,"Handler: CalendarList response has no calendars.");
					return;
				}
				CalendarList calendarlist=new CalendarList();
				
				for(int index=0;  index<items.length();	index++){
					
					JSONObject parts=items.getJSONObject(index);
					Calendar calendar=new Calendar();
					
					String id=new ConversionHelper().tryParseString(parts.opt("id"));
					String summary=new ConversionHelper().tryParseString(parts.opt("summary"));
					String desc=new ConversionHelper().tryParseString(parts.opt("description"));
					
					calendar.setId(id);
					calendar.setSummary(summary);
					calendar.setDescription(desc);
					//Log.d(TAG,"Calendar "+index+":  Id="+id+"  summary="+summary+"   desc="+desc);
					calendarlist.getList().add(calendar);
					GoogleCalendarDataStore.getInstance().setCalendarlist(calendarlist);
				}
				Log.d(TAG,"CalendarList and Calendar Objects are set in the bean class");
			} catch (JSONException e) {
				Log.d(TAG, "Handler: "+e.toString());
			}
			runOnUiThread(new Runnable(){
				public void run(){
					listview.setAdapter(adapter);
				}
			});
			listview.setOnItemClickListener(rowclick);
		}
	};
	
    OnItemClickListener rowclick=new OnItemClickListener() {
		
    	public void onItemClick(AdapterView<?> arg0, View v, int arg2,
				long arg3) {
			Object obj=v.getTag();
			if(obj!=null || obj instanceof String){
				String tag=(String)obj;
				Log.d(TAG,"OnClickListener: Calendar Id="+tag);
				Intent intent=new Intent(getBaseContext(),CalendarEventsActivity.class);
				intent.putExtra("calendarId", tag);
				startActivity(intent);
			}
			else
				Log.d(TAG,"OnClickListener: Could not retrieve the calendar Id="+obj);
		}
	};
}
