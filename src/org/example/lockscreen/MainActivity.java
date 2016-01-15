package org.example.lockscreen;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.text.InputFilter;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("NewApi") public class MainActivity extends Activity implements LocationListener {

	public static final String KEY_MESSAGE = "message";
    //protected EditText mPinCodeField;
    protected InputFilter[] filters = null;
    protected TextView topMessage = null;
    protected TextView ps=null;
    Toast tempMessage;
    public boolean lock=false;
    public double lo=0;
    public double la=0;
    final Context h=this;
    protected LocationManager locationManager;
    protected LocationListener locationListener;
    protected Context context;
    
    String lat;
    String provider;
    protected String latitude,longitude;
    protected boolean gps_enabled,network_enabled;
    String myAddress;
    
    public WindowManager winManager;
    public RelativeLayout wrapperView;
    public String typed;
    public String password;
    public int count=0;
    public boolean first_key=true;
    
     Button unlockScreen;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        
        startService(new Intent(this,LockScreenService.class));
        setContentView(R.layout.activity_main);
        WindowManager.LayoutParams localLayoutParams = new	WindowManager.LayoutParams( WindowManager.LayoutParams.TYPE_SYSTEM_ERROR,WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|	WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL|WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,PixelFormat.TRANSLUCENT);
        this.winManager =((WindowManager)getApplicationContext().getSystemService(WINDOW_SERVICE));
        this.wrapperView = new RelativeLayout(getBaseContext());
        getWindow().setAttributes(localLayoutParams);
        View.inflate(this, R.layout.activity_main,this.wrapperView);
        this.winManager.addView(this.wrapperView,localLayoutParams);
        MyPhoneListener phoneListener = new MyPhoneListener();
        TelephonyManager telephonyManager =	(TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE); 
        telephonyManager.listen(phoneListener,PhoneStateListener.LISTEN_CALL_STATE);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        //HomeKeyLocker home=new HomeKeyLocker();
        //home.lock(this);
        ps = (TextView)findViewById(R.id.password);
        //setContentView(R.layout.activity_main);
    }
    @TargetApi(Build.VERSION_CODES.HONEYCOMB) @SuppressLint("NewApi") public void makeFullScreen() {
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        if(Build.VERSION.SDK_INT < 19) { //View.SYSTEM_UI_FLAG_IMMERSIVE is only on API 19+
            this.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        } else {
            this.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE);
        }
    }
    @Override
    public void onBackPressed() {
        return; //Do nothing!
    }
    public void call(View view) {
    	unlockScreen = (Button) findViewById(R.id.button0);
    	unlockScreen.setVisibility(View.GONE);
    	try {
    		String uri = "tel:7200606039";
    		Intent dialIntent = new Intent(Intent.ACTION_CALL, Uri.parse(uri));
			startActivity(dialIntent);
    	}
    	catch(Exception e) {
    		tempMessage= Toast.makeText(getApplicationContext(),"Ypur call has failed.",Toast.LENGTH_SHORT);
        	tempMessage.setGravity(Gravity.BOTTOM| Gravity.CENTER, Gravity.CENTER_HORIZONTAL, 1);
        	tempMessage.show();
        	e.printStackTrace();;
    	}
    }
    public void accept(View view){
    	accept(this);
    }
    private void accept(Context c) {
		// TODO Auto-generated method stub
		Intent buttonUp = new Intent(Intent.ACTION_MEDIA_BUTTON);
		buttonUp.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_UP,KeyEvent.KEYCODE_HEADSETHOOK));
		c.sendOrderedBroadcast(buttonUp,"android.permission.CALL_PRIVILEGED");
	}
    public void disconnect(View view){
    	int disc=disconnectCallAndroid();
	}
    private int disconnectCallAndroid() {
		// TODO Auto-generated method stub
    	Runtime r=Runtime.getRuntime();
    	int nresp=0;
    	try{
    		Log.d("Keys.LOGTAG","service call phone 5\n");
    		r.exec("service call phone 5 \n");
    	}
    	catch(Exception e)
    	{
    		Log.e("LOGTAG",e.getMessage());
    		e.printStackTrace();
    	}
		return nresp;
	}
    private class MyPhoneListener extends PhoneStateListener {
		private boolean onCall = false;
 		public void onCallStateChanged(int state, String incomingNumber) {
 			switch (state) {
			case TelephonyManager.CALL_STATE_RINGING:
				tempMessage= Toast.makeText(getApplicationContext(),incomingNumber + " calls you",Toast.LENGTH_SHORT);
		    	tempMessage.setGravity(Gravity.BOTTOM| Gravity.CENTER, Gravity.CENTER_HORIZONTAL, 1);
		    	tempMessage.show();
		    	break;
			case TelephonyManager.CALL_STATE_OFFHOOK:
			
				tempMessage= Toast.makeText(getApplicationContext(),"on call.",Toast.LENGTH_SHORT);
		    	tempMessage.setGravity(Gravity.BOTTOM| Gravity.CENTER, Gravity.CENTER_HORIZONTAL, 1);
		    	tempMessage.show();
				onCall = true;
				break;
			case TelephonyManager.CALL_STATE_IDLE:
				if (onCall == true) {
					Intent restart = getBaseContext().getPackageManager().
						getLaunchIntentForPackage(getBaseContext().getPackageName());
					restart.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(restart);
 					onCall = false;
				}
				break;
			default:
				break;
			}
		}
	}

	public void onLocationChanged(Location location) 
	{
		la= location.getLatitude();
		lo= location.getLongitude();
		Geocoder geocoder = new Geocoder(this, Locale.ENGLISH);
		List<Address> addresses=null;
		try {
			addresses = geocoder.getFromLocation(la, lo, 1);
			if(addresses != null) {
				Address returnedAddress = addresses.get(0);
				StringBuilder strReturnedAddress = new StringBuilder("Address:\n");
				for(int i=0; i<returnedAddress.getMaxAddressLineIndex(); i++) 
				{
					strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n");
				}
				myAddress=strReturnedAddress.toString();
			}
			else
			{
				myAddress="No Address returned!";
			}
		} catch (IOException e) 
		{
			e.printStackTrace();
			myAddress="Cannot get Address!";
		}
	}
	
	public void onProviderDisabled(String provider) 
	{
	   try{
		Log.d("Latitude","disable");
	   }
	   catch(Exception e){}
	}
	public void onProviderEnabled(String provider) 
	{
	  try{
		Log.d("Latitude","enable");
	  }
      catch(Exception e){}
	}
	public void onStatusChanged(String provider, int status	, Bundle extras) 
	{
	  try{
		Log.d("Latitude","status");
	  }
      catch(Exception e){}
	}
	public void sms(View view)
	{
		try
		{	
			SmsManager smsManager= SmsManager.getDefault();
			smsManager.sendTextMessage("7200606039",null,"Emergency!!!!!"+"\n"+"Latitude:"+la+"\nLongitude:"+lo+"\n"+myAddress,null,null);
			tempMessage= Toast.makeText(getApplicationContext(), "SMS sent"+la+"  "+lo +" "+ myAddress,Toast.LENGTH_SHORT);
			tempMessage.setGravity(Gravity.BOTTOM| Gravity.CENTER, Gravity.CENTER_HORIZONTAL, 1);
			tempMessage.show();
			tempMessage= Toast.makeText(getApplicationContext(), "SMS sent",Toast.LENGTH_SHORT);
			tempMessage.setGravity(Gravity.BOTTOM| Gravity.CENTER, Gravity.CENTER_HORIZONTAL, 1);
			tempMessage.show();
		}
		catch(Exception e){
			tempMessage= Toast.makeText(getApplicationContext(), "SMS sending failed",Toast.LENGTH_SHORT);
			tempMessage.setGravity(Gravity.BOTTOM| Gravity.CENTER, Gravity.CENTER_HORIZONTAL, 1);
			tempMessage.show();
		}
	}
	public void unlockScreen(View view) 
    {
      try{
    	if(android.text.format.DateFormat.is24HourFormat(getBaseContext()))
      	{
      		SimpleDateFormat sDateFormat = new SimpleDateFormat("HHmm");
              String date = sDateFormat.format(new java.util.Date());

              StringBuffer mystring = new StringBuffer(date);
              mystring.reverse();
              password = mystring.toString();
          }
      	else {
      		SimpleDateFormat sDateFormat = new SimpleDateFormat("hhmm");
              String date = sDateFormat.format(new java.util.Date());
              StringBuffer mystring = new StringBuffer(date);
              mystring.reverse();
              password = mystring.toString();
  		}
          password+="121";
    	if(typed.compareTo(password)==0)
    	{
    		android.os.Process.killProcess(android.os.Process.myPid());
    	}
    	tempMessage= Toast.makeText(getApplicationContext(),"INCORRECT PASSWORD",Toast.LENGTH_SHORT);
    	tempMessage.setGravity(Gravity.BOTTOM| Gravity.CENTER, Gravity.CENTER_HORIZONTAL, 1);
    	tempMessage.show();
    	count=0;
    	settext(0);
    	typed=typed.substring(0, count);
    	return;
      }
      catch(Exception e){}
    }
   
    public void settext(int c)
    {
      try{
    	String stars = null;
    	for(int i=0;i<count;i++)
    	{
    		if(i==0)
    			stars="*";
    		else
    			stars+='*';
    	}
    	ps.setText(stars);
      }
      catch(Exception e){}
    }
    public void one(View view)
    {
      try{
    	if(first_key==true)
    		typed="1";
    	else
    		typed=typed+'1';
    	first_key=false;
    	count++;
    	settext(count);
      }
      catch(Exception e){}
    }
    public void two(View view)
    {
      try{
    	if(first_key==true)
    		typed="2";
    	else
    	   	typed=typed+'2';
    	first_key=false;
    	count++;
    	settext(count);
      }
      catch(Exception e){}
    }
    public void three(View view)
    {
      try{
    	if(first_key==true)
    		typed="3";
    	else
    		typed=typed+'3';
    	first_key=false;
    	count++;
    	settext(count);
      }
      catch(Exception e){}
    }
    public void four(View view)
    {
      try{
    	if(first_key==true)
    		typed="4";
    	else
    		typed=typed+'4';
    	first_key=false;
    	count++;
    	settext(count);
      }
      catch(Exception e){}
    }
    public void five(View view)
    {
      try{
    	if(first_key==true)
    		typed="5";
    	else
    		typed=typed+'5';
    	first_key=false;
    	count++;
    	settext(count);
      }
      catch(Exception e){}
    }
    public void six(View view)
    {
      try{
    	if(first_key==true)
    		typed="6";
    	else
    		typed=typed+'6';
    	first_key=false;
    	count++;
    	settext(count);
      }
      catch(Exception e){}
    }
    public void seven(View view)
    {
      try{
    	if(first_key==true)
    		typed="7";
    	else
    		typed=typed+'7';
    	first_key=false;
    	count++;
    	settext(count);
      }
      catch(Exception e){}
    }
    public void eight(View view)
    {
      try{
    	if(first_key==true)
    		typed="8";
    	else
    		typed=typed+'8';
    	first_key=false;
    	count++;
    	settext(count);
      }
      catch(Exception e){}
    }
    public void nine(View view)
    {
      try{
    	if(first_key==true)
    		typed="9";
    	else
    		typed=typed+'9';
    	first_key=false;
    	count++;
    	settext(count);
      }
      catch(Exception e){}
    }
    public void zero(View view)
    {
      try{
    	if(first_key==true)
    		typed="0";
    	else
    		typed=typed+'0';
    	first_key=false;
    	count++;
    	settext(count);
      }
      catch(Exception e){}
    }
    public void erase(View view)
    {
      try{
    	if(count>0)
    		count--;
    	if(count==0)
    		first_key=true;
    	typed=typed.substring(0, count);
    	settext(count);
      }
      catch(Exception e){}
    }
	public void onDestroy()
    {
        this.winManager.removeView(this.wrapperView);
        this.wrapperView.removeAllViews();
        super.onDestroy();
    }

}
