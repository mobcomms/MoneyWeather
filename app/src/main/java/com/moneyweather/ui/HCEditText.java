package com.moneyweather.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.moneyweather.R;


public class HCEditText extends LinearLayout {

    private EditText editText;
    private View line;
    private ImageView imgInputResult;

    private boolean readOnly;
    private String hint;
    private String text;
    private int inputType;
    private String privateImeOptions;
    private int imeOptions;
    private String imeActionLabel;
    private int maxLength;

    public HCEditText(Context context) {
        super(context);
        init(context, null);
    }

    public HCEditText(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public HCEditText(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {

        inflate(context, R.layout.view_edittext, this);

        editText = findViewById(R.id.edittext);
        line = findViewById(R.id.line);
        imgInputResult = findViewById(R.id.imgInputResult);

        TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.HCEditText);

        readOnly = attributes.getBoolean(R.styleable.HCEditText_readOnly, false);
        hint = attributes.getString(R.styleable.HCEditText_hint);
        text = attributes.getString(R.styleable.HCEditText_android_text);
        inputType = attributes.getInt(R.styleable.HCEditText_inputType, 0);
        privateImeOptions = attributes.getString(R.styleable.HCEditText_privateImeOptions);
        imeOptions = attributes.getInt(R.styleable.HCEditText_imeOptions, 0);
        imeActionLabel = attributes.getString(R.styleable.HCEditText_imeActionLabel);
        maxLength = attributes.getInteger(R.styleable.HCEditText_maxLength, 0);
        attributes.recycle();

        if (readOnly) {
            editText.setInputType(InputType.TYPE_NULL);
        } else {
            editText.setInputType(inputType);
        }

        if (!TextUtils.isEmpty(hint)) {
            editText.setHint(hint);
        }

        if (!TextUtils.isEmpty(text)) {
            editText.setText(text);
        }

        if (!TextUtils.isEmpty(privateImeOptions)) {
            editText.setPrivateImeOptions(privateImeOptions);
        }

        editText.setImeOptions(imeOptions);

        if (!TextUtils.isEmpty(imeActionLabel)) {
            editText.setImeActionLabel(imeActionLabel, KeyEvent.KEYCODE_ENTER);
        }

        if (maxLength > 0) {
            editText.setFilters(new InputFilter[] {new InputFilter.LengthFilter(maxLength)});
        }

        editText.setOnFocusChangeListener((view, focus) -> line.setSelected(focus));

        imgInputResult.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                editText.setText("");
                visibleClearButton(false);
            }
        });

    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
        if (readOnly) {
            editText.setInputType(InputType.TYPE_NULL);
        }
        else {
            editText.setInputType(inputType);
        }
    }

//    public void setWarning(InputResult inputResult) {
//        if (inputResult == InputResult.WARNING) {
//            imgInputResult.setVisibility(VISIBLE);
//            imgInputResult.setImageResource(R.drawable.ico_alert_error);
//            line.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.point_red_two));
//        } else if (inputResult == InputResult.SUCCESS) {
//            imgInputResult.setVisibility(VISIBLE);
//            imgInputResult.setImageResource(R.drawable.ico_alert_success);
//            line.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.sub_color));
//        } else {
//            imgInputResult.setVisibility(GONE);
//            line.setBackgroundResource(R.drawable.line_edittext_under);
//        }
//    }

    public int getInputType() {
        return inputType;
    }

    public void visibleClearButton(Boolean is) {
        imgInputResult.setVisibility(is ? View.VISIBLE : View.GONE);
    }

    public void setClearButtonImage(int resouceId) {
        imgInputResult.setImageResource(resouceId);
    }

    public void setInputType(int inputType) {
        this.inputType = inputType;
        editText.setInputType(inputType);
    }

    public String getPrivateImeOptions() {
        return privateImeOptions;
    }

    public void setPrivateImeOptions(String privateImeOptions) {
        this.privateImeOptions = privateImeOptions;
        editText.setPrivateImeOptions(privateImeOptions);
    }

    public int getImeOptions() {
        return imeOptions;
    }

    public void setImeOptions(int imeOptions) {
        this.imeOptions = imeOptions;
        editText.setImeOptions(imeOptions);
    }

    public void setHint(CharSequence hint) {
        this.hint = hint.toString();
        editText.setHint(hint);
    }

    public void setHint(int hintRes) {
        this.hint = getContext().getResources().getString(hintRes);
        editText.setHint(hint);
    }

    public String getHint() {
        return hint;
    }

    public void setText(CharSequence text) {
        editText.setText(text);
    }

    public void setText(int textRes) {
        editText.setText(textRes);
    }

    public Editable getText() {
        return editText.getText();
    }

    public void addTextChangedListener(TextWatcher watcher) {
        editText.addTextChangedListener(watcher);

    }

    public void removeTextChangedListener(TextWatcher watcher) {
        editText.removeTextChangedListener(watcher);
    }

    public void setSelection(int index) {
        editText.setSelection(index);
    }

    public void setRawInputType(int type) {
        editText.setRawInputType(type);
    }

    public void setOnEditorActionListener(TextView.OnEditorActionListener listener) {
        editText.setOnEditorActionListener(listener);
    }

    public void setOnFocusChangeListener(View.OnFocusChangeListener listener) {
        setOnFocusChangeListener(listener);
        editText.setOnFocusChangeListener(listener);
    }

    public void setLineSelected(boolean focus) {
        line.setSelected(focus);
    }


}
