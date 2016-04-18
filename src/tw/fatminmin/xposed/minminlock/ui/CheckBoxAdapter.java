package tw.fatminmin.xposed.minminlock.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import tw.fatminmin.xposed.minminlock.R;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.Filter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

public class CheckBoxAdapter extends BaseAdapter {
	
	
	private List<Map<String, Object>> mItemList, oriItemList;
	private Context mContext;
	private LayoutInflater mInflater;
	private SharedPreferences pref;
	private Filter mFilter;
	
	public CheckBoxAdapter(Context context, List<Map<String, Object>> itemList) {
		mContext = context;
		mInflater = LayoutInflater.from(context);
		oriItemList = mItemList = itemList;
		pref = PreferenceManager.getDefaultSharedPreferences(mContext);
		mFilter = new MyFilter();
	}
	
	@Override
	public int getCount() {
		return mItemList.size();
	}

	@Override
	public Object getItem(int position) {
		return mItemList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return mItemList.get(position).hashCode();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		if(convertView == null) {
			convertView = mInflater.inflate(R.layout.listview_item, null);
		}
		
		final TextView title = (TextView) convertView.findViewById(R.id.title);
		final TextView summary = (TextView) convertView.findViewById(R.id.summary);
		final ImageView icon = (ImageView) convertView.findViewById(R.id.icon);
		final ToggleButton toggleLock = (ToggleButton) convertView.findViewById(R.id.toggleLock);
		final ImageButton imgEdit = (ImageButton) convertView.findViewById(R.id.edit);
		
		final String sTitle = (String) mItemList.get(position).get("title");
		final String sSummary = (String) mItemList.get(position).get("summary");
		final String key = (String) mItemList.get(position).get("key");
		final Drawable dIcon = (Drawable) mItemList.get(position).get("icon");
		
		title.setText(sTitle);
		summary.setText(sSummary);
		icon.setImageDrawable(dIcon);
		
		if(pref.getBoolean(key, false)) {
		    toggleLock.setChecked(true);
		    imgEdit.setVisibility(View.VISIBLE);
		}
		else {
		    toggleLock.setChecked(false);
		    imgEdit.setVisibility(View.GONE);
		}
		
		icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityManager am = (ActivityManager) mContext.getSystemService(Activity.ACTIVITY_SERVICE);
                am.killBackgroundProcesses(key);
                Intent it = mContext.getPackageManager().getLaunchIntentForPackage(key);
                it.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(it);
            }
        });
		
		imgEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View checkBoxView = View.inflate(mContext, R.layout.per_app_settings, null);
                CheckBox fakeDie = (CheckBox) checkBoxView.findViewById(R.id.cb_fake_die);
                
                fakeDie.setChecked(pref.getBoolean(key + "_fake", false));
                
                fakeDie.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        
                        ActivityManager am = (ActivityManager) mContext.getSystemService(Activity.ACTIVITY_SERVICE);
                        am.killBackgroundProcesses(key);
                        
                        CheckBox cb = (CheckBox) v;
                        boolean value = cb.isChecked();
                        pref.edit()
                            .putBoolean(key + "_fake", value)
                            .commit();
                    }
                });

                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setTitle(mContext.getString(R.string.txt_settings))
                           .setIcon(R.drawable.ic_launcher)
                           .setView(checkBoxView)
                           .setCancelable(false)
                           .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dlg, int id) {
                                    dlg.dismiss();
                                }
                           })
                           .show();
            }
        });
		
		toggleLock.setOnClickListener(new View.OnClickListener() {
			
            @Override
            public void onClick(View v) {
                
                ActivityManager am = (ActivityManager) mContext.getSystemService(Activity.ACTIVITY_SERVICE);
                am.killBackgroundProcesses(key);
                
                ToggleButton tb = (ToggleButton) v;
                
                boolean value = tb.isChecked();
                pref.edit()
                    .putBoolean(key, value)
                    .commit();
                if(value) {
                    imgEdit.setVisibility(View.VISIBLE);
                }
                else {
                    imgEdit.setVisibility(View.GONE);
                }
            }
		});
		
		return convertView;
	}
	
	public Filter getFilter() {
	    return mFilter;
	}
	
	class MyFilter extends Filter {

        @SuppressLint("DefaultLocale")
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            
            constraint = constraint.toString().toLowerCase();
            
            FilterResults results = new FilterResults();
            
            if(constraint.length() == 0) {
                results.values = oriItemList;
                results.count = oriItemList.size();
            }
            else {
                List<Map<String, Object>> filteredList = new ArrayList<Map<String, Object>>();
                
                for(Map<String, Object> app : oriItemList) {
                    String title = ((String) app.get("title")).toLowerCase();
                    if(title.indexOf((String) constraint) == 0) {
                        filteredList.add(app);
                    }
                }
                
                results.values = filteredList;
                results.count = filteredList.size();
            }
            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            
            mItemList = (List<Map<String, Object>>) results.values;
            notifyDataSetChanged();
        }

	    
	}
	
}
