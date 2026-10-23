int nbw(char*p,int c,char**f){int i=0;for(;i<c;i++)if(strpbrk(f[i],"\36\37"))return 1;FILE*F=fopen(p,"ab");if(!F)return 1;for(i=0;i<c;i++)fprintf(F,"%s%s",f[i],i<c-1?"\37":"\36\n");return fclose(F);}
