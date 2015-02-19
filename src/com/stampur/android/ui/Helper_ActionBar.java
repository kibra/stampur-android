package com.stampur.android.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.stampur.android.R;

public class Helper_ActionBar extends RelativeLayout {

	private LayoutInflater mInflater;
	private ImageView mLogoView;
	private TextView mTitleView;
	private ProgressBar mProgress;
	private LinearLayout mActionIconContainer;
	private ImageView mRefresh;

	public Helper_ActionBar(Context context, AttributeSet attrs){
		super(context, attrs);

		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		LinearLayout barView = (LinearLayout) mInflater.inflate(R.layout.actionbar_layout, null);
		this.addView(barView);

		mLogoView = (ImageView) barView.findViewById(R.id.actionbar_home_logo);
		mProgress = (ProgressBar) barView.findViewById(R.id.actionbar_progress);
		mRefresh = (ImageView) barView.findViewById(R.id.actionbar_refresh);
		mTitleView = (TextView) barView.findViewById(R.id.actionbar_title);
		mActionIconContainer = (LinearLayout) barView.findViewById(R.id.actionbar_actionIcons);
	}

	public void setHomeLogo(int resId){	setHomeLogo(resId, null); }
	public void setHomeLogo(int resId, OnClickListener onClickListener) {
		mLogoView.setImageResource(resId);
		mLogoView.setVisibility(View.VISIBLE);		
		if(onClickListener != null) mLogoView.setOnClickListener(onClickListener);		
	}

	public void setTitle(CharSequence title){ mTitleView.setText(title); }
	public void setTitle(int resid){ mTitleView.setText(resid);	}

	public void setProgressBar(OnClickListener onClickListener) {
		mRefresh.setOnClickListener(onClickListener);
	}

	public void showProgressBar(){
		mRefresh.setVisibility(View.GONE);
		mProgress.setVisibility(View.VISIBLE);
	}

	public void hideProgressBar(){
		mProgress.setVisibility(View.GONE);
		mRefresh.setVisibility(View.VISIBLE);
	}

	public void toggleProgressBar() {
		if(mRefresh.getVisibility() == View.VISIBLE) showProgressBar();
		else hideProgressBar();
	}

	public void addActionIcon(int iconResourceId, OnClickListener onClickListener) {
		// Inflate
		View view = mInflater.inflate(R.layout.actionbar_icon,	mActionIconContainer, false);
		ImageButton imgButton = (ImageButton) view.findViewById(R.id.actionbar_item);
		imgButton.setImageResource(iconResourceId);
		imgButton.setOnClickListener(onClickListener);
		mActionIconContainer.addView(view, mActionIconContainer.getChildCount());
	}
	
	public boolean removeActionIconAt(int index) {
		int count = mActionIconContainer.getChildCount();
		if (count > 0 && index >= 0 && index < count) {
			mActionIconContainer.removeViewAt(index);
			return true;
		}
		return false;
	}
}
