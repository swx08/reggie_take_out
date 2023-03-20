项目访问路径：后台：http://82.157.234.124:8081/backend/page/login/login.html
             前台：http://82.157.234.124:8081/front/page/login.html

产品原型:瑞吉外卖后台(管理端)
        瑞吉外卖前台(用户端)

技术选型：

   用户层：html5、VUE.js、ElementUI、微信小程序
   网关层：Nginx
   应用层：SpringBoot、SpringMVC、Spring Session、Spring、Swagger、lombok
   数据层：Mysql、Mybatis、Mybatis Plus、Redis

工具：

   git、maven、junit

功能架构：

   移动前端(h5、微信小程序)：手机号登录、微信登录、地址管理、历史订单
                           菜品规格、购物车、下单、菜品浏览

   系统管理后台：分类管理、菜品管理、套餐管理、菜品口味管理
                员工登录、员工退出、员工管理、订单管理           


# 瑞吉外卖项目学习心得

## 一、登录功能

### 1、md5加密算法

是由spring提供的工具

```java
import org.springframework.util.DigestUtils;
//1.将页面提交的password进行md5加密处理
        String password = employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());
```

### 2、配置请求过滤器

#### 2.1 实现Filter类

```java
/**
 * 过滤器注解urlPatterns = "/*"表示过滤掉所有请求
 */
@WebFilter(filterName = "LoginCheckFilter",urlPatterns = "/*")
@Slf4j
public class LoginCheckFilter implements Filter {
    //路径匹配器，支持通配符
    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        //1.获取当前的请求url
        String requestURI = request.getRequestURI();
        //以下是不需要处理的请求url,直接放行
        String urls[] = new String[]{
                "/employee/login",
                "/employee/logout",
                "/backend/**",
                "/front/**"
        };
        //2.判断本次请求是否需要处理
        boolean check = check(urls, requestURI);
        //3.如果不需要处理则直接放行
        if(check){
            log.info("本次请求不需要处理:{}",request.getRequestURI());
            filterChain.doFilter(request,response);
            return;
        }
        //4.查看登录状态，如果已登录则放行
        if(request.getSession().getAttribute("employee") != null){
            log.info("用户已登录,id为:"+request.getSession().getAttribute("employee"));
            filterChain.doFilter(request,response);
            return;
        }
        log.info("用户未登录!");
        //5.如果未登录则返回未登录结果，通过输出流方式响应给前端
        //如果未登录,只要返回NOTLOGIN字符串，页面跳转由前端js来控制
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
        return;
    }

    /**
     * 判断本次请求是否需要处理封装方法
     * @param urls
     * @param requestURI
     * @return
     */
    public boolean check(String[] urls,String requestURI){
        for (String url : urls) {
            //如果match为true,说明当前请求路径为不需要处理的请求,则说明路径匹配上
            boolean match = PATH_MATCHER.match(url, requestURI);
            if(match){
                return true;
            }
        }
        return false;
    }
}
```

#### 2.2 在启动类加@ServletComponentScan

```java
@Slf4j//lombok提供的日志
@SpringBootApplication//启动类
@ServletComponentScan//开启过滤器
public class ReggieApplication {
    public static void main(String[] args) {
        SpringApplication.run(ReggieApplication.class,args);
        log.info("项目启动成功!");
    }
}
```

### 3、日志输出@Slf4j

这是由lombok提供的日志输出，方便调试。

```java
@Slf4j//lombok提供的日志
@SpringBootApplication//启动类
@ServletComponentScan//开启过滤器
public class ReggieApplication {
    public static void main(String[] args) {
        SpringApplication.run(ReggieApplication.class,args);
        log.info("项目启动成功!");
    }
}
```

### 4、全局异常处理

### @ControllerAdvice

@ControllerAdvice是@Controller注解的一个增强，这个注解是Spring里面的东西，可以处理全局异常。当然在Spring Boot中也可以使用，但是Spring Boot中有它全自动化配置的异常处理，因为是全自动化的，因此也可以自己定制，比如定制它的异常页面，异常信息提示，异常视图。这里的话主要看一下，这个注解怎么用。它主要有一下几个功能：

- 处理全局异常
- 预设全局数据
- 请求参数预处理

### 全局异常处理格式参考

