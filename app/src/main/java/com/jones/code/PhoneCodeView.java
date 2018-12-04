package com.jones.code;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.constraint.ConstraintLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class PhoneCodeView extends ConstraintLayout {
    private static final String TAG = "PhoneCodeView";

    private Context context;
    private LayoutInflater inflater;
    private InputMethodManager imm;

    private LinearLayout llCode;
    private EditText et;

    private ArrayList<TextView> tvCodes = new ArrayList<>();
    private ArrayList<View> lines = new ArrayList<>();
    private ArrayList<String> codes = new ArrayList<>();

    //验证码几位数
    private int codeCount = 4;
    private int lineDefaultColor = 0xff859654;
    private int lineSelectColor = 0xff3F8EED;
    private int lineErrorColor = 0xffff0000;
    //此值为控件之间间距的一半，如控件间距40,此值为20
    private int space = 20;
    //文本值大小
    private float textSize;
    private int textColor;

    //值为0表示还没有输入验证码
    private int currentIndex = 0;

    private OnInputCompleteListener onInputCompleteListener;

    public void setOnInputCompleteListener(OnInputCompleteListener onInputCompleteListener) {
        this.onInputCompleteListener = onInputCompleteListener;
    }

    public PhoneCodeView(Context context) {
        this(context, null, 0);
    }

    public PhoneCodeView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PhoneCodeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        inflater = LayoutInflater.from(context);
        imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);

        initView();

        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.PhoneCodeView);
        codeCount = array.getInteger(R.styleable.PhoneCodeView_code_count, codeCount);
        lineDefaultColor = array.getColor(R.styleable.PhoneCodeView_line_default_color, lineDefaultColor);
        lineSelectColor = array.getColor(R.styleable.PhoneCodeView_line_select_color, lineSelectColor);
        lineErrorColor = array.getColor(R.styleable.PhoneCodeView_line_error_color, lineErrorColor);
        space = array.getDimensionPixelOffset(R.styleable.PhoneCodeView_space, space);
        textSize = array.getDimensionPixelSize(R.styleable.PhoneCodeView_text_size, 0);
        textColor = array.getColor(R.styleable.PhoneCodeView_text_color, 0);
        array.recycle();

        for (int i = 0; i < codeCount; i++) {
            View view = inflater.inflate(R.layout.code_item, llCode, false);
            TextView tv = view.findViewById(R.id.tv_code);
            if (textSize != 0) {
                tv.setTextSize(textSize);
            }
            if (textColor != 0) {
                tv.setTextColor(textColor);
            }
            tvCodes.add(tv);
            View line = view.findViewById(R.id.view);
            line.setBackgroundColor(lineDefaultColor);
            lines.add(line);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            params.weight = 1;
            params.leftMargin = space;
            params.rightMargin = space;
            llCode.addView(view, params);
        }
    }

    private void initView() {
        llCode = new LinearLayout(context);
        llCode.setId(R.id.ll_code);
        ConstraintLayout.LayoutParams codeParams = new LayoutParams(0, LayoutParams.WRAP_CONTENT);
        codeParams.leftToLeft = LayoutParams.PARENT_ID;
        codeParams.rightToRight = LayoutParams.PARENT_ID;
        codeParams.topToTop = LayoutParams.PARENT_ID;
        addView(llCode, codeParams);

        et = new EditText(context);
        ConstraintLayout.LayoutParams etParams = new LayoutParams(0, LayoutParams.WRAP_CONTENT);
        etParams.leftToLeft = R.id.ll_code;
        etParams.rightToRight = R.id.ll_code;
        etParams.topToTop = R.id.ll_code;
        etParams.bottomToBottom = R.id.ll_code;
        et.setBackgroundResource(android.R.color.transparent);
        et.setTextColor(getResources().getColor(android.R.color.transparent));
        et.setCursorVisible(false);
        et.setInputType(EditorInfo.TYPE_CLASS_NUMBER);
        addView(et, etParams);

        et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s != null && s.length() > 0) {
                    et.setText("");
                    if (codes.size() < codeCount) {
                        currentIndex++;
                        codes.add(s.toString());
                        showCode();
                    }
                }

            }
        });

        et.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_DEL && event.getAction() == KeyEvent.ACTION_DOWN && codes.size() > 0) {
                    codes.remove(codes.size() - 1);
                    currentIndex--;
                    showCode();
                    return true;
                }
                return false;
            }
        });

    }

    /**
     * 显示验证码
     */
    private void showCode() {
        for (TextView tv : tvCodes) {
            tv.setText("");
        }
        for (int i = 0; i < codes.size(); i++) {
            tvCodes.get(i).setText(codes.get(i));
            lines.get(i).setBackgroundColor(lineSelectColor);
        }

        for (int i = currentIndex; i < lines.size(); i++) {
            lines.get(currentIndex).setBackgroundColor(lineDefaultColor);
        }

        if (codes.size() == tvCodes.size()) {
            if (onInputCompleteListener != null) {
                onInputCompleteListener.onInputComplete(getCode());
            }
        }

    }

    private String getCode() {
        StringBuilder sb = new StringBuilder();
        for (String s : codes) {
            sb.append(s);
        }
        return sb.toString();
    }

    public void setError(){
        for (View view: lines) {
            view.setBackgroundColor(lineErrorColor);
        }
    }

    public void showSoftInput() {
        //显示软键盘
        if (imm != null && et != null) {
            et.postDelayed(new Runnable() {
                @Override
                public void run() {
                    imm.showSoftInput(et, 0);
                }
            }, 200);
        }
    }

    public interface OnInputCompleteListener {
        void onInputComplete(String code);
    }

}
