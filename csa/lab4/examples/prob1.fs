\ Euler problem 4:
\ largest palindrome made from product of two 3-digit numbers.

var best
var i
var j
var step
var product

var n
var original
var reversed
var digit


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


: main
  0 best !

  999 i !

  begin
    i @ 100 >=
  while

    i @ 11 mod 0 =
    if
      999 j !
      1 step !
    else
      990 j !
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