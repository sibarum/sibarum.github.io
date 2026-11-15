# sibarum.github.io

sibarum's portfolio and the VexelRay pages, served by GitHub Pages at <https://sibarum.github.io/>. The VexelRay install lines:

```powershell
irm https://sibarum.github.io/install.ps1 | iex
```

```sh
curl -fsSL https://sibarum.github.io/install.sh | sh
```

Nothing outside this repo writes to it: edit the pages here. (`nanobuf-golf.html` is the one generated file, written by a script in this repo; see below.)

| File | What |
| --- | --- |
| `index.html` | the home page: the portfolio of Pontif, COTT, VexelRay, SupirVast and Atchung! ElektroQ!, in that order. Its facts come from each repository's README; check them there when they change. A visit to one of the VexelRay page's old anchors on it (`/#apps` and the like) is sent on to `vexelray.html` |
| `vexelray.html` | the VexelRay landing page, which was `index.html` until the portfolio took the root. Every VexelRay page's header names it as home, beside an **All projects** link back to the portfolio |
| `install.ps1` | the suite's installer for Windows, a copy of `vexelray-dist/install.ps1` that differs only in the URL its comment names |
| `install.sh` | the suite's installer for Linux, a copy of `vexelray-dist/install.sh` that differs only in the URL its comment names. Kept LF, like `install.ps1` |
| `versions.js` | fills each `data-version="windows"` or `"linux"` element with the version that platform's install line installs now, read from the published manifests, so no page has to be edited when a version ships. `index.html`, `technology.html` and the framework docs load it; give any page that names a version `<span data-version="windows">0.1.1</span>` (the text inside is what shows if the manifest can't be read) and `<script src="https://sibarum.github.io/versions.js" defer></script>` |
| `analytics.js` | counts page views and a few actions (command copies, the Windows/Linux switch, opened sections, links off the site, files opened) with [GoatCounter](https://www.goatcounter.com): no cookies, so no consent banner. Its comment lists the events. Every page loads it with `<script src="analytics.js" defer></script>` before `</body>`; give a new page the same line. A page on another host can load `https://sibarum.github.io/analytics.js` and is counted under its own host name. It reports to the GoatCounter site `sibarum` (dashboard: <https://sibarum.goatcounter.com>), set by `SITE` in it |
| `img/` | each application's icon and screenshot |
| `img/portfolio/` | the portfolio's marks and pictures. `pontif.svg`, `cott.svg`, `supirvast.svg`, `atchung-mark.svg` and `sibarum.svg` were drawn for this page; the PNGs and JPGs are copies from the projects' own repositories |
| `.nojekyll` | serve the files as they are |
| `technology.html` | the framework's feature page. Linked from the header of every page. Its figures come from each repository's own docs, so check them there when they change |
| `design.html` | the seven design principles every application is held to, each linked to the doc that states it. Linked from the header of every page |
| `release-notes.html` | what each release brought, newest first. Linked from the header of every page. Add a section when a version ships |
| `licenses.html` | VexelRay's licenses, the third-party software built into the applications, and trademark notices. Its third-party list follows the `THIRD-PARTY-NOTICES.txt` files of the 0.1.2 releases; update it when an application gains or drops a library, font or native component |
| `nanobuf-golf.html` | Nanobuf golfed: a one-line reader and writer per language, with a leaderboard. **Written by `nanobuf-golf/fill.pl`** from `nanobuf-golf/page.template.html` and the entries; edit the template, not the page, and rerun the script after an entry changes |
| `nanobuf-golf/` | The golf entries, a driver per language, and `Golf.java`, the harness that holds every entry to the same cases. See its README |

Pages setting: **Settings > Pages > Deploy from a branch**, `main`, `/ (root)`.
