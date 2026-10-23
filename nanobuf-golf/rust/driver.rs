NANOBUF

fn main() {
    let a: Vec<String> = std::env::args().collect();
    if a[1] == "r" {
        let Some(rows) = nbr(&a[2], &a[3], &a[4]) else { std::process::exit(1) };
        let mut o = String::new();
        for r in rows {
            for f in r {
                o += &format!("[{f}]");
            }
            o.push('\n');
        }
        std::fs::write(&a[5], o).unwrap();
    } else {
        for line in std::fs::read_to_string(&a[2]).unwrap().lines() {
            let f: Vec<String> = line
                .split(',')
                .map(|h| String::from_utf8((0..h.len()).step_by(2).map(|i| u8::from_str_radix(&h[i..i + 2], 16).unwrap()).collect()).unwrap())
                .collect();
            let f: Vec<&str> = f.iter().map(|s| s.as_str()).collect();
            if nbw(&a[3], &f).is_none() {
                std::process::exit(1)
            }
        }
    }
}