```java
//处理全局异常
@ControllerAdvice(annotations = {RestController.class, Controller.class})
//json格式返回
@ResponseBody
@Slf4j
public class GlobalExceptionHandler {
    //捕获异常SQLIntegrityConstraintViolationException(SQL异常)
    @ExceptionHandler({SQLIntegrityConstraintViolationException.class})
    public R<String> exceptionHandler(SQLIntegrityConstraintViolationException e){
        log.error(e.getMessage());
        //由于数据库中账号(username)为unique,所以这里捕获的是SQL异常
        //查看SQL异常信息里面是否包含"Duplicate entry"字符
        if(e.getMessage().contains("Duplicate entry")){
            //将捕获到的SQL异常信息放到split里
            String[] split = e.getMessage().split(" ");
            //将split数组里的字符串进行分割,split[2]里存得是账号信息
            String msg = split[2] + "已存在";
            return R.error(msg);
        }
        return R.error("未知错误!");
    }
}
```

## 二、员工管理

### 5、Mybatis-Plus分页查询

**MybatisPlusInterceptor**
        **MybatisPlusInterceptor是一系列的实现InnerInterceptor的拦截器链，也可以理解为一个集合。可以包括如下的一些拦截器：**

自动分页: PaginationInnerInterceptor（最常用）
多租户: TenantLineInnerInterceptor
动态表名: DynamicTableNameInnerInterceptor
乐观锁: OptimisticLockerInnerInterceptor
sql性能规范: IllegalSQLInnerInterceptor
防止全表更新与删除: BlockAttackInnerInterceptor

#### 5.1 分页插件配置类

```java
/**
 * 配置MP的分页插件
 */
@Configuration
public class MybatisPlusPageConfig {
    @Bean//交给spring管理
    public MybatisPlusInterceptor mybatisPlusInterceptor(){
        MybatisPlusInterceptor mybatisPlusInterceptor = new MybatisPlusInterceptor();
        mybatisPlusInterceptor.addInnerInterceptor(new PaginationInnerInterceptor());
        return mybatisPlusInterceptor;
    }
}
```

#### 5.2 分页查询

```java
/**
     * 员工信息分页查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page,int pageSize,String name){
        log.info("page={},pageSize={},name={}",page,pageSize,name);
        //构造分页构造器
        Page pageInfo = new Page(page,pageSize);
        //构造条件构造器
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        //添加过滤条件(模糊查询)
        queryWrapper.like(!StringUtils.isEmpty(name),Employee::getName,name);
        //添加排序条件
        queryWrapper.orderByDesc(Employee::getUpdateTime);
        //执行查询
        employeeService.page(pageInfo,queryWrapper);
        return R.success(pageInfo);
    }
```

#### 5.3 分页前端代码

```vue
<el-pagination
        class="pageList"
        :page-sizes="[10, 20, 30, 40]"
        :page-size="pageSize"
        layout="total, sizes, prev, pager, next, jumper"
        :total="counts"
        :current-page.sync="page"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      ></el-pagination>

new Vue({
        el: '#member-app',
        data() {
          return {
             input: '',
             counts: 0,
             page: 1,
             pageSize: 10,
			}
		methods:{
			handleSizeChange (val) {
            	this.pageSize = val
            	this.init()
          		},
          	 handleCurrentChange (val) {
            	this.page = val
            	this.init()
          		}
			}
```

### 6、禁用、启用  Id 精度丢失bug

#### 6.1 bug现象

用户信息通过id进行修改，但是浏览器传过来的id与数据库中的id匹配不一致，无法进行用户信息修改。

id精度丢失，由于数据库中的id为长整型，浏览器只能精确到前16位，后几位按四舍五入取整，有精度损失。

#### 6.2 解决方案:

我们可以在服务端给页面响应json数据时进行处理，将Long型数据全部转换为String字符串。

#### 6.3 代码修复

具体实现步骤：

1）提供对象转换器jacksonObjectMapper,基于jackson进行java对象到json数据的转换。

