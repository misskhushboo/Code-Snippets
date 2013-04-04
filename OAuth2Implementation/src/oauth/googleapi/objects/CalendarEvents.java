package oauth.googleapi.objects;

import java.util.ArrayList;

public class CalendarEvents {

	//private String kind;				//"calendar#events"
	//private String etag;	
	private String summary;				//"khushboo.kaur@3pillarglobal.com
	//private String updated;			//"2013-02-IST10:28:15.0432"
	private String timezone;			//"Asia/Calcutta"
	//private String accessRole;		//"owner"
	//private String[] defaultReminers;
	private ArrayList<CalendarEventItems> items;
	
	public CalendarEvents() {
		items=new ArrayList<CalendarEventItems>();
		//kind="";
		//etag="";
		summary="";
		//updated="";
		timezone="";
		//accessRole="";
	}
	
	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public String getTimezone() {
		return timezone;
	}

	public void setTimezone(String timezone) {
		this.timezone = timezone;
	}

	public ArrayList<CalendarEventItems> getItems() {
		return items;
	}

	public void setItems(ArrayList<CalendarEventItems> items) {
		this.items = items;
	}

	
	
}
