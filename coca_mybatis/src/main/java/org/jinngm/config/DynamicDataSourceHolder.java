package org.jinngm.config;

public class DynamicDataSourceHolder {

    public static final ThreadLocal<String> holder = new ThreadLocal<>();

    public static void putDataSource(String key){
        holder.set(key);
    }

    public static String getDataSource(){
        return holder.get();
    }

    public static void cleanDataSource(){
        holder.remove();
    }

    public static boolean isMaster(){
        if(holder.get()==null){
            return true;
        }
        return holder.get().equals(DataSources.MASTER_DATASOURCE);
    }
}
