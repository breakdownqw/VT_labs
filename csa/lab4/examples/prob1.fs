\ Euler problem 4:
\ largest palindrome made from product of two 3-digit numbers.

var best
var i
var j
var step
var product
var max_factor

var n
var original
var reversed
var digit
var ch


: palindrome?
  n !
  n @ original !
  0 reversed !

  begin
    n @ 0 >
  while
    n @ 10 mod digit !
    reversed @ 10 * digit @ + reversed !
    n @ 10 / n !
  repeat

  original @ reversed @ =
;


: read-max-factor
  0 max_factor !

  begin
    read-char ch !

    ch @ '\n' =
    if
      1
    else
      max_factor @ 10 * ch @ '0' - + max_factor !
      0
    then
  until
;


: main
  read-max-factor

  0 best !

  max_factor @ i !

  begin
    i @ 100 >=
  while

    i @ 11 mod 0 =
    if
      max_factor @ j !
      1 step !
    else
      max_factor @ 11 / 11 * j !
      11 step !
    then

    begin
      j @ 100 >=
    while

      i @ j @ * product !

      product @ best @ >
      if
        product @ palindrome?
        if
          product @ best !
        then

        j @ step @ - j !
      else
        99 j !
      then

    repeat

    i @ 1 - i !
  repeat

  best @ .
  '\n' emit
;
