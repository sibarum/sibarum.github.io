# sibarum.github.io

The VexelRay landing page, served by GitHub Pages at <https://sibarum.github.io/>:

```powershell
irm https://sibarum.github.io/install.ps1 | iex
```

Nothing outside this repo writes to it: edit the pages here. (`nanobuf-golf.html` is the one generated file, written by a script in this repo; see below.)

| File | What |
| --- | --- |
| `index.html` | the page |
| `install.ps1` | the suite's installer, a copy of `vexelray-dist/install.ps1` that differs only in the URL its comment names |
| `versions.js` | fills each `data-version="windows"` or `"linux"` element with the version that platform's install line installs now, read from the published manifests, so no page has to be edited when a version ships. `index.html`, `technology.html` and the framework docs load it; give any page that names a version `<span data-version="windows">0.1.1</span>` (the text inside is what shows if the manifest can't be read) and `<script src="https://sibarum.github.io/versions.js" defer></script>` |
| `img/` | each application's icon and screenshot |
| `.nojekyll` | serve the files as they are |
| `technology.html` | the framework's feature page. Linked from the header of every page. Its figures come from each repository's own docs, so check them there when they change |
| `licenses.html` | VexelRay's licenses, the third-party software built into the applications, and trademark notices. Its third-party list was taken from what the 0.1.0 executables contain; update it when an application gains or drops a library, font or native component |
| `nanobuf-golf.html` | Nanobuf golfed: a one-line reader and writer per language, with a leaderboard. **Written by `nanobuf-golf/fill.pl`** from `nanobuf-golf/page.template.html` and the entries; edit the template, not the page, and rerun the script after an entry changes |
| `nanobuf-golf/` | The golf entries, a driver per language, and `Golf.java`, the harness that holds every entry to the same cases. See its README |

Pages setting: **Settings > Pages > Deploy from a branch**, `main`, `/ (root)`.
