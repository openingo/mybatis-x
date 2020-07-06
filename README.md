### MyBatis(Plus) Extensions

![maven](https://img.shields.io/maven-central/v/org.openingo.kits/mybatis-x.svg)

```xml
<dependency>
  <groupId>org.openingo.kits</groupId>
  <artifactId>mybatis-x</artifactId>
  <version>new_version</version>
</dependency>
```

#### features

- MpModelExt: 针对MyBatis-Plus的Model扩展

#### 使用方式

- generator配置

```java
// 策略配置
StrategyConfig strategy = new StrategyConfig();
strategy.setNaming(NamingStrategy.underline_to_camel);
strategy.setColumnNaming(NamingStrategy.underline_to_camel);
strategy.setSuperEntityClass(MpModelExt.class); // MpModelExt在这里
```

#### 示例

> 以UserDO举例
>
> ```java
> @TableName("t_user")
> public UserDO extends MpModelExt<UserDO> {
>   private static final long serialVersionUID=1L;
>   
>   @TableId(value = "user_id", type = IdType.AUTO)
>   private Integer userId;
>   
>   private String userName;
>   
>   private Integer sex;
> }
> ```
>
> 

- doQuery

  ```java
  // sql: select user_id, user_name from t_user where user_name = 'qicz';
  UserDO.dao(UserDO.class).eq(UserDO::getUserName, "qicz").eq(UserDO::getSex: 1).select(UserDO::getUserId, UserDO::getUserName).doQuery();
  ```

  > select输出的位置不限

- doQueryLimitOne

  ```java
  // sql: select user_id, user_name from t_user where user_name = 'qicz' limit 1;
  UserDO.dao(UserDO.class).eq(UserDO::getUserName, "qicz").eq(UserDO::getSex: 1).select(UserDO::getUserId, UserDO::getUserName).doQueryLimitOne();
  ```

  > 【暂仅支持MySQL】会自动在queryWrapper拼接`LIMIT 1`，不影响添加`LIMIT 1`之后的其他操作(MyBatis-Plus只能进行一次last操作的问题解决)。
                                                                                                                                                                                                                                                                         
- doQueryOne

  ```java
  // sql: select user_id, user_name from t_user where user_name = 'qicz';
  UserDO.dao(UserDO.class).eq(UserDO::getUserName, "qicz").eq(UserDO::getSex: 1).select(UserDO::getUserId, UserDO::getUserName).doQueryOne();
  ```

  > 当查询结果大于1条时，会有异常。

- doUpdate

  ```java
  // sql: update t_user set user_name = 'qicz-new-name' where user_name = 'qicz' and sex = 1;
  UserDO.dao(UserDO.class).eq(UserDO::getUserName, "qicz").eq(UserDO::getSex: 1).set(UserDO::getUserName, "qicz-new-name").doUpdate();
  ```

- doDelete

  ```java
  // sql: delete from t_user where user_name = 'qicz' and sex = 1;
  UserDO.dao(UserDO.class).eq(UserDO::getUserName, "qicz").eq(UserDO::getSex: 1).doDelete();
  ```

- 先doQuery再doUpdate或doDelete

  ```java
  // do query
  // sql: select user_id, user_name from t_user where user_name = 'qicz';
  UserDO userDO = UserDO.dao(UserDO.class).eq(UserDO::getUserName, "qicz").select(UserDO::getUserId, UserDO::getUserName).doQuery();
  
  // do delete
  // sql: delete from t_user where user_name = 'qicz' and sex = 1;
  userDO.eq(UserDO::getSex: 1).doDelete();
  
  // do update
  // sql: update t_user set user_name = 'qicz-new-name' where user_name = 'qicz' and sex = 1;
  userDO.set(UserDO::getUserName, "qicz-new-name").doUpdate();
  ```
  
- by

  ```java
  // sql: select user_id, user_name from t_user where user_name = 'qicz' and sex = 1;
  UserDO userDO = UserDO.dao(UserDO.class);
  userDO.setUserName("qicz");
  userDO.setSex(1);
  userDO.by(userDO).select(UserDO::getUserId, UserDO::getUserName).doQuery();
  ```

  

