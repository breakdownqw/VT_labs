\ Vector array addition for comparison with vector_scalar_add.fs.

array a 4
array b 4
array c 4


: init
  65 a !
  66 a cell+ !
  67 a 2 cells + !
  68 a 3 cells + !

  1 b !
  1 b cell+ !
  1 b 2 cells + !
  1 b 3 cells + !
;


: print-c
  c @ emit
  c cell+ @ emit
  c 2 cells + @ emit
  c 3 cells + @ emit
;


: main
  init

  vload v0 a
  vload v1 b
  vadd v2 v0 v1
  vstore v2 c

  print-c
;
