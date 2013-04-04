package oauth.googleapi.adapters;

import java.util.ArrayList;

import oauth.googleapi.authentication.R;
import oauth.googleapi.helper.GoogleCalendarDataStore;
import oauth.googleapi.objects.CalendarEventItems;
import oauth.googleapi.objects.CalendarEvents;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

public class CalendarEventListAdapter extends ArrayAdapter<String> {

	Context context;
	static String TAG="CalendarEventListAdapter";
	String calendarId;
	
	public CalendarEventListAdapter(Context context, String calendarId) {
		super(context, 0);
		this.context=context;
		this.calendarId=calendarId;
	}
	
	public View getView(int position, View convertView, ViewGroup parent){
    
		LinearLayout row;
        if(convertView == null){
            LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = (LinearLayout)inflater.inflate(R.layout.tworows, null);
        }else{
            row = (LinearLayout)convertView;
        }
        CalendarEvents events=GoogleCalendarDataStore.getInstance().getCalendarEventsList();
        ArrayList<CalendarEventItems> items_arr=events.getItems();
        
        CalendarEventItems items = items_arr.get(position);
        ((TextView)row.findViewById(R.id.textView1)).setText(items.getSummary());
        ((TextView)row.findViewById(R.id.textView2)).setText(items.getLocation());
        
        row.setTag(items);	//calendarId=eventId
        return row;
    }

	public int getCount() {
		CalendarEvents events=GoogleCalendarDataStore.getInstance().getCalendarEventsList();
		if(events==null){
			Log.d(TAG,"GetCount: CalendarEventList class instance is null. Aborting drawing listview.");
			return 0;
		}
        ArrayList<CalendarEventItems> items_arr=events.getItems();
        if(items_arr==null){
			Log.d(TAG,"GetCount: CalendarEventItems Class instance is null. Aborting drawing listview.");
			return 0;
		}
		return items_arr.size();
	}

}
