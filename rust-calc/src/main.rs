use rust_calc::{evaluate, format};
use std::io::{self, Write};

fn main() {
    let args: Vec<String> = std::env::args().collect();

    if args.len() > 1 {
        // Treat everything after the program name as one expression.
        let expr = args[1..].join(" ");
        run(&expr);
    } else {
        // Interactive REPL.
        println!("Advanced calculator (Rust) — type an expression, or Ctrl-D to exit.");
        loop {
            print!("> ");
            io::stdout().flush().unwrap();
            let mut line = String::new();
            match io::stdin().read_line(&mut line) {
                Ok(0) => break,
                Ok(_) => run(line.trim()),
                Err(e) => {
                    eprintln!("input error: {e}");
                    break;
                }
            }
        }
    }
}

fn run(expr: &str) {
    if expr.is_empty() {
        return;
    }
    match evaluate(expr) {
        Ok(v) => println!("{}", format(v)),
        Err(e) => eprintln!("error: {e}"),
    }
}
