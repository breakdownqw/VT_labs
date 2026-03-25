#include <iostream>
#include <string>
#include <vector>
using namespace std;

int main() {
  string s;
  cin >> s;
  int total = s.size();
  int n = total / 2;

  vector<int> order(total);
  int ac = 0, tc = 0;
  for (int i = 0; i < total; i++) {
    order[i] = islower(s[i]) ? ++ac : ++tc;
  }

  vector<int> pos;
  vector<int> result(n + 1, 0);

  for (int i = 0; i < total; i++) {
    if (!pos.empty() && tolower(s[i]) == tolower(s[pos.back()]) &&
        islower(s[i]) != islower(s[pos.back()])) {
      int top = pos.back();
      pos.pop_back();
      int trapPos = isupper(s[i]) ? i : top;
      int animalPos = islower(s[i]) ? i : top;
      result[order[trapPos]] = order[animalPos];
    } else {
      pos.push_back(i);
    }
  }

  if (!pos.empty()) {
    cout << "Impossible\n";
    return 0;
  }

  cout << "Possible\n";
  for (int i = 1; i <= n; i++) {
    cout << result[i] << ' ';
  }
  return 0;
}