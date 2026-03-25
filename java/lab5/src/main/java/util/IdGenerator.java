package util;

import java.util.*;

public final class IdGenerator {
  private static final Set<Integer> usedIds = new HashSet<>();
  private static final Queue<Integer> freeIds = new PriorityQueue<>();

  public static synchronized int getNextId() {
    if (!freeIds.isEmpty()) {
      return freeIds.poll();
    }

    // Если нет свободных - ищем минимальный незанятый
    int id = 1;
    while (usedIds.contains(id)) {
      id++;
    }
    usedIds.add(id);
    return id;
  }

  public static synchronized void releaseId(int id) {
    usedIds.remove(id);
    freeIds.add(id);
  }

  public static synchronized void init(Collection<Integer> existingIds) {
    usedIds.clear();
    freeIds.clear();
    usedIds.addAll(existingIds);
  }
}