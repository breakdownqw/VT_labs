\ Double precision demo.
\ 64-bit addition using two 32-bit words:
\ 0x00000001_FFFFFFFF + 0x00000000_00000001 = 0x00000002_00000000

var a_hi
var a_lo
var b_hi
var b_lo
var r_hi
var r_lo
var carry

pstr hi_label "hi="
pstr lo_label " lo="


: add64
  \ low part
  a_lo @ b_lo @ + r_lo !

  \ carry = 1 if r_lo < a_lo as unsigned 32-bit values, else 0
  r_lo @ a_lo @ u< carry !

  \ high part
  a_hi @ b_hi @ + carry @ + r_hi !
;


: main
  1 a_hi !
  -1 a_lo !

  0 b_hi !
  1 b_lo !

  add64

  hi_label type-pstr
  r_hi @ .
  lo_label type-pstr
  r_lo @ .
  '\n' emit
;
