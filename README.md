# sibarum.github.io

The VexelRay landing page, served by GitHub Pages at <https://sibarum.github.io/>:

```powershell
irm https://sibarum.github.io/install.ps1 | iex
```

Everything here except `technology.html` is generated; don't edit it by hand. `vexelray-dist/deploy.ps1` writes it from
`vexelray-dist/installer-suite.json` (its `site` section, and each member's `installer.json` and `image`), using
vexelray-installer's `--site` mode. [vexelray-dist/DEPLOYING.md](https://github.com/sibarum/vexelray-dist/blob/main/DEPLOYING.md)
says when to commit and push it.

| File | What |
| --- | --- |
| `index.html` | the page |
| `install.ps1` | the suite's installer, the same script as `vexelray-dist/install.ps1` |
| `img/` | each application's icon and screenshot |
| `.nojekyll` | serve the files as they are |
| `technology.html` | the framework's feature page. **Written by hand**: the generator never touches it, and the landing page links to it through a `links` entry in `installer-suite.json`. Its figures come from each repository's own docs, so check them there when they change |

Pages setting: **Settings > Pages > Deploy from a branch**, `main`, `/ (root)`.
