package oauth.googleapi.adapters;

import java.util.ArrayList;

import oauth.googleapi.authentication.R;
import oauth.googleapi.helper.GoogleCalendarDataStore;
import oauth.googleapi.objects.Calendar;
import oauth.googleapi.objects.CalendarList;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TwoLineListItem;

public class CalendarListAdapter extends ArrayAdapter<String> {
	
	Context context;
	static String TAG="CalendarListAdapter";
	public CalendarListAdapter(Context context) {
		super(context, 0);
		this.context=context;
	}
	
	public View getView(int position, View convertView, ViewGroup parent){
    
		LinearLayout row;
        if(convertView == null){
            LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = (LinearLayout)inflater.inflate(R.layout.tworows, null);
        }else{
            row = (LinearLayout)convertView;
        }
        CalendarList calendarlist=GoogleCalendarDataStore.getInstance().getCalendarlist();
        ArrayList<Calendar> calendars=calendarlist.getList();
        
        Calendar calendar = calendars.get(position);
        ((TextView)row.findViewById(R.id.textView1)).setText(calendar.getSummary());
        ((TextView)row.findViewById(R.id.textView2)).setText(calendar.getDescription());
        row.setTag(calendar.getId());

        return row;
    }

	public int getCount() {
		CalendarList calendarlist=GoogleCalendarDataStore.getInstance().getCalendarlist();
		if(calendarlist==null){
			Log.d(TAG,"GetCount: CalendarList class instance is null. Aborting drawing listview.");
			return 0;
		}
        ArrayList<Calendar> calendars=calendarlist.getList();
        if(calendars==null){
			Log.d(TAG,"GetCount: Calendar Class instance is null. Aborting drawing listview.");
			return 0;
		}
		return calendars.size();
	}
}
