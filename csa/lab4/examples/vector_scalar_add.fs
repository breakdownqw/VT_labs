\ Scalar array addition for comparison with vector_vector_add.fs.

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

  a @ b @ + c !
  a cell+ @ b cell+ @ + c cell+ !
  a 2 cells + @ b 2 cells + @ + c 2 cells + !
  a 3 cells + @ b 3 cells + @ + c 3 cells + !

  print-c
;