```java
/**
 * 对象映射器:基于jackson将Java对象转为json，或者将json转为Java对象
 * 将JSON解析为Java对象的过程称为 [从JSON反序列化Java对象]
 * 从Java对象生成JSON的过程称为 [序列化Java对象到JSON]
 */
public class JacksonObjectMapper extends ObjectMapper {

    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";
    public static final String DEFAULT_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String DEFAULT_TIME_FORMAT = "HH:mm:ss";

    public JacksonObjectMapper() {
        super();
        //收到未知属性时不报异常
        this.configure(FAIL_ON_UNKNOWN_PROPERTIES, false);

        //反序列化时，属性不存在的兼容处理
        this.getDeserializationConfig().withoutFeatures(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);


        SimpleModule simpleModule = new SimpleModule()
                .addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern(DEFAULT_DATE_TIME_FORMAT)))
                .addDeserializer(LocalDate.class, new LocalDateDeserializer(DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT)))
                .addDeserializer(LocalTime.class, new LocalTimeDeserializer(DateTimeFormatter.ofPattern(DEFAULT_TIME_FORMAT)))

                .addSerializer(BigInteger.class, ToStringSerializer.instance)
                .addSerializer(Long.class, ToStringSerializer.instance)
                .addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(DEFAULT_DATE_TIME_FORMAT)))
                .addSerializer(LocalDate.class, new LocalDateSerializer(DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT)))
                .addSerializer(LocalTime.class, new LocalTimeSerializer(DateTimeFormatter.ofPattern(DEFAULT_TIME_FORMAT)));

        //注册功能模块 例如，可以添加自定义序列化器和反序列化器
        this.registerModule(simpleModule);
    }
}
```

2）在WebMvcConfig配置类中扩展Spring mvc的消息转换器，在此消息转换器中使用提供的对象转换器进行java对象到json数据的转换。

```java
/**
     * 扩展mvc框架的消息转换器
     * @param converters
     */
    @Override
    protected void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        //创建消息转换器对象
        MappingJackson2HttpMessageConverter messageConverter = new MappingJackson2HttpMessageConverter();
        //设置对象转换器，底层使用jackson将java对象转为json
        messageConverter.setObjectMapper(new JacksonObjectMapper());
        //将上面的消息转换器对象追加到mvc框架的转换器集合中
        converters.add(0,messageConverter);
    }
```

## 三、分类管理

### 7、Mybatis-Plus自动填充公共字段

#### 7.1使用注解@TableField

```java
	@TableField(fill = FieldFill.INSERT)//新增时自动填充
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)//新增或修改时自动填充
    private LocalDateTime updateTime;
```

#### 7.2 编写元数据对象处理器

```java
/**
 * 自动填充公共字段处理器
 */
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {
    /**
     * 更新时填充
     * @param metaObject
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        metaObject.setValue("createTime", LocalDateTime.now());
        metaObject.setValue("updateTime",LocalDateTime.now());
        metaObject.setValue("createUser",BaseContext.getCurrentId());
        metaObject.setValue("updateUser",BaseContext.getCurrentId());
    }

    /**
     * 修改时自动填充
     * @param metaObject
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        metaObject.setValue("updateTime",LocalDateTime.now());
        metaObject.setValue("updateUser",BaseContext.getCurrentId());
    }
}

```

#### 7.3 ThreadLocal使用

##### 7.3.1 使用前景

因为自动填充公共字段时需要获取当前登录用户的Id,所以引入了ThreadLocal。

在学习ThreadLocal之前，我们需要确认一个事情，就是客户端发送的每次http请求,对应的在服务端都会分配一个新的线程来处理，要确保以下所执行到的方法都是同一个线程：

```java
1）LoginCheckFilter的doFilter方法
2）EmployeeController的update方法
3）MyMetaObjectHandler的updateFill方法
可以使用以下代码查看当前线程Id
long id = ThreadLocal.currentThread().getId();
```

##### 7.3.2 什么是ThreadLocal？

由JDK所提供。ThreadLocal并不是一个Thread,而是Thread的局部变量，当使用ThreadLocal维护变量时，ThreadLocal为每个使用该变量的线程提供独立的变量副本，所以每一个线程都可以独立地改变自己的副本，而不会影响其它线程所对应的副本。

ThreadLocal为每一个线程提供单独的一份存储空间，具有线程隔离的效果，只有在线程内才能获取到对应的值，线程外则不能访问。

ThreadLocal常用方法：

```java
public void set(T value) 设置当前线程的线程局部变量的值
public T get()           返回当前线程所对应的线程局部变量的值
```

具体使用:

我们可以在LoginCheckFilter的doFilter方法中获取当前登录的用户Id，并调用ThreadLocal的set方法设置当前线程的线程局部变量的值 ( 用户Id ) ,然后在MyMetaObjectHandler的updateFill方法中调用ThreadLocal的get方法来获取当前的线程所对应的线程局部变量的值 ( 用户Id )。

代码实现：

```java
/**
 * 基于ThreadLocal封装工具类，用来保存和获取当前登录的用户Id
 */
public class BaseContext {
    private static ThreadLocal<Long> threadLocal = new ThreadLocal<Long>();

    public static void setCurrentId(Long id){
        threadLocal.set(id);
    }
    public static Long getCurrentId(){
        return threadLocal.get();
    }
}
```

在过滤器方法里面一旦用户登录就设置用户Id

```java
//4.查看登录状态，如果已登录则放行
        if(request.getSession().getAttribute("employee") != null){
            log.info("用户已登录,id为:"+request.getSession().getAttribute("employee"));
            Long empId = (Long) request.getSession().getAttribute("employee");
            //同一个线程保存用户Id
            BaseContext.setCurrentId(empId);
            filterChain.doFilter(request,response);
            return;
        }
```

## 四、菜品管理业务开发

### 8、文件上传、下载介绍

前端Form表单要求:

```java
method="post" 采用post方式提交数据
enctype="multipart/form-data" 采用multipart格式上传文件
type="file" 使用input的file控件上传
```

后端代码实现：

properties文件配置：

```properties
#文件上传位置
reggie.path=D:/img/
```



```java
/**
 * 文件上传、下载功能
 */
@RestController
@RequestMapping("/common")
public class CommonController {

    //获取配置文件中的路径
    @Value("${reggie.path}")
    private String basePath;

    @PostMapping("/upload")
    public R<String> upload(MultipartFile file) throws IOException {
        //file是一个临时文件，需要转存到指定位置，否则本次请求完成后临时文件会被删除。
        //获取原始文件名
        String originalFilename = file.getOriginalFilename();//abc.jpg
        //截取后缀.jpg
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));

        //使用UUID重新生成文件名,防止上传同名文件被覆盖
        String finalFileName = UUID.randomUUID().toString() + suffix;//skfjd.jpg

        //创建一个目录对象
        File dir = new File(basePath);
        if(!dir.exists()){
            //此目录不存在则创建
            dir.mkdirs();
        }

        //将临时文件转存到指定位置
        file.transferTo(new File(basePath + finalFileName));

        return R.success(finalFileName);
    }
}
```

文件下载具体实现

```java
	/**
     * 文件下载
     * @param name
     * @param response
     */
    @GetMapping("/download")
    public void downLoad(String name, HttpServletResponse response){
        //通过输入流读取指定文件中的File
        FileInputStream fileInputStream = null;
        ServletOutputStream outputStream = null;
        //设置下载格式
        response.setContentType("image/jpeg");
        try {
            fileInputStream = new FileInputStream(basePath + name);
            outputStream = response.getOutputStream();

            //读写文件
            int len = 0;
            byte[] bytes = new byte[1024];
            while ((len = fileInputStream.read(bytes)) != -1 ){
                outputStream.write(bytes,0,len);
                outputStream.flush();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        //关闭流
        try {
            fileInputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            outputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

```

### 9、对象复制

```java
BeanUtils.copyProperties(dish,dishDto);
```

### 10、多表查询添加事务

```java
	/**
     * 保存新增菜品同时保存菜品口味
     * @param dishDto
     */
    @Transactional
    public void saveWithFlavor(DishDto dishDto)
    
    
@Slf4j//lombok提供的日志
@SpringBootApplication//启动类
@ServletComponentScan//开启过滤器
@EnableTransactionManagement//开启事务注解
public class ReggieApplication
```



## 五、套餐业务管理开发

## 六、手机移动端业务开发

### 11、手机短信验证码使用

#### 11.1 使用阿里云的短信服务，需要签名，模板code，设置AccessKey

<img src="C:\Users\石文学\OneDrive\图片\屏幕快照\屏幕截图 2023-02-10 230943.png" alt="屏幕截图 2023-02-10 230943" style="zoom:50%;" />

