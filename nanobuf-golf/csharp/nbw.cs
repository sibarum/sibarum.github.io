static void nbw(string p,params string[]f){if(Regex.IsMatch(string.Concat(f),"[\0\x1e\x1f]"))throw null;File.AppendAllText(p,string.Join("\x1f",f)+"\x1e\n");}
