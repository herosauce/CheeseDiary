package com.writeitdown.cheesediary;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by herosauce on 11/1/2016.
 */

public class BadgeAdapter extends BaseAdapter {

    ArrayList<BadgeTemplate> list;
    Context c;
    private ArrayList<BadgeTemplate> privateArray;

    public BadgeAdapter(Context c, ArrayList<BadgeTemplate> list){
        this.list = list;
        this.c = c;
        privateArray = new ArrayList<>();
        privateArray.addAll(list);
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View row = inflater.inflate(R.layout.badge_item,null, true);

        TextView name = (TextView) row.findViewById(R.id.tv_badge_name);
        TextView details = (TextView) row.findViewById(R.id.tv_badge_details);
        ImageView icon = (ImageView) row.findViewById(R.id.iv_badge);

        BadgeTemplate badge = list.get(position);

        name.setText(badge.name);
        details.setText(badge.details);
        icon.setImageResource(badge.icon);

        return row;
    }
}
