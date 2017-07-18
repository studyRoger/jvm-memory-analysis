package org.roger;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;

/**
 * Created by cher5 on 2017/7/13.
 */
public class Memory {

    public static void main(String[] args) {
        int ONE_MEGA = 1024 * 1024;
        Queue<Object> memory = new LinkedList<>();
        Scanner in = new Scanner(System.in);

        while(in.hasNext()) {
            String input = in.next();
            if(input.equals("release") && !memory.isEmpty()) {
                memory.remove();
                System.gc();
            } else {
                try {
                    int size = Integer.valueOf(input);
                    memory.add(new byte[size * ONE_MEGA]);
                } catch (NumberFormatException e) {
                    System.out.println("cannot parse " + input);
                }

            }

        }

    }
}
