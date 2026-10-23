Dim nbw As Action(Of String,String())=Sub(p,f)If Regex.IsMatch(Join(f,""),"[\x00\x1e\x1f]")Then Throw New Exception Else File.AppendAllText(p,Join(f,ChrW(31))&ChrW(30)&vbLf)
