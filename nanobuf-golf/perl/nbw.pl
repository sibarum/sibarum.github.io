sub nbw{"@_"=~/[\0\36\37]/&&die;open my$f,'>>:raw',shift;print$f join("\37",@_),"\36\n"}
