NANOBUF

if ($ARGV[0] eq 'r') {
    my @rows = eval { nbr(@ARGV[1 .. 3]) };
    exit 1 if $@;
    open my $o, '>:raw', $ARGV[4] or die;
    print $o map({ "[$_]" } @$_), "\n" for @rows;
} else {
    open my $s, '<:raw', $ARGV[1] or die;
    for (<$s>) {
        chomp;
        eval { nbw($ARGV[2], map { pack 'H*', $_ } split /,/, $_, -1); 1 } or exit 1;
    }
}
