#!/usr/bin/perl
# Writes ../nanobuf-golf.html from page.template.html, with each entry's code and byte count read from the files the
# harness tests, and the shell lines' counts from intrabuf, checked out beside this repository. Run it after an entry
# changes and the harness passes: perl nanobuf-golf/fill.pl
use strict;
use warnings;
use File::Basename qw(dirname);

my $here = dirname(__FILE__);
my ($template, $golf, $shells, $out) = (
    "$here/page.template.html",
    $here,
    $ENV{NANOBUF_LINES} // "$here/../../intrabuf/intrabuf-nanobuf/src/main/resources/sibarum/intrabuf/nanobuf",
    "$here/../nanobuf-golf.html",
);

sub slurp { my ($p) = @_; open my $f, '<:raw', $p or die "$p: $!"; local $/; my $t = <$f>; $t =~ s/[\r\n]+\z//; die "$p is not one line" if $t =~ /[\r\n]/; $t }
sub esc { my ($t) = @_; $t =~ s/&/&amp;/g; $t =~ s/</&lt;/g; $t =~ s/>/&gt;/g; $t =~ s/"/&quot;/g; $t }

# Golf entries: [name, id, dir, ext, interface, needs, tested with, how]
my @golf = (
    ['Perl', 'perl', 'perl', 'pl',
     '<code>nbr(PATH, SCHEMA, MAJOR)</code> returns the rows as array references; <code>nbw(PATH, FIELD, ...)</code> appends one. Both <code>die</code> to refuse.',
     'Nothing.', 'Perl 5.42 (Git for Windows)',
     'One regex with <code>/g</code> picks out every record that ends in <code>0x1E</code> and eats the line break in front of it, so a truncated tail never matches. The header is checked as a regex on the raw record, where <code>\c_</code> is <code>0x1F</code>. The writer checks all the fields at once by testing <code>"@_"</code>, the fields joined with spaces.'],
    ['JScript .NET', 'jscript', 'jscript', 'js',
     '<code>nbr(path, schema, major)</code> returns an array of rows; <code>nbw(path, fields)</code> takes the fields as an array. Both <code>throw</code> to refuse.',
     'Nothing. The lines name <code>System.IO.File</code> in full.', 'jsc 14.0 (.NET Framework 4.8)',
     'ECMAScript 3 with the .NET library behind it: a regex <code>exec</code> loop does what Perl\'s <code>/g</code> does, and <code>File.AppendAllText</code> already writes UTF-8 without a byte order mark.'],
    ['Tcl', 'tcl', 'tcl', 'tcl',
     '<code>nbr path schema major</code> returns a list of rows; <code>nbw path field ...</code> appends one. Both raise an error to refuse.',
     'Nothing.', 'Tcl 8.6.18',
     'Opens the file in binary, so fields come back as the file\'s bytes. <code>regexp -all -inline</code> finds the records, <code>lassign</code> splits off the header, and <code>string match $m.*</code> on the version with a <code>.</code> appended accepts <code>1</code> and <code>1.2</code> but not <code>11</code>.'],
    ['C#', 'csharp', 'csharp', 'cs',
     '<code>static List&lt;string[]&gt; nbr(path, schema, major)</code>; <code>static void nbw(path, params string[] fields)</code>. Both throw to refuse.',
     '<code>using System.Collections.Generic; using System.IO; using System.Linq; using System.Text.RegularExpressions;</code>', 'C# 5 (csc 4.8, .NET Framework)',
     '<code>GetRange(1, Count - 2)</code> drops the header and whatever follows the last <code>0x1E</code> in one call, and throws on a file with no <code>0x1E</code> at all. <code>throw null</code> is the shortest throw C# has.'],
    ['VB.NET', 'vb', 'vb', 'vb',
     '<code>nbr(path, schema, major)</code> returns a <code>List(Of String())</code>, or <code>Nothing</code> to refuse; <code>nbw(path, fields)</code> takes an array and throws to refuse.',
     '<code>Imports System.Collections.Generic, System.IO, System.Linq, System.Text.RegularExpressions</code>', 'Visual Basic 2012 (vbc 14.8, .NET Framework)',
     'VB won\'t put a method\'s body on the same line as its declaration, so both lines are delegates holding a one-expression lambda. The reader binds its variables with a LINQ query\'s <code>Let</code> over a one-element array.'],
    ['Java', 'java', 'java', 'java',
     '<code>List&lt;String[]&gt; nbr(String path, String schema, String major)</code>; <code>void nbw(String path, String... fields)</code>. Both throw to refuse.',
     'Nothing: they are top-level methods in a compact source file, which imports all of <code>java.base</code> by itself.', 'Java 25 (GraalVM 25.0.3)',
     'A compact source file needs no class and no imports. <code>Matcher.results()</code> streams the records, the header is matched as one regex with the schema quoted, and <code>throw null</code> refuses.'],
    ['Rust', 'rust', 'rust', 'rs',
     '<code>fn nbr(&amp;str, &amp;str, &amp;str) -&gt; Option&lt;Vec&lt;Vec&lt;String&gt;&gt;&gt;</code>; <code>fn nbw(&amp;str, &amp;[&amp;str]) -&gt; Option&lt;()&gt;</code>. <code>None</code> refuses.',
     'Nothing.', 'Rust 1.98.1',
     '<code>rsplit_once</code> drops what follows the last <code>0x1E</code>, and its <code>?</code> refuses a file with none. Slices of <code>String</code> compare directly with an array of <code>&amp;str</code>.'],
    ['C', 'c', 'c', 'c',
     '<code>int nbr(path, schema, major, void (*row)(int n, char **fields))</code> calls <code>row</code> for each row, like the shell lines; <code>int nbw(path, int n, char **fields)</code>. Both return non-zero to refuse.',
     '<code>#include &lt;stdio.h&gt;</code>, <code>&lt;stdlib.h&gt;</code> and <code>&lt;string.h&gt;</code>', 'MSVC 19.51 (Visual Studio 18)',
     'Reads the whole file, then splits it in place in one pass, writing a NUL over each separator. The fields are pointers into that one buffer, freed when the reading is done.'],
);

# The shell lines in intrabuf-nanobuf: [name, read file, write file]
my @shell = (
    ['sh', 'read.sh', 'write.sh'],
    ['Bash', 'read.bash', 'write.bash'],
    ['PowerShell', 'read.ps1', 'write.ps1'],
);

my @rows;
for my $g (@golf) {
    my ($name, $id, $dir) = @$g;
    my ($r, $w) = map { slurp("$golf/$dir/$_.$g->[3]") } qw(nbr nbw);
    push @rows, { name => $name, id => $id, r => length $r, w => length $w, rc => $r, wc => $w, g => $g };
}
for my $s (@shell) {
    my ($r, $w) = map { slurp("$shells/$_") } @$s[1, 2];
    push @rows, { name => $s->[0], shell => 1, r => length $r, w => length $w };
}
@rows = sort { $a->{r} + $a->{w} <=> $b->{r} + $b->{w} } @rows;

my $board = join "\n", map {
    my $t = $_->{r} + $_->{w};
    my $th = $_->{shell}
        ? qq{<a href="https://github.com/sibarum/intrabuf/tree/main/intrabuf-nanobuf">$_->{name}</a><span class="tag">intrabuf</span>}
        : qq{<a href="#$_->{id}">$_->{name}</a>};
    qq{          <tr} . ($_->{shell} ? ' class="shell"' : '') . qq{><th scope="row">$th</th><td class="n">$_->{r}</td><td class="n">$_->{w}</td><td class="n"><b>$t</b></td>}
    . qq{<td class="bar"><div class="bars" style="--r:$_->{r};--w:$_->{w}" aria-hidden="true"><i class="r"></i><i class="w"></i></div></td></tr>}
} @rows;

my $entries = join "\n", map {
    my ($name, $id, $dir, $ext, $iface, $needs, $tested, $how) = @{$_->{g}};
    my $t = $_->{r} + $_->{w};
    my $line = sub {
        my ($label, $which, $code, $n) = @_;
        my $pid = "$id-$which";
        qq{        <div class="line"><div class="line-label"><span>$label &middot; <b>$n bytes</b></span><button class="copy" type="button" data-for="$pid">Copy</button></div><pre id="$pid">} . esc($code) . qq{</pre></div>\n};
    };
    qq{      <article class="entry" id="$id">\n}
    . qq{        <div class="entry-head"><h3>$name</h3><span class="total">$t bytes</span></div>\n}
    . qq{        <p>$how</p>\n}
    . $line->('Reader', 'r', $_->{rc}, $_->{r})
    . $line->('Writer', 'w', $_->{wc}, $_->{w})
    . qq{        <dl><dt>Use</dt><dd>$iface</dd><dt>Needs</dt><dd>$needs</dd><dt>Tested with</dt><dd>$tested</dd></dl>\n}
    . qq{      </article>\n}
} grep { !$_->{shell} } @rows;

open my $in, '<:raw', $template or die;
my $html = do { local $/; <$in> };
$html =~ s/\{\{board\}\}/$board/ or die 'no board';
$html =~ s/\{\{entries\}\}/$entries/ or die 'no entries';
open my $o, '>:raw', $out or die;
print $o $html;
print "wrote $out\n";
printf "%-12s %4d %4d %4d\n", $_->{name}, $_->{r}, $_->{w}, $_->{r} + $_->{w} for @rows;
