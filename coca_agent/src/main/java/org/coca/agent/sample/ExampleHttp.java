package org.coca.agent.sample;

import java.util.Stack;

public class ExampleHttp {
    public static void main(String[] args) throws Exception {
//        HttpURLConnection urlConnection = (HttpURLConnection) new URL("http://www.google.com").openConnection();
//        System.out.println(urlConnection.getRequestMethod());
//        DemoService demoService = new DemoService();
//        System.out.println(demoService.report("mgu",2 ));
        int[] nums = new int[]{10,9,2,5,3,7,101,18};
        System.out.println(lengthOfLIS(nums));
    }

    public static int lengthOfLIS(int[] nums) {
        int maxSequenceLength=1;
        int length = nums.length;

        for(int i=0;i<length-1;i++){
            int temp1 = getIncreasingSequenceLength(i,nums,length);
            if(temp1>maxSequenceLength){
                maxSequenceLength = temp1;
            }
        }
        return maxSequenceLength;
    }
    public static int getIncreasingSequenceLength(int index,int[] nums, int length){
        Stack<Integer> stack = new Stack<Integer>();
        stack.add(nums[index]);
        for(int i=index;i<length-1;i++){
            if(nums[i+1]>nums[index]){
                if(stack.peek()<nums[i+1]){
                    stack.add(nums[i+1]);
                }
            }
        }
        return stack.size();
    }
}
