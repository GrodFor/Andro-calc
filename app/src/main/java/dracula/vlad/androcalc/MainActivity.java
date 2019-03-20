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
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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
    public static final double ERROR_RESULT = -0.0;
    public static final String SPLIT_WITH_WHITESPACES = "([+\\-/*])(\\d)";
    public static final String ADD_WHITESPACE_BEFORE_FIRST_NEGATION = "(^-)(\\s)";
    public static final String REMOVE_WHITESPACE_BETWEEN_NEGATION_AND_NUMBER = "(\\D\\s\\D)(\\s)";
    public static final String ADD_WHITESPACES_AFTER_NUMBERS_BEFORE_NEGATION = "(\\d)([-+/*])";
    public static final String ON_TEXT_CHANGED_INPUT = "onTextChanged input: ";
    public static final String ON_TEXT_CHANGED_RESULT = "onTextChanged result: ";
    public static final String ON_TEXT_CHANGED_GETTING_WRONG_INPUT = "onTextChanged: getting wrong input";

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
    private Button[] numbersButtons;
    private Button[] funcsButtons;

    private MyTextWatcher myTextWatcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        hideSoftwareKeyboard();

        initNumsButtons();

        initResultAndInputViews();

        initNumsClickListener();

        initClearAndDelButtons();

        initDotButton();

        initFuncsButtonsClickListeners();
    }

    private void initFuncsButtonsClickListeners() {
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

    private void initDotButton() {
        dotInputButton = findViewById(R.id.dot_input_button);

        final View.OnClickListener dotButtonClickListener = v -> {
            final String inputText = inputView.getText().toString();

            if (inputText.length() > 0) {
                int index = inputView.getSelectionStart();

                if (index != 0) {
                    String lastChar = inputText;
                    lastChar = String.valueOf(lastChar.charAt(index - 1));

                    if (!(lastChar.equals(DOT))) {
                        addAtCursorPos(DOT);
                    }
                }
            }
        };

        dotInputButton.setOnClickListener(dotButtonClickListener);
    }

    private void initClearAndDelButtons() {
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
    }

    private void initNumsClickListener() {
        for (final Button button : numbersButtons) {
            button.setOnClickListener(v -> {
                String currentButtonText = button.getText().toString();
                addAtCursorPos(currentButtonText);
            });
        }
    }

    private void initNumsButtons() {
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

        numbersButtons = new Button[]{oneButton, twoButton, threeButton, fourButton, fiveButton, sixButton,
                sevenButton, eightButton, nineButton, zeroButton};

        funcsButtons = new Button[]{additionButton, subtractionButton, divisionButton, multiplicationButton, equationButton};
    }

    private void initResultAndInputViews() {
        resultView = findViewById(R.id.result_text_view);
        resultView.setOnLongClickListener(v -> {
            openDialog();

            return true;
        });

        resultView.setOnClickListener(v -> Toast.makeText(this, getString(R.string.hold_to_copy), Toast.LENGTH_SHORT).show());

        myTextWatcher = new MyTextWatcher();
        inputView.addTextChangedListener(myTextWatcher);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void hideSoftwareKeyboard() {
        inputView = findViewById(R.id.user_input_edit_text);
        inputView.requestFocus();

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

        String fullString = inputView.getText().toString();
        String beforeCursorText = fullString.substring(0, index);
        String afterCursorText = fullString.substring(index, inputLength);

        inputView.setText(String.format("%s%s%s", beforeCursorText, currentButtonText, afterCursorText));
        inputView.setSelection(inputLength > 0 ? index + 1 : 1);
    }

    private void openDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.add_to_byffer)
                .setCancelable(true)
                .setPositiveButton(getString(android.R.string.yes), (dialog, id) -> {
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("result", resultView.getText().toString());
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(this, getString(R.string.copied), Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(getString(android.R.string.no), (dialog, id) -> dialog.cancel())
        ;

        AlertDialog alert = builder.create();
        alert.show();
    }

    private class MyTextWatcher implements TextWatcher {

        @Override
        public void onTextChanged(CharSequence c, int start, int before, int count) {
            userInput = c.toString();
            Log.d(TAG, ON_TEXT_CHANGED_INPUT + userInput);

            userInput = userInput.replaceAll(SPLIT_WITH_WHITESPACES, " $1 $2");
            Log.d(TAG, ON_TEXT_CHANGED_INPUT + userInput);

            userInput = userInput.trim().replaceAll(ADD_WHITESPACE_BEFORE_FIRST_NEGATION, "$1");
            Log.d(TAG, ON_TEXT_CHANGED_INPUT + userInput);

            userInput = userInput.replaceAll(REMOVE_WHITESPACE_BETWEEN_NEGATION_AND_NUMBER, "$1");
            Log.d(TAG, ON_TEXT_CHANGED_INPUT + userInput);

            userInput = userInput.replaceAll(ADD_WHITESPACES_AFTER_NUMBERS_BEFORE_NEGATION, "$1 $2");
            Log.d(TAG, ON_TEXT_CHANGED_INPUT + userInput);

            try {
                userResult = evalRPN(infixToPostfix(userInput));
                Log.d(TAG, ON_TEXT_CHANGED_RESULT + userResult);

                if (!Double.valueOf(userResult).equals(ERROR_RESULT)) {
                    int intResult = (int) userResult;
                    resultView.setText(intResult == userResult ? String.valueOf(intResult) : String.valueOf(userResult));
                }

            } catch (NoSuchElementException e) {
                userResult = 0;
                Log.d(TAG, ON_TEXT_CHANGED_GETTING_WRONG_INPUT + e);
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
    }
}