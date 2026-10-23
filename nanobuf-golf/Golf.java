import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Holds every golfed nanobuf reader and writer in this folder to the cases intrabuf-nanobuf holds its shell lines to,
 * plus one the spec asks for and those don't test: a truncated final record is ignored.
 *
 * <p>Each language's folder has {@code nbr.*} and {@code nbw.*}, one line each, and a driver whose {@code NANOBUF} line
 * is replaced with them. The driver is the harness's side and isn't counted. It speaks two commands:
 * <ul>
 * <li>{@code r IN SCHEMA MAJOR OUT}: read IN, write each row to OUT as {@code [field]...} and a line feed, exit 0;
 *     exit 1 if the reader refuses.</li>
 * <li>{@code w SPEC OUT}: SPEC holds one row per line, its fields in hex joined by commas. Write each to OUT with the
 *     writer; exit 1 at the first refusal.</li>
 * </ul>
 *
 * <p>Run from this folder: {@code java Golf.java [language...]}. Windows only for now: the compilers are found where a
 * Windows machine keeps them.
 */
public class Golf {

    static final String SCHEMA = "test.Schema";

    /** The rows NanobufLineTest uses: everything the spec says a reader must survive, and each shell's specials. */
    static final List<List<String>> ROWS = List.of(
            List.of("app", "calculator", "Calculator", "sibarum"),
            List.of("version", "1.4.2", "2026-10-01"),
            List.of("empty", "", "mid", "", ""),
            List.of("ws", "a\tb", "line1\nline2", "\nleading", " spaced ", "trailing\n", "crlf\r\ninside", "\r\nleading crlf", "cr\r"),
            List.of("unicode", "é", "中文", "🎉"),
            List.of("one"),
            List.of("specials", "a\\b", "$HOME", "`x`", "$(x)", "%PATH%", "'q'", "\"dq\"", "-n", "--", "&|;<>"),
            List.of("globs", "*", "?", "[a", "~"),
            List.of("wide", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12"));

    static final String VSDEVCMD = System.getProperty("golf.vsdevcmd",
            "C:\\Program Files\\Microsoft Visual Studio\\18\\Community\\Common7\\Tools\\VsDevCmd.bat");
    static final String NETFX = "C:\\Windows\\Microsoft.NET\\Framework64\\v4.0.30319\\";

    /**
     * One language. {@code build} is a cmd script run in the build folder, or null; in {@code run}, {@code @} is the
     * build folder. {@code nul} says whether the language's strings can hold NUL, so whether the writer can be asked
     * to refuse one.
     */
    record Lang(String name, String dir, String ext, String driver, String build, List<String> run, boolean nul) {
    }

    static final List<Lang> LANGS = List.of(
            new Lang("Perl", "perl", "pl", "driver.pl", null, List.of("perl", "@/driver.pl"), true),
            new Lang("Tcl", "tcl", "tcl", "driver.tcl", null, List.of("tclsh", "@/driver.tcl"), true),
            new Lang("Java", "java", "java", "driver.java", "javac -d . driver.java", List.of("java", "-cp", "@", "driver"), true),
            new Lang("Rust", "rust", "rs", "driver.rs", "rustc -O driver.rs", List.of("@/driver.exe"), true),
            new Lang("C", "c", "c", "driver.c",
                    "call \"" + VSDEVCMD + "\" -arch=x64 -no_logo >nul && cl /nologo /O2 /utf-8 driver.c >nul",
                    List.of("@/driver.exe"), false),
            new Lang("C#", "csharp", "cs", "driver.cs", "\"" + NETFX + "csc.exe\" /nologo driver.cs", List.of("@/driver.exe"), true),
            new Lang("JScript .NET", "jscript", "js", "driver.js", "\"" + NETFX + "jsc.exe\" /nologo driver.js",
                    List.of("@/driver.exe"), true),
            new Lang("VB.NET", "vb", "vb", "driver.vb", "\"" + NETFX + "vbc.exe\" /nologo driver.vb", List.of("@/driver.exe"), true));

    static final Path HERE = Path.of("").toAbsolutePath();

    /** Everything a run builds and writes, deleted when it ends. */
    static Path TMP;

    public static void main(String[] args) throws Exception {
        List<String> only = Arrays.asList(args);
        boolean allPassed = true;
        TMP = Files.createTempDirectory("nanobuf-golf");
        try {
            for (Lang lang : LANGS) {
                if (!only.isEmpty() && !only.contains(lang.dir)) {
                    continue;
                }
                allPassed &= run(lang);
            }
        } finally {
            try (var all = Files.walk(TMP)) {
                all.sorted(Comparator.reverseOrder()).forEach(p -> p.toFile().delete());
            }
        }
        System.exit(allPassed ? 0 : 1);
    }

    static boolean run(Lang lang) throws Exception {
        String nbr = line(lang, "nbr"), nbw = line(lang, "nbw");
        Path build = Files.createTempDirectory(TMP, lang.dir);
        String driver = Files.readString(HERE.resolve(lang.dir).resolve(lang.driver), StandardCharsets.UTF_8);
        if (!driver.contains("NANOBUF")) {
            throw new IllegalStateException(lang.driver + " has no NANOBUF line");
        }
        Files.writeString(build.resolve(lang.driver), driver.replace("NANOBUF", nbr + "\n" + nbw), StandardCharsets.UTF_8);
        if (lang.build != null) {
            Files.writeString(build.resolve("build.cmd"), "@echo off\r\n" + lang.build + "\r\n");
            // By full path: cmd won't run one from the current folder if NoDefaultCurrentDirectoryInExePath is set.
            Result r = exec(List.of("cmd", "/c", build.resolve("build.cmd").toString()), build);
            if (r.code != 0) {
                System.out.printf("%-13s does not build:%n%s%n", lang.name, r.log);
                return false;
            }
        }
        List<String> run = lang.run.stream().map(a -> a.replace("@", build.toString().replace('\\', '/'))).toList();
        List<String> failed = new ArrayList<>();
        for (Case c : cases(lang)) {
            String why = c.check(run);
            if (why != null) {
                failed.add(c.name() + ": " + why);
            }
        }
        System.out.printf("%-13s nbr %4d  nbw %4d  %s%n", lang.name, bytes(nbr), bytes(nbw),
                failed.isEmpty() ? "all " + cases(lang).size() + " cases pass" : failed.size() + " FAILED");
        failed.forEach(f -> System.out.println("    " + f));
        return failed.isEmpty();
    }

    /** The entry's one line; refuses one that is several. */
    static String line(Lang lang, String which) throws IOException {
        String code = Files.readString(HERE.resolve(lang.dir).resolve(which + "." + lang.ext), StandardCharsets.UTF_8)
                .replaceAll("\\R+$", "");
        if (code.contains("\n") || code.contains("\r")) {
            throw new IllegalStateException(lang.dir + "/" + which + " is not one line");
        }
        return code;
    }

    static int bytes(String code) {
        return code.getBytes(StandardCharsets.UTF_8).length;
    }

    interface Case {
        String name();

        /** Null if it passes, otherwise why not. */
        String check(List<String> run) throws Exception;
    }

    static List<Case> cases(Lang lang) {
        List<Case> cases = new ArrayList<>();
        String lf = new String(Reference.write(SCHEMA, 1, 2, ROWS), StandardCharsets.UTF_8);
        cases.add(read("reads what the reference writes", lf, SCHEMA, "1", ROWS));
        cases.add(read("reads a final record without its line feed",
                new String(Reference.write(SCHEMA, 1, 0, ROWS), StandardCharsets.UTF_8).replaceAll("\n$", ""), SCHEMA, "1", ROWS));
        cases.add(read("reads CR LF after every record", lf.replace("\u001e\n", "\u001e\r\n"), SCHEMA, "1", ROWS));
        cases.add(read("reads CR after every record", lf.replace("\u001e\n", "\u001e\r"), SCHEMA, "1", ROWS));
        cases.add(read("reads nothing after every record", lf.replace("\u001e\n", "\u001e"), SCHEMA, "1", ROWS));
        cases.add(read("survives Git's line-ending conversion", crlf(lf), SCHEMA, "1",
                ROWS.stream().map(r -> r.stream().map(Golf::crlf).toList()).toList()));
        cases.add(read("reads a header with no rows",
                new String(Reference.write(SCHEMA, 3, 7, List.of()), StandardCharsets.UTF_8), SCHEMA, "3", List.of()));
        cases.add(read("ignores a truncated final record", lf + "trunc\u001fated", SCHEMA, "1", ROWS));
        cases.add(read("refuses another schema", lf, "test.Other", "1", null));
        cases.add(read("refuses a schema that is a prefix", lf, "test", "1", null));
        cases.add(read("refuses another major", lf, SCHEMA, "2", null));
        cases.add(read("refuses major 11 as 1",
                new String(Reference.write(SCHEMA, 11, 0, ROWS), StandardCharsets.UTF_8), SCHEMA, "1", null));
        cases.add(read("refuses an empty file", "", SCHEMA, "1", null));
        cases.add(read("refuses nanobuf 2", lf.replaceFirst("nanobuf\u001f1", "nanobuf\u001f2"), SCHEMA, "1", null));
        List<List<String>> all = new ArrayList<>();
        all.add(List.of("nanobuf", "1", SCHEMA, "1.2"));
        all.addAll(ROWS);
        cases.add(write("writes what the reference writes", all, Reference.write(SCHEMA, 1, 2, ROWS)));
        for (char c : lang.nul ? new char[]{0x1E, 0x1F, 0} : new char[]{0x1E, 0x1F}) {
            cases.add(write(String.format("refuses 0x%02X and writes nothing", (int) c), List.of(List.of("k", "ok", "a" + c + "b")), null));
        }
        return cases;
    }

    /** Reads {@code file}; {@code expected} null means the reader must refuse it. */
    static Case read(String name, String file, String schema, String major, List<List<String>> expected) {
        return new Case() {
            public String name() {
                return name;
            }

            public String check(List<String> run) throws Exception {
                Path dir = Files.createTempDirectory(TMP, "case");
                Files.write(dir.resolve("in"), file.getBytes(StandardCharsets.UTF_8));
                Result r = exec(concat(run, "r", "in", schema, major, "out"), dir);
                if (expected == null) {
                    return r.code == 1 ? null : "should refuse (exit 1), exited " + r.code + r.tail();
                }
                if (r.code != 0) {
                    return "refused (exit " + r.code + ")" + r.tail();
                }
                String want = canonical(dump(expected));
                String got = canonical(Files.readString(dir.resolve("out"), StandardCharsets.UTF_8));
                return want.equals(got) ? null : "\n      want " + show(want) + "\n      got  " + show(got);
            }
        };
    }

    /** Writes {@code rows}; {@code expected} null means the writer must refuse and leave the file empty. */
    static Case write(String name, List<List<String>> rows, byte[] expected) {
        return new Case() {
            public String name() {
                return name;
            }

            public String check(List<String> run) throws Exception {
                Path dir = Files.createTempDirectory(TMP, "case");
                String spec = rows.stream().map(r -> r.stream()
                                .map(f -> HexFormat.of().formatHex(f.getBytes(StandardCharsets.UTF_8)))
                                .collect(Collectors.joining(",")) + "\n")
                        .collect(Collectors.joining());
                Files.writeString(dir.resolve("spec"), spec);
                Result r = exec(concat(run, "w", "spec", "out"), dir);
                Path out = dir.resolve("out");
                if (expected == null) {
                    if (r.code != 1) {
                        return "should refuse (exit 1), exited " + r.code + r.tail();
                    }
                    return Files.notExists(out) || Files.size(out) == 0 ? null : "refused but wrote " + Files.size(out) + " bytes";
                }
                if (r.code != 0) {
                    return "refused (exit " + r.code + ")" + r.tail();
                }
                byte[] got = Files.readAllBytes(out);
                return Arrays.equals(expected, got) ? null
                        : "\n      want " + show(new String(expected, StandardCharsets.UTF_8))
                        + "\n      got  " + show(new String(got, StandardCharsets.UTF_8));
            }
        };
    }

    record Result(int code, String log) {
        String tail() {
            String t = log.strip();
            return t.isEmpty() ? "" : ": " + (t.length() > 300 ? t.substring(0, 300) + "..." : t);
        }
    }

    static Result exec(List<String> cmd, Path dir) throws Exception {
        Path log = Files.createTempFile(TMP, "run", ".log");
        Process p = new ProcessBuilder(cmd).directory(dir.toFile()).redirectErrorStream(true)
                .redirectOutput(log.toFile()).start();
        if (!p.waitFor(120, TimeUnit.SECONDS)) {
            p.destroyForcibly();
            return new Result(-1, "timed out");
        }
        return new Result(p.exitValue(), Files.readString(log, StandardCharsets.ISO_8859_1));
    }

    static List<String> concat(List<String> a, String... b) {
        List<String> all = new ArrayList<>(a);
        all.addAll(List.of(b));
        return all;
    }

    /** How the drivers print rows. */
    static String dump(List<List<String>> rows) {
        return rows.stream().map(r -> r.stream().map(f -> "[" + f + "]").collect(Collectors.joining()) + "\n")
                .collect(Collectors.joining());
    }

    /** A missing trailing field reads as empty, so a reader may hand it over or not: drop them before comparing. */
    static String canonical(String dump) {
        return dump.replaceAll("(\\[\\])+\n", "\n");
    }

    static String crlf(String s) {
        return s.replaceAll("(?<!\r)\n", "\r\n");
    }

    static String show(String s) {
        return s.replace("\u001f", "␟").replace("\u001e", "␞").replace("\r", "\\r").replace("\n", "\\n");
    }

    /** Nanobuf as intrabuf-nanobuf's Reference.java specifies it. */
    static final class Reference {
        static byte[] write(String schema, int major, int minor, List<List<String>> rows) {
            StringBuilder sb = new StringBuilder();
            record(sb, List.of("nanobuf", "1", schema, major + "." + minor));
            rows.forEach(r -> record(sb, r));
            return sb.toString().getBytes(StandardCharsets.UTF_8);
        }

        private static void record(StringBuilder sb, List<String> fields) {
            sb.append(String.join("\u001f", fields)).append('\u001e').append('\n');
        }
    }
}
