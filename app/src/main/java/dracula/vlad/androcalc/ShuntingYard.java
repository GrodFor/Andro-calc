package dracula.vlad.androcalc;

import android.util.Log;

import java.util.LinkedList;
import java.util.Stack;

class ShuntingYard {

    private static final String TAG = ShuntingYard.class.getName();
    private static final String OPERATORS = "-+/*";
    private static final double ERROR_VALUE = -0.0;
    private static final int PRIORITY_HALF_DIVIDER = 2;
    public static final String REMOVE_INVALID_SYMBOLS = "[^*+\\-\\d./\\s]";
    public static final String WHITESPACE_SPLITTER = "\\s";

    private static Stack<Integer> priorities;
    private static StringBuilder postfix;

    static String infixToPostfix(String infix) {
        infix = infix.replaceAll(REMOVE_INVALID_SYMBOLS, "");
        postfix = new StringBuilder();
        priorities = new Stack<>();

        String token;
        String[] tokens = infix.split(WHITESPACE_SPLITTER);

        for (String singleToken : tokens) {
            token = singleToken;

            if (token.isEmpty()) {
                continue;
            }

            int index = OPERATORS.indexOf(token);

            if (index != -1) {

                if (priorities.isEmpty()) {
                    priorities.push(index);
                } else {
                    pickHigherPriorityOperator(index);
                    priorities.push(index);
                }
            } else {
                postfix.append(token).append(' ');
            }
        }

        while (!priorities.isEmpty()) {
            postfix.append(OPERATORS.charAt(priorities.pop())).append(' ');
        }

        return postfix.toString();
    }

    private static void pickHigherPriorityOperator(int index) {
        while (!priorities.isEmpty()) {
            int previousSecond = priorities.peek() / PRIORITY_HALF_DIVIDER;
            int previousFirst = index / PRIORITY_HALF_DIVIDER;

            if (previousSecond > previousFirst || previousSecond == previousFirst) {
                postfix.append(OPERATORS.charAt(priorities.pop())).append(' ');
            } else {
                break;
            }
        }
    }

    static double evalRPN(String postfix) {
        LinkedList<Double> stack = new LinkedList<>();
        String token;
        String[] array = postfix.split(WHITESPACE_SPLITTER);

        for (String anArray : array) {
            token = anArray;
            double tokenNum;

            try {
                tokenNum = Double.parseDouble(token);
            } catch (NumberFormatException e) {
                tokenNum = ERROR_VALUE;
                Log.d(TAG, "evalRPN: getting wrong number" + e);
            }

            if (tokenNum != ERROR_VALUE) {
                stack.push(Double.parseDouble(token + ""));
            } else if (token.equals("*")) {
                double secondOperand = stack.pop();
                double firstOperand = stack.pop();
                stack.push(firstOperand * secondOperand);
            } else if (token.equals("/")) {
                double secondOperand = stack.pop();
                double firstOperand = stack.pop();
                stack.push(firstOperand / secondOperand);
            } else if (token.equals("-")) {
                double secondOperand = stack.pop();
                double firstOperand = stack.pop();
                stack.push(firstOperand - secondOperand);
            } else if (token.equals("+")) {
                double secondOperand = stack.pop();
                double firstOperand = stack.pop();
                stack.push(firstOperand + secondOperand);
            } else {
                return ERROR_VALUE;
            }
        }

        return stack.pop();
    }
}