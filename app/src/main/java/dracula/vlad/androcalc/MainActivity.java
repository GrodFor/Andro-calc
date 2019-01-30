package dracula.vlad.androcalc;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private String TAG = "LOG_AC";
    private ArrayList<String> numsList= new ArrayList<>();
    private ArrayList<Button> buttonArrayList= new ArrayList<>();

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

        getChildViews((ViewGroup) findViewById(R.id.ll_nums));
        Collections.sort(numsList);
        String nums = Arrays.deepToString(numsList.toArray());
        Log.d(TAG, nums);

        for (final Button btn: buttonArrayList){
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String curBtnText = btn.getText().toString();
                    addAtCursorPos(curBtnText);
                }
            });
            btn.setBackgroundResource(R.drawable.btn_round);
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
                    String lastChar = editText.getText().toString();
                    lastChar=String.valueOf(lastChar.charAt(index-1));
                    //lastChar = lastChar.substring(length-1, length);
                    Log.d(TAG, "lastChar: "+lastChar);
                    if (!(lastChar.equals(".")))
                            //&& !(editText.getText().toString().contains(".")))
                    {
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

                    int length = tv_Res.getText().length();
                    int lengthET = editText.getText().length();
                    String curBtn=btn.getText().toString();


                    if (!btn.getText().toString().equals("=") && lengthET!=0) {
//                        String lastChar = editText.getText().toString();
//                        lastChar = lastChar.substring(lengthET-1, lengthET);

                        String lastChar = editText.getText().toString();
                        int index = editText.getSelectionStart();
                        if(index!=0){
                            Log.d(TAG, "onClick index: "+index);
                            lastChar=String.valueOf(lastChar.charAt(index-1));
                        }
                        else lastChar=" ";


                        if (!(lastChar.equals(curBtn))){
                            if (lastChar.equals("/") && curBtn.equals("*")) {
//                                editText.getText().delete(lengthET - 1, lengthET);
//                                editText.setText(String.format("%s%s", String.valueOf(editText.getText()), curBtn));
                                //editText.setSelection(editText.getText().length());
                                delAtCursorPos();
                                addAtCursorPos(curBtn);
                            }
                            else if (lastChar.equals("*") && curBtn.equals("/")) {
//                                editText.getText().delete(lengthET - 1, lengthET);
//                                editText.setText(String.format("%s%s", String.valueOf(editText.getText()), curBtn));
                                //editText.setSelection(editText.getText().length());
                                delAtCursorPos();
                                addAtCursorPos(curBtn);
                            }
                            else if (lastChar.equals("+") && curBtn.equals("-")) {
//                                editText.getText().delete(lengthET - 1, lengthET);
//                                editText.setText(String.format("%s%s", String.valueOf(editText.getText()), curBtn));
                                //editText.setSelection(editText.getText().length());
                                delAtCursorPos();
                                addAtCursorPos(curBtn);
                            }
                            else if (lastChar.equals("-") && curBtn.equals("+")) {
//                                editText.getText().delete(lengthET - 1, lengthET);
//                                editText.setText(String.format("%s%s", String.valueOf(editText.getText()), curBtn));
                                //editText.setSelection(editText.getText().length());
                                delAtCursorPos();
                                addAtCursorPos(curBtn);
                            }
//                            else {
//                                //editText.setText(String.format("%s%s", String.valueOf(editText.getText()), curBtn));
//                                //editText.setSelection(editText.getText().length());
//                                addAtCursorPos(curBtn);
//                            }
                        }
                    }
                    else{
                        if (length>0){
                            editText.setText(tv_Res.getText());
                            tv_Res.setText("");
                        }
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
                    ((Button) view).setTextColor(Color.BLUE);
                    String s = ((Button) view).getText().toString();
                    numsList.add(s);
                    buttonArrayList.add((Button) view);
                }
            }
            //return numsList;
        }
        //return null;
    }

    private void delAtCursorPos(){
        final EditText editText = findViewById(R.id.et_Input);
        //final TextView tv_Res = findViewById(R.id.tv_Result);

        int index = editText.getSelectionStart();
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
        else
            editText.setSelection(1);
    }
}
