## 基础信息管理

基础信息管理主要基于KV结构进行存储跟查询信息，目前支持`String` 、`byte[]`、`int`、`double`、`bean对象`、`List<Bean>`等作为Value的类型。

### 插入信息

插入信息接口将强制覆盖key。

```java
// 交易发起方公私钥对 
CryptoKeyPair operator = new CryptoSuite(CryptoType.ECDSA_TYPE, "operator private key").getCryptoKeyPair(); 
String key = "test";
String value = "testValue";
// 获取交易发起方的交易序列号
BigInteger nonce = (BigInteger) authCenterSDK.getNonceFromAccount(operator.getAddress()).getResult();
// 对参数进行hash
byte[] message = IdentitySDK.genHashByte(key, value, nonce);
// 对hash进行签名
ECDSASignatureResult sign = OpenLedgerUtils.sign(operator, message);
//调用插入数据接口
ResponseData<Boolean> responseData = identitySDK.insertWithSignatureResult(key, value, message,sign);
// 返回操作是否成功
Boolean isSuccess = responseData.getResult();
```



### 添加信息

添加信息接口只能添加不存在的key。

```java
// 交易发起方公私钥对 
CryptoKeyPair operator = new CryptoSuite(CryptoType.ECDSA_TYPE, "operator private key").getCryptoKeyPair(); 
String key = "test";
String value = "testValue";
// 获取交易发起方的交易序列号
BigInteger nonce = (BigInteger) authCenterSDK.getNonceFromAccount(operator.getAddress()).getResult();
// 对参数进行hash
byte[] message = IdentitySDK.genHashByte(key, value, nonce);
// 对hash进行签名
ECDSASignatureResult sign = OpenLedgerUtils.sign(operator, message);
//调用添加数据接口
ResponseData<Boolean> responseData = identitySDK.addWithSignatureResult(key, value,message, sign);
// 返回操作是否成功
Boolean isSuccess = responseData.getResult();
```



### 更新信息

更新信息只能更新已存在的key。

```java
// 交易发起方公私钥对 
CryptoKeyPair operator = new CryptoSuite(CryptoType.ECDSA_TYPE, "operator private key").getCryptoKeyPair(); 
String key = "test";
String value = "testValue";
// 获取交易发起方的交易序列号
BigInteger nonce = (BigInteger) authCenterSDK.getNonceFromAccount(operator.getAddress()).getResult();
// 对参数进行hash
byte[] message = IdentitySDK.genHashByte(key, value, nonce);
// 对hash进行签名
ECDSASignatureResult sign = OpenLedgerUtils.sign(operator, message);
//调用更新数据接口
ResponseData<Boolean> responseData = identitySDK.setWithSignatureResult(key, value, message,sign);
// 返回操作是否成功
Boolean isSuccess = responseData.getResult();
```



### 根据key查询数据

#### 1.基础数据类型以及对象查询

```java
// 交易发起方公私钥对 
CryptoKeyPair operator = new CryptoSuite(CryptoType.ECDSA_TYPE, "operator private key").getCryptoKeyPair(); 
// 获取交易发起方的交易序列号
BigInteger nonce = (BigInteger) authCenterSDK.getNonceFromAccount(operator.getAddress()).getResult();
// 查询key值
String key="test";
// 拼接参数并获取hash
byte[] message = IdentitySDK.genHashByte(key, nonce);
// 对hash进行签名
ECDSASignatureResult sign = OpenLedgerUtils.sign(operator, message);
// 调用查询接口 返回查询结果
String valueObj = (String) identitySDK.getWithSignatureResult(key, message, sign);
```

#### 2.List类型查询

```java
// 交易发起方公私钥对 
CryptoKeyPair operator = new CryptoSuite(CryptoType.ECDSA_TYPE, "operator private key").getCryptoKeyPair(); 
// 获取交易发起方的交易序列号
BigInteger nonce = (BigInteger) authCenterSDK.getNonceFromAccount(operator.getAddress()).getResult();
// 查询key值
String key="test";
// 拼接参数并获取hash
byte[] message = IdentitySDK.genHashByte(key, nonce);
// 对hash进行签名
ECDSASignatureResult sign = OpenLedgerUtils.sign(operator, message);
// 调用查询接口 以List<Bean>为例 返回查询结果
List<User> valueObj = (List<User>) identitySDK.getWithSignatureResult(key, message, sign, new TypeReference<List<User>>() {
        });
```

#### 3.byte[]类型查询

```java
// 交易发起方公私钥对 
CryptoKeyPair operator = new CryptoSuite(CryptoType.ECDSA_TYPE, "operator private key").getCryptoKeyPair(); 
// 获取交易发起方的交易序列号
BigInteger nonce = (BigInteger) authCenterSDK.getNonceFromAccount(operator.getAddress()).getResult();
// 查询key值
String key="test";
// 拼接参数并获取hash
byte[] message = IdentitySDK.genHashByte(key, nonce);
// 对hash进行签名
ECDSASignatureResult sign = OpenLedgerUtils.sign(operator, message);
// 调用查询接口 返回查询结果
byte[] valueObj =  identitySDK.getByteWithSignatureResultWith(key, message, sign);
```

​     

### 移除key

```java
// 交易发起方公私钥对 
CryptoKeyPair operator = new CryptoSuite(CryptoType.ECDSA_TYPE, "operator private key").getCryptoKeyPair(); 
// 获取交易发起方的交易序列号
BigInteger nonce = (BigInteger) authCenterSDK.getNonceFromAccount(operator.getAddress()).getResult();
// 移除key值
String key="test";
// 拼接参数并获取hash
byte[] message = IdentitySDK.genHashByte(key, nonce);
// 对hash进行签名
ECDSASignatureResult sign = OpenLedgerUtils.sign(operator, message);
// 调用移除接口 返回移除结果
ResponseData<Boolean> responseData = identitySDK.removeWithSignatureResult(key, message, sign);
```

