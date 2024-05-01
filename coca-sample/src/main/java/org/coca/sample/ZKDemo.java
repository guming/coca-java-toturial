package org.coca.sample;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;
import java.util.List;


public class ZKDemo {
    public static void main(String[] args) {
        ZooKeeper client = null;
        try {
            client = new ZooKeeper("127.0.0.1:2180", 3000, null);
            List<String> children = client.getChildren("/", false);
            client.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (KeeperException e) {
            throw new RuntimeException(e);
        }

    }
}
