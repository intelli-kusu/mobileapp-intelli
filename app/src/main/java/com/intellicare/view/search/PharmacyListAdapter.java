package com.intellicare.view.search;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.intellicare.databinding.SearchRowItemBinding;
import com.intellicare.model.other.PharmacyData;

import java.util.List;

public class PharmacyListAdapter extends BaseAdapter {
    private List<PharmacyData.Pharmacy> mData;
    private LayoutInflater inflater;

    public PharmacyListAdapter(List<PharmacyData.Pharmacy> cancel_type) {
        mData = cancel_type;
    }


    @Override
    public int getCount() {
        if (mData != null)
            return mData.size();
        return 0;
    }

    @Override
    public PharmacyData.Pharmacy getItem(int position) {
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
        rowItemBinding.tvSearchListItem.setText(mData.get(position).getDisplayName());
        return rowItemBinding.getRoot();
    }

    public void refresh(List<PharmacyData.Pharmacy> freshData) {
        mData = freshData;
        notifyDataSetChanged();
    }
}