//! Advanced calculator engine written in Rust.
//!
//! Recursive-descent expression evaluator supporting:
//! - operators: + - * / % ^ ! (and Unicode × ÷ −)
//! - parentheses and unary minus/plus
//! - functions: sin cos tan asin acos atan log ln sqrt abs
//!   (trig functions take degrees)
//! - constants: pi, π, e
//! - prefix square root: √

/// Evaluates an arithmetic expression and returns the result.
pub fn evaluate(expr: &str) -> Result<f64, String> {
    let mut parser = Parser::new(expr);
    let value = parser.parse_expression()?;
    parser.skip_whitespace();
    if let Some(c) = parser.peek() {
        return Err(format!("unexpected character: {c}"));
    }
    Ok(value)
}

/// Formats a float the way a calculator display would: whole numbers without
/// a trailing `.0`, otherwise the default decimal representation.
pub fn format(v: f64) -> String {
    if v.is_nan() {
        return "NaN".to_string();
    }
    if v.is_infinite() {
        return if v > 0.0 { "∞".to_string() } else { "-∞".to_string() };
    }
    if v == v.trunc() && v.abs() < 1e15 {
        return format!("{}", v as i64);
    }
    format!("{v}")
}

struct Parser {
    chars: Vec<char>,
    pos: usize,
}

impl Parser {
    fn new(expr: &str) -> Self {
        Parser {
            chars: expr.chars().collect(),
            pos: 0,
        }
    }

    fn peek(&self) -> Option<char> {
        self.chars.get(self.pos).copied()
    }

    fn bump(&mut self) -> Option<char> {
        let c = self.peek();
        if c.is_some() {
            self.pos += 1;
        }
        c
    }

    fn skip_whitespace(&mut self) {
        while let Some(c) = self.peek() {
            if c.is_whitespace() {
                self.pos += 1;
            } else {
                break;
            }
        }
    }

    fn parse_expression(&mut self) -> Result<f64, String> {
        let mut value = self.parse_term()?;
        loop {
            self.skip_whitespace();
            match self.peek() {
                Some('+') | Some('-') | Some('−') => {
                    let op = self.bump().unwrap();
                    let rhs = self.parse_term()?;
                    if op == '+' {
                        value += rhs;
                    } else {
                        value -= rhs;
                    }
                }
                _ => break,
            }
        }
        Ok(value)
    }

    fn parse_term(&mut self) -> Result<f64, String> {
        let mut value = self.parse_power()?;
        loop {
            self.skip_whitespace();
            match self.peek() {
                Some('*') | Some('/') | Some('%') | Some('×') | Some('÷') => {
                    let op = self.bump().unwrap();
                    let rhs = self.parse_power()?;
                    value = match op {
                        '*' | '×' => value * rhs,
                        '%' => value % rhs,
                        _ => value / rhs,
                    };
                }
                _ => break,
            }
        }
        Ok(value)
    }

    fn parse_power(&mut self) -> Result<f64, String> {
        let mut value = self.parse_unary()?;
        loop {
            self.skip_whitespace();
            if self.peek() == Some('^') {
                self.bump();
                let rhs = self.parse_unary()?;
                value = value.powf(rhs);
            } else {
                break;
            }
        }
        Ok(value)
    }

    fn parse_unary(&mut self) -> Result<f64, String> {
        self.skip_whitespace();
        match self.peek() {
            Some('-') | Some('−') => {
                self.bump();
                Ok(-self.parse_unary()?)
            }
            Some('+') => {
                self.bump();
                self.parse_unary()
            }
            _ => self.parse_postfix(),
        }
    }

    fn parse_postfix(&mut self) -> Result<f64, String> {
        let mut value = self.parse_primary()?;
        loop {
            self.skip_whitespace();
            if self.peek() == Some('!') {
                self.bump();
                let n = value.round() as i64;
                if n < 0 {
                    return Err("factorial of a negative number".to_string());
                }
                value = factorial(n) as f64;
            } else {
                break;
            }
        }
        Ok(value)
    }

    fn parse_primary(&mut self) -> Result<f64, String> {
        self.skip_whitespace();
        let c = match self.peek() {
            Some(c) => c,
            None => return Err("unexpected end of expression".to_string()),
        };

        match c {
            '(' => {
                self.bump();
                let value = self.parse_expression()?;
                self.skip_whitespace();
                if self.peek() == Some(')') {
                    self.bump();
                    Ok(value)
                } else {
                    Err("missing ')'".to_string())
                }
            }
            '√' => {
                self.bump();
                Ok(self.parse_unary()?.sqrt())
            }
            'π' => {
                self.bump();
                Ok(std::f64::consts::PI)
            }
            c if c.is_ascii_digit() || c == '.' => self.parse_number(),
            c if c.is_alphabetic() => self.parse_name_or_function(),
            _ => Err(format!("unexpected character: {c}")),
        }
    }

