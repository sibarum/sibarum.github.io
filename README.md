# sibarum.github.io

The VexelRay landing page, served by GitHub Pages at <https://sibarum.github.io/>:

```powershell
irm https://sibarum.github.io/install.ps1 | iex
```

Everything here is generated; don't edit it by hand. `vexelray-dist/deploy.ps1` writes it from
`vexelray-dist/installer-suite.json` (its `site` section, and each member's `installer.json` and `image`), using
vexelray-installer's `--site` mode. [vexelray-dist/DEPLOYING.md](https://github.com/sibarum/vexelray-dist/blob/main/DEPLOYING.md)
says when to commit and push it.

| File | What |
| --- | --- |
| `index.html` | the page |
| `install.ps1` | the suite's installer, the same script as `vexelray-dist/install.ps1` |
| `img/` | each application's icon and screenshot |
| `.nojekyll` | serve the files as they are |

Pages setting: **Settings > Pages > Deploy from a branch**, `main`, `/ (root)`.
