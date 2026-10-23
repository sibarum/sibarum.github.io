sub nbr{local$/;open F,'<:raw',$_[0];($h,@r)=<F>=~/\r?\n?(.*?)\36/gs;$h=~/^nanobuf\c_1\c_\Q$_[1]\E\c_$_[2](\.|\c_|$)/?map[split/\37/],@r:die}
