package org.recsys.mapper;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class IndexMapper {
    private Map<Long, Integer> externalToInternal = new HashMap<>();
    private Map<Integer, Long> internalToExternal = new HashMap<>();
    private int nextIndex = 0;

    /**
     * Custom Constructor for Protobuf Rehydration
     */
    public IndexMapper(Map<Long, Integer> loadedMap) {
        this.externalToInternal = new HashMap<>(loadedMap);
        this.internalToExternal = new HashMap<>();
        // Reconstruct the reverse mapping
        loadedMap.forEach((dbId, idx) -> internalToExternal.put(idx, dbId));
        // Set nextIndex to the highest current index + 1
        int maxIdx = loadedMap.values().stream()
                .mapToInt(Integer::intValue)
                .max()
                .orElse(-1);
        this.nextIndex = maxIdx + 1;
    }

    public int getInternalIndex(Long dbId) {
        return externalToInternal.computeIfAbsent(dbId, id -> {
            int idx = nextIndex++;
            internalToExternal.put(idx, id);
            return idx;
        });
    }

    public Long getExternalId(Integer internalIdx) {
        return internalToExternal.get(internalIdx);
    }

    public Integer getExistingInternalIndex(Long externalIdx) {
        return externalToInternal.get(externalIdx);
    }

    public int getSize() {
        return externalToInternal.size();
    }

    public Map<Long, Integer> getInternalMap() {
        return Collections.unmodifiableMap(externalToInternal);
    }
}
