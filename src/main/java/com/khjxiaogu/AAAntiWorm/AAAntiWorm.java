package com.khjxiaogu.AAAntiWorm;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;

public class AAAntiWorm extends JavaPlugin{

	public AAAntiWorm() {
		super();
		
        ClassLoader classloader =new URLClassLoader(new URL[] {AAAntiWorm.class.getProtectionDomain().getCodeSource().getLocation()},ClassLoader.getSystemClassLoader().getParent());
        Class<?> clazz;
		try {
			clazz = classloader.loadClass("com.khjxiaogu.AAAntiWorm.JarWatcher");
			Constructor<?> ctor=clazz.getDeclaredConstructor(File.class);
			ctor.setAccessible(true);
			Object jw=ctor.newInstance(this.getDataFolder().getParentFile());
			clazz.getMethod("start").invoke(jw);
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
	}

	@Override
	public void onLoad() {
		getLogger().info("Basic AntiWorm By Github@khjxiaogu/khjxiaogu@qq.com");
	}
	
}
