void nbw(String p,String...f)throws Exception{if(String.join("",f).matches("(?s).*[\0\36\37].*"))throw null;try(var w=new FileWriter(p,true)){w.write(String.join("\37",f)+"\36\n");}}
