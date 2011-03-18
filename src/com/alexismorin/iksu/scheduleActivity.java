package com.alexismorin.iksu;

import java.io.File;
//import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
//import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Date;

import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.alexismorin.iksu.IKSUHelper.ApiException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class scheduleActivity extends Activity {
	//views from the XML file
	public LayoutInflater m_inflater;
	public ListView m_class_list;
	public TextView m_date;
	
	public ArrayList<String> scheduleDates = new ArrayList<String>();
	public ArrayList<String> activities = new ArrayList<String>();
	public ArrayList<String> alSchedule;
	public Object[] wholeSchedule;
	public IKSUSchedule iksuSchedule;
	
	int dayIndex = 0;
	
	private ProgressDialog dialog;
	ScheduleAdapter scheduleAdapter = null;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        iksuSchedule = (IKSUSchedule) getLastNonConfigurationInstance();
        
        if( iksuSchedule == null){//if the app is freshly started, need to initialize again.
        	iksuSchedule = new IKSUSchedule(); 
        }
        
        m_class_list = (ListView) findViewById(R.id.activities_view);
        m_date = (TextView) findViewById(R.id.title_header);
        m_inflater = LayoutInflater.from(this);
        
        scheduleAdapter = new ScheduleAdapter(getApplicationContext());
        m_class_list.setAdapter(scheduleAdapter);
        
        if(isOnline(getApplicationContext())){
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
        }else{
        	Toast.makeText(getApplicationContext(),"No Internet.", Toast.LENGTH_SHORT).show();
        }
    }
    
    /*@Override
    public void onResume(){
    	if(iksuSchedule.activities.size() == 0){
    		loadData();
    	}
    }*/
    
    public void loadData(){
    	dialog = ProgressDialog.show(this, "", 
                this.getString(R.string.fetching_schedule), true);
    	new IksuScheduleTask().execute();
    }
    
    @Override
    public Object onRetainNonConfigurationInstance() {//makes sure that the data is saved when an instance of the app is destroyed
        final IKSUSchedule data = iksuSchedule;
        return data;
    }
    
    
    
    private class IksuScheduleTask extends AsyncTask<String, Integer, Document>{
    	
    	Exception bgException;
    	
		@Override
		protected Document doInBackground(String... arg0) {
			//android.os.Debug.waitForDebugger();
				//Log.i("IksuScheduleTask", "Fetching Web Page");
			
				try {
					String thePage = IKSUHelper.getXml();
					Log.i("IksuScheduleTask", "Length of Page OK.");
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
				}
		}
		
		private void handleError(Exception e){
			Log.i("IksuScheduleTask", "Handling the exception, like a boss.");
			bgException = e;
			bgException.printStackTrace();
			if(dialog.isShowing())
				dialog.dismiss();
			cancel(true);
		}
		
		@Override
		protected void onPostExecute(Document result) {
			Log.i("IksuScheduleTask", "PostExecuting");
			if(!isCancelled()){
				iksuSchedule.thePage = result;
				iksuSchedule.dates = IKSUHelper.getDatesArray(result);
				
				Log.i("IksuScheduleTask", "first date:"+iksuSchedule.dates.get(0));
				
				//Log.i("IksuScheduleTask", "Got the dates");
				dialog.dismiss();
				iksuSchedule.lastRefreshed = new Date();
				loadDayToView(dayIndex);
				
				if(iksuSchedule.getNumActivities() == 0){					
					loadDayToView(++dayIndex);
				}
				
				if(saveScheduleToCache())
					Toast.makeText(getApplicationContext(), "Saved Object successfully", Toast.LENGTH_SHORT);
				else
					Toast.makeText(getApplicationContext(), "Failed to save object", Toast.LENGTH_SHORT);
			}else{
				Log.i("IksuScheduleTask","Task Cancelled due to caught exception.(PostExecute)");
				cancel(true);
				Toast.makeText(getApplicationContext(), "Ran into an error. Are you logged into CAS?", Toast.LENGTH_LONG).show();
				
				Intent viewIntent = new Intent("android.intent.action.VIEW", Uri.parse("https://netlogon.umu.se/index.cgi?referer=www.google.com"));
				startActivity(viewIntent);
			}
		}
		
		@Override
		protected void onCancelled(){
			//kill the dialog window so it doesn't leak ALL OVER THE PLACE!
			if(dialog.isShowing())
				dialog.dismiss();
			
			Log.i("IksuScheduleTask","Task Cancelled due to caught exception.");
			
			Toast.makeText(getApplicationContext(), 
					"Are you logged into CAS? (onCancelled)", Toast.LENGTH_LONG);
		}
    	
    }
    
    private void loadDayToView(int newDayIndex){
    	dayIndex = newDayIndex;
    	iksuSchedule.activities = IKSUHelper.getScheduleFromPageForDay(iksuSchedule.thePage, dayIndex);
    	Log.i("IksuScheduleTask", "Got the schedule");
    	m_date.setText(iksuSchedule.dates.get(dayIndex));
    	
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
    	Log.i("Cache", "Saving Object");
    	
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
		
		if(keep)
			Log.i("Cache", "Object successfully saved");
		
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

