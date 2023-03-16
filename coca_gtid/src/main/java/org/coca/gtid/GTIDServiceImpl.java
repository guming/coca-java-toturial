package org.coca.gtid;

public class GTIDServiceImpl implements GTIDservice {

    public Result makeGtid(String tansactionKey, long currentTime, int timeout) {
        return null;
    }

    public boolean remove(String tansactionKey) {
        return false;
    }

    public boolean closeGtid(Result oldGtid) {
        return false;
    }

    public boolean updateCtime(String tansactionKey, long currentTime) {
        return false;
    }

}
