package com.iliayugai.deeds.view;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.iliayugai.deeds.R;
import com.iliayugai.deeds.model.UserData;
import com.makeramen.roundedimageview.RoundedImageView;
import com.parse.ParseFile;
import com.squareup.picasso.Picasso;

import java.util.Calendar;

public class ViewHolderReviewInfo extends ViewHolderBase {

        private RoundedImageView mImgViewPhoto;
        private TextView mTextUsername;
        private Button mButReviewNum;
        private TextView mTextInfo;

        private Context mContext;

        public ViewHolderReviewInfo(Context ctx, View itemView) {
            super(itemView);

            mContext = ctx;

            mImgViewPhoto = (RoundedImageView)itemView.findViewById(R.id.imgview_photo);
            mTextUsername = (TextView)itemView.findViewById(R.id.text_username);
            mButReviewNum = (Button)itemView.findViewById(R.id.but_review_num);
            mTextInfo = (TextView)itemView.findViewById(R.id.text_info);
        }

        public void fillContent(UserData data) {
            if (data.getCreatedAt() != null) {
                data.showPhoto(mContext, mImgViewPhoto);

                mTextUsername.setText(data.getUsernameToShow());

                // member since
                int nYear = Calendar.getInstance().get(Calendar.YEAR);
                String strDesc = "Member Since: " + nYear;

                if (!TextUtils.isEmpty(data.getAddress())) {
                    strDesc += " Location: " + data.getAddress();
                }

                mTextInfo.setText(strDesc);
            }
            else {
                mTextInfo.setText("");
            }

            // review num
            mButReviewNum.setText("" + data.mnReviewCount);
        }
    }