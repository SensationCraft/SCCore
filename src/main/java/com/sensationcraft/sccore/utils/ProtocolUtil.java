package com.sensationcraft.sccore.utils;

import com.comphenix.protocol.reflect.MethodUtils;
import com.comphenix.protocol.utility.MinecraftReflection;
import org.bukkit.inventory.ItemStack;

public class ProtocolUtil {

    public static String getItemStackName(ItemStack stack) {
        try {
            return (String) MethodUtils.invokeExactMethod(MinecraftReflection.getMinecraftItemStack(stack), "getName", null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "ERROR";
    }

}
