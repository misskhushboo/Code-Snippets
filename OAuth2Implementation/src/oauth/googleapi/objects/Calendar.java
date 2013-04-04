package oauth.googleapi.objects;

public class Calendar {
	
	private String id;							//"#contacts@group.v.calendar.google.com","en.indian#holiday@group.v.calendar.google.com",
	private String summary;						//"Contacts' birthdays and events","Indian Holidays",
	private String description;					//"Your contacts' birthdays and anniversaries","Indian Holidays",
	
	public Calendar() {
		id="";
		summary="";
		description="";
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
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
	//String kind;
	//String etag;
	//String timezone;
	//String colorId;
	//String backgroundColor;
	//String foregroundColor;
	//String accessRole;
	//boolean selected
	//ArrayList defaultReminders
		//String method;
		//String minutes;
	
	
}
