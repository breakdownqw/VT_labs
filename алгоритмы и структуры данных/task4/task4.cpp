#include <iostream>
using namespace std;

int next_state(int x, int b, int c, int d) {
  int afterIncubator = x * b;
  if (afterIncubator < c)
    return 0;
  int after = afterIncubator - c;
  if (after > d)
    return d;
  return after;
}

int main() {
  int a, b, c, d;
  long long k;
  cin >> a >> b >> c >> d >> k;

  int cur = a;

  for (long long day = 1; day <= k; day++) {
    int nxt = next_state(cur, b, c, d);

    if (nxt == 0) {
      cout << 0 << "\n";
      return 0;
    }

    if (nxt == cur) {
      cout << cur << "\n";
      return 0;
    }

    cur = nxt;
  }

  cout << cur << "\n";
  return 0;
}