<img src="C:\Users\石文学\OneDrive\图片\屏幕快照\屏幕截图 2023-02-10 231100.png" alt="屏幕截图 2023-02-10 231100" style="zoom:50%;" />

<img src="C:\Users\石文学\OneDrive\图片\屏幕快照\屏幕截图 2023-02-10 231226.png" alt="屏幕截图 2023-02-10 231226" style="zoom:50%;" />

#### 11.2 代码实现

导入maven依赖

```java
<dependency>
      <groupId>com.aliyun</groupId>
      <artifactId>aliyun-java-sdk-core</artifactId>
      <version>4.5.16</version>
    </dependency>
    <!-- https://mvnrepository.com/artifact/com.aliyun/aliyun-java-sdk-dysmsapi -->
    <dependency>
      <groupId>com.aliyun</groupId>
      <artifactId>aliyun-java-sdk-dysmsapi</artifactId>
      <version>2.1.0</version>
    </dependency>
```

两个工具类

```java
/**
 * 短信发送工具类
 */
public class SMSUtils {

	/**
	 * 发送短信
	 * @param signName 签名
	 * @param templateCode 模板
	 * @param phoneNumbers 手机号
	 * @param param 参数
	 */
	public static void sendMessage(String signName, String templateCode,String phoneNumbers,String param){
		DefaultProfile profile = DefaultProfile.getProfile("cn-hangzhou", "", "");//填短信服务id和密码
		IAcsClient client = new DefaultAcsClient(profile);

		SendSmsRequest request = new SendSmsRequest();
		request.setSysRegionId("cn-hangzhou");
		request.setPhoneNumbers(phoneNumbers);
		request.setSignName(signName);
		request.setTemplateCode(templateCode);
		request.setTemplateParam("{\"code\":\""+param+"\"}");
		try {
			SendSmsResponse response = client.getAcsResponse(request);
			System.out.println("短信发送成功");
		}catch (ClientException e) {
			e.printStackTrace();
		}
	}

}
```

```java
/**
 * 随机生成验证码工具类
 */
public class ValidateCodeUtils {
    /**
     * 随机生成验证码
     * @param length 长度为4位或者6位
     * @return
     */
    public static Integer generateValidateCode(int length){
        Integer code =null;
        if(length == 4){
            code = new Random().nextInt(9999);//生成随机数，最大为9999
            if(code < 1000){
                code = code + 1000;//保证随机数为4位数字
            }
        }else if(length == 6){
            code = new Random().nextInt(999999);//生成随机数，最大为999999
            if(code < 100000){
                code = code + 100000;//保证随机数为6位数字
            }
        }else{
            throw new RuntimeException("只能生成4位或6位数字验证码");
        }
        return code;
    }

    /**
     * 随机生成指定长度字符串验证码
     * @param length 长度
     * @return
     */
    public static String generateValidateCode4String(int length){
        Random rdm = new Random();
        String hash1 = Integer.toHexString(rdm.nextInt());
        String capstr = hash1.substring(0, length);
        return capstr;
    }
}
```

具体业务代码实现

```java
	@PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user, HttpServletRequest request){
        //获取手机号
        String phone = user.getPhone();
        if(!StringUtils.isEmpty(phone)){
            //生成随机的4位验证码
            String code = ValidateCodeUtils.generateValidateCode(4).toString();
            //调用阿里云提供的短信服务API完成发送短信
            //SMSUtils.sendMessage("ruigi","SMS_269495445",phone,code);
            //需要将生成的验证码保存到Session
            request.getSession().setAttribute(phone,code);
            return R.success("短信发送成功!");
        }
        return R.error("短信发送失败!");
    }
```

## 七、Linux常用命令

```
ls           查看当前目录下的内容
pwd          查看当前所在目录
touch        如果文件不存在，创建文件
cd           切换目录
mkdir        创建目录
rm           删除文件
```

```
Linux命令使用技巧

Tab键自动补全
连续两次Tab键，给出操作提示
使用上下键快速调出曾经使用过的命令
使用clear命令或者Ctrl+L快捷键实现清屏
```

## 八、路径分析

```java
1.@PathVariable使用场景
path: http://localhost:8081/page/12
2.@RequestParam使用场景
path: http://localhost:8081/page?ids=12346545
3.@RequestBody使用场景
path: http://localhost:8081/page
data{
	id:12,
	name:'jack',
	phone:123465789
}
```