    fn parse_number(&mut self) -> Result<f64, String> {
        let start = self.pos;
        while let Some(c) = self.peek() {
            if c.is_ascii_digit() || c == '.' {
                self.pos += 1;
            } else {
                break;
            }
        }
        let text: String = self.chars[start..self.pos].iter().collect();
        text.parse::<f64>()
            .map_err(|_| format!("invalid number: {text}"))
    }

    fn parse_name_or_function(&mut self) -> Result<f64, String> {
        let start = self.pos;
        while let Some(c) = self.peek() {
            if c.is_alphabetic() {
                self.pos += 1;
            } else {
                break;
            }
        }
        let name: String = self.chars[start..self.pos].iter().collect();
        self.skip_whitespace();
        if self.peek() == Some('(') {
            self.bump();
            let arg = self.parse_expression()?;
            self.skip_whitespace();
            if self.peek() == Some(')') {
                self.bump();
            } else {
                return Err("missing ')'".to_string());
            }
            apply_function(&name, arg)
        } else {
            constant(&name).ok_or_else(|| format!("unknown identifier: {name}"))
        }
    }
}

fn factorial(n: i64) -> i64 {
    (1..=n.max(1)).product()
}

fn constant(name: &str) -> Option<f64> {
    match name.to_lowercase().as_str() {
        "pi" | "π" => Some(std::f64::consts::PI),
        "e" => Some(std::f64::consts::E),
        _ => None,
    }
}

fn apply_function(name: &str, arg: f64) -> Result<f64, String> {
    match name.to_lowercase().as_str() {
        "sin" => Ok(arg.to_radians().sin()),
        "cos" => Ok(arg.to_radians().cos()),
        "tan" => Ok(arg.to_radians().tan()),
        "asin" => Ok(arg.asin().to_degrees()),
        "acos" => Ok(arg.acos().to_degrees()),
        "atan" => Ok(arg.atan().to_degrees()),
        "log" => Ok(arg.log10()),
        "ln" => Ok(arg.ln()),
        "sqrt" => Ok(arg.sqrt()),
        "abs" => Ok(arg.abs()),
        _ => Err(format!("unknown function: {name}")),
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    fn close(a: f64, b: f64) -> bool {
        (a - b).abs() < 1e-9
    }

    #[test]
    fn basic_arithmetic() {
        assert!(close(evaluate("1+2").unwrap(), 3.0));
        assert!(close(evaluate("10/2").unwrap(), 5.0));
        assert!(close(evaluate("2*3").unwrap(), 6.0));
        assert!(close(evaluate("7-2").unwrap(), 5.0));
        assert!(close(evaluate("10%3").unwrap(), 1.0));
    }

    #[test]
    fn precedence() {
        assert!(close(evaluate("2+3*4").unwrap(), 14.0));
        assert!(close(evaluate("2^3^2").unwrap(), 512.0));
        assert!(close(evaluate("(2+3)*4").unwrap(), 20.0));
    }

    #[test]
    fn unary_and_factorial() {
        assert!(close(evaluate("-5+3").unwrap(), -2.0));
        assert!(close(evaluate("5!").unwrap(), 120.0));
        assert!(close(evaluate("3+2!").unwrap(), 5.0));
    }

    #[test]
    fn functions_and_constants() {
        assert!(close(evaluate("sin(90)").unwrap(), 1.0));
        assert!(close(evaluate("cos(0)").unwrap(), 1.0));
        assert!(close(evaluate("log(100)").unwrap(), 2.0));
        assert!(close(evaluate("ln(e)").unwrap(), 1.0));
        assert!(close(evaluate("sqrt(16)").unwrap(), 4.0));
        assert!(close(evaluate("abs(-3)").unwrap(), 3.0));
        assert!(close(evaluate("pi").unwrap(), std::f64::consts::PI));
    }

    #[test]
    fn unicode_and_sqrt() {
        assert!(close(evaluate("4×5").unwrap(), 20.0));
        assert!(close(evaluate("8÷2").unwrap(), 4.0));
        assert!(close(evaluate("√9").unwrap(), 3.0));
    }

    #[test]
    fn format_whole_numbers() {
        assert_eq!(format(3.0), "3");
        assert_eq!(format(2.5), "2.5");
        assert_eq!(format(f64::INFINITY), "∞");
    }
}
