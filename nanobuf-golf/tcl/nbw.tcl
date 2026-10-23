proc nbw {p args} {if {[regexp {[\0\x1e\x1f]} [join $args]]} {error n};set f [open $p ab];puts $f [join $args \x1f]\x1e;close $f}
