import System;
import System.IO;
import System.Text;

NANOBUF

function hex(h) {
    var b : byte[] = new byte[h.length / 2];
    for (var i = 0; i < b.length; i++) b[i] = byte(parseInt(h.substr(2 * i, 2), 16));
    return Encoding.UTF8.GetString(b);
}

var a = Environment.GetCommandLineArgs();  // a[0] is the program
if (a[1] == "r") {
    var rows;
    try {
        rows = nbr(a[2], a[3], a[4]);
    } catch (e) {
        Environment.Exit(1);
    }
    var o = "";
    for (var i = 0; i < rows.length; i++) {
        for (var j = 0; j < rows[i].length; j++) o += "[" + rows[i][j] + "]";
        o += "\n";
    }
    File.WriteAllText(a[5], o);
} else {
    var lines = File.ReadAllText(a[2]).split("\n");
    for (var i = 0; i < lines.length; i++) {
        if (lines[i] == "") continue;
        var hs = lines[i].split(","), f = [];
        for (var j = 0; j < hs.length; j++) f.push(hex(hs[j]));
        try {
            nbw(a[3], f);
        } catch (e) {
            Environment.Exit(1);
        }
    }
}
