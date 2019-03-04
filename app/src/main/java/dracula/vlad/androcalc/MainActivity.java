package dracula.vlad.androcalc;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.NoSuchElementException;

import static dracula.vlad.androcalc.ShuntingYard.*;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getName();

    private String userInput = "";
    private ArrayList<Button> buttons = new ArrayList<>();
    private double userResult;

    private TextView resultView = null;
    private EditText inputView;
    private Button clearAllButton;
    private Button deleteOneButton;
    private Button dotButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getChildViews(findViewById(R.id.ll_Buttons));

        inputView = findViewById(R.id.et_Input);
        inputView.requestFocus();

        resultView = findViewById(R.id.tv_Result);

        hideSoftwareKeyboard();

        inputView.addTextChangedListener(new TextWatcher() {

            public void onTextChanged(CharSequence c, int start, int before, int count) {
                userInput = c.toString();
                Log.d(TAG, "1 onTextChanged input: " + userInput);

                userInput = userInput.replaceAll("([+\\-/*])(\\d)", " $1 $2");
                Log.d(TAG, "2 onTextChanged input: " + userInput);

                userInput = userInput.trim().replaceAll("(^-)(\\s)", "$1");
                Log.d(TAG, "3 onTextChanged input: " + userInput);

                userInput = userInput.replaceAll("(\\D\\s\\D)(\\s)", "$1");
                Log.d(TAG, "5 onTextChanged input: " + userInput);

                userInput = userInput.replaceAll("(\\d)([-+/*])", "$1 $2");
                Log.d(TAG, "6 onTextChanged input: " + userInput);

                try {
                    userResult = evalRPN(infixToPostfix(userInput));
                    Log.d(TAG, "result: " + userResult);

                    if (!String.valueOf(userResult).equals("-0.0")) {
                        int intResult = (int) userResult;
                        resultView.setText(intResult == userResult ? String.valueOf(intResult) : String.valueOf(userResult));
                    }

                } catch (NoSuchElementException e) {
                    userResult = 0;
                    Log.d(TAG, "onTextChanged: getting wrong input" + e);
                    Toast.makeText(MainActivity.this, "Please check input values.", Toast.LENGTH_SHORT).show();
                }
            }

            public void beforeTextChanged(CharSequence c, int start, int count, int after) {
                // this space intentionally left blank
            }

            public void afterTextChanged(Editable c) {
                // this one too
            }
        });

        resultView.setOnLongClickListener(v -> {
            openDialog();

            return true;
        });

        resultView.setOnClickListener(v -> Toast.makeText(this, "Удерживайте, чтобы скопировать", Toast.LENGTH_SHORT).show());

        getChildViews(findViewById(R.id.ll_nums));

        for (final Button button : buttons) {
            button.setOnClickListener(v -> {
                String currentButtonText = button.getText().toString();
                addAtCursorPos(currentButtonText);
            });
        }

        clearAllButton = findViewById(R.id.btn_Clear);
        clearAllButton.setOnClickListener(v -> {
            inputView.getText().clear();
            resultView.setText("");
        });

        deleteOneButton = findViewById(R.id.btn_Del);
        deleteOneButton.setOnClickListener(v -> {
            int length = inputView.getText().length();

            if (length > 0) {
                delAtCursorPos();
            }

            if (inputView.getText().length() == 0) {
                resultView.setText("");
            }
        });

        dotButton = findViewById(R.id.btn_Dot);
        dotButton.setOnClickListener(v -> {
            int length = inputView.getText().length();

            if (length > 0) {
                int index = inputView.getSelectionStart();

                if (index != 0) {
                    String lastChar = inputView.getText().toString();
                    lastChar = String.valueOf(lastChar.charAt(index - 1));
                    Log.d(TAG, "lastChar: " + lastChar);

                    if (!(lastChar.equals("."))) {
                        addAtCursorPos(".");
                    }
                }
            }
        });

        buttons.clear();
        getChildViews(findViewById(R.id.ll_funcs));

        for (final Button button : buttons) {
            button.setOnClickListener(v -> {
                int lengthET = inputView.getText().length();
                String curBtn = button.getText().toString();

                if (!button.getText().toString().equals("=") && lengthET != 0) {
                    String lastChar = inputView.getText().toString();
                    int index = inputView.getSelectionStart();
                    lastChar = index != 0 ? String.valueOf(lastChar.charAt(index - 1)) : " ";

                    if (!(lastChar.equals(curBtn))) {

                        if (lastChar.equals("/") && curBtn.equals("*")) {
                            replaceOperator(curBtn);
                        } else if (lastChar.equals("*") && curBtn.equals("/")) {
                            replaceOperator(curBtn);
                        } else if (lastChar.equals("+") && curBtn.equals("-")) {
                            replaceOperator(curBtn);
                        } else if (lastChar.equals("-") && curBtn.equals("+")) {
                            replaceOperator(curBtn);
                        } else {
                            addAtCursorPos(curBtn);
                        }
                    }
                } else if (button.getText().toString().equals("-")) {
                    addAtCursorPos(curBtn);
                } else if (button.getText().toString().equals("=")) {
                    inputView.setText(resultView.getText());
                    userInput = "";
                    inputView.setSelection(inputView.getText().length());
                }
            });
        }

    }

    @SuppressLint("ClickableViewAccessibility")
    private void hideSoftwareKeyboard() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            inputView.setShowSoftInputOnFocus(false);
        } else {
            inputView.setOnTouchListener((v, event) -> {
                v.onTouchEvent(event);
                InputMethodManager inputMethod = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);

                if (inputMethod != null) {
                    inputMethod.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }

                return true;
            });
        }
    }

    private void replaceOperator(String curBtn) {
        delAtCursorPos();
        addAtCursorPos(curBtn);
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
                    buttons.add((Button) view);
                }
            }
        }
    }

    private void delAtCursorPos() {
        int index = inputView.getSelectionStart();

        if (index != 0) {
            inputView.getText().delete(index - 1, index);
        }
    }

    private void addAtCursorPos(String curBtnText) {
        int lengthET = inputView.getText().length();
        int index = inputView.getSelectionStart();
        Log.d(TAG, "index: " + index);

        String full = inputView.getText().toString();

        String first = full.substring(0, index);
        Log.d(TAG, "first: " + first);

        String second = full.substring(index, lengthET);
        Log.d(TAG, "second: " + second);

        inputView.setText(String.format("%s%s%s", first, curBtnText, second));
        inputView.setSelection(lengthET > 0 ? index + 1 : 1);
    }

    private void openDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Скопировать результат в буфер?")
                .setCancelable(true)
                .setPositiveButton("Да", (dialog, id) -> {
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("result", resultView.getText().toString());
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(this, "Скопировано", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Нет", (dialog, id) -> dialog.cancel())
        ;

        AlertDialog alert = builder.create();
        alert.show();
    }
}
