package com.pablo67340.region.utils;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

public class Cooldowns {
    private static Table<String, String, Long> cooldowns = HashBasedTable.create();
    
    public static long getCooldown(String player, String key) {
        return calculateRemainder(cooldowns.get(player, key));
    }
    
    public static long setCooldown(String player, String key, long delay) {
        return calculateRemainder(
                cooldowns.put(player, key, System.currentTimeMillis() + delay));
    }
    
    public static boolean tryCooldown(String player, String key, long delay) {
        if (getCooldown(player, key) <= 0) {
            setCooldown(player, key, delay);
            return true;
        }
        return false;
    }
    
    private static long calculateRemainder(Long expireTime) {
        return expireTime != null ? expireTime - System.currentTimeMillis() : Long.MIN_VALUE;
    }
}