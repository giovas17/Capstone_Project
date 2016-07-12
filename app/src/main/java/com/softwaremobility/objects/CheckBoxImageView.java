package com.softwaremobility.objects;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import com.softwaremobility.monin.R;


public class CheckBoxImageView extends ImageView implements View.OnClickListener {
    private boolean isChecked;
    private int mDefaultImageResource;
    private int mCheckedImageResource;
    private OnCheckedChangeListener onCheckedChangeListener;

    public CheckBoxImageView(Context context, AttributeSet attr, int defStyle) {
        super(context, attr, defStyle);
        init(attr, defStyle);
    }

    public CheckBoxImageView(Context context, AttributeSet attr) {
        super(context, attr);
        init(attr, -1);
    }

    public CheckBoxImageView(Context context) {
        super(context);
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        this.isChecked = checked;
        setImageResource(checked ? mCheckedImageResource : mDefaultImageResource);
    }

    private void init(AttributeSet attributeSet, int defStyle) {
        TypedArray typedArray = null;
        if (defStyle != -1)
            typedArray = getContext().obtainStyledAttributes(attributeSet, R.styleable.CheckBoxImageView, defStyle, 0);
        else
            typedArray = getContext().obtainStyledAttributes(attributeSet, R.styleable.CheckBoxImageView);
        mDefaultImageResource = typedArray.getResourceId(0, 0);
        mCheckedImageResource = typedArray.getResourceId(R.styleable.CheckBoxImageView_checked_img, 0);
        isChecked = typedArray.getBoolean(R.styleable.CheckBoxImageView_checked, false);
        typedArray.recycle();
        setImageResource(isChecked ? mCheckedImageResource : mDefaultImageResource);
        setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        isChecked = !isChecked;
        setImageResource(isChecked ? mCheckedImageResource : mDefaultImageResource);
        onCheckedChangeListener.onCheckedChanged(this, isChecked);
    }

    public void setOnCheckedChangeListener(OnCheckedChangeListener onCheckedChangeListener) {
        this.onCheckedChangeListener = onCheckedChangeListener;
    }

    public interface OnCheckedChangeListener {
        void onCheckedChanged(View buttonView, boolean isChecked);
    }
}
