function nbw(p,f){if(/[\0\x1e\x1f]/.test(f.join('')))throw 0;System.IO.File.AppendAllText(p,f.join('\x1f')+'\x1e\n')}
