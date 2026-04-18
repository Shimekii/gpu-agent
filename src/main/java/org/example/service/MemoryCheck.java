package org.example.service;

import java.io.File;
import java.util.Map;
import java.util.TreeSet;
import java.util.logging.Logger;

public class MemoryCheck {
    private static final Logger logger = Logger.getLogger(MemoryCheck.class.getName());
    private final StorageService storageService;
    private final long maxSizeBytes;
    private final String dbFilePath;

    public MemoryCheck(StorageService storageService, String dbFilePath, long maxSizeBytes) {
        this.storageService = storageService;
        this.dbFilePath = dbFilePath;
        this.maxSizeBytes = maxSizeBytes;
    }

    public void enforceMemoryLimit() {
        File dbFile = new File(dbFilePath);
        if (!dbFile.exists()) return;

        long fileSize = dbFile.length();
        if (fileSize <= maxSizeBytes) return;

        logger.warning("MVStore file size " + fileSize + " exceeds limit " + maxSizeBytes + ". Deleting old records");
        Map<String, Object> map = storageService.getMap();
        if (map.isEmpty()) return;

        int totalKeys = map.size();
        int keysToRemove = (int) Math.ceil(totalKeys * 0.2);
        TreeSet<String> sortedKeys = new TreeSet<>(map.keySet());

        int removed = 0;
        for (String key : sortedKeys) {
            if (removed >= keysToRemove) break;
            storageService.remove(key);
            removed++;
        }

        storageService.commit();
        logger.info("Removed " + removed + " old records. New file size: " + dbFilePath.length());
    }
}
