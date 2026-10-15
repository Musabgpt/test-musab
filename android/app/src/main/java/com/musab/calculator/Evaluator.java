package com.musab.calculator;

import java.util.Locale;
import java.math.BigInteger;

/**
 * Recursive-descent expression evaluator.
 * Supports + - * / % ^ ( ), unary minus,
 * functions sin cos tan asin acos atan log ln sqrt abs,
 * postfix factorial (!), and constants pi/π and e.
 * Programmer mode: bitwise & | ^ ~ << >>, bases 0x/0b/0o, base conversion.
 */
public class Evaluator {

    // Programmer mode state
    public static boolean programmerMode = false;
    public static int currentBase = 10; // 2, 8, 10, 16

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

    // ===== Precedence chain (lowest to highest) =====
    // parseExpression (bitwise OR) -> parseBitwiseXor -> parseBitwiseAnd
    // -> parseEquality -> parseRelational -> parseShift
    // -> parseAdditive (+ -) -> parseMultiplicative (* / %)
    // -> parsePower (^) -> parseUnary -> parsePostfix -> parsePrimary

    private double parseExpression() throws Exception {
        return parseBitwiseOr();
    }

    // Bitwise OR (|) - lowest precedence
    private double parseBitwiseOr() throws Exception {
        double value = parseBitwiseXor();
        while (pos < expr.length() && expr.charAt(pos) == '|') {
            pos++;
            double rhs = parseBitwiseXor();
            value = (long) value | (long) rhs;
        }
        return value;
    }

    // Bitwise XOR (^)
    private double parseBitwiseXor() throws Exception {
        double value = parseBitwiseAnd();
        while (pos < expr.length() && expr.charAt(pos) == '^') {
            pos++;
            double rhs = parseBitwiseAnd();
            value = (long) value ^ (long) rhs;
        }
        return value;
    }

    // Bitwise AND (&)
    private double parseBitwiseAnd() throws Exception {
        double value = parseEquality();
        while (pos < expr.length() && expr.charAt(pos) == '&') {
            pos++;
            double rhs = parseEquality();
            value = (long) value & (long) rhs;
        }
        return value;
    }

    // Equality operators (== !=)
    private double parseEquality() throws Exception {
        double value = parseRelational();
        while (pos + 1 < expr.length()) {
            if (expr.charAt(pos) == '=' && expr.charAt(pos + 1) == '=') {
                pos += 2;
                double rhs = parseRelational();
                value = (value == rhs) ? 1 : 0;
            } else if (expr.charAt(pos) == '!' && expr.charAt(pos + 1) == '=') {
                pos += 2;
                double rhs = parseRelational();
                value = (value != rhs) ? 1 : 0;
            } else {
                break;
            }
        }
        return value;
    }

    // Relational operators (< > <= >=)
    private double parseRelational() throws Exception {
        double value = parseShift();
        while (pos < expr.length()) {
            char c = expr.charAt(pos);
            if (c == '<' || c == '>') {
                pos++;
                boolean isEq = pos < expr.length() && expr.charAt(pos) == '=';
                if (isEq) pos++;
                double rhs = parseShift();
                switch (c) {
                    case '<': value = isEq ? (value <= rhs ? 1 : 0) : (value < rhs ? 1 : 0); break;
                    case '>': value = isEq ? (value >= rhs ? 1 : 0) : (value > rhs ? 1 : 0); break;
                }
            } else {
                break;
            }
        }
        return value;
    }

    // Shift operators (<< >>)
    private double parseShift() throws Exception {
        double value = parseAdditive();
        while (pos + 1 < expr.length()) {
            if (expr.charAt(pos) == '<' && expr.charAt(pos + 1) == '<') {
                pos += 2;
                double rhs = parseAdditive();
                value = (long) value << (long) rhs;
            } else if (expr.charAt(pos) == '>' && expr.charAt(pos + 1) == '>') {
                pos += 2;
                double rhs = parseAdditive();
                value = (long) value >> (long) rhs;
            } else {
                break;
            }
        }
        return value;
    }

    // Additive operators (+ -)
    private double parseAdditive() throws Exception {
        double value = parseMultiplicative();
        while (pos < expr.length()) {
            char c = expr.charAt(pos);
            if (c == '+' || c == '-' || c == '−') {
                pos++;
                double rhs = parseMultiplicative();
                if (c == '+' || c == '−') value += rhs;
                else value -= rhs;
            } else {
                break;
            }
        }
        return value;
    }

