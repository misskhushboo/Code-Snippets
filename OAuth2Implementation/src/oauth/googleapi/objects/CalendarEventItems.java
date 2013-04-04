package oauth.googleapi.objects;

public class CalendarEventItems {
	private String id;
	private String creator;
	private String summary;
	private String location;
	private String startDateTime;
	private String endDateTime;
	private String organizer;
	private int number_attendees;
	
	public CalendarEventItems() {
		id="";
		creator="";
		summary="";
		location="";
		startDateTime="-";
		endDateTime="-";
		organizer="";
		number_attendees=0;
	}
	
	public String getCreator() {
		return creator;
	}

	public void setCreator(String creator) {
		this.creator = creator;
	}
	
	public String getOrganizer() {
		return organizer;
	}

	public void setOrganizer(String organizer) {
		this.organizer = organizer;
	}

	public int getNumber_attendees() {
		return number_attendees;
	}

	public void setNumber_attendees(int number_attendees) {
		this.number_attendees = number_attendees;
	}

	

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public String getStartDateTime() {
		return startDateTime;
	}

	public void setStartDateTime(String startDateTime) {
		this.startDateTime = startDateTime;
	}

	public String getEndDateTime() {
		return endDateTime;
	}

	public void setEndDateTime(String endDateTime) {
		this.endDateTime = endDateTime;
	}
	
}
