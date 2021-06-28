## 权限管理



### 获取所有权限操作列表

调用示例：

```java
// 调用获取所有权限操作列表接口
ResponseData<HashMap> rsp = authCenterService.getAllKeyType();
// 返回权限map：<操作,具有该操作权限的用户组>
HashMap<String,String> result = rsp.getResult();
```

> 操作权限用户组主要分为3种，分别是管理员(admin)具有所有权限，账户主人（owner）具有可全量读，部分写权限，其他账户(public)具有可读不可写。

### 添加操作权限

向某个用户组添加操作权限，仅有权限管理合约(AuthCenter)的拥有方，也就是部署用户有权限操作。

调用示例：

```java
String key = "send";
// 设置"send"权限的用户组为owner
String type=ProjectService.TYPE_OWNER;
// 调用添加操作权限的接口
ResponseData<Boolean> rsp = authCenterService.addKeyType(key.getBytes(), type.getBytes());
// 返回操作是否成功
Boolean isSuccess=rsp.getResult();
```

### 删除权限

删除某权限操作，仅有权限管理合约(AuthCenter)的拥有方，也就是部署用户有权限操作。

调用示例：

```java
// 操作名
String key = "send";
// 调用删除权限接口
ResponseData<Boolean> rsp = authCenterService.removeKeyType(key.getBytes());
// 返回操作是否成功
Boolean isSuccess=rsp.getResult();
```

