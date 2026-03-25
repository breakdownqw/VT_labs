#include <iostream>
#include <map>
#include <string>
#include <vector>
using namespace std;

int main() {
  map<string, vector<pair<int, long long>>> vars;
  vector<vector<string>> changes(1);
  int level = 0;

  string line;
  while (getline(cin, line)) {
    if (line == "{") {
      level++;
      changes.push_back({});
      continue;
    }
    if (line == "}") {
      for (int i = 0; i < (int)changes[level].size(); i++) {
        vars[changes[level][i]].pop_back();
      }
      changes.pop_back();
      level--;
      continue;
    }

    int eq = line.find('=');
    string left = line.substr(0, eq);
    string right = line.substr(eq + 1);

    long long val = 0;
    if (right[0] == '-' || isdigit(right[0])) {
      val = stoll(right);
    } else {
      if (!vars[right].empty())
        val = vars[right].back().second;
      cout << val << '\n';
    }

    if (!vars[left].empty() && vars[left].back().first == level) {
      vars[left].back().second = val;
    } else {
      vars[left].push_back({level, val});
      changes[level].push_back(left);
    }
  }

  return 0;
}