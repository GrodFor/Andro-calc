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
    private static final String DIVISION = "/";
    private static final String MULTIPLICATION = "*";
    private static final String ADDITION = "+";
    private static final String SUBTRACTION = "-";
    private static final String EQUATION = "=";
    public static final String DOT = ".";
    public static final String ERROR_RESULT = "-0.0";

    private String userInput;
    private double userResult;

    private TextView resultView;
    private EditText inputView;

    private Button clearAllButton;
    private Button deleteSingleCharButton;
    private Button dotInputButton;

    private Button additionButton;
    private Button subtractionButton;
    private Button divisionButton;
    private Button multiplicationButton;
    private Button equationButton;

    private Button oneButton;
    private Button twoButton;
    private Button threeButton;
    private Button fourButton;
    private Button fiveButton;
    private Button sixButton;
    private Button sevenButton;
    private Button eightButton;
    private Button nineButton;
    private Button zeroButton;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        resultView = findViewById(R.id.result_text_view);
        inputView = findViewById(R.id.user_input_edit_text);
        inputView.requestFocus();

        hideSoftwareKeyboard();

        oneButton = findViewById(R.id.one_button);
        twoButton = findViewById(R.id.two_button);
        threeButton = findViewById(R.id.three_button);
        fourButton = findViewById(R.id.four_button);
        fiveButton = findViewById(R.id.five_button);
        sixButton = findViewById(R.id.six_button);
        sevenButton = findViewById(R.id.seven_button);
        eightButton = findViewById(R.id.eight_button);
        nineButton = findViewById(R.id.nine_button);
        zeroButton = findViewById(R.id.zero_button);

        additionButton = findViewById(R.id.add_button);
        subtractionButton = findViewById(R.id.substact_button);
        divisionButton = findViewById(R.id.division_button);
        multiplicationButton = findViewById(R.id.multiply_button);
        equationButton = findViewById(R.id.equal_button);

        final Button[] numbersButtons =
                {oneButton, twoButton, threeButton, fourButton, fiveButton, sixButton,
                        sevenButton, eightButton, nineButton, zeroButton};

        final Button[] funcsButtons =
                {additionButton, subtractionButton, divisionButton, multiplicationButton, equationButton};

        final TextWatcher inputTextWatcher = new TextWatcher() {

            @Override
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

                    if (!String.valueOf(userResult).equals(ERROR_RESULT)) {
                        int intResult = (int) userResult;
                        resultView.setText(intResult == userResult ? String.valueOf(intResult) : String.valueOf(userResult));
                    }

                } catch (NoSuchElementException e) {
                    userResult = 0;
                    Log.d(TAG, "onTextChanged: getting wrong input" + e);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence c, int start, int count, int after) {
                // this space intentionally left blank
            }

            @Override
            public void afterTextChanged(Editable c) {
                // this one too
            }
        };

        inputView.addTextChangedListener(inputTextWatcher);

        resultView.setOnLongClickListener(v -> {
            openDialog();

            return true;
        });

        resultView.setOnClickListener(v -> Toast.makeText(this, "Удерживайте, чтобы скопировать", Toast.LENGTH_SHORT).show());

        for (final Button button : numbersButtons) {
            button.setOnClickListener(v -> {
                String currentButtonText = button.getText().toString();
                addAtCursorPos(currentButtonText);
            });
        }

        clearAllButton = findViewById(R.id.clear_all_button);
        clearAllButton.setOnClickListener(v -> {
            inputView.getText().clear();
            resultView.setText("");
        });

        deleteSingleCharButton = findViewById(R.id.delete_button);
        deleteSingleCharButton.setOnClickListener(v -> {
            int length = inputView.getText().length();

            if (length > 0) {
                delAtCursorPos();
                length = inputView.getText().length();
            }

            if (length == 0) {
                resultView.setText("");
            }
        });

        dotInputButton = findViewById(R.id.dot_input_button);

        final View.OnClickListener dotButtonClickListener = v -> {
            final String inputText = inputView.getText().toString();

            if (inputText.length() > 0) {
                int index = inputView.getSelectionStart();

                if (index != 0) {
                    String lastChar = inputText;
                    lastChar = String.valueOf(lastChar.charAt(index - 1));
                    Log.d(TAG, "lastChar: " + lastChar);

                    if (!(lastChar.equals(DOT))) {
                        addAtCursorPos(DOT);
                    }
                }
            }
        };

        dotInputButton.setOnClickListener(dotButtonClickListener);

        for (final Button button : funcsButtons) {
            button.setOnClickListener(v -> {
                int lengthET = inputView.getText().length();
                final String curBtn = button.getText().toString();

                if (!curBtn.equals(EQUATION) && lengthET != 0) {
                    String lastChar = inputView.getText().toString();
                    int index = inputView.getSelectionStart();
                    lastChar = index != 0 ? String.valueOf(lastChar.charAt(index - 1)) : " ";

                    if (lastChar.equals(DIVISION) && curBtn.equals(MULTIPLICATION)) {
                        swapSamePriorityOperators(curBtn);
                    } else if (lastChar.equals(MULTIPLICATION) && curBtn.equals(DIVISION)) {
                        swapSamePriorityOperators(curBtn);
                    } else if (lastChar.equals(ADDITION) && curBtn.equals(SUBTRACTION)) {
                        swapSamePriorityOperators(curBtn);
                    } else if (lastChar.equals(SUBTRACTION) && curBtn.equals(ADDITION)) {
                        swapSamePriorityOperators(curBtn);
                    } else {
                        addAtCursorPos(curBtn);
                    }
                } else if (curBtn.equals(SUBTRACTION)) {
                    addAtCursorPos(curBtn);
                } else if (curBtn.equals(EQUATION)) {
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

    private void swapSamePriorityOperators(String newOperator) {
        delAtCursorPos();
        addAtCursorPos(newOperator);
    }

    private void delAtCursorPos() {
        int index = inputView.getSelectionStart();

        if (index != 0) {
            inputView.getText().delete(index - 1, index);
        }
    }

    private void addAtCursorPos(String currentButtonText) {
        int inputLength = inputView.getText().length();
        int index = inputView.getSelectionStart();
        Log.d(TAG, "index: " + index);

        String fullString = inputView.getText().toString();
        Log.d(TAG, "full string: " + fullString);

        String beforeCursorText = fullString.substring(0, index);
        Log.d(TAG, "first: " + beforeCursorText);

        String afterCursorText = fullString.substring(index, inputLength);
        Log.d(TAG, "second: " + afterCursorText);

        inputView.setText(String.format("%s%s%s", beforeCursorText, currentButtonText, afterCursorText));
        inputView.setSelection(inputLength > 0 ? index + 1 : 1);
    }

    private void openDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Скопировать результат в буфер?")
                .setCancelable(true)
                .setPositiveButton(getString(android.R.string.yes), (dialog, id) -> {
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("result", resultView.getText().toString());
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(this, "Скопировано", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(getString(android.R.string.no), (dialog, id) -> dialog.cancel())
        ;

        AlertDialog alert = builder.create();
        alert.show();
    }
}