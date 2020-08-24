package com.lixi.open.mybatis;

import com.google.gson.Gson;
import com.lixi.open.mybatis.mapper.UserDao;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class Main {

    static {
        // 动态生成的代理类保存到磁盘上，运行一次以后在 idea 的左边窗口有 一个 com 的文件夹，一直点进去就看到了
        System.setProperty("sun.misc.ProxyGenerator.saveGeneratedFiles", "true");
    }

    // 配置文件的路径
    private static final String configPath = "config.txt";

    // 用于反序列化 json 文件
    // 使用了 json 用来描述 mapper 而不是 xml
    private static final Gson GSON = new Gson();

    // dao 对应的动态生成的实例
    private static final Map<Class<?>, Object> DAO_TO_PROXY_INSTANCE = new HashMap<>();

    public static void main(String[] args) throws ClassNotFoundException, NoSuchMethodException{
        log.info("项目启动");
        prepare();
        log.info("读取配置完毕，可以使用了");

        log.info("\r\n==============================================\r\n");

        // 模拟注入
        log.info("模拟注入 UserDao");
        UserDao userDao = (UserDao) DAO_TO_PROXY_INSTANCE.get(UserDao.class);

        // 使用
        userDao.count("lixi");
        userDao.find(22L);
    }

    /**
     * 读取配置文件，生成对应 proxy
     * @throws NoSuchMethodException
     * @throws ClassNotFoundException
     */
    private static void prepare() throws NoSuchMethodException, ClassNotFoundException {
        // 读取配置文件，找到 mapper 的包，和 json 文件的位置
        Map<String, Object> configMap = getConfig();
        String mapperPackage = Objects.toString(configMap.get("mapper-package"));
        String mapperLocation = Objects.toString(configMap.get("mapper-location"));

        log.info("获得配置项 mapper-package: [{}]", mapperPackage);
        log.info("获得配置项 mapper-location: [{}]", mapperLocation);

        URL mapperUrl = Main.class.getResource("/" + mapperLocation);

        // json 文件搞成 Mapper 的描述
        List<MapperInfo> mapperInfos = new ArrayList<>();
        getMapperJson(mapperInfos, mapperUrl.getPath());

        // 拿到所有的 dao 的 class
        List<Class<?>> classList = ClassUtil.getClassList(mapperPackage);
        Map<String, ? extends Class<?>> classNameToCls = classList.stream().collect(Collectors.toMap(Class::getName, e -> e));

        // 生成对应的代理类
        for (MapperInfo mapperInfo : mapperInfos) {

            String daoName = mapperInfo.getCls();
            Class<?> daoCls = classNameToCls.get(daoName);

            // daoCls 方法与 json 描述方法的对应
            Map<Method, MapperMethod> daoMethodToActualMethodToCall = new HashMap<>();
            for (MapperMethod actMethod : mapperInfo.getMethods()) {
                String pType = actMethod.getPType();

                Method daoMethod = daoCls.getDeclaredMethod(actMethod.getMethod(), Class.forName(pType));
                daoMethodToActualMethodToCall.put(daoMethod, actMethod);
            }

            MapperProxyFactory<?> mapperProxyFactory = new MapperProxyFactory<>(daoCls, daoMethodToActualMethodToCall);

            // dao类型对应代理类
            // 动态体现在不需要每一个 daoCls 写对应的 proxy，而是由程序动态生成 proxy
            Object proxy = mapperProxyFactory.newInstance();
            DAO_TO_PROXY_INSTANCE.put(daoCls, proxy);
            log.info("生成 [{}] 对应的 Proxy 实例", daoCls);
        }
    }

    private static void getMapperJson(List<MapperInfo> mapperInfos, String resource) {
        File[] files = new File(resource).listFiles(file -> (file.getName().endsWith("-sql.json") && file.isFile() || file.isDirectory()));

        for (File file : files) {
            if (file.isFile()) {
                log.info("读取 mapper 对应 sql 描述的 json 文件: [{}]", file.getName());
                StringBuilder sb = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }

                MapperInfo mapperInfo = GSON.fromJson(sb.toString(), MapperInfo.class);
                log.info("生成对应的 java MapperInfo 实例: [{}]", mapperInfo);
                mapperInfos.add(mapperInfo);
            } else {
                getMapperJson(mapperInfos, file.getPath());
            }
        }
    }

    /**
     * 读取配置文件
     *
     * @return
     */
    private static Map<String, Object> getConfig() {
        log.info("读取配置文件: [{}]", configPath);
        Map<String, Object> configMap = new HashMap<>();

        InputStream configResource = Main.class.getResourceAsStream("/" + configPath);

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(configResource))) {
            String line;
            while ((line = reader.readLine()) != null && StringUtils.isNotBlank(line)) {
                String[] split = line.split("=");
                configMap.put(split[0], split[1]);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return configMap;
    }


}
