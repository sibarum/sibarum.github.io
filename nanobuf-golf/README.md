# Nanobuf golf

The entries behind [nanobuf-golf.html](https://sibarum.github.io/nanobuf-golf.html): a [nanobuf](https://github.com/sibarum/intrabuf/blob/main/docs/nanobuf.md)
reader and writer per language, one line each, as short as they'll go while still passing every case.

| file | what |
| --- | --- |
| `<language>/nbr.*`, `nbw.*` | the entry: the reader and the writer, one line each. These are what's counted |
| `<language>/driver.*` | the harness's side, not counted: its `NANOBUF` line is replaced with the entry's two lines |
| `Golf.java` | the harness. It builds each driver, runs the cases against it and prints the byte counts |
| `fill.pl`, `page.template.html` | write `../nanobuf-golf.html` from the entries and intrabuf's shell lines |

## Running it

With Java 25 or later, from this folder:

```
java Golf.java            # every language
java Golf.java perl tcl   # just these, by folder name
```

It runs on Windows: it calls Visual Studio's `VsDevCmd.bat` for C (point it elsewhere with
`-Dgolf.vsdevcmd=...`), and the C#, JScript and VB compilers that come with .NET Framework 4.8. Perl, Tcl, Java and
Rust are found on the PATH. Git for Windows ships `perl` (in `usr\bin`) and `tclsh` (in `mingw64\bin`); Git Bash puts
both on the PATH, PowerShell doesn't.

## Changing an entry

1. Edit its `nbr.*` or `nbw.*`. It has to stay one line.
2. Run `java Golf.java <language>` until every case passes.
3. Run `perl fill.pl` to rewrite the page, which shows each line and its count.

A new language needs a folder with the three files and a `Lang` entry in `Golf.java`, plus an entry in `fill.pl`'s
`@golf` list, which also holds what the page says about each one.

The cases are intrabuf-nanobuf's `NanobufLineTest` ones, plus one the spec asks for and that suite doesn't check: a
truncated final record is ignored.