## 九、redis优化

### 1.导入maven坐标

```java
<dependency>
       <groupId>org.springframework.boot</groupId>
       <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
```

### 2.配置文件

```properties
#redis配置
spring.redis.host=localhost
spring.redis.port=6379
spring.redis.database=0
```

### 3.缓存菜品数据(查询功能)

实现思路如下：

1）改造DishController的list方法，先从Redis中获取数据，如果有则直接返回，无需查询数据库；如果没有则查询数据库，并将查询的菜品数据放入Redis.

```java
/**
     * 根据条件获取菜品
     * @return
     */
    @GetMapping("/list")
    public R<List<DishDto>> list(Dish dish){

        List<DishDto> dishDtoList = null;
        //动态构造redis存储的key:dish_1397844303408574465_1//根据菜品分类查询
        String key = "dish_" + dish.getCategoryId() + "_" + dish.getStatus();
        //先从redis获取缓存数据
        dishDtoList = (List<DishDto>) redisTemplate.opsForValue().get(key);

        if(dishDtoList != null){
            //如果存在，直接返回，无需查询数据库
            return R.success(dishDtoList);
        }

        //构造查询条件
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(dish.getCategoryId() != null,Dish::getCategoryId,dish.getCategoryId());
        queryWrapper.eq(Dish::getStatus,1);
        //添加排序条件
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);

        List<Dish> dishList = dishService.list(queryWrapper);

        dishDtoList = dishList.stream().map((item) ->{
            DishDto dishDto = new DishDto();
            //对象复制
            BeanUtils.copyProperties(item,dishDto);
            Long categoryId = item.getCategoryId();
            //根据categoryId查询分类对象
            Category category = categoryService.getById(categoryId);
            if(category != null){
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);
            }
            //根据dishId查询菜品对应的口味信息
            Long dishId = item.getId();
            LambdaQueryWrapper<DishFlavor> dishDtoLambdaQueryWrapper = new LambdaQueryWrapper<>();
            dishDtoLambdaQueryWrapper.eq(DishFlavor::getDishId,dishId);
            List<DishFlavor> flavorList = dishFlavorService.list(dishDtoLambdaQueryWrapper);
            dishDto.setFlavors(flavorList);
            return dishDto;
        }).collect(Collectors.toList());

        //如果不存在，则查询数据库，将查询到的菜品数据缓存到redis中(60分钟清理一次缓存)
        redisTemplate.opsForValue().set(key,dishDtoList,60, TimeUnit.MINUTES);

        return R.success(dishDtoList);
    }
```



2）改造DishController的save和update方法，加入清理缓存的逻辑。

```java
    @PutMapping
    public R<String> updateWithFlavor(@RequestBody DishDto dishDto){
        dishService.updateWithFlavor(dishDto);
        //1.清理所有菜品的缓存数据
//        Set keys = redisTemplate.keys("dish_*");
//        redisTemplate.delete(keys);

        //2.精准清理某个分类下的菜品数据
        String key = "dish_" + dishDto.getCategoryId() + "_1";
        redisTemplate.delete(key);

        return R.success("修改菜品成功!");
    }
```



注意事项：

​	在使用缓存过程中，要注意保证数据库中的数据和缓存中的数据一致，如果数据库中的数据发生变化，需要及时清理缓存数据。

## 十、Spring Cache优化

### 1.介绍

Spring Cache是一个框架，实现了基于注解的缓存功能，只需要简单的加一个注解，就能实现缓存功能。

Spring Cache提供了一层抽象，底层可以切换不同的cache实现。具体就是通过CacheManager接口统一不同的缓存技术。

CacheManager是Spring提供的各种缓存技术抽象接口。

针对不同的缓存技术需要不同的CacheManager:

```java
CacheManager                       描述
EhCacheCacheManager                使用EhCache作为缓存技术
GuavaCacheManager                  使用Google的GuavaCache作为缓存技术
RedisCacheManager                  使用Redis作为缓存技术
```

### 2.Spring Cache 常用注解

```
注解               说明
@EnableCaching     开启缓存注解功能
@Cacheable         在方法执行前spring先查看缓存中是否有数据，如果有数据，则直接返回缓存数据；
@CachePut           将方法的返回值放到缓存中
@CacheEvict         将一条或多条数据从缓存中删除
```

