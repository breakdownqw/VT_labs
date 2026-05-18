\ Vector operation demo.
\ Each line prints the result of one vector operation:
\ add, sub, mul, div, eq.

array a 4
array b 4
array result 4


: init
  8 a 0 + !
  6 a 1 + !
  4 a 2 + !
  2 a 3 + !

  2 b 0 + !
  2 b 1 + !
  4 b 2 + !
  1 b 3 + !
;


: print-result
  result 0 + @ .
  ' ' emit
  result 1 + @ .
  ' ' emit
  result 2 + @ .
  ' ' emit
  result 3 + @ .
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
