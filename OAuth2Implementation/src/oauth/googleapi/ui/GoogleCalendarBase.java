package oauth.googleapi.ui;

import oauth.googleapi.authentication.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.view.Gravity;
import android.widget.TextView;

public class GoogleCalendarBase extends Activity {
	
	ProgressDialog dialog;
	
	public void showOkAlert(String message, final boolean finishOrNot){
		AlertDialog.Builder alert=new AlertDialog.Builder(this);
		alert.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {			
			public void onClick(DialogInterface dialog, int which) {
				if(finishOrNot)
					finish();
			}});
		alert.setMessage(message);
		AlertDialog dialog = alert.show();
		TextView messageView = (TextView)dialog.findViewById(android.R.id.message);
		messageView.setGravity(Gravity.CENTER);

	}
	protected void showProgressDialog(){
		dialog=new ProgressDialog(this);
		dialog.setMessage(getResources().getString(R.string.please_wait_message));
		dialog.show();
	}
	protected void cancelProgressDialog(){
		if(dialog!=null && dialog.isShowing())
	    	  dialog.cancel();
	}
}
