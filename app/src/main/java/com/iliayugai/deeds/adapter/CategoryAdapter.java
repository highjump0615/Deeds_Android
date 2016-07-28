package com.iliayugai.deeds.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.iliayugai.deeds.R;
import com.iliayugai.deeds.model.CategoryData;

import java.util.ArrayList;

/**
 * Created by highjump on 15-6-3.
 */
public class CategoryAdapter extends BaseAdapter {

    private static LayoutInflater mInflater = null;
    public ArrayList<CategoryData> maryData;

    public CategoryData mCategorySelected = null;

    public CategoryAdapter(Context ctx, ArrayList<CategoryData> values, CategoryData categorySelected)
    {
        mInflater = (LayoutInflater)ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        this.maryData = values;
        this.mCategorySelected = categorySelected;
    }

    @Override
    public int getCount() {
        return maryData.size() + 1;
    }

    @Override
    public Object getItem(int i) {
        return i;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        View vi = view;
        if (vi == null)
        {
            vi = mInflater.inflate(R.layout.layout_filter_category_item, null);
        }

        TextView textName = (TextView)vi.findViewById(R.id.text_name);
        CheckBox checkBox = (CheckBox)vi.findViewById(R.id.checkbox);

        checkBox.setChecked(false);

        if (i == 0) {
            textName.setText("All");

            if (mCategorySelected == null) {
                checkBox.setChecked(true);
            }
        }
        else {
            CategoryData cData = maryData.get(i - 1);

            textName.setText(cData.getName());
            if (mCategorySelected != null && cData.getObjectId().equals(mCategorySelected.getObjectId())) {
                checkBox.setChecked(true);
            }
        }

        return vi;
    }
}