    // Multiplicative operators (* / %)
    private double parseMultiplicative() throws Exception {
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

    // Power operator (^) - right associative
    private double parsePower() throws Exception {
        double value = parseUnary();
        if (pos < expr.length() && expr.charAt(pos) == '^') {
            pos++;
            double rhs = parsePower(); // right associative
            value = Math.pow(value, rhs);
        }
        return value;
    }

    // Unary operators (- + ~)
    private double parseUnary() throws Exception {
        if (pos < expr.length()) {
            char c = expr.charAt(pos);
            if (c == '-' || c == '−') {
                pos++;
                return -parseUnary();
            } else if (c == '+') {
                pos++;
                return parseUnary();
            } else if (c == '~') {
                pos++;
                double value = parseUnary();
                return ~(long) value;
            }
        }
        return parsePostfix();
    }

    // Postfix operators (!)
    private double parsePostfix() throws Exception {
        double value = parsePrimary();
        while (pos < expr.length() && expr.charAt(pos) == '!') {
            pos++;
            value = factorial((int) Math.round(value));
        }
        return value;
    }

    // Primary expressions: numbers, parentheses, functions, constants
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

        if (c == 'e') {
            pos++;
            return Math.E;
        }

        // Number parsing (including hex/bin/oct prefixes)
        int start = pos;
        if (c == '0' && pos + 1 < expr.length()) {
            char next = expr.charAt(pos + 1);
            if (next == 'x' || next == 'X') { // hex
                pos += 2;
                while (pos < expr.length() && isHexDigit(expr.charAt(pos))) pos++;
                String hexStr = expr.substring(start, pos);
                return Long.parseLong(hexStr.substring(2), 16);
            } else if (next == 'b' || next == 'B') { // binary
                pos += 2;
                while (pos < expr.length() && (expr.charAt(pos) == '0' || expr.charAt(pos) == '1')) pos++;
                String binStr = expr.substring(start, pos);
                return Long.parseLong(binStr.substring(2), 2);
            } else if (next == 'o' || next == 'O') { // octal
                pos += 2;
                while (pos < expr.length() && expr.charAt(pos) >= '0' && expr.charAt(pos) <= '7') pos++;
                String octStr = expr.substring(start, pos);
                return Long.parseLong(octStr.substring(2), 8);
            }
        }

        // Regular decimal number
        while (pos < expr.length() && (Character.isDigit(expr.charAt(pos)) || expr.charAt(pos) == '.')) {
            pos++;
        }
        if (pos == start) {
            // Not a number, try function/constant
            return parseFunctionOrConstant();
        }
        String numStr = expr.substring(start, pos);
        double value = Double.parseDouble(numStr);

        // Postfix factorial
        if (pos < expr.length() && expr.charAt(pos) == '!') {
            pos++;
            value = factorial((int) Math.round(value));
        }

        return value;
    }

    private boolean isHexDigit(char c) {
        return Character.isDigit(c) || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');
    }

    private long factorial(int n) {
        if (n < 0) throw new Exception("Factorial of negative");
        long r = 1;
        for (int i = 2; i <= n; i++) {
            r *= i;
        }
        return r;
    }

    // Function and constant parsing
    private double parseFunctionOrConstant() throws Exception {
        // Functions: sin, cos, tan, asin, acos, atan, log, ln, sqrt, abs
        if (pos + 3 <= expr.length()) {
            String three = expr.substring(pos, pos + 3).toLowerCase();
            if (three.equals("sin") || three.equals("cos") || three.equals("tan") ||
                three.equals("abs")) {
                String func = three;
                pos += 3;
                double arg = parseUnary();
                switch (func) {
                    case "sin": return Math.sin(Math.toRadians(arg));
                    case "cos": return Math.cos(Math.toRadians(arg));
                    case "tan": return Math.tan(Math.toRadians(arg));
                    case "abs": return Math.abs(arg);
                }
            }
        }

        if (pos + 4 <= expr.length()) {
            String four = expr.substring(pos, pos + 4).toLowerCase();
            if (four.equals("asin") || four.equals("acos") || four.equals("atan") ||
                four.equals("log ") || four.equals("ln(") || four.equals("sqrt")) {
                String func = four;
                pos += 4;
                double arg = parseUnary();
                switch (func) {
                    case "asin": return Math.toDegrees(Math.asin(arg));
                    case "acos": return Math.toDegrees(Math.acos(arg));
                    case "atan": return Math.toDegrees(Math.atan(arg));
                    case "log ": return Math.log10(arg);
                    case "ln(": return Math.log(arg);
                    case "sqrt": return Math.sqrt(arg);
                }
            }
        }

        // Constants: pi, e
        if (pos + 2 <= expr.length()) {
            String two = expr.substring(pos, pos + 2).toLowerCase();
            if (two.equals("pi")) {
                pos += 2;
                return Math.PI;
            }
        }

        if (pos + 1 <= expr.length()) {
            char c = expr.charAt(pos);
            if (c == 'e') {
                pos++;
                return Math.E;
            }
        }

        throw new Exception("Unknown token at position " + pos + ": " + expr.substring(pos, Math.min(pos + 10, expr.length())));
    }

    public static String format(double v) {
        if (Double.isNaN(v)) return "NaN";
        if (Double.isInfinite(v)) return v > 0 ? "∞" : "-∞";
        long l = (long) v;
        switch (currentBase) {
            case 2: return "0b" + Long.toBinaryString(l);
            case 8: return "0o" + Long.toOctalString(l);
            case 16: return "0x" + Long.toHexString(l).toUpperCase();
            default: return String.valueOf(l);
        }
    }
}