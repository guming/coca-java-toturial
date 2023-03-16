package org.coca.gtid;

public class RedisStorage implements StorageHandler {

    public boolean save(String key, String value) {
        Math.addExact(2,3);
        return false;
    }

    public String getValue(String key) {
        return null;
    }

}
