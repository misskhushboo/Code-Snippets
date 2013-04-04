package oauth.googleapi.helper;

import android.util.Log;

public class ConversionHelper {

	static String TAG="ConversionHelper";
	public String tryParseString(Object obj) {
		
		if (obj==null || !(obj instanceof String))
			return "";
		String value=(String)obj;
		try {
			if(value==null || value.toLowerCase().equals("null") || value.equals(""))
				return "";
			else
				return value.trim();
		} catch(Exception e) {
			Log.d(TAG, e.toString());
			return "";
		}
	}
}
