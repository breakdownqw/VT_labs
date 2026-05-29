\ Sort one-digit numbers.
\ Input format:
\ first char: count n
\ then n digit chars, separated by newlines or spaces

array nums 16

var n
var i
var j
var temp


: skip-separators
  begin
    read-char
    dup '\n' =
    over ' ' =
    +
  while
    drop
  repeat
;


: read-digit
  skip-separators
  '0' -
;


: read-input
  read-digit n !

  0 i !

  begin
    i @ n @ <
  while
    read-digit nums i @ cells + !
    i @ 1 + i !
  repeat
;


: sort-array
  0 i !

  begin
    i @ n @ <
  while

    0 j !

    begin
      j @ n @ 1 - i @ - <
    while

      nums j @ cells + @
      nums j @ cells + cell+ @
      >
      if
        nums j @ cells + @ temp !

        nums j @ cells + cell+ @
        nums j @ cells + !

        temp @
        nums j @ cells + cell+ !
      then

      j @ 1 + j !
    repeat

    i @ 1 + i !
  repeat
;


: print-array
  0 i !

  begin
    i @ n @ <
  while
    nums i @ cells + @ .

    i @ n @ 1 - <
    if
      ' ' emit
    then

    i @ 1 + i !
  repeat

  '\n' emit
;


: main
  read-input
  sort-array
  print-array
;
