package com.lixi.open.mybatis;

import org.apache.commons.lang3.StringUtils;
import sun.net.www.protocol.jar.JarURLConnection;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ClassUtil {

    public static List<Class<?>> getClassList(String packageName) {
        List<Class<?>> classList = new ArrayList<>();


        try {
            Enumeration<URL> urls = ClassUtil.class.getClassLoader().getResources(packageName.replace(".", "/"));

            // 遍历
            while (urls.hasMoreElements()) {
                URL url = urls.nextElement();
                if (url == null) {
                    continue;
                }

                // 协议名 file / jar
                String protocol = url.getProtocol();
                if ("file".equals(protocol)) {
                    // 若在 class 目录中，则添加类
                    String packagePath = url.getPath().replaceAll("%20", " ");
                    addClass(classList, packagePath, packageName);
                } else if ("jar".equals(protocol)) {
                    // 在 jar 包里
                    JarURLConnection jarURLConnection = (JarURLConnection) url.openConnection();
                    JarFile jarFile = jarURLConnection.getJarFile();

                    Enumeration<JarEntry> entries = jarFile.entries();

                    while (entries.hasMoreElements()) {
                        JarEntry jarEntry = entries.nextElement();
                        String name = jarEntry.getName();

                        if (name.endsWith(".class")) {
                            // 获取类名
                            String className = name.substring(0, name.lastIndexOf(".")).replaceAll("/", ".");
                            doAddClass(classList, className);
                        }
                    }
                }

            }


        } catch (IOException e) {
            e.printStackTrace();
        }

        return classList;
    }

    private static void addClass(List<Class<?>> classList, String packagePath, String packageName) {

        File[] files = new File(packagePath).listFiles(
                file -> (file.isFile() && file.getName().endsWith(".class"))
                        || file.isDirectory());

        for (File file : files) {

            String fileName = file.getName();

            if (file.isFile()) {

                String className = fileName.substring(0, fileName.lastIndexOf("."));

                if (StringUtils.isNotEmpty(packageName)) {
                    className = packageName + "." + className;
                }
                doAddClass(classList, className);
            } else {

                String subPackagePath = fileName;
                if (StringUtils.isNotBlank(packagePath)) {
                    subPackagePath = packagePath + "/" + subPackagePath;
                }

                String subPackageName = fileName;
                if (StringUtils.isNotBlank(packageName)) {
                    subPackageName = packageName + "." + subPackageName;
                }

                // 递归
                addClass(classList, subPackagePath, subPackageName);
            }
        }
    }

    private static void doAddClass(List<Class<?>> classList, String className) {

        Class<?> cls;
        try {
            cls = Class.forName(className, false, ClassUtil.class.getClassLoader());
            classList.add(cls);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
