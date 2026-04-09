package org.example.service;

import org.h2.mvstore.MVStore;

import java.util.Map;

public class StorageService {
    private final MVStore store;
    private final Map<String, Object> map;

    public StorageService(String fileName, String mapName){
        store = new MVStore.Builder()
                .fileName(fileName)
                .open();
        map = store.openMap(mapName);
    }

    public void put(String key, Object obj){
        map.put(key, obj);
        store.commit();
    }

    public Object get(String key){
        return map.get(key);
    }

    public Map<String, Object> getMap(){
        return map;
    }

    public void close(){
        store.close();
    }

    public MVStore getStore() {
        return store;
    }

    public void remove(String key) {
        map.remove(key);
    }

    public void commit() {
        store.commit();
    }
}
