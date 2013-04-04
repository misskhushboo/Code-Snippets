package oauth.googleapi.objects;

import java.util.ArrayList;

public class CalendarList {

	private ArrayList<Calendar> list;
	
	public CalendarList() {
		list=new ArrayList<Calendar>();
	}

	public ArrayList<Calendar> getList() {
		return list;
	}

	public void setList(ArrayList<Calendar> list) {
		this.list = list;
	}
}
