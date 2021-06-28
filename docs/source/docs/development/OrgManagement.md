## 组织管理

### 创建组织

调用示例：

```java
// 合约地址
String projectAddr = "0x9a27d2e748799ae6b929de2e69745fc8a5fe8e31";
// 区块链配置管理对象
BlockChain blockchain = new Blockchain("application.properties");
// 加载projectService
ProjectService<Project> projectService= new ProjectService(blockchain, projectAddr);
//调用创建组织接口
ResponseData<String> ret = projectService.createOrganization();
//返回组织地址
String orgAddr =ret.getResult();
```



### 配置组织默认权限

调用示例：

```java
// 授权管理地址
String authManagerAddr= "0x7a27d2e748799ae6b929de2e69745fc8a5fe8e31";
// 组织地址
String orgAddr="0x8a27d2e748799ae6b929de2e69745fc8a5fe8e31";
// 授权中心地址
String authCenterAddr="0x6a27d2e748799ae6b929de2e69745fc8a5fe8e31";
// 调用设置默认权限接口，返回调用是否成功
Boolean isSuccess=projectService.addOrgDefaultAuth(authManagerAddr, orgAddr).getResult();
// 调用设置默认权限列表接口，返回调用是否成功
isSuccess=projectService.addDefaultKeyType(authCenterAddr).getResult();
```

### 账户管理


#### 创建组织管理员账户

仅项目管理员具有创建组织管理员账户权限。

- 1.管理员账户未创建，创建账户并把该账户添加到组织管理员列表。

  调用示例：

```java
// 组织地址
String orgAddr="0x8a27d2e748799ae6b929de2e69745fc8a5fe8e31";
 /**
  *  账户外部地址 可通过如下方式创建：
  *  CryptoSuite ecdsaCryptoSuite = new CryptoSuite(CryptoType.ECDSA_TYPE);
  *  String adminAddr = ecdsaCryptoSuite.createKeyPair().getAddress();
  */
String adminAddr = "0x5a27d2e748799ae6b929de2e69745fc8a5fe8e31";
// 组织管理员公私钥对
CryptoSuite org1Admin = new CryptoSuite(CryptoType.ECDSA_TYPE, "admin private key");
// 账户资料 eg: username: aaaa
LinkedHashMap<String, String> kvMap = new LinkedHashMap<>();
// 组装参数并对参数hash
List<byte[]> keyList = new ArrayList<>();
List<byte[]> valueList = new ArrayList<>();
List<byte[]> kvList = new ArrayList<>();
for(Map.Entry<String,String> entry : kvMap.entrySet()){
     byte[] k = entry.getKey().getBytes(StandardCharsets.UTF_8);
     byte[] v = entry.getValue().getBytes(StandardCharsets.UTF_8);
     keyList.add(k);
     valueList.add(v);
     kvList.add(k);
     kvList.add(v);
}

// 调用创建管理员账户接口
ResponseData<String> ret = projectService.createAddOrgAdmin(orgAddr, adminAddr, keyList, valueList);
//管理员账户内部地址 
String adminInnerAddr = ret.getResult();
```

- 管理员账户已创建，添加该账户到组织管理员列表。

  调用示例：

```java
// 组织地址
String orgAddr="0x8a27d2e748799ae6b929de2e69745fc8a5fe8e31";
// 账户外部地址
String adminAddr = "0x5a27d2e748799ae6b929de2e69745fc8a5fe8e31";
// 调用添加管理员接口
ResponseData<Boolean> ret = projectService.addOrgAdmin(orgAddr, adminAddr);
// 返回调用是否成功
Boolean isSucces = ret.getResult();
```

#### 创建普通账户

仅项目管理员以及组织管理员具有创建组织普通账户权限。

调用示例：

