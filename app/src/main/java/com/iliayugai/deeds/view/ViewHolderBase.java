package com.iliayugai.deeds.view;

import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import com.iliayugai.deeds.utils.DeedEditorActionListener;
import com.iliayugai.deeds.utils.DeedItemClickListener;

/**
 * Created by highjump on 15-6-8.
 */
public class ViewHolderBase extends RecyclerView.ViewHolder implements View.OnClickListener, TextView.OnEditorActionListener {

    private static final String TAG = ViewHolderBase.class.getSimpleName();

    DeedItemClickListener mClickListner;
    DeedEditorActionListener mEditorActionListener;

    public ViewHolderBase(View itemView) {
        super(itemView);

        itemView.setOnClickListener(this);
    }

    public void setOnItemClickListener(DeedItemClickListener listener) {
        mClickListner = listener;
    }

    public void setDeedEditorActionListener(DeedEditorActionListener listener) {
        mEditorActionListener = listener;
    }

    @Override
    public void onClick(View view) {
        if (mClickListner != null) {
            mClickListner.onItemClick(view, getPosition());
        }
    }

    @Override
    public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
        if (mEditorActionListener != null) {
            mEditorActionListener.onEditorAction(textView, actionId);
        }
        return false;
    }
}
