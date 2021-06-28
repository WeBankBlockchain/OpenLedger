## 资金管理

### 开户

账户需要对资金进行存取交易操作，必须先通过资金管理员进行开户，该操作仅对组织管理员，普通账户无权限。

调用示例：

```java
 // 已加入组织且用户状态正常的账户外部地址
String account = "0x693bc024c1127cae3c108f1e2bc49e51b067cc57";
// 获取交易发起方的交易序号
BigInteger nonce = authCenterService.getNonceFromAccount(admin.getAddress()).getResult();
// 对交易参数计算hash
byte[] message = StandardAssetService.computeOpenAccountMsg(account, nonce);
// 对hash进行签名
ECDSASignatureResult sign =OpenLedgerUtils.sign(admin, messageOpenAccount);
// 调用开户接口
ResponseData<Boolean> responseData = assetService.openAccount(account, message,sign);
// 返回操作是否成功 TransferResult：{isSuccess(交易是否成功),termNo(交易所属账期),seqNo(交易记账序列号)}
Boolean isSuccess = responseData.getResult();
```



### 资金存入

开户成功的账户可通过资金管理员进行资金存入，该操作仅对组织管理员，普通账户无权限。

调用示例：

```java
// 组织管理员公私钥对
CryptoKeyPair admin = new CryptoSuite(CryptoType.ECDSA_TYPE, "admin private key").getCryptoKeyPair(); // 存入资金的账户地址
String account = "0x693bc024c1127cae3c108f1e2bc49e51b067cc56";
// 关联资金地址 自定义可为空
String relateAsset="0x693bc024c1127cae3c108f1e2bc49e51b067cc54";
// 存入数量
BigInteger amount = BigInteger.valueOf(100);
// 交易描述 自定义
String detail = "deposit";
// 操作类型 自定义
int operationType=1;
// 关联会计科目 自定义可为空
String subject = "subject";
// 获取交易发起方交易序列号
BigInteger nonce = authCenterService.getNonceFromAccount(admin.getAddress()).getResult();

// 组装交易相关地址列表
List<String> addressList = StandardAssetService.genAddress(null, account, admin.getAddress(), contractAddress, relateAsset);
// 对交易相关所有参数计算hash
byte[] message = StandardAssetService.computeTxMsg(addressList, amount, StandardAssetService.genType(operationType), StandardAssetService.genDetail(detail, null), nonce);
// 对hash进行签名
ECDSASignatureResult sign = OpenLedgerUtils.sign(admin, message);

// 调用资金存入接口
ResponseData<TransferResult> responseData = assetService.deposit(admin.getAddress(), account, amount, operationType, detail,subject,relateAsset, message, sign);
// 获取返回结果 TransferResult：{isSuccess(交易是否成功),termNo(交易所属账期),seqNo(交易记账序列号)}
TransferResult tr = responseData.getResult();
```

### 资金提取

开户成功的账户可通过资金管理员进行资金存入，该操作仅对组织管理员，普通账户无权限。

调用示例：

```java
// 组织管理员公私钥对
CryptoKeyPair admin = new CryptoSuite(CryptoType.ECDSA_TYPE, "admin private key").getCryptoKeyPair(); // 提取资金的账户地址
String account = "0x693bc024c1127cae3c108f1e2bc49e51b067cc56";
// 关联资金地址 自定义可为空
String relateAsset="0x693bc024c1127cae3c108f1e2bc49e51b067cc54";
// 存入数量
BigInteger amount = BigInteger.valueOf(100);
// 交易描述 自定义
String detail = "withDrawal";
// 操作类型 自定义
int operationType=2;
// 关联会计科目 自定义可为空
String subject = "subject";
// 获取交易发起方交易序列号
BigInteger nonce = authCenterService.getNonceFromAccount(admin.getAddress()).getResult();

// 组装交易相关地址列表
List<String> addressList = StandardAssetService.genAddress(account, null, admin.getAddress(), contractAddress, relateAsset);
// 对交易相关所有参数计算hash
byte[] message = StandardAssetService.computeTxMsg(addressList, amount, StandardAssetService.genType(operationType), StandardAssetService.genDetail(detail, null), nonce);
// 对hash进行签名
ECDSASignatureResult sign = OpenLedgerUtils.sign(admin, message);
// 调用资金提取接口
ResponseData<TransferResult> responseData = assetService.withDrawal(admin.getAddress(), account, amount, operationType, detail,subject,relateAsset, message, sign);
// 获取返回结果 TransferResult：{isSuccess(交易是否成功),termNo(交易所属账期),seqNo(交易记账序列号)}
TransferResult tr = responseData.getResult();
```



### 资金交易

持有资金的账户都有权限进行资金交易。

调用示例：

```java
// 交易发起方公私钥对 可以是交易的转出方以及组织管理员
CryptoKeyPair operator = new CryptoSuite(CryptoType.ECDSA_TYPE, "operator private key").getCryptoKeyPair(); 
// 交易转出方账户外部地址
String fromAddress = "0x698bc024c1127cae3c108f1e2bc49e51b067cc56"
// 交易收入方账户外部地址
String toAddress = "0x793bd024c1127cae3c108f1e2bc49e51b067cc56";
// 交易发起方账户外部地址
String operatorAddress = operator.getAddress();
// 关联资金地址 自定义可为空
String relateAsset="0x693bc024c1127cae3c108f1e2bc49e51b067cc54";
// 交易数量
BigInteger amount = BigInteger.valueOf(10);
// 交易描述 自定义
String detail = "transfer";
// 操作类型 自定义
int operationType=3;
// 关联会计科目 自定义可为空
String subject = "subject";
//获取交易发起方的交易序列号
BigInteger nonce = authCenterService.getNonceFromAccount(operatorAddress).getResult();

// 计算所有参数的hash
byte[] message = StandardAssetService.computeTxMsg(StandardAssetService.genAddress(fromAddress, toAddress, operatorAddress, contractAddress, relateAsset), amount, StandardAssetService.genType(operationType), StandardAssetService.genDetail(detail, null), nonce);
// 对hash进行签名
ECDSASignatureResult sign = OpenLedgerUtils.sign(operator, message);

// 调用交易接口
ResponseData<TransferResult> responseData = assetService.transfer(operatorAddress, fromAddress, toAddress, amount, operationType, detail,subject,relateAsset, message,sign);
// 获取返回结果
TransferResult tr = responseData.getResult();
```

### 查询账户余额

请参考[资产查询账户余额](./AssetManagement.html#id9) 搭建。

### 查询账户列表

请参考[资产查询账户列表](AssetManagement.html#id10)搭建。

### 查询资金总额

请参考[资产查询总额](AssetManagement.html#id11)搭建。

### 查询台账

请参考[资产查询台账](./AssetManagement.html#id12)搭建。

### 增加账本号

请参考[资产增加账本号](AssetManagement.html#id13)搭建。

