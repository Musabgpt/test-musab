package com.musab.calculator;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {

    private TextView display;
    private TextView history;
    private final StringBuilder input = new StringBuilder();
    private boolean justEvaluated = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.parseColor("#0F172A"));
        root.setPadding(dp(16), dp(32), dp(16), dp(16));

        history = new TextView(this);
        history.setTextSize(16);
        history.setTextColor(Color.parseColor("#94A3B8"));
        history.setGravity(Gravity.END);
        history.setTypeface(Typeface.MONOSPACE);
        history.setMaxLines(1);
        history.setText(" ");

        display = new TextView(this);
        display.setTextSize(42);
        display.setTextColor(Color.WHITE);
        display.setGravity(Gravity.END);
        display.setTypeface(Typeface.MONOSPACE);
        display.setMaxLines(1);
        display.setHorizontallyScrolling(true);
        display.setPadding(0, dp(8), 0, dp(8));
        display.setText("0");

        root.addView(history);
        root.addView(display);

        GridLayout grid = new GridLayout(this);
        grid.setColumnCount(4);
        grid.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        String[] labels = {
                "C", "⌫", "(", ")",
                "sin", "cos", "tan", "÷",
                "log", "ln", "√", "×",
                "7", "8", "9", "−",
                "4", "5", "6", "+",
                "1", "2", "3", "^",
                "0", ".", "π", "!",
                "e", "%", "x²", "="
        };

        for (String label : labels) {
            Button b = new Button(this);
            b.setText(label);
            b.setTextSize(19);
            b.setAllCaps(false);
            b.setTextColor(Color.WHITE);

            GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
            lp.width = 0;
            lp.height = dp(62);
            lp.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            lp.setMargins(dp(4), dp(4), dp(4), dp(4));
            b.setLayoutParams(lp);

            styleButton(b, label);
            b.setOnClickListener(this::onClick);
            grid.addView(b);
        }

        root.addView(grid);
        setContentView(root);
    }

    private void onClick(View v) {
        String label = ((Button) v).getText().toString();

        if (label.equals("C")) {
            input.setLength(0);
            history.setText(" ");
            justEvaluated = false;
        } else if (label.equals("⌫")) {
            if (input.length() > 0) {
                input.deleteCharAt(input.length() - 1);
            }
        } else if (label.equals("=")) {
            String expr = input.toString();
            if (expr.isEmpty()) return;
            try {
                double result = new Evaluator().evaluate(expr);
                history.setText(expr + " =");
                input.setLength(0);
                input.append(Evaluator.format(result));
                justEvaluated = true;
            } catch (Exception ex) {
                history.setText("خطأ");
            }
        } else {
            if (justEvaluated) {
                if (startsNewExpression(label)) {
                    input.setLength(0);
                    history.setText(" ");
                }
                justEvaluated = false;
            }
            append(label);
        }

        updateDisplay();
    }

    private void append(String label) {
        switch (label) {
            case "sin":
            case "cos":
            case "tan":
            case "log":
            case "ln":
                input.append(label).append("(");
                break;
            case "√":
                input.append("√");
                break;
            case "x²":
                input.append("^2");
                break;
            case "π":
                input.append("π");
                break;
            case "e":
                input.append("e");
                break;
            default:
                input.append(label);
                break;
        }
    }

    private boolean startsNewExpression(String label) {
        if (label.length() == 0) return false;
        char c = label.charAt(0);
        if (Character.isDigit(c)) return true;
        return label.equals(".") || label.equals("(")
                || label.equals("sin") || label.equals("cos")
                || label.equals("tan") || label.equals("log")
                || label.equals("ln") || label.equals("√")
                || label.equals("π") || label.equals("e");
    }

    private void updateDisplay() {
        if (input.length() == 0) {
            display.setText("0");
        } else {
            display.setText(input.toString());
        }
    }

    private boolean isFunction(String label) {
        return label.equals("sin") || label.equals("cos") || label.equals("tan")
                || label.equals("log") || label.equals("ln") || label.equals("√")
                || label.equals("x²");
    }

    private void styleButton(Button b, String label) {
        if (label.equals("C") || label.equals("⌫")) {
            b.setBackgroundColor(Color.parseColor("#EF4444"));
        } else if (label.equals("=")) {
            b.setBackgroundColor(Color.parseColor("#3B82F6"));
        } else if (isFunction(label) || label.equals("π") || label.equals("e")) {
            b.setBackgroundColor(Color.parseColor("#1E3A5F"));
            b.setTextColor(Color.parseColor("#93C5FD"));
        } else if (label.equals("+") || label.equals("−") || label.equals("×")
                || label.equals("÷") || label.equals("^") || label.equals("%")
                || label.equals("(") || label.equals(")")) {
            b.setBackgroundColor(Color.parseColor("#B45309"));
        } else {
            b.setBackgroundColor(Color.parseColor("#1E293B"));
        }
    }

    private int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }
}
