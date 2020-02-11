package org.js.msb2kml.DisplayLog;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.js.msb2kml.R;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MetaListAdapt extends BaseAdapter {

    String patrnExtrm = "^([^:]+):{1}([^;]+);{1}([^;]+)$";
    Pattern pExtrm = Pattern.compile(patrnExtrm);
    private List<String> Xtrm = null;
    Context context;

    public MetaListAdapt(Context context, List<String> extrmString) {
        this.context = context;
        Xtrm = extrmString;
    }

    @Override
    public int getCount() {
        if (Xtrm != null) return Xtrm.size();
        return 0;
    }

    @Override
    public String getItem(int i) {
        return Xtrm != null ? Xtrm.get(i) : null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.list_item,
                    parent, false);
        }
        ViewHolder viewHolder = (ViewHolder) convertView.getTag();
        if (viewHolder == null) {
            viewHolder = new ViewHolder();
            viewHolder.vMin = (TextView) convertView.findViewById(R.id.minval);
            viewHolder.vHead = (TextView) convertView.findViewById(R.id.measure);
            viewHolder.vMax = (TextView) convertView.findViewById(R.id.maxval);
            convertView.setTag(viewHolder);
        }
        String xtrm=Xtrm.get(position);
        Matcher ma=pExtrm.matcher(xtrm);
        if (ma.find()){
            String head=ma.group(1).trim();
            String minHead=ma.group(2).trim();
            String maxHead=ma.group(3).trim();
            viewHolder.vMin.setText(minHead);
            viewHolder.vHead.setText(head);
            viewHolder.vMax.setText(maxHead);
        }
        convertView.setVisibility(View.VISIBLE);
        return convertView;
    }

    static class ViewHolder{
        TextView vMin;
        TextView vHead;
        TextView vMax;
    }


}
