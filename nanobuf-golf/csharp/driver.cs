using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;
using System.Text.RegularExpressions;

static class Driver {
NANOBUF

    static string Hex(string h) {
        return Encoding.UTF8.GetString(Enumerable.Range(0, h.Length / 2).Select(i => Convert.ToByte(h.Substring(2 * i, 2), 16)).ToArray());
    }

    static int Main(string[] a) {
        if (a[0] == "r") {
            List<string[]> rows;
            try {
                rows = nbr(a[1], a[2], a[3]);
            } catch (Exception) {
                return 1;
            }
            File.WriteAllText(a[4], string.Concat(rows.Select(r => string.Concat(r.Select(f => "[" + f + "]")) + "\n")));
            return 0;
        }
        foreach (var line in File.ReadAllText(a[1]).Split('\n').Where(l => l != "")) {
            try {
                nbw(a[2], line.Split(',').Select(Hex).ToArray());
            } catch (Exception) {
                return 1;
            }
        }
        return 0;
    }
}
