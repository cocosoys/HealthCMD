package soys.plugin.healthcmd;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class NMS {
//    public void killEntity(Player player) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
//        Method method= getNMSClass("EntityPlayer").getMethod("killEntity",null);
//
//        method.invoke(player, null);
//    }
//
//    public Class<?> getNMSClass(String className) {
//        String rootName = Bukkit.getServer().getClass().getName();
//        //这里的rootName是OBC路径，需要把它替换成NMS路径
//        //在NMS中找一个类，将它替换成我们要找的类就行了。
//        try {
//            return Class.forName(rootName.replace("org.bukkit.craftbukkit", "net.minecraft.server").
//                    replace("CraftServer", className));
//        } catch (ClassNotFoundException e) {
//            return null;
//        }
//    }

    public static Class<?> getCraftClass(String className) {
        String rootName = Bukkit.getServer().getClass().getName();
        try {
            return Class.forName(rootName.replace("CraftServer", className));
        } catch (ClassNotFoundException e) {
            return null;
        }
    }
//
//    private String getNMSVersion() {
//        return Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
//    }

    public static void killEntityNMS(org.bukkit.entity.Entity bukkitEntity) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        // 获取CraftEntity类
        Class<?> craftEntityClass = getCraftClass("entity.CraftEntity");

        // 转换为CraftEntity并获取NMS实体
        Object craftEntity = craftEntityClass.cast(bukkitEntity);
        Method getHandleMethod = craftEntityClass.getMethod("getHandle");
        Object nmsEntity = getHandleMethod.invoke(craftEntity);

        // 调用killEntity方法
        Method killMethod = nmsEntity.getClass().getMethod("killEntity");
        killMethod.invoke(nmsEntity);
    }
}
