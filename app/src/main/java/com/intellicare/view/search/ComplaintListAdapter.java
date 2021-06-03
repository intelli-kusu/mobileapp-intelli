package com.intellicare.view.search;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import com.intellicare.databinding.SearchRowItemBinding;
import com.intellicare.model.other.ComplaintData;

import java.util.ArrayList;
import java.util.List;

public class ComplaintListAdapter extends BaseAdapter implements Filterable {
    private final List<ComplaintData.Complaint> mStringFilterList;
    private List<ComplaintData.Complaint> mData;
    private ValueFilter valueFilter;
    private LayoutInflater inflater;

    public ComplaintListAdapter(List<ComplaintData.Complaint> cancel_type) {
        mData = cancel_type;
        mStringFilterList = cancel_type;
    }


    @Override
    public int getCount() {
        if (mData != null)
            return mData.size();
        return 0;
    }

    @Override
    public ComplaintData.Complaint getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, final ViewGroup parent) {

        if (inflater == null) {
            inflater = (LayoutInflater) parent.getContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
        SearchRowItemBinding rowItemBinding = SearchRowItemBinding.inflate(inflater);
        rowItemBinding.tvSearchListItem.setText(mData.get(position).getComplaintName());
        return rowItemBinding.getRoot();
    }

    @Override
    public Filter getFilter() {
        if (valueFilter == null) {
            valueFilter = new ValueFilter();
        }
        return valueFilter;
    }

    private class ValueFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();

            if (constraint != null && constraint.length() > 0) {
                List<ComplaintData.Complaint> filterList = new ArrayList<>();
                for (int i = 0; i < mStringFilterList.size(); i++) {
                    if ((mStringFilterList.get(i).getComplaintName().toUpperCase()).contains(constraint.toString().toUpperCase())) {
                        filterList.add(mStringFilterList.get(i));
                    }
                }
                results.count = filterList.size();
                results.values = filterList;
            } else {
                results.count = mStringFilterList.size();
                results.values = mStringFilterList;
            }
            return results;

        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mData = (List<ComplaintData.Complaint>) results.values;
            notifyDataSetChanged();
        }

    }

}