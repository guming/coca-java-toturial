package org.coca.gtid;

public interface StorageHandler {

    boolean save(String key,String value);

    String getValue(String key);

}
