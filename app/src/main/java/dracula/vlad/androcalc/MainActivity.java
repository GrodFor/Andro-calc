package dracula.vlad.androcalc;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.NoSuchElementException;

import static dracula.vlad.androcalc.ShuntingYard.*;

public class MainActivity extends AppCompatActivity {

    private String TAG = "LOG_Main";
    private String input = "";
    private ArrayList<Button> buttonArrayList= new ArrayList<>();
    private double result;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getChildViews((ViewGroup) findViewById(R.id.ll_Buttons));

        final EditText editText = findViewById(R.id.et_Input);
        editText.requestFocus();

        final TextView tv_Res = findViewById(R.id.tv_Result);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            editText.setShowSoftInputOnFocus(false);
        }
        else{
            editText.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    v.onTouchEvent(event);
                    InputMethodManager inputMethod = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (inputMethod != null) {
                        inputMethod.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    }
                    return true;
                }
            });
        }

        editText.addTextChangedListener(new TextWatcher() {

            // the user's changes are saved here
            public void onTextChanged(CharSequence c, int start, int before, int count) {
                input=c.toString();
                Log.d(TAG, "1 onTextChanged input: "+input);

                input=input.replaceAll("([+\\-/*])(\\d)"," $1 $2");
                Log.d(TAG, "2 onTextChanged input: "+input);

                input=input.trim().replaceAll("(^-)(\\s)","$1");
                Log.d(TAG, "3 onTextChanged input: "+input);

                //input=input.replaceAll("\\s+", " ");
                //Log.d(TAG, "4 onTextChanged input: "+input);

                input=input.replaceAll("(\\D\\s\\D)(\\s)", "$1");
                Log.d(TAG, "5 onTextChanged input: "+input);

                input=input.replaceAll("(\\d)([-+/*])", "$1 $2");
                Log.d(TAG, "6 onTextChanged input: "+input);

                //input=input.replaceAll("\\s+", " ");
                //Log.d(TAG, "7 onTextChanged input: "+input);

                //input=input.replaceAll("(\\s)(\\.)", "$2");
                //Log.d(TAG, "8 onTextChanged input: "+input);

                try {
                    result = evalRPN(infixToPostfix(input));
                    Log.d(TAG, "result: "+result);

                    if (!String.valueOf(result).equals("-0.0")) {
                        int intResult = (int)result;
                        if (intResult==result)
                            tv_Res.setText(String.valueOf(intResult));
                        else {
                            tv_Res.setText(String.valueOf(result));
                        }
                    }
                }
                catch (NoSuchElementException ignored){}
            }

            public void beforeTextChanged(CharSequence c, int start, int count, int after) {
                // this space intentionally left blank
            }

            public void afterTextChanged(Editable c) {
                // this one too
            }
        });

        getChildViews((ViewGroup) findViewById(R.id.ll_nums));

        for (final Button btn: buttonArrayList){
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String curBtnText = btn.getText().toString();
                    addAtCursorPos(curBtnText);
                }
            });
        }

        Button btn_Clear = findViewById(R.id.btn_Clear);
        btn_Clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editText.getText().clear();
                tv_Res.setText("");
            }
        });

        Button btn_Del = findViewById(R.id.btn_Del);
        btn_Del.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int length = editText.getText().length();
                if (length > 0) {
                    delAtCursorPos();
                }
            }
        });

        Button btn_Dot = findViewById(R.id.btn_Dot);
        btn_Dot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int length = editText.getText().length();
                if (length>0){
                    int index = editText.getSelectionStart();
                    if (index!=0){
                        String lastChar = editText.getText().toString();
                        lastChar=String.valueOf(lastChar.charAt(index-1));
                        Log.d(TAG, "lastChar: "+lastChar);
                        if (!(lastChar.equals(".")))
                            addAtCursorPos(".");
                    }
                }
            }
        });

        buttonArrayList.clear();
        getChildViews((ViewGroup) findViewById(R.id.ll_funcs));

        for (final Button btn: buttonArrayList){

            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    int lengthET = editText.getText().length();
                    String curBtn=btn.getText().toString();


                    if (!btn.getText().toString().equals("=") && lengthET!=0) {

                        String lastChar = editText.getText().toString();
                        int index = editText.getSelectionStart();
                        if(index!=0)
                            lastChar=String.valueOf(lastChar.charAt(index-1));
                        else lastChar=" ";

                        //Log.d(TAG, "lastChar: "+lastChar);
                        if (!(lastChar.equals(curBtn))){
                            if (lastChar.equals("/") && curBtn.equals("*")) {
                                delAtCursorPos();
                                addAtCursorPos(curBtn);
                            }
                            else if (lastChar.equals("*") && curBtn.equals("/")) {
                                delAtCursorPos();
                                addAtCursorPos(curBtn);
                            }
                            else if (lastChar.equals("+") && curBtn.equals("-")) {
                                delAtCursorPos();
                                addAtCursorPos(curBtn);
                            }
                            else if (lastChar.equals("-") && curBtn.equals("+")) {
                                delAtCursorPos();
                                addAtCursorPos(curBtn);
                            }
                            else {
                                addAtCursorPos(curBtn);
                            }
                        }
                    }
                    else if (btn.getText().toString().equals("-")){
                        addAtCursorPos(curBtn);
                    }
                    else if (btn.getText().toString().equals("=")) {
                        editText.setText(tv_Res.getText());
                        input="";
                        editText.setSelection(editText.getText().length());
                    }
                }
            });
        }

    }

    public void getChildViews(ViewGroup viewGroup) {

        if (viewGroup != null) {
            int children = viewGroup.getChildCount();
            for (int i = 0; i < children; i++) {
                View view = viewGroup.getChildAt(i);
                if (view instanceof ViewGroup) {
                    getChildViews((ViewGroup) view);
                }
                if (view instanceof Button) {
                    TypedValue outValue = new TypedValue();
                    view.getContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackground,
                            outValue, true);
                    view.setBackgroundResource(outValue.resourceId);
                    buttonArrayList.add((Button) view);
                }
            }
        }
    }

    private void delAtCursorPos(){
        final EditText editText = findViewById(R.id.et_Input);
        int index = editText.getSelectionStart();
        if(index!=0)
            editText.getText().delete(index - 1, index);
    }

    private void addAtCursorPos(String curBtnText){
        final EditText editText = findViewById(R.id.et_Input);

        int lengthET = editText.getText().length();
        int index = editText.getSelectionStart();
        Log.d(TAG, "index: "+index);

        String full = editText.getText().toString();

        String first = full.substring(0,index);
        Log.d(TAG, "first: "+first);

        String second = full.substring(index,lengthET);
        Log.d(TAG, "second: "+second);

        editText.setText(String.format("%s%s%s", first, curBtnText, second));

        Log.d(TAG, "lengthET: "+lengthET);

        if (lengthET>0)
            editText.setSelection(index + 1);
        else editText.setSelection(1);
    }
}
