package com.musab.calculator;

import java.util.Locale;

/**
 * Recursive-descent expression evaluator.
 * Supports + - * / % ^ ( ), unary minus,
 * functions sin cos tan asin acos atan log ln sqrt abs,
 * postfix factorial (!), and constants pi/π and e.
 */
public class Evaluator {

    private String expr;
    private int pos;

    public double evaluate(String s) throws Exception {
        expr = s.replace(" ", "");
        pos = 0;
        double result = parseExpression();
        if (pos < expr.length()) {
            throw new Exception("Unexpected character: " + expr.charAt(pos));
        }
        return result;
    }

    private double parseExpression() throws Exception {
        double value = parseTerm();
        while (pos < expr.length()) {
            char c = expr.charAt(pos);
            if (c == '+' || c == '-' || c == '−') {
                pos++;
                double rhs = parseTerm();
                if (c == '+') value += rhs;
                else value -= rhs;
            } else {
                break;
            }
        }
        return value;
    }

    private double parseTerm() throws Exception {
        double value = parsePower();
        while (pos < expr.length()) {
            char c = expr.charAt(pos);
            if (c == '*' || c == '/' || c == '%' || c == '×' || c == '÷') {
                pos++;
                double rhs = parsePower();
                switch (c) {
                    case '*':
                    case '×':
                        value *= rhs;
                        break;
                    case '%':
                        value %= rhs;
                        break;
                    default:
                        value /= rhs;
                        break;
                }
            } else {
                break;
            }
        }
        return value;
    }

    private double parsePower() throws Exception {
        double value = parseUnary();
        while (pos < expr.length() && expr.charAt(pos) == '^') {
            pos++;
            double rhs = parseUnary();
            value = Math.pow(value, rhs);
        }
        return value;
    }

    private double parseUnary() throws Exception {
        if (pos < expr.length()) {
            char c = expr.charAt(pos);
            if (c == '-' || c == '−') {
                pos++;
                return -parseUnary();
            }
            if (c == '+') {
                pos++;
                return parseUnary();
            }
        }
        return parsePostfix();
    }

    private double parsePostfix() throws Exception {
        double value = parsePrimary();
        while (pos < expr.length() && expr.charAt(pos) == '!') {
            pos++;
            value = factorial((int) Math.round(value));
        }
        return value;
    }

    private double parsePrimary() throws Exception {
        if (pos >= expr.length()) {
            throw new Exception("Unexpected end of expression");
        }
        char c = expr.charAt(pos);

        if (c == '(') {
            pos++;
            double value = parseExpression();
            if (pos < expr.length() && expr.charAt(pos) == ')') {
                pos++;
            } else {
                throw new Exception("Missing ')'");
            }
            return value;
        }

        if (c == '√') {
            pos++;
            return Math.sqrt(parseUnary());
        }

        if (c == 'π') {
            pos++;
            return Math.PI;
        }

        if (Character.isDigit(c) || c == '.') {
            return parseNumber();
        }

        if (Character.isLetter(c)) {
            String name = parseName();
            if (pos < expr.length() && expr.charAt(pos) == '(') {
                pos++;
                double arg = parseExpression();
                if (pos < expr.length() && expr.charAt(pos) == ')') {
                    pos++;
                } else {
                    throw new Exception("Missing ')'");
                }
                return applyFunction(name, arg);
            }
            Double constant = constant(name);
            if (constant != null) {
                return constant;
            }
            throw new Exception("Unknown identifier: " + name);
        }

        throw new Exception("Unexpected character: " + c);
    }

    private double parseNumber() throws Exception {
        int start = pos;
        while (pos < expr.length()
                && (Character.isDigit(expr.charAt(pos)) || expr.charAt(pos) == '.')) {
            pos++;
        }
        return Double.parseDouble(expr.substring(start, pos));
    }

    private String parseName() {
        int start = pos;
        while (pos < expr.length() && Character.isLetter(expr.charAt(pos))) {
            pos++;
        }
        return expr.substring(start, pos);
    }

    private Double constant(String name) {
        switch (name.toLowerCase(Locale.ROOT)) {
            case "pi":
            case "π":
                return Math.PI;
            case "e":
                return Math.E;
            default:
                return null;
        }
    }

    private double applyFunction(String name, double arg) throws Exception {
        switch (name.toLowerCase(Locale.ROOT)) {
            case "sin": return Math.sin(Math.toRadians(arg));
            case "cos": return Math.cos(Math.toRadians(arg));
            case "tan": return Math.tan(Math.toRadians(arg));
            case "asin": return Math.toDegrees(Math.asin(arg));
            case "acos": return Math.toDegrees(Math.acos(arg));
            case "atan": return Math.toDegrees(Math.atan(arg));
            case "log": return Math.log10(arg);
            case "ln": return Math.log(arg);
            case "sqrt": return Math.sqrt(arg);
            case "abs": return Math.abs(arg);
            default: throw new Exception("Unknown function: " + name);
        }
    }

    private long factorial(int n) {
        long r = 1;
        for (int i = 2; i <= n; i++) {
            r *= i;
        }
        return r;
    }

    public static String format(double v) {
        if (Double.isNaN(v)) return "NaN";
        if (Double.isInfinite(v)) return v > 0 ? "∞" : "-∞";
        if (v == Math.floor(v) && Math.abs(v) < 1e15) {
            return String.valueOf((long) v);
        }
        return String.valueOf(v);
    }
}
