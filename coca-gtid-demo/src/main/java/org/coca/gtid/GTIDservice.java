package org.coca.gtid;

public interface GTIDservice {

    public Result makeGtid(String tansactionKey,long currentTime,int timeout);

    public boolean remove(String tansactionKey);

    public boolean closeGtid(Result oldGtid);

    public boolean updateCtime(String tansactionKey,long currentTime);

}
