package com.example.springioc.bean;

import com.example.springioc.anno.Bean;
import com.example.springioc.anno.Di;

import java.io.File;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class AnnotationApplicationContext implements ApplicationContext {
    //创建map集合，放入bean对象
    private Map<Class,Object> beanFactory = new HashMap<>();
    private  static String rootPath;

    //根据key返回对象
    @Override
    public Object getBean(Class clazz) {
        return beanFactory.get(clazz);
    }

    //关键处：设置包扫描规则（当前包及其子包，判断类上是否有@Bean注解，则通过反射实例化）
    //创建有参数构造，传递包路径，设置扫描路径
    public AnnotationApplicationContext(String basePackage){
        //传递参数值类似IdeaProject.spring-ioc的路径，找其绝对路径，并且递归获取子文件夹和子文件
        //1 将.转换成\
        //Regex的”.”有特别的意思须要转义成”\.”，然后”\”在Java跟Regex里都有转义的意思须要转成”\\”才能使用，所以以Regex来看，”\\.”是”\.”；”\\\\”是”\\”
        String packagePath = basePackage.replaceAll("\\.", "\\\\");
        //2 获取包绝对路径（编译后路径）
        try {
            Enumeration<URL> urls = Thread.currentThread().getContextClassLoader().getResources(packagePath);

            while (urls.hasMoreElements()){
                //获取完整的绝对路径
                URL url = urls.nextElement();
                String filePath = URLDecoder.decode(url.getFile(), "utf-8");

                //获取包前面路径的部分，字符串截取
                rootPath = filePath.substring(0, filePath.length() - packagePath.length());
                //包扫描
                loadBean(new File(filePath));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        //属性注入
        loadDi();
    }

    private void loadDi() {
        //实例化的对象均在beanFactory的map里
        //1 遍历map集合
        Set<Map.Entry<Class, Object>> entries = beanFactory.entrySet();
        for (Map.Entry<Class, Object> entry : entries) {
            //2 获取map集合里的每个对象，每个对象的属性获取到
            Object obj = entry.getValue();

            //获取对象Class（备注：这里为什么不通过Map的keyset直接获取？因为可能是接口，所以通过values获取对象才能确保获得实例的class对象）
            Class<?> clazz = obj.getClass();

            //获取每个对象属性
            Field[] declaredFields = clazz.getDeclaredFields();

            //3 遍历每个对象属性数组，得到每个属性（无论个数，私有/共有的属性都需要获得）
            for (Field field : declaredFields) {

                Di annotation = field.getAnnotation(Di.class);
                if (annotation != null) {
                    //4 判断属性上是否有Di
                    field.setAccessible(true);
                    //5 如果有Di注解，把对象进行注入
                    try {
                        field.set(obj,beanFactory.get(field.getType()));//field.getType()表示属性的类型，通过类型获得对象再注入
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }
            }



        }




    }

    //包扫描，bean实例化
    private void loadBean(File file) throws Exception {
        //1 判断当前内容是否是文件夹
        if(file.isDirectory()){
            //2 是文件夹，获取文件夹里面所有内容
            File[] childrenFiles = file.listFiles();

            //3 判断文件夹里内容为空，直接返回
            if(childrenFiles == null||childrenFiles.length==0){
                return;
            }
            //4 如果文件不为空，遍历文件夹内所有内容
            for(File child: childrenFiles){
                //4.1 遍历得到每个file对象，继续判断，还是文件夹则递归
                if(child.isDirectory()){
                    loadBean(child);
                }else {
                    //4.2 遍历得到文件
                    //4.3 得到包路径+类名称 - 字符串截取
                    String pathWithClass = child.getAbsolutePath().substring(rootPath.length()-1);
                    //4.4 判断文件类型是否为.class
                    if(pathWithClass.endsWith(".class")){
                        //4.5 如果是.class，把路径\替换成. 把.class去掉
                        String allName = pathWithClass.replaceAll("\\\\", ".").replace(".class", "");

                        //4.6 判断类上面是否有注解@Bean,有再实例化
                        //4.6.1 获取类的class对象
                        Class<?> clazz = Class.forName(allName);
                        //4.6.2 判断不是接口 再实例化
                        if(!clazz.isInterface()){
                            //4.6.3 判断是否有注解
                            Bean annotation = clazz.getAnnotation(Bean.class);
                            if (annotation != null) {
                                //4.6.4 实例化
                                Object instance = clazz.getDeclaredConstructor().newInstance();
                                //4.7 实例化后放入map中（判断类是否有接口，有接口作为key，无则类作为key）
                                //4.7.1 有接口作为key
                                if (clazz.getInterfaces().length>0){
                                    beanFactory.put(clazz.getInterfaces()[0],instance);
                                }else {
                                    beanFactory.put(clazz,instance);
                                }
                            }
                        }
                    }

                }

            }
        }
    }
}
