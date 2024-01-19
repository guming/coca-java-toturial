package org.coca.gtid;

import java.util.HashMap;
import java.util.Map;

public class Test {
    public static int[][] store;
    public static void main(String[] args) {

        System.out.println(longestPalindrome("babad"));
    }
    public static String longestPalindrome(String s) {
        Map<Integer,String> store=new HashMap<Integer,String>();
        if(s==null){
            return null;
        }
        int len=s.length();
        if(len>1000||len==0){
            return null;
        }
        int cur=len;
        int max=0;
        for(int i=0;i<len;i++){
            int calc=div(s,i,len-1,store);
            System.out.println(calc);
            max=Math.max(calc,max);
        }
        String temp=store.get(max);
        if(temp!=null&&temp.indexOf(",")!=-1){
            String[] str=temp.split(",");
            System.out.println("------"+temp);
            return s.substring(Integer.valueOf(str[0]),Integer.valueOf(str[1]));
        }else{
            return s.substring(max,max+1);
        }
    }
    public static int div(String s,int start,int end,Map<Integer,String> store){
        int res1=start;
        int res2=end;

        while(s.charAt(start)==s.charAt(end)&&start<end){
            end--;
            start++;
            if(start>=end){
                System.out.println("true");
                store.put(res2-res1,res1+","+(res2+1));
                return res2-res1;
            }
        }
        if(start+1<s.length()&&end>0) {
            return div(s, start, end - 1, store);
        }else{
            return 0;
        }
    }
}
