\ Vector array addition for comparison with vector_scalar_add.fs.

array a 4
array b 4
array c 4


: init
  65 a 0 + !
  66 a 1 + !
  67 a 2 + !
  68 a 3 + !

  1 b 0 + !
  1 b 1 + !
  1 b 2 + !
  1 b 3 + !
;


: print-c
  c 0 + @ emit
  c 1 + @ emit
  c 2 + @ emit
  c 3 + @ emit
;


: main
  init

  vload v0 a
  vload v1 b
  vadd v2 v0 v1
  vstore v2 c

  print-c
;
