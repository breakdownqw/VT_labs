\ Scalar array addition for comparison with vector_vector_add.fs.

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

  a 0 + @ b 0 + @ + c 0 + !
  a 1 + @ b 1 + @ + c 1 + !
  a 2 + @ b 2 + @ + c 2 + !
  a 3 + @ b 3 + @ + c 3 + !

  print-c
;
