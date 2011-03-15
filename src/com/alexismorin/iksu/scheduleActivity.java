package com.alexismorin.iksu;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Date;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.htmlcleaner.XPatherException;
import org.xml.sax.SAXException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
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
	public IKSUSchedule iksuSchedule = new IKSUSchedule();
	
	int dayIndex = 0;
	
	private ProgressDialog dialog;
	ScheduleAdapter scheduleAdapter = null;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        m_class_list = (ListView) findViewById(R.id.schedule);
        m_date = (TextView) findViewById(R.id.date);
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
					dialog = ProgressDialog.show(this, "", 
		                    this.getString(R.string.fetching_schedule), true);
		        	new IksuScheduleTask().execute();
				//}
	        }
        }else{
        	Toast.makeText(getApplicationContext(),"No Internet", Toast.LENGTH_SHORT);
        }
    }
    
    private class IksuScheduleTask extends AsyncTask<String, Integer, Object[]>{
    	
		@Override
		protected Object[] doInBackground(String... arg0) {
			try {
				return IksuSiteHelper.performXPathSelectorOnWebPage(
						IksuSiteHelper.XPATH_IKSU_ALL,
						IksuSiteHelper.IKSU_SCHEDULE_URL);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (XPathExpressionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ParserConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SAXException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (XPatherException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(Object[] result) {
			Log.i("IksuScheduleTask", "PostExecuting");
			
			try {
				iksuSchedule.thePage = result;
				iksuSchedule.dates = IksuSiteHelper.getDatesArray(result);
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
				
			} catch (XPatherException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
    	
    }
    
    private void loadDayToView(int newDayIndex) throws XPatherException{
    	dayIndex = newDayIndex;
    	iksuSchedule.activities = IksuSiteHelper.getScheduleFromPageForDay(iksuSchedule.thePage, dayIndex);
    	m_date.setText(iksuSchedule.dates.get(dayIndex));
    	
    	scheduleAdapter.notifyDataSetChanged();
    }
    
    private boolean scheduleIsCached(){
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
    }
    
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
    	TextView class_name, class_time;
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
        		convertView = m_inflater.inflate(R.layout.schedule_item, parent, false);
        		actHolder.class_name = (TextView) convertView.findViewById(R.id.class_name);
        		actHolder.class_time = (TextView) convertView.findViewById(R.id.class_time);
        		
        		convertView.setTag(actHolder);
        	}else{
        		actHolder = (ActivityHolder) convertView.getTag();
        	}
        	        	
        	actHolder.class_name.setText(iksuSchedule.getActivityAtPosition(pos).name);
        	actHolder.class_time.setText(iksuSchedule.getActivityAtPosition(pos).time);

        	return convertView;
        }
    }
}

