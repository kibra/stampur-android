package com.stampur.android.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.stampur.android.R;
import com.stampur.android.Stamp;
import com.stampur.android.Stamp.Category;

public class Adapter_Stamp_ALT extends ArrayAdapter<Stamp> {
    private Context context;
    private List<Stamp> stampsList;
    //private ArrayList<View> stampView_list;

    //TODO: TEMP - Store User Stamps Locally (Absolutely TEMP logic)
    //private ArrayList<Stamp> userStamps;

    private Comparator<Stamp> comparator;

    private LayoutInflater inflater;

    //private ServiceConnection serviceConnection;

    public Adapter_Stamp_ALT(Context context, int resource, int textViewResourceId,
            List<Stamp> objects) {

        super(context, resource, textViewResourceId, objects);

        this.context = context;
        this.stampsList = (objects != null) ? objects : new ArrayList<Stamp>();
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        //initServcie();
        sortAndNotify();
    }

    /*
    private void initServcie() {

        serviceConnection = new ServiceConnection() {
            public void onServiceConnected(ComponentName className, IBinder serviceBinder) {
                StampurService stampurService =
                        ((StampurService.StampurService_Binder) serviceBinder).getService();
                userStamps = stampurService.getUserStamps();
            }

            public void onServiceDisconnected(ComponentName arg0) {
            }
        };
    }  */

    /*
    public Adapter_Stamp(Activity act, ArrayList<Stamp> stamps) {
    	this.act = act;
        this.stamps = new ArrayList<Stamp>(stamps);
        stampView_list = new ArrayList<View>();
    }   */

    public int getCount() {
        return stampsList.size();
    }

    public Stamp getItem(int position) {
        return stampsList.get(position);
    }

    /*
    public long getItemId(int position) {
        return stampsList.get(position).getId();
    }  */

    public void setComparator(Comparator<Stamp> comparator) {
        this.comparator = comparator;
    }

    /*
    public int getCount(){ return stamp_list.size(); }
    public Object getItem(int position){ return stamp_list.get(position); }
    public long getItemId(int position){ return stampView_list.get(position).getId(); }

    public void addStamp(Stamp s){ stamp_list.add(s); }
    public void removeStamp(Stamp s){ stamp_list.remove(s); }
    */

    /*
    // create a new StampView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
    	View rowView;
        TextView stampText;
        ImageView add_sub;
<<<<<<< HEAD
        Stamp s = stampsList.get(position);
=======
        Stamp s = (Stamp) this.getItem(position);        
>>>>>>> d3e8cb43559a7976b9048b836afff2947aa7a87d

        if (convertView == null)        	
        	rowView = inflater.inflate(R.layout.item_stampbookitem, null, true);
        else rowView = convertView;       

        stampText = (TextView) rowView.findViewById(R.id.stampbookitem_stampName);
        add_sub = (ImageView) rowView.findViewById(R.id.stampbookitem_plusMinus);

        User thisUser = Stampur.getUser();
        if (thisUser.hasStamp(s) == false) {
            add_sub.setImageResource(R.drawable.plus);
            add_sub.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    //((Activity_StampBook) act).plus(v);
                }
            });
        } else {
            add_sub.setImageResource(R.drawable.minus);
            add_sub.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    //((Activity_StampBook) act).minus(v);
                }
            });
        }

        stampText.setTag(s.getId());
        stampText.setText(s.getLabel());
		rowView.setTag(s.getId());

        if(s.getCategory().equals(Category.PEOPLE))stampText.setBackgroundResource(R.drawable.bg_stamp_user);
        else stampText.setBackgroundResource(R.drawable.bg_stamp);
<<<<<<< HEAD
		stampsList.add(position, rowView);

=======
		
		stampView_list.add(position, rowView);
>>>>>>> d3e8cb43559a7976b9048b836afff2947aa7a87d
        return rowView;
    }  */

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        StampViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_stampbookitem, null);
            holder = new StampViewHolder();
            holder.label = (TextView) convertView.findViewById(R.id.stampbookitem_stampName);
            holder.label.setBackgroundResource(R.drawable.bg_stamp);
            holder.addsub = (ImageView) convertView.findViewById(R.id.stampbookitem_plusMinus);
            convertView.setTag(holder);
        } else {
            holder = (StampViewHolder) convertView.getTag();
        }

        Stamp stamp = stampsList.get(position);
        holder.label.setText(stamp.getLabel());

        //TODO: Remove this TEMP logic after finding a better way
        if (true) {
            holder.addsub.setImageResource(R.drawable.minus);
        } else {
            holder.addsub.setImageResource(R.drawable.plus);
        }

        if(stamp.getCategory().equals(Category.PEOPLE)) {
            holder.label.setBackgroundResource(R.drawable.bg_stamp_user);
        }

        return convertView;
    }

    private class StampViewHolder {
        TextView label;
        ImageView addsub;
    }

    protected void sortAndNotify() {
        Collections.sort(stampsList, comparator);
        this.sort(comparator);
        this.notifyDataSetChanged();
    }

}