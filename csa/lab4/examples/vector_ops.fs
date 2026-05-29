\ Vector operation demo.
\ Each line prints the result of one vector operation:
\ add, sub, mul, div, eq.

array a 4
array b 4
array result 4


: init
  8 a !
  6 a cell+ !
  4 a 2 cells + !
  2 a 3 cells + !

  2 b !
  2 b cell+ !
  4 b 2 cells + !
  1 b 3 cells + !
;


: print-result
  result @ .
  ' ' emit
  result cell+ @ .
  ' ' emit
  result 2 cells + @ .
  ' ' emit
  result 3 cells + @ .
  '\n' emit
;


: main
  init

  vload v0 a
  vload v1 b

  vadd v2 v0 v1
  vstore v2 result
  print-result

  vsub v2 v0 v1
  vstore v2 result
  print-result

  vmul v2 v0 v1
  vstore v2 result
  print-result

  vdiv v2 v0 v1
  vstore v2 result
  print-result

  veq v2 v0 v1
  vstore v2 result
  print-result
;
