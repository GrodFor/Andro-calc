package dracula.vlad.androcalc;

import android.util.Log;

import java.util.LinkedList;
import java.util.Stack;

class ShuntingYard {

    private static final String TAG = ShuntingYard.class.getName();

    static String infixToPostfix(String infix) {

        infix = infix.replaceAll("[^*+\\-\\d./\\s]", "");
        String operators = "-+/*";
        StringBuilder postfix = new StringBuilder();
        Stack<Integer> priorities = new Stack<>();

        String token;
        String[] tokens = infix.split("\\s");

        for (String singleToken : tokens) {
            token = singleToken;

            if (token.isEmpty()) {
                continue;
            }

            int index = operators.indexOf(token);

            if (index != -1) {

                if (priorities.isEmpty()) {
                    priorities.push(index);
                } else {

                    while (!priorities.isEmpty()) {
                        int previousSecond = priorities.peek() / 2;
                        int previousFirst = index / 2;

                        if (previousSecond > previousFirst || previousSecond == previousFirst) {
                            postfix.append(operators.charAt(priorities.pop())).append(' ');
                        } else {
                            break;
                        }
                    }
                    priorities.push(index);
                }
            } else {
                postfix.append(token).append(' ');
            }
        }

        while (!priorities.isEmpty()) {
            postfix.append(operators.charAt(priorities.pop())).append(' ');
        }

        return postfix.toString();
    }

    static double evalRPN(String postfix) {

        LinkedList<Double> stack = new LinkedList<>();
        String token;
        String[] array = postfix.split("\\s");

        for (String anArray : array) {
            token = anArray;
            double tokenNum;

            try {
                tokenNum = Double.parseDouble(token);
            } catch (NumberFormatException e) {
                tokenNum = -0.0;
                Log.d(TAG, "evalRPN: getting wrong number" + e);
            }

            if (tokenNum != -0.0) {
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
                return -0.0;
            }
        }

        return stack.pop();
    }
}