package com.iliayugai.deeds.view;

import android.content.Context;
import android.view.View;
import android.widget.Button;

import com.iliayugai.deeds.R;
import com.iliayugai.deeds.model.UserData;

/**
 * Created by highjump on 15-6-11.
 */
public class ViewHolderFollow extends ViewHolderBase {

    public Context mContext;

    public Button mButFollow;
    private UserData mUser;

    public ViewHolderFollow(Context ctx, View itemView) {
        super(itemView);

        mContext = ctx;

        mButFollow = (Button)itemView.findViewById(R.id.but_follow);
        mButFollow.setOnClickListener(this);
    }

    public void updateFollowButton(UserData data) {
        mUser = data;
        updateButton();
    }

    private void updateButton() {
        UserData currentUser = (UserData) UserData.getCurrentUser();

        int nColorTheme = mContext.getResources().getColor(R.color.color_theme);
        int nColorRed = mContext.getResources().getColor(R.color.color_red);

        if (currentUser.isUserFollowing(mUser)) {
            mButFollow.setTextColor(nColorRed);
            mButFollow.setText("UNFOLLOW");
        }
        else {
            mButFollow.setTextColor(nColorTheme);
            mButFollow.setText("FOLLOW");
        }
    }

    @Override
    public void onClick(View view) {
        super.onClick(view);

        int id = view.getId();

        switch (id) {
            case R.id.but_follow:
                UserData currentUser = (UserData) UserData.getCurrentUser();

                switch (id) {
                    case R.id.but_follow:
                        if (currentUser.isUserFollowing(mUser)) {
                            currentUser.doUnFollow(mUser);
                        }
                        else {
                            currentUser.doFollow(mUser);
                        }

                        updateButton();

                        break;
                }

                break;
        }
    }
}