```java
// 组织地址
String orgAddr="0x8a27d2e748799ae6b929de2e69745fc8a5fe8e31";
 /**
  *  账户外部地址 可通过如下方式创建：
  *  CryptoSuite ecdsaCryptoSuite = new CryptoSuite(CryptoType.ECDSA_TYPE);
  *  String accountAddr = ecdsaCryptoSuite.createKeyPair().getAddress();
  */
String accountAddr = "0x5b27d2e748799ae6b929de2e69745fc8a5fe8e31"; 
// 授权中心地址
String authCenterAddr="0x6a27d2e748799ae6b929de2e69745fc8a5fe8e31";
// 组织管理员公私钥对
CryptoSuite org1Admin = new CryptoSuite(CryptoType.ECDSA_TYPE, "admin private key");

// 封装合约所需参数 参考创建组织管理员账户
// 组装参数并对参数hash
List<byte[]> keyList = new ArrayList<>();
List<byte[]> valueList = new ArrayList<>();
List<byte[]> kvList = new ArrayList<>();
for(Map.Entry<String,String> entry : kvMap.entrySet()){
     byte[] k = entry.getKey().getBytes(StandardCharsets.UTF_8);
     byte[] v = entry.getValue().getBytes(StandardCharsets.UTF_8);
     keyList.add(k);
     valueList.add(v);
     kvList.add(k);
     kvList.add(v);
}


// 加载authCenterService
AuthCenterService authCenterService =  new AuthCenterService<>(blockchain, authCenterAddr);
// 加载orgService
OrganizationService<Organization> orgService = new OrganizationService(blockchain, orgAddr);

// 获取交易调用方交易序号
BigInteger nonce = authCenterService.getNonceFromAccount(org1Admin.getAddress()).getResult();
// 组装交易参数
byte[] args = OpenLedgerUtils.concatByte(OpenLedgerUtils.convertStringToAddressByte(accountAddr),
                OpenLedgerUtils.concatByte(kvList),
                OpenLedgerUtils.getBytes32(nonce.toByteArray()));
// 对交易参数进行hash
byte[] message = OpenLedgerUtils.computeKeccak256Hash(args);
// 使用交易调用方私钥对hash的参数进行签名
ECDSASignatureResult sign = OpenLedgerUtils.sign(org1Admin.getCryptoKeyPair(), message);

// 调用创建普通账户接口
ResponseData<String> ret = this.orgService.createAccount(accountAddr, keyList, valueList, message, sign);
// 获取账户内部地址
String accountInnerAddress = ret.getResult();
```



#### 冻结/解冻账户

##### 1.冻结账户

调用示例：

```java
// 获取交易调用方交易序号
BigInteger nonce = authCenterService.getNonceFromAccount(org1Admin.getCryptoKeyPair().getAddress()).getResult();
// 组装交易参数 accountAddr：冻结账户地址
byte[] args = OpenLedgerUtils.concatByte(OpenLedgerUtils.convertStringToAddressByte(accountAddr),
                OpenLedgerUtils.getBytes32(nonce.toByteArray()));
//对交易参数进行hash
byte[] message = OpenLedgerUtils.computeKeccak256Hash(args);
// 使用交易调用方私钥对hash的参数进行签名
ECDSASignatureResult sign = OpenLedgerUtils.sign(org1Admin.getCryptoKeyPair(), message);
// 调用冻结接口
ResponseData<Boolean> ret = this.orgService.freeze(accountAddr, message, sign);
// 返回操作是否成功
Boolean isSuccess = ret.getResult();       
```



##### 2.解冻账户

调用示例：

```java
// 获取交易调用方交易序号
BigInteger nonce = authCenterService.getNonceFromAccount(org1Admin.getCryptoKeyPair().getAddress()).getResult();
// 组装交易参数 accountAddr：解冻账户地址
byte[] args = OpenLedgerUtils.concatByte(OpenLedgerUtils.convertStringToAddressByte(accountAddr),
                OpenLedgerUtils.getBytes32(nonce.toByteArray()));
//对交易参数进行hash
byte[] message = OpenLedgerUtils.computeKeccak256Hash(args);
// 使用交易调用方私钥对hash的参数进行签名
ECDSASignatureResult sign = OpenLedgerUtils.sign(org1Admin.getCryptoKeyPair(), message);

// 调用解冻接口
ResponseData<Boolean> ret = this.orgService.unfreeze(accountAddr, message, sign);
// 返回操作是否成功
Boolean isSuccess = ret.getResult();      
```



