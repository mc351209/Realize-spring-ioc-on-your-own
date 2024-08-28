#这个一个基于注解实现的Bean管理及依赖注入的spring实现#
代码内有详细备注。

手写ioc项目的整体逻辑简述如下：
1、构建整个项目包spring-ioc
2、构建测试类及相应的包（service层和dao层，包括对应的接口和实现类）
3、构建注解（@Bean 用于实现对象创建 @Di 用于实现属性注入）
4、构建ApplicationContext接口（容器，类似spring中的容器可以基于此接口实现各种实现类，例：基于xml文件，基于注解）
5、构建AnnotationApplicationContext实现类（完成bean管理以及属性注入）

对于实现类的处理逻辑简述：
（一）Bean管理
1、构造AnnotationApplicationContext类的带参数的构造方法（参数为包路径，例如：com.testspring）。通过此构造方法实现，对包路径下所有文件的扫描，并获取带有@Bean注解的类，进行对象创建并存储map中。
2、获取包含传入参数的所有绝对路径。遍历所有路径，遍历过程调用loadBean方法（用于创建带有@Bean注解的所有实现类）。
3、loadBean的具体过程：判断是否文件夹-判断是否为空文件夹-非空则遍历文件夹（对于文件夹递归嗲用loadBean）-对于文件（获取全限定名，并判断是否以.class结尾，并生成class对象）-对class对象判断是否为借口类型-对非借口类型的class对象判断是否包含Bean注解（Bean annotation = clazz.getAnnotation(Bean.class);）
-最后对于符合条件的对象判断是否存在接口（存在则key为接口类型，不存在则key为实现类类型，值为Bean对象）
（二）依赖注入
1、构造方法中调用diload（）方法
2、通过map集合获取class对象，遍历调用field判断是否有Di注解；
3、如果有Di注解，通过类型注入：field.set(obj,beanFactory.get(field.getType()));
