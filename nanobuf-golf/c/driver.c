#include <stdio.h>
#include <stdlib.h>
#include <string.h>

NANOBUF

static FILE *out;

static void dump(int c, char **v) {
    for (int i = 0; i < c; i++) fprintf(out, "[%s]", v[i]);
    fputc('\n', out);
}

int main(int argc, char **argv) {
    if (!strcmp(argv[1], "r")) {
        out = fopen(argv[5], "wb");
        int rc = nbr(argv[2], argv[3], argv[4], dump);
        fclose(out);
        return rc ? 1 : 0;
    }
    FILE *s = fopen(argv[2], "rb");
    static char line[65536];
    while (fgets(line, sizeof line, s)) {
        line[strcspn(line, "\n")] = 0;
        char *f[256], *w = line;  /* decodes the hex in place: w never passes h */
        int c = 0;
        f[c++] = w;
        for (char *h = line; *h; h++) {
            if (*h == ',') {
                *w++ = 0;
                f[c++] = w;
            } else {
                unsigned x;
                sscanf(h++, "%2x", &x);
                *w++ = (char) x;
            }
        }
        *w = 0;
        if (nbw(argv[3], c, f)) return 1;
    }
    return 0;
}
