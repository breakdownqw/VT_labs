#include <iostream>
#include <vector>

int main() {
  int n;
  std::cin >> n;

  std::vector<long long> a(n);
  for (int i = 0; i < n; ++i) {
    std::cin >> a[i];
  }

  int begin = 0;
  int bestL = 0, bestR = 0;
  int bestLen = 1;

  for (int end = 0; end < n; ++end) {
    if (end >= 2 && a[end] == a[end - 1] && a[end] == a[end - 2]) {
      begin = std::max(begin, end - 1);
    }

    int len = end - begin + 1;

    if (len > bestLen || (len == bestLen && begin < bestL)) {
      bestLen = len;
      bestL = begin;
      bestR = end;
    }
  }

  std::cout << bestL + 1 << " " << bestR + 1 << "\n";

  return 0;
}