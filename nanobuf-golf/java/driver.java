NANOBUF

void main(String[] a) throws Exception {
    if (a[0].equals("r")) {
        List<String[]> rows;
        try {
            rows = nbr(a[1], a[2], a[3]);
        } catch (Exception e) {
            System.exit(1);
            return;
        }
        var o = new StringBuilder();
        for (var r : rows) {
            for (var f : r) o.append('[').append(f).append(']');
            o.append('\n');
        }
        Files.writeString(Path.of(a[4]), o);
    } else {
        for (var line : Files.readAllLines(Path.of(a[1]))) {
            var f = Arrays.stream(line.split(",", -1))
                    .map(h -> new String(HexFormat.of().parseHex(h), StandardCharsets.UTF_8)).toArray(String[]::new);
            try {
                nbw(a[2], f);
            } catch (Exception e) {
                System.exit(1);
            }
        }
    }
}
