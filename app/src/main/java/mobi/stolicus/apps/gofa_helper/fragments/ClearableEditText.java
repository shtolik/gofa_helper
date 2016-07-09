package mobi.stolicus.apps.gofa_helper.fragments;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;

public class ClearableEditText extends EditText {
    private final Drawable mImageX = ContextCompat.getDrawable(getContext(),
            android.R.drawable.presence_offline);
    final Drawable mImgSearch = ContextCompat.getDrawable(getContext(),
            android.R.drawable.ic_menu_search);
    private boolean mShowSearchButton = false;

    public ClearableEditText(Context context) {
        super(context);

        init();
    }

    public ClearableEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        init();
    }

    public ClearableEditText(Context context, AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    public void reInit() {
        init();
    }

    private void init() {

        // Set bounds of our X button
        mImageX.setBounds(0, 0, mImageX.getIntrinsicWidth(),
                mImageX.getIntrinsicHeight());

        mImgSearch.setBounds(0, 0, mImgSearch.getIntrinsicWidth(),
                mImgSearch.getIntrinsicHeight());

        // There may be initial text in the field, so we may need to display the
        // button
        manageClearButton();
        manageSearchButton();

        this.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                ClearableEditText et = ClearableEditText.this;

                // Is there an X showing?
                if (et.getCompoundDrawables()[2] == null)
                    return false;
                // Only do this for up touches
                if (event.getAction() != MotionEvent.ACTION_UP)
                    return false;
                // Is touch on our clear button?
                if (event.getX() > et.getWidth() - et.getPaddingRight()
                        - mImageX.getIntrinsicWidth()) {
                    et.setText("");
                    ClearableEditText.this.removeClearButton();
                }
                return false;
            }
        });

        this.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {

                ClearableEditText.this.manageClearButton();
            }

            @Override
            public void afterTextChanged(Editable arg0) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }
        });
    }

    private void manageClearButton() {
        if (this.getText().toString().equals(""))
            removeClearButton();
        else
            addClearButton();
    }

    private void manageSearchButton() {
        if (mShowSearchButton) {
            addSearchButton();
        } else {
            removeSearchButton();
        }
    }

    public void addClearButton() {
        this.setCompoundDrawables(this.getCompoundDrawables()[0],
                this.getCompoundDrawables()[1],
                mImageX,
                this.getCompoundDrawables()[3]);
    }

    public void removeClearButton() {
        this.setCompoundDrawables(this.getCompoundDrawables()[0],
                this.getCompoundDrawables()[1],
                null,
                this.getCompoundDrawables()[3]);
    }

    public void showSearchButton(boolean show) {
        mShowSearchButton = show;
    }

    public void addSearchButton() {
        this.setCompoundDrawables(mImgSearch,
                this.getCompoundDrawables()[1],
                this.getCompoundDrawables()[2],
                this.getCompoundDrawables()[3]);
    }

    public void removeSearchButton() {
        this.setCompoundDrawables(null,
                this.getCompoundDrawables()[1],
                this.getCompoundDrawables()[2],
                this.getCompoundDrawables()[3]);
    }

}
