package dracula.vlad.androcalc;

import android.util.Log;
import java.util.LinkedList;
import java.util.Stack;

class ShuntingYard {

    private static String TAG = "LOG_SY";

    static String infixToPostfix(String infix) {

        infix = infix.replaceAll("[^*+\\-\\d./\\s]", "");
        //Log.d(TAG, "infixToPostfix: "+infix);

        String ops = "-+/*";
        StringBuilder sb = new StringBuilder();
        Stack<Integer> s = new Stack<>();

        String token;
        String[] array=infix.split("\\s");

        for (String anArray : array) {
            token = anArray;

            if (token.isEmpty())
                continue;

            int idx = ops.indexOf(token);

            // check for operator
            if (idx != -1) {
                if (s.isEmpty())
                    s.push(idx);

                else {
                    while (!s.isEmpty()) {
                        int prec2 = s.peek() / 2;
                        int prec1 = idx / 2;
                        if (prec2 > prec1 || prec2 == prec1)
                            sb.append(ops.charAt(s.pop())).append(' ');
                        else break;
                    }
                    s.push(idx);
                }
            } else {
                sb.append(token).append(' ');
            }
        }
        while (!s.isEmpty())
            sb.append(ops.charAt(s.pop())).append(' ');
        return sb.toString();
    }

    static double evalRPN(String expr){

        LinkedList<Double> stack = new LinkedList<>();
        String token;
        String[] array= expr.split("\\s");

        for (String anArray : array) {
            token = anArray;
            Double tokenNum = null;

            try {
                tokenNum = Double.parseDouble(token);
            } catch (NumberFormatException ignored) {}

            if (tokenNum != null) {
                stack.push(Double.parseDouble(token + ""));
            }
            else if (token.equals("*")) {
                double secondOperand = stack.pop();
                double firstOperand = stack.pop();
                stack.push(firstOperand * secondOperand);
            }
            else if (token.equals("/")) {
                double secondOperand = stack.pop();
                double firstOperand = stack.pop();
                stack.push(firstOperand / secondOperand);
            }
            else if (token.equals("-")) {
                double secondOperand = stack.pop();
                double firstOperand = stack.pop();
                stack.push(firstOperand - secondOperand);
            }
            else if (token.equals("+")) {
                double secondOperand = stack.pop();
                double firstOperand = stack.pop();
                stack.push(firstOperand + secondOperand);
            }
            else { //error
                return -0.0;
            }
        }
        return stack.pop();
    }
}