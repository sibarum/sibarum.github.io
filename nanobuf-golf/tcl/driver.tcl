NANOBUF

lassign $argv mode a b c d
if {$mode eq "r"} {
    if {[catch {nbr $a $b $c} rows]} {exit 1}
    set o [open $d wb]
    foreach row $rows {
        foreach f $row {puts -nonewline $o "\[$f\]"}
        puts -nonewline $o \n
    }
    close $o
} else {
    set s [open $a rb]
    foreach line [split [string trimright [read $s] \n] \n] {
        if {[catch {nbw $b {*}[lmap h [split $line ,] {binary format H* $h}]}]} {exit 1}
    }
}