#### 注销账户

调用示例：

```java
// 获取交易调用方交易序号
BigInteger nonce = authCenterService.getNonceFromAccount(org1Admin.getCryptoKeyPair().getAddress()).getResult();
// 组装交易参数 accountAddr：注销账户地址
byte[] args = OpenLedgerUtils.concatByte(OpenLedgerUtils.convertStringToAddressByte(accountAddr),
                OpenLedgerUtils.getBytes32(nonce.toByteArray()));
//对交易参数进行hash
byte[] message = OpenLedgerUtils.computeKeccak256Hash(args);
// 使用交易调用方私钥对hash的参数进行签名
ECDSASignatureResult sign = OpenLedgerUtils.sign(org1Admin.getCryptoKeyPair(), message);

// 调用注销接口
ResponseData<Boolean> ret = this.orgService.unfreeze(accountAddr, message, sign);
// 返回操作是否成功
Boolean isSuccess = ret.getResult(); 
```



#### 重置账户地址

当用户丢失原本账户公私钥或者需要重新设置公私钥，可调用重置账户地址接口。

调用示例：

```java
 /**
  *  重置账户外部地址 可通过如下方式创建：
  *  CryptoSuite ecdsaCryptoSuite = new CryptoSuite(CryptoType.ECDSA_TYPE);
  *  CryptoKeyPair newAccount = ecdsaCryptoSuite.createKeyPair()；
  */
CryptoKeyPair newAccount =new CryptoSuite(CryptoType.ECDSA_TYPE, "new Account private key").getCryptoKeyPair();
// 原账户地址
String accountAddr = "0x8927d2e748799ae6b929de2e69745fc8a5fe8e31";
// 获取交易调用方交易序号
BigInteger nonce = authCenterService.getNonceFromAccount(org1Admin.getCryptoKeyPair().getAddress()).getResult();
// 组装交易参数 accountAddr：注销账户地址
byte[] args = OpenLedgerUtils.concatByte(OpenLedgerUtils.convertStringToAddressByte(accountAddr),
               OpenLedgerUtils.convertStringToAddressByte(newAccount.getAddress()));
args = OpenLedgerUtils.concatByte(args,
                OpenLedgerUtils.getBytes32(nonce.toByteArray()));
//对交易参数进行hash
byte[] message = OpenLedgerUtils.computeKeccak256Hash(args);
// 使用交易调用方私钥对hash的参数进行签名
ECDSASignatureResult sign = OpenLedgerUtils.sign(org1Admin.getCryptoKeyPair(), message);

// 调用重置账户地址接口
ResponseData<Boolean> ret =orgService.changeExternalAccount(accountAddr,newAccount.getAddress(), message, sign);
// 返回操作是否成功
Boolean isSuccess = ret.getResult(); 
```



### 资产管理

#### 创建资产

组织管理员可创建通过调用创建资产接口，创建同质化资产与非同质化资产。

调用示例：

```
String assetName = "asset";
// 是否为同质化资产
Boolean isFungible = false;
BigInteger nonce = authCenterService.getNonceFromAccount(admin.getAddress()).getResult();

byte[] args = OpenLedgerUtils.concatByte(OpenLedgerUtils.convertStringToAddressByte(admin.getAddress()),
        OpenLedgerUtils.getBytes32(nonce.toByteArray()));
byte[] message = OpenLedgerUtils.computeKeccak256Hash(args);
ECDSASignatureResult sign = OpenLedgerUtils.sign(admin, message);
// 返回资产合约地址
ResponseData<String> ret = this.orgService.createAsset(admin.getAddress(), assetName, isFungible, message, sign);
```



#### 创建资金

资金是项目中特殊的资产，作为项目中具备价格属性的等价物资产，在项目中有且只有一个。

调用示例：

```java
// 名称
String name ="mymoney";
// 缩写
String symbol="abc";
// 精度设置
BigInteger decimal = BigInteger.valueOf(2);
// 返回资金合约地址
ResponseData<String> responseData = orgService.createCurrency(name,symbol,decimal);

```



