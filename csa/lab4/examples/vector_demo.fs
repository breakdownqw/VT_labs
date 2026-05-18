array a 4
array b 4
array c 4

: main
  65 a 0 + !
  66 a 1 + !
  67 a 2 + !
  68 a 3 + !

  1 b 0 + !
  1 b 1 + !
  1 b 2 + !
  1 b 3 + !

  vload v0 a
  vload v1 b
  vadd v2 v0 v1
  vstore v2 c

  c 0 + @ emit
  c 1 + @ emit
  c 2 + @ emit
  c 3 + @ emit
;