package org.recsys.mapper;

import java.util.HashMap;
import java.util.Map;

public class IndexMapper {
    private Map<Long, Integer> externalToInternal = new HashMap<>();
    private Map<Integer, Long> internalToExternal = new HashMap<>();
    private int nextIndex = 0;

    public int getInternalIndex(Long dbId) {
        return externalToInternal.computeIfAbsent(dbId, id -> {
            int idx = nextIndex++;
            internalToExternal.put(idx, id);
            return idx;
        });
    }

    public int getSize() {
        return internalToExternal.size();
    }
}
