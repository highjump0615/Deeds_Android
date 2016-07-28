package com.iliayugai.deeds.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.iliayugai.deeds.DetailActivity;
import com.iliayugai.deeds.R;
import com.iliayugai.deeds.model.CategoryData;
import com.iliayugai.deeds.model.UserData;
import com.iliayugai.deeds.utils.CommonUtils;
import com.makeramen.roundedimageview.RoundedImageView;
import com.parse.codec.binary.StringUtils;

import org.w3c.dom.Text;

import java.util.ArrayList;

/**
 * Created by highjump on 15-6-3.
 */
public class MentionAdapter extends BaseAdapter {

    private static LayoutInflater mInflater = null;

    private Context mContext;
    public ArrayList<UserData> maryData;

    public MentionAdapter(Context ctx, ArrayList<UserData> values)
    {
        mContext = ctx;
        mInflater = (LayoutInflater)ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        this.maryData = values;
    }

    public UserData getAtUser(int i) {
        UserData resData = null;
        int nCount = 0;

        DetailActivity activity = (DetailActivity) mContext;
        String strAtText = activity.mAdapter.mstrAtText;

        for (UserData uData : maryData) {
            if (!TextUtils.isEmpty(strAtText)) {
                if (!uData.getUsernameToShow().toLowerCase().contains(strAtText.toLowerCase())) {
                    continue;
                }
            }

            if (nCount == i) {
                resData = uData;
                break;
            }

            nCount++;
        }

        return resData;
    }

    @Override
    public int getCount() {
        int nCount = 0;

        DetailActivity activity = (DetailActivity) mContext;
        float fMaxHeight = activity.mAdapter.mfMaxMentionHeight;
        int nAtPos = activity.mAdapter.mnAtPos;
        String strAtText = activity.mAdapter.mstrAtText;

        // get user from follow data
        for (UserData uData : maryData) {
            if (!TextUtils.isEmpty(strAtText)) {
                if (uData.getUsernameToShow().toLowerCase().contains(strAtText.toLowerCase())) {
                    nCount++;
                }
            }
            else {
                nCount++;
            }
        }

        if (nCount > 0 && nAtPos > 0) {
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) activity.mLayoutMention.getLayoutParams();

            int nCellHeight = CommonUtils.dipToPixels(mContext, 41 * nCount + 1);
            int nPadding = activity.mLayoutMention.getPaddingBottom() + activity.mLayoutMention.getPaddingTop();
            float fHeight = Math.min(fMaxHeight, nPadding + nCellHeight);

            layoutParams.height = (int) fHeight;

            activity.mLayoutMention.setLayoutParams(layoutParams);
        }
        else {
            activity.showMentionView(false);
        }

        return nCount;
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
            vi = mInflater.inflate(R.layout.layout_mention_item, null);
        }

        RoundedImageView imgViewUser = (RoundedImageView)vi.findViewById(R.id.imgview_user);
        TextView textName = (TextView)vi.findViewById(R.id.text_name);

        UserData uData = getAtUser(i);

        uData.showPhoto(mContext, imgViewUser);
        textName.setText(uData.getUsernameToShow());

        return vi;
    }
}
