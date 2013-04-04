package oauth.googleapi.helper;

import oauth.googleapi.objects.CalendarEvents;
import oauth.googleapi.objects.CalendarList;

public class GoogleCalendarDataStore {

	static GoogleCalendarDataStore googleCalendarDataStore;
	
	private String authorizationCode;
	private String accessToken;
	private String refreshToken;
	private int expirationTime;
	private CalendarList calendarlist;
	private CalendarEvents events;
	
	private GoogleCalendarDataStore(){
	}
	public static GoogleCalendarDataStore getInstance(){
		if(googleCalendarDataStore==null)
			googleCalendarDataStore=new GoogleCalendarDataStore();
		return googleCalendarDataStore;
	}
	public String getAccessToken() {
		return accessToken;
	}
	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}
	public String getRefreshToken() {
		return refreshToken;
	}
	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}
	public int getExpirationTime() {
		return expirationTime;
	}
	public void setExpirationTime(int expirationTime) {
		this.expirationTime = expirationTime;
	}
	public String getAuthorizationCode() {
		return authorizationCode;
	}
	public void setAuthorizationCode(String authorizationCode) {
		this.authorizationCode = authorizationCode;
	}
	public CalendarList getCalendarlist() {
		return calendarlist;
	}
	public void setCalendarlist(CalendarList calendarlist) {
		this.calendarlist = calendarlist;
	}
	public void setCalendarEventslist(CalendarEvents events) {
		this.events=events;
	}
	public CalendarEvents getCalendarEventsList(){
		return this.events;
	}
}