在spring boot项目中，使用缓存技术只需要在项目中导入相关缓存技术的依赖包，并在启动类上使用@EnableCaching开启缓存支持即可。

例如：使用Redis作为缓存技术，只需要导入Spring data Redis的maven坐标即可。

### 3.缓存套餐数据



#### 1.导入maven依赖

```java
		<dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-cache</artifactId>
        </dependency>

```

#### 2.配置文件

```properties
#Spring Cache配置设置缓存数据的过期时间大约30分钟
spring.cache.redis.time-to-live=1800000
```

#### 3.启动类配置@EnableCaching

```java
@Slf4j//lombok提供的日志
@SpringBootApplication//启动类
@ServletComponentScan//开启过滤器
@EnableTransactionManagement//开启事务注解
@EnableCaching //开启缓存功能
public class ReggieApplication {
    public static void main(String[] args) {
        SpringApplication.run(ReggieApplication.class,args);
        log.info("项目启动成功!");
    }
}
```

#### 4.代码实现

```java
/**
     * 移动端展示套餐数据
     * @return
     */
    @GetMapping("/list")
    //在方法执行前spring先查看缓存中是否有数据，如果有数据，则直接返回缓存数据；
    @Cacheable(value = "setmealCache",key = "#setmeal.categoryId + '_' + #setmeal.id")
    public R<List<Setmeal>> list(Setmeal setmeal){
        //根据CategoryId和status查询
        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = new LambdaQueryWrapper<>();
        setmealLambdaQueryWrapper.eq(setmeal.getCategoryId() != null,Setmeal::getCategoryId,setmeal.getCategoryId());
        setmealLambdaQueryWrapper.eq(setmeal.getStatus() !=null,Setmeal::getStatus,setmeal.getStatus());
        setmealLambdaQueryWrapper.orderByDesc(Setmeal::getUpdateTime);
        List<Setmeal> setmealList = setmealService.list(setmealLambdaQueryWrapper);
        return R.success(setmealList);
    }



/**
     * 新增套餐
     * @return
     */
    @PostMapping
    //将一条或多条数据从缓存中删除,新增或删除或修改时，需要将套餐下的所有缓存数据删除
    @CacheEvict(value = "setmealCache",allEntries = true)
    public R<String> saveWithDish(@RequestBody SetmealDto setmealDto){
        setmealService.saveWithDish(setmealDto);
        return R.success("新增套餐成功!");
    }

    /**
     * 删除套餐
     * @return
     */
    @DeleteMapping
    //将一条或多条数据从缓存中删除,新增或删除或修改时，需要将套餐下的所有缓存数据删除
    @CacheEvict(value = "setmealCache",allEntries = true)
    public R<String> delete(@RequestParam List<Long> ids){
        setmealService.removeWithDish(ids);
        return R.success("套餐删除成功!");
    }


    /**
     * 修改套餐数据同时修改菜品数据
     * @param setmealDto
     * @return
     */
    @PutMapping
    //将一条或多条数据从缓存中删除,新增或删除或修改时，需要将套餐下的所有缓存数据删除
    @CacheEvict(value = "setmealCache",allEntries = true)
    public R<String> update(@RequestBody SetmealDto setmealDto){
        setmealService.updateWithDish(setmealDto);
        return R.success("修改成功!");
    }
```

## 十一、Mysql主从复制(读写分离)

### 1.介绍

Mysql主从复制是一个异步的复制过程，底层是基于Mysql数据库自带的二进制日志功能。就是一台或多台Mysql数据库(slave,即从库)从另一台Mysql数据库(master,即主库)进行日志的复制然后再解析日志并应用到自身，最终实现从库的数据和主库的数据保持一致。Mysql主从复制是Mysql数据库自带功能，无需借助第三方工具。

Mysql复制过程分成三步：

1）master将改变记录到二进制日志(binary log)

2)slave将master的binary log 拷贝到它的中继日志(relay log)

3)slave重做中继日志的事件，将改变应用到自己的数据库中

<img src="C:\Users\石文学\OneDrive\图片\屏幕快照\屏幕截图 2023-02-15 215026.png" alt="屏幕截图 2023-02-15 215026" style="zoom:50%;" />
