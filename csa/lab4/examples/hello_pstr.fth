pstr hello "Hello, world!"

var pstr_addr
var pstr_len
var pstr_index


: print-pstr
  pstr_addr !
  pstr_addr @ @ pstr_len !
  0 pstr_index !

  begin
    pstr_index @ pstr_len @ <
  while
    pstr_addr @ 1 + pstr_index @ + @ emit
    pstr_index @ 1 + pstr_index !
  repeat
;


: main
  hello print-pstr
;
