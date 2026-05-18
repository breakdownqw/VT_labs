pstr question "What is your name?"
pstr greeting "Hello, "
pstr exclamation "!"

array name 32

var len
var index

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


: read-name
  0 len !

  begin
    read-char

    dup '\n' =
    if
      drop
      1
    else
      name len @ + !
      len @ 1 + len !
      0
    then
  until
;


: print-name
  0 index !

  begin
    index @ len @ <
  while
    name index @ + @ emit
    index @ 1 + index !
  repeat
;


: main
  question print-pstr
  '\n' emit

  read-name

  greeting print-pstr
  print-name
  exclamation print-pstr
  '\n' emit
;
