package com.iliayugai.deeds.view;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.iliayugai.deeds.R;
import com.iliayugai.deeds.model.ItemData;
import com.iliayugai.deeds.model.UserData;
import com.iliayugai.deeds.utils.CommonUtils;
import com.makeramen.roundedimageview.RoundedImageView;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * Created by highjump on 15-6-5.
 */
public class ViewHolderItem extends ViewHolderBase {

    private static final String TAG = ViewHolderItem.class.getSimpleName();

    public int mnIndex = -1;

    public ImageView mImgViewItem;
    public ImageView mImgViewType;
    public TextView mTextTitle;
    public TextView mTextDist;
    public TextView mTextDate;
    public RoundedImageView mImgViewPhoto;
    public Button mButUser;
    public RelativeLayout mLayoutDone;

    private Context mContext;

    public ViewHolderItem(View itemView, Context ctx) {
        super(itemView);

        mContext = ctx;

        mImgViewItem = (ImageView) itemView.findViewById(R.id.imgview_item);
        mImgViewType = (ImageView) itemView.findViewById(R.id.imgview_type);
        mTextTitle = (TextView) itemView.findViewById(R.id.text_title);
        mTextDist = (TextView) itemView.findViewById(R.id.text_distance);
        mTextDate = (TextView) itemView.findViewById(R.id.text_date);

        mImgViewPhoto = (RoundedImageView) itemView.findViewById(R.id.imgview_photo);
        mImgViewPhoto.setOnClickListener(this);

        mButUser = (Button)itemView.findViewById(R.id.but_user);
        mButUser.setOnClickListener(this);

        mLayoutDone = (RelativeLayout) itemView.findViewById(R.id.layout_done);
    }

    public void fillContent(ItemData data, final int index) {
        // user info
        final UserData user = data.getUser();

        user.fetchIfNeededInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject parseObject, ParseException e) {
                ParseFile filePhoto = user.getPhoto();

                user.showPhoto(mContext, mImgViewPhoto);

                mButUser.setText(user.getUsernameToShow());
            }
        });
        mButUser.setText(data.getUsername());

        // item info
        ParseFile filePhoto = data.getImages().get(0);

//        Log.d(TAG, "mIndex: " + mnIndex + ", index: " + index);

        Picasso.with(mContext)
                .load(filePhoto.getUrl())
                .placeholder(R.mipmap.home_item_default)
                .error(R.mipmap.home_item_default)
                .into(mImgViewItem);

        mTextTitle.setText(data.getTitle());

        // if done
        if (data.getDone()) {
            mLayoutDone.setVisibility(View.VISIBLE);
        }
        else {
            mLayoutDone.setVisibility(View.GONE);
        }

        // if type
        if (data.getType() == ItemData.ITEMTYPE_DEED) {
            mImgViewType.setImageResource(R.mipmap.deed_tag);
        }
        else {
            mImgViewType.setImageResource(R.mipmap.need_tag);
        }

        // date
        DateFormat dateFormat = new SimpleDateFormat("MMMM dd yyyy");
        String strDate = dateFormat.format(data.getCreatedAt());
        mTextDate.setText(strDate);

        // distance
        String strDist;
        double dDist = CommonUtils.getDistanceFromPoint(data.getLocation());
        if (dDist < 0) {
            strDist = "Unknown";
        }
        else {
            strDist = String.format("%.0fKM Away", dDist);
        }

        mTextDist.setText(strDist);

        mnIndex = index;
    }
}
