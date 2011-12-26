package com.alexismorin.iksu;

import java.io.File;
//import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
//import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.SocketTimeoutException;
import java.util.Date;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.alexismorin.iksu.IKSUHelper.ApiException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ScheduleActivity extends Activity implements OnClickListener{	
	//views from the XML file
	public LayoutInflater m_inflater;
	public Button m_p, m_n;
	public ListView m_class_list;
	public TextView m_date;
	//@TODO remove comment once IKSU logo is approved
	//public ImageView m_image_view;
	public Dialog loginDialog;
	
	public IKSUSchedule iksuSchedule;
	
	private ProgressDialog dialog;
	private AlertDialog alertDialog;
	ScheduleAdapter scheduleAdapter = null;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        m_inflater = LayoutInflater.from(this);
        m_class_list = (ListView) findViewById(R.id.activities_view);
        m_date = (TextView) findViewById(R.id.title_header);
        m_p = (Button) findViewById(R.id.btnPrev);
        m_n = (Button) findViewById(R.id.btnNext);
    	//@TODO remove comment once IKSU logo is approved
        //m_image_view = (ImageView) findViewById(R.id.header_image);
        
        //m_image_view.setOnClickListener(this);
        m_p.setOnClickListener(this);
        m_n.setOnClickListener(this);
        
        
        iksuSchedule = (IKSUSchedule) getLastNonConfigurationInstance();
        
        if(iksuSchedule == null){//if the app is freshly started, need to initialize again.
        	m_p.setVisibility(View.INVISIBLE);
        	m_n.setVisibility(View.INVISIBLE);
        	iksuSchedule = new IKSUSchedule();
        }else{
        	if(iksuSchedule.dates.size() > 0)//this happens when the app is rotated
        		m_date.setText(iksuSchedule.dates.get(iksuSchedule.currentDateIndex));
        	
        	if(iksuSchedule.currentDateIndex == 0){
        		m_p.setVisibility(View.INVISIBLE);
            	m_n.setVisibility(View.VISIBLE);
        	}
        	if(iksuSchedule.currentDateIndex == iksuSchedule.dates.size()-1){
        		m_p.setVisibility(View.VISIBLE);
            	m_n.setVisibility(View.INVISIBLE);
        	}
        	/*if(iksuSchedule.activities.size() >= 0){
        		m_image_view.setVisibility(View.INVISIBLE);
        	}*/
        }
        
        scheduleAdapter = new ScheduleAdapter(getApplicationContext());
        m_class_list.setAdapter(scheduleAdapter);
        
        //determine if user is logged or has credentials stored
        if(!loadPrefString("iksuUsername").equals("") && !loadPrefString("iksuPassword").equals("")){
        	iksuSchedule.username = loadPrefString("iksuUsername");
        	iksuSchedule.password = loadPrefString("iksuPassword");
        }
        
        if(iksuSchedule.activities.size() == 0){
        	/*if(scheduleIsCached()){
        		Log.i("Cache", "Schedule was found to be cached");
        		try{
	        		iksuSchedule = loadScheduleFromCache();
	        	}catch (Exception e) {
					Log.i("Cache", "No schedule found in cache");
	        	}
	        }else{*/
				loadData();
			//}
        }
    }
    
    /*
    public AlertDialog createLoginDialog(){
    	LayoutInflater factory = LayoutInflater.from(this);
    	final View textEntryView = factory.inflate(R.layout.alert_dialog_text_entry, null);
    	final EditText usernameEdit = (EditText)textEntryView.findViewById(R.id.username_edit);
    	final EditText passwordEdit = (EditText)textEntryView.findViewById(R.id.password_edit);
    	return new AlertDialog.Builder(ScheduleActivity.this)
            //.setIcon(R.drawable.alert_dialog_icon)
            .setTitle("Login")
            .setView(textEntryView)
            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {

                    // That works! Save username and password to preferences
                	SharedPreferences usernamePref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                    SharedPreferences.Editor prefsEditor = usernamePref.edit();
                    prefsEditor.putString("iksuUsername", usernameEdit.getText().toString());
                    prefsEditor.putString("iksuPassword", passwordEdit.getText().toString());
                    // Commit the edits!
                    prefsEditor.commit();
                    
                	//Log.i("AlertBox", usernameEdit.getText() + " " + passwordEdit.getText());
                }
            })
            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {

                    // User clicked cancel so do some stuff 
                }
            })
            .show();
    }
    */
    
    public void onClick(View v) {
        // do something when the button is clicked  	
    	if(v == m_n){
    		loadDay(1);
    	}
    	if(v == m_p){
    		loadDay(-1);
    	}
    	/*if(v == m_image_view){
    		loadData();
    	}*/
      }
    
    @Override
    public void onResume(){
    	super.onResume();
    	
    	if(iksuSchedule.activities.size() > 0){
    		iksuSchedule.typeFilter = loadPrefString("iksuActType");
    		loadDayToView(iksuSchedule.currentDateIndex);
    	}else{
    		//loadData();
    	}
    }
    
    @Override
    public void onDestroy(){
    	super.onDestroy();
    	
    	//prevent leaks!
    	if(dialog != null){
    		if(dialog.isShowing())
    			dialog.dismiss();
    	}
    	
    	if(alertDialog != null){
	    	if(alertDialog.isShowing())
	    		alertDialog.dismiss();
    	}
    	
    	if(loginDialog != null){
    		if(loginDialog.isShowing())
    			loginDialog.dismiss();
    	}
    }
    
    private String loadPrefString(String prefName){
    	return PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getString(prefName, "");
    	//iksuSchedule.typeFilter = prefs.getString("iksuActType", "");
    	//return prefs.getString(prefName, "");
    }
    
    public void loadData(){
    	if(isOnline(getApplicationContext())){
	    	iksuSchedule.typeFilter = loadPrefString("iksuActType");
	    	dialog = ProgressDialog.show(this, "", 
	                this.getString(R.string.fetching_schedule), true);
	    	new IksuScheduleTask().execute();
    	}else{
        	Toast.makeText(getApplicationContext(),R.string.no_connection, Toast.LENGTH_SHORT).show();
        }
    }
    
    @Override
    public Object onRetainNonConfigurationInstance() {//makes sure that the data is saved when an instance of the app is destroyed
        final IKSUSchedule data = iksuSchedule;
        return data;
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.schedule_menu, menu);
        
        return true;
    }
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) { 
	    super.onPrepareOptionsMenu(menu);
	
	    MenuItem today = menu.findItem(R.id.today_menu_btn);
	    
	    if(iksuSchedule.currentDateIndex == 0){
	    	today.setEnabled(false);
	    }else{
	    	if(!today.isEnabled()){
	    		today.setEnabled(true);
	    	}
	    }
	
	    return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        /*case R.id.reload_menu_btn:
            loadData();
            return true;*/
        case R.id.filter_menu_btn:
        	makeFilterList();
        	return true;
        case R.id.today_menu_btn:
        	loadDayToView(0);
        	return true;
        case R.id.prefs_menu_btn:
        	Intent settingsActivity = new Intent(getBaseContext(), Preferences.class);
        	startActivity(settingsActivity);
        	return true;
        /*
         * @TODO: remove comments for version 3
         case R.id.alert_menu_btn:
        	alertDialog = createLoginDialog();
        	alertDialog.show();
        	return true;*/
        }
        return super.onOptionsItemSelected(item);
    }
    
    public void makeFilterList(){
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	
    	builder.setSingleChoiceItems(R.array.iksuActivityFilter, iksuSchedule.activityFilterIndex, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				//save the choice
				PreferenceManager.getDefaultSharedPreferences(getBaseContext()).edit().putInt("iksuActivityFilter", which).commit();
				
				//affect it to the schedule
				iksuSchedule.activityFilterIndex = which;
			}
		}).
		setCancelable(true).
		setTitle(R.string.filterTitle).
		setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}
		}).
		setPositiveButton("OK", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				reloadDay();
				dialog.dismiss();
			}
		});
    	
		alertDialog = builder.show();//store a reference to the list as to not leak the view when rotating the device
    }
    
    private class IksuScheduleTask extends AsyncTask<String, Integer, Document>{
    	
    	Exception bgException;
    	
		@Override
		protected Document doInBackground(String... arg0) {
			//android.os.Debug.waitForDebugger();
				//Log.i("IksuScheduleTask", "Fetching Web Page");
			
				try {
					String thePage;
					/*if(iksuSchedule.hasCredentials()){
						thePage = IKSUHelper.getLoggedXML(iksuSchedule.username, iksuSchedule.password);
						iksuSchedule.containsLoggedUserData = true;
					} else {*/
						thePage = IKSUHelper.getXml();
					/*	iksuSchedule.containsLoggedUserData = false;
					}*/
					//String thePage = IKSUHelper.getXml();
					//Log.i("IksuScheduleTask", thePage);
					try {
						//@REMINDER your return is here. So many potential errors. WTF
						return IKSUHelper.parseTheXml(thePage);
						} catch (SAXException e) {
						handleError(e);
						return null;
						} catch (IOException e) {
						handleError(e);
						return null;
						} catch (ParserConfigurationException e) {
						handleError(e);
						return null;
						} catch (Exception e){
						handleError(e);
						return null;
					}
				} catch (ApiException e) {
					handleError(e);
					return null;
				} catch (SocketTimeoutException e) {
					handleError(e);
					return null;
				} /*catch (UnsupportedEncodingException e){
					handleError(e);
					return null;
				}*/ catch (Exception e){
					handleError(e);
					return null;
				}
		}
		
		private void handleError(Exception e){
			//Log.i("IksuScheduleTask", "Handling the exception, like a boss.");
			bgException = e;
			bgException.printStackTrace();
			if(dialog.isShowing())
				dialog.dismiss();
			cancel(true);
		}
		
		@Override
		protected void onPostExecute(Document result) {
			//Log.i("IksuScheduleTask", "PostExecuting");
			if(!isCancelled()){
				iksuSchedule.thePage = result;
				iksuSchedule.dates = IKSUHelper.getDatesArray(result);
				
				//Log.i("IksuScheduleTask", "first date:"+iksuSchedule.dates.get(0));
				
				//Log.i("IksuScheduleTask", "Got the dates");
				dialog.dismiss();
				iksuSchedule.lastRefreshed = new Date();
				
				//m_image_view.setVisibility(View.INVISIBLE);
				
				loadDayToView(iksuSchedule.currentDateIndex);
				
				if(iksuSchedule.getNumActivities() == 0){					
					loadDayToView(++iksuSchedule.currentDateIndex);
				}
				
				saveScheduleToCache();
				
				//if()
					//Toast.makeText(getApplicationContext(), "Saved Object successfully", Toast.LENGTH_SHORT);
				//else
					//Toast.makeText(getApplicationContext(), "Failed to save object", Toast.LENGTH_SHORT);
			}else{
				Log.i("IksuScheduleTask","Task Cancelled due to caught exception.(PostExecute)");
				cancel(true);
				
				Toast.makeText(getApplicationContext(), R.string.error, Toast.LENGTH_LONG).show();
				
				//Intent viewIntent = new Intent("android.intent.action.VIEW", Uri.parse("https://netlogon.umu.se/index.cgi?referer=www.google.com"));
				//startActivity(viewIntent);
			}
		}
		
		@Override
		protected void onCancelled(){
			//kill the dialog window so it doesn't leak ALL OVER THE PLACE!
			if(dialog.isShowing())
				dialog.dismiss();
			
			Log.i("IksuScheduleTask","Task Cancelled");
			
			//Toast.makeText(getApplicationContext(),R.string.casError, Toast.LENGTH_LONG);
		}
    	
    }
    
    private void loadDay(int direction){
    	if(iksuSchedule.dates.size() > 0){
    		if(direction == 1){
    			if(iksuSchedule.currentDateIndex < iksuSchedule.dates.size()-1){
    				loadDayToView(iksuSchedule.currentDateIndex + direction);
    			}
    		}
    		if(direction == -1){
    			if(iksuSchedule.currentDateIndex > 0){
    				loadDayToView(iksuSchedule.currentDateIndex + direction);
    			}
    		}
    	}
    }
    
    private void reloadDay(){//convenience method to apply any new filtering conditions
    	//or you crash, cause it tries to parse non-existing XML down the line
    	if(iksuSchedule.activities.size() > 0)
    		loadDayToView(iksuSchedule.currentDateIndex);
    }
    
    private void loadDayToView(int newDayIndex){
    	iksuSchedule.currentDateIndex = newDayIndex;
    	//load the filter
        iksuSchedule.activityFilterIndex = PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getInt("iksuActivityFilter", 0);
    	
    	iksuSchedule.activities = IKSUHelper.getScheduleFromPageForDay(
    			iksuSchedule.thePage, 
    			iksuSchedule.currentDateIndex, 
    			iksuSchedule.typeFilter, 
    			getResources().getStringArray(R.array.iksuActivityFilterCodes)[iksuSchedule.activityFilterIndex]);
    	
    	m_date.setText(iksuSchedule.dates.get(iksuSchedule.currentDateIndex).trim());
    	iksuSchedule.currentDateIndex = iksuSchedule.currentDateIndex;
    	
    	if(iksuSchedule.currentDateIndex == 0 && iksuSchedule.dates.size() > 0){
			m_p.setVisibility(View.INVISIBLE);
			m_n.setVisibility(View.VISIBLE);
		}else if(iksuSchedule.currentDateIndex == iksuSchedule.dates.size() -1){
    		m_p.setVisibility(View.VISIBLE);
			m_n.setVisibility(View.INVISIBLE);
    	}else{
    		m_p.setVisibility(View.VISIBLE);
    		m_n.setVisibility(View.VISIBLE);
    	}
    	
    	//Log.i("IksuScheduleTask", "Day is" + iksuSchedule.dates.get(dayIndex));
    	
    	scheduleAdapter.notifyDataSetChanged();
    }
    
   /* private boolean scheduleIsCached(){
    	return new File(getIKSUCacheDir(), "schedule").exists();
    }
    
    private IKSUSchedule loadScheduleFromCache(){
    	Log.i("Cache", "Loading Object");
    	
    	IKSUSchedule cachedSched = null;
    	FileInputStream fis = null;
    	ObjectInputStream ois = null;
    	
    	final File scheduleFile = new File(getIKSUCacheDir(), "schedule");
    	
    	try {
			fis = new FileInputStream(scheduleFile);
			ois = new ObjectInputStream(fis);
			cachedSched = (IKSUSchedule) ois.readObject();
		} catch (Exception e) {
			Log.e("Cache", e.getMessage());
		}finally{
			try{
				if(fis != null) fis.close();
				if(ois != null) ois.close();
			}catch (Exception e) {}
		}
    	
    	return cachedSched;
    }*/
    
    private boolean saveScheduleToCache(){
    	//Log.i("Cache", "Saving Object");
    	
    	FileOutputStream fos = null;
    	ObjectOutputStream oos = null;
    	boolean keep = true;
    	
    	final File scheduleFile = new File(getIKSUCacheDir(), "schedule");
    	
    	try{
    		fos = new FileOutputStream(scheduleFile);
    		oos = new ObjectOutputStream(fos);
    		oos.writeObject(iksuSchedule);
    	}catch (Exception e) {
			// TODO: handle exception
		}finally{
			try{
				if(oos != null) oos.close();
				if(fos != null) fos.close();
				if(keep == false) scheduleFile.delete();
			}catch (Exception e) {
				/* do nothing */
			}
		}
		
		return keep;
    }
    
    private File getIKSUCacheDir(){
    	File cacheDir;
    	
    	if(android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED))
    		cacheDir = new File(android.os.Environment.getExternalStorageDirectory(), "rawIksuSchedule");
    	else
    		cacheDir = getCacheDir();
    	
    	if(!cacheDir.exists())
    		cacheDir.mkdirs();
    	
    	return cacheDir;
    }
    
    public boolean isOnline(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
        .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnectedOrConnecting())
            return true;
        return false;
	}
    
    static class ActivityHolder{
    	TextView class_name, class_time, class_room, class_instructor;
    }
    
    private class ScheduleAdapter extends BaseAdapter{
    	
    	//private Context mCtx;
    	
    	public ScheduleAdapter (Context c){
    		//mCtx = c;
    	}
    	
    	@Override
        public int getCount() {
            return iksuSchedule.getNumActivities();
        }

        @Override
        public Object getItem(int arg0) {
            return null;
        }

        @Override
        public long getItemId(int pos) {
            return pos;
        }
        
        public View getView(int pos, View convertView, ViewGroup parent){
        	ActivityHolder actHolder;
        	
        	if(convertView == null){
        		actHolder = new ActivityHolder();
        		
        		convertView = m_inflater.inflate(R.layout.main_list_item, parent, false);
        		
        		actHolder.class_name = (TextView) convertView.findViewById(R.id.activity_name);
        		actHolder.class_time = (TextView) convertView.findViewById(R.id.activity_time);
        		actHolder.class_room = (TextView) convertView.findViewById(R.id.activity_room);
        		actHolder.class_instructor = (TextView) convertView.findViewById(R.id.activity_instructor);
        		
        		convertView.setTag(actHolder);
        	}else{
        		actHolder = (ActivityHolder) convertView.getTag();
        	}
        	        	
        	actHolder.class_name.setText(iksuSchedule.getActivityAtPosition(pos).name);
        	actHolder.class_time.setText(iksuSchedule.getActivityAtPosition(pos).time);
        	actHolder.class_room.setText(iksuSchedule.getActivityAtPosition(pos).room);
        	actHolder.class_instructor.setText(iksuSchedule.getActivityAtPosition(pos).instructor);
        	
        	return convertView;
        }
    }
}

