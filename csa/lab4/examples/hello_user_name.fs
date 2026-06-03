pstr question "What is your name?"
pstr greeting "Hello, "
pstr exclamation "!"

buffer name 32

var len
var index


: read-name
  0 len !

  begin
    read-char

    dup '\n' =
    if
      drop
      1
    else
      name len @ + c!
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
    name index @ + c@ emit
    index @ 1 + index !
  repeat
;


: main
  question type-pstr
  '\n' emit

  read-name

  greeting type-pstr
  print-name
  exclamation type-pstr
  '\n' emit
;
