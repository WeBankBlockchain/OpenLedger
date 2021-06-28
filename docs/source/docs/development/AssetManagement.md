## 账本管理

OpenLedger支持基于余额数量变化的记账模式和基于所有权变化的记账模式。

###  余额数量账目管理

#### 登记账户

在记账前需要登记账户,该操作仅对组织管理员，普通账户无权限。

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
// 调用登记账户接口
ResponseData<Boolean> responseData = assetService.openAccount(account, message,sign);
// 返回操作是否成功 TransferResult：{isSuccess(交易是否成功),termNo(交易所属账期),seqNo(交易记账序列号)}
Boolean isSuccess = responseData.getResult();
```



#### 资产存入

当账户资产发生数量增加时,可调用登记资产存入接口登记到账本，该操作仅对组织管理员，普通账户无权限。

调用示例：

```java
// 组织管理员公私钥对
CryptoKeyPair admin = new CryptoSuite(CryptoType.ECDSA_TYPE, "admin private key").getCryptoKeyPair();
// 存入资产的账户地址
String account = "0x693bc024c1127cae3c108f1e2bc49e51b067cc56";
// 存入数量
BigInteger amount = BigInteger.valueOf(100);
// 交易描述 自定义
String detail = "deposit";
// 操作类型 自定义
int operationType=1;
// 获取交易发起方交易序列号
BigInteger nonce = authCenterService.getNonceFromAccount(admin.getAddress()).getResult();

// 组装交易相关地址列表
List<String> addressList = StandardAssetService.genAddress(null, account, admin.getAddress(), contractAddress, null);
// 对交易相关所有参数计算hash
byte[] message = StandardAssetService.computeTxMsg(addressList, amount, StandardAssetService.genType(operationType), StandardAssetService.genDetail(detail, null), nonce);
// 对hash进行签名
ECDSASignatureResult sign = OpenLedgerUtils.sign(admin, message);

// 调用资产存入接口
ResponseData<TransferResult> responseData = assetService.deposit(admin.getAddress(), account, amount, operationType, detail, message, sign);
// 获取返回结果 TransferResult：{isSuccess(交易是否成功),termNo(交易所属账期),seqNo(交易记账序列号)}
TransferResult tr = responseData.getResult();
```

#### 资产提取

当账户资产数量发生减少时,可调用登记资产提取接口登记到账本，该操作仅对组织管理员，普通账户无权限。

调用示例：

```java
// 组织管理员公私钥对
CryptoKeyPair admin = new CryptoSuite(CryptoType.ECDSA_TYPE, "admin private key").getCryptoKeyPair(); // 提取资产的账户地址
String account = "0x693bc024c1127cae3c108f1e2bc49e51b067cc56";
// 存入数量
BigInteger amount = BigInteger.valueOf(100);
// 交易描述 自定义
String detail = "withdrawal";
// 操作类型 自定义
int operationType=2;
// 获取交易发起方交易序列号
BigInteger nonce = authCenterService.getNonceFromAccount(admin.getAddress()).getResult();

// 组装交易相关地址列表
List<String> addressList = StandardAssetService.genAddress(account, null, admin.getAddress(), contractAddress, null);
// 对交易相关所有参数计算hash
byte[] message = StandardAssetService.computeTxMsg(addressList, amount, StandardAssetService.genType(operationType), StandardAssetService.genDetail(detail, null), nonce);
// 对hash进行签名
ECDSASignatureResult sign = OpenLedgerUtils.sign(admin, message);
// 调用资产提取接口
ResponseData<TransferResult> responseData = assetService.withdrawal(admin.getAddress(), account, amount, operationType, detail, message, sign);
// 获取返回结果 TransferResult：{isSuccess(交易是否成功),termNo(交易所属账期),seqNo(交易记账序列号)}
TransferResult tr = responseData.getResult();
```



#### 资产转移

当发生两个账户之间资产数量转移时,可调用资产转移接口登记到账本。

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
// 交易数量
BigInteger amount = BigInteger.valueOf(10);
// 交易描述 自定义
String detail = "transfer";
// 操作类型 自定义
int operationType=3;
//获取交易发起方的交易序列号
BigInteger nonce = authCenterService.getNonceFromAccount(operatorAddress).getResult();

// 计算所有参数的hash
byte[] message = StandardAssetService.computeTxMsg(StandardAssetService.genAddress(fromAddress, toAddress, operatorAddress, contractAddress, null), amount, StandardAssetService.genType(operationType), StandardAssetService.genDetail(detail, null), nonce);
// 对hash进行签名
ECDSASignatureResult sign = OpenLedgerUtils.sign(operator, message);

// 调用交易接口
ResponseData<TransferResult> responseData = assetService.transfer(operatorAddress, fromAddress, toAddress, amount, operationType, detail, message,sign);
// 获取返回结果
TransferResult tr = responseData.getResult();
```

#### 查询账户余额

持有资产的账户都有权限进行账户余额查询。

调用示例：

```java
// 交易发起方公私钥对 可以是资产拥有方以及组织管理员
CryptoKeyPair operator = new CryptoSuite(CryptoType.ECDSA_TYPE, "operator private key").getCryptoKeyPair(); 
// 查询余额账户
String account = "0x698bc024c1127cae3c108f1e2bc49e51b067cc56"
// 获取交易发起方交易序号
BigInteger nonce = authCenterService.getNonceFromAccount(operator.getAddress()).getResult();
// 组装参数并进行hash
byte[] message = OpenLedgerUtils.computeKeccak256HashFromBigInteger(nonce);
// 对hash进行签名
ECDSASignatureResult sign = OpenLedgerUtils.sign(operator, message);
// 调用查询余额接口 获取资产余额
BigInteger balance = assetService.getBalance(account, message, sign);
```



#### 查询账户列表

获取资产的账户列表，仅组织管理员有权限进行操作。

调用示例：

```java
// 组织管理员公私钥对 
CryptoKeyPair admin = new CryptoSuite(CryptoType.ECDSA_TYPE, "admin private key").getCryptoKeyPair(); 
// 获取交易发起方交易序号
BigInteger nonce = authCenterService.getNonceFromAccount(admin.getAddress()).getResult();
// 组装参数并进行hash
byte[] message = OpenLedgerUtils.computeKeccak256HashFromBigInteger(nonce); 
// 对hash进行签名
ECDSASignatureResult sign = OpenLedgerUtils.sign(admin, message);
// 调用查询账户列表接口 获取账户地址列表
List<String> accountList = assetService.getHolders(message, sign);
```



#### 查询资产总额

获取资产总额，仅组织管理员有权限进行操作。

调用示例：

```java
// 组织管理员公私钥对 
CryptoKeyPair admin = new CryptoSuite(CryptoType.ECDSA_TYPE, "admin private key").getCryptoKeyPair(); 
// 获取交易发起方交易序号
BigInteger nonce = authCenterService.getNonceFromAccount(admin.getAddress()).getResult();
// 组装参数并进行hash
byte[] message = OpenLedgerUtils.computeKeccak256HashFromBigInteger(nonce); 
// 对hash进行签名
ECDSASignatureResult sign = OpenLedgerUtils.sign(admin, message);
// 调用查询账户列表接口 获取账户地址列表
BigInteger totalBalance = assetService.getTotalBalance(message, sign);
```



#### 查询台账

OpenLedger支持多维查询账本,，其中参数主要包括账期号、交易序列号、转出账户地址、存入账户地址，转出以及存入账户具有查询个人账户的账本记录的权限，组织管理员具有查询全部台账记录的权限。

调用示例：

```java
// 交易发起方公私钥对 可以是资产拥有方以及组织管理员
CryptoKeyPair operator = new CryptoSuite(CryptoType.ECDSA_TYPE, "operator private key").getCryptoKeyPair(); 

//账期号
BigInteger termNo = BigInteger.valueOf(1);
// 交易记账序列号
BigInteger seqNo = BigInteger.valueOf(1);
// 交易转出方账户外部地址
String fromAddress = "0x698bc024c1127cae3c108f1e2bc49e51b067cc56"
// 交易接收方账户外部地址
String toAddress = "0x793bd024c1127cae3c108f1e2bc49e51b067cc56";

/**
 * 构建查询条件对象 其中4个参数在保证存在一个非空的参数值的前提下，其余皆可为空。
 *  查询某账期的所有记录
 *  eg: Condition condition = new Condition(termNo, null, null, null);
 *  查询某账户所有转出记录
 *  eg: Condition condition = new Condition(null, null, fromAddress, null);
 *  查询某账户所有收入记录
 *  eg: Condition condition = new Condition(null, null, null, toAddress);
 *  查询某两个账户之间的所有交易记录
 *  eg: Condition condition = new Condition(null, null, fromAddress, toAddress);
 *  根据交易记账序列号查询某条交易记录
 *  eg: Condition condition = new Condition(null, seqNo, null, null);
 *   设置返回记录范围，默认返回前10条
 *  List<BigInteger> limits = new ArrayList<>();
 *  limits.add(BigInteger.valueOf(0));limits.add(BigInteger.valueOf(100));
 *  Conditon condition = new Condition(termNo,null,from,null,limits);
*/
Condition condition = new Condition(termNo, seqNo, fromAddress, toAddress);
// 获取交易发起方的交易序列号
BigInteger nonce = authCenterService.getNonceFromAccount(admin.getAddress()).getResult();
// 对序列号进行hash
byte[] message = OpenLedgerUtils.computeKeccak256HashFromBigInteger(nonce);
// 对hash进行签名
ECDSASignatureResult rs = OpenLedgerUtils.sign(admin, message);
// 调用查询接口获取返回
List<RecordEntity> recordEntities = assetService.query(condition, message, rs);
```

#### 增加账本号

当账本记录达到一定数量，比如100，从查询优化的角度可通过调用增加账本号接口方式提升查询效率，该操作仅管理员具备权限。

调用示例：

```java
BigInteger nonce = authCenterSDK.getNonceFromAccount(admin.getAddress()).getResult();
byte[] message = OpenLedgerUtils.computeKeccak256HashFromBigInteger(nonce);
ResponseData<BigInteger> responseData = assetService.addBook(message, OpenLedgerUtils.sign(admin, message));
```

#### 获取资产基本信息

资产基本信息主要包括价格(price)以及利率(rate)。

调用示例：

```java
// 资产合约地址 通过组织createAsse接口创建
String contractAddress="0x7a3bc024c1127cae3c108f1e2bc49e51b067cc57";
// 加载区块链信息
Blockchain blockchain = new Blockchain("application.properties");
// 加载assetService
FungibleAssetService assetService = new FungibleAssetService(blockchain, contractAddress);
// 调用获取资产信息接口
AssetEntity assetEntity = assetService.getAssetInfo();
// 获取价格
BigInteger price = assetEntity.getPrice();
// 获取利率
BigInteger rate = assetEntity.getRate();
```



#### 设置资产价格

仅有组织管理员组下的用户有权限操作。

调用示例：

```java
// 组织管理员公私钥对
CryptoKeyPair admin = new CryptoSuite(CryptoType.ECDSA_TYPE, "admin private key").getCryptoKeyPair();
// 资产价格
BigInteger assetPrice = BigInteger.valueOf(100);
// 获取交易发起方交易序号
BigInteger nonce = authCenterService.getNonceFromAccount(admin.getAddress()).getResult();
// 对交易参数进行hash计算
byte[] messagePrice = OpenLedgerUtils.computeKeccak256Hash(OpenLedgerUtils.concatByte(OpenLedgerUtils.getBytes32(assetPrice.toByteArray()), OpenLedgerUtils.getBytes32(nonce.toByteArray())));
// 对hash进行签名
ECDSASignatureResult sign=OpenLedgerUtils.sign(admin, messagePrice);
// 调用设置价格接口
ResponseData<BigInteger> responsePriceData = assetService.setPrice(assetPrice, messagePrice,sign)
// 返回当前资产价格
BigInteger price =responsePriceData.getResult();
```





#### 新增/修改/查询Asset基础信息

- 1. 获取Asset基础信息Service

     调用示例：

     ```java
     IdentityService identity = assetService.getIdentity();
     ```

     

  2. 调用Asset基础信息Service

  ​      请参考请参考[资产基础信息操作](./IdentityManagement.html)搭建。

### 权益所有权账目管理

权益所有权账目记录的权益都是非同质化，即每个权益都是独一无二的。

#### 登记账户

在记账前需要登记账户,该操作仅对组织管理员，普通账户无权限。

```java

// 组织管理员公私钥对 
CryptoKeyPair admin = new CryptoSuite(CryptoType.ECDSA_TYPE, "admin private key").getCryptoKeyPair(); 
// 获取交易发起方交易序号
BigInteger nonce = authCenterService.getNonceFromAccount(admin.getAddress()).getResult();
// 开户账户地址
String accountAddr="0x45281c1f0b173f72ba73f29b54ce5bc3c0bc28f2"
// 组装参数并进行hash
byte[] args = OpenLedgerUtils.concatByte(OpenLedgerUtils.convertStringToAddressByte(accountAddr), OpenLedgerUtils.getBytes32(nonce.toByteArray()));
byte[] message = OpenLedgerUtils.computeKeccak256Hash(args);
// 对hash进行签名
ECDSASignatureResult sign = OpenLedgerUtils.sign(admin, message);
//调用登记账户接口
ResponseData<Boolean> response = nonFungibleAssetService.openAccount(accountAddr, message, sign);
```



#### 权益定义

管理员有权限通过定义权益数量、权益编码规则定义权益,每个权益是唯一的,不可替代。

```java
// 权益数量  
BigInteger num = BigInteger.valueOf(100);
// 权益编号前缀
BigInteger notePreFix = BigInteger.valueOf(2023);
// 权益编号占位数量 如2023001 001为编号占位 2023为编号前缀
BigInteger noteNoSize = BigInteger.valueOf(3);
// 权益起效时间 可为空
Date effectiveDate =DateUtils.addYears(new Date(), 1);
// 权益过期时间
Date expireDate = DateUtils.addYears(new Date(), 2);
// 操作账户地址 自定义
String operatorAddr = admin.getAddress();
// 定义账户地址 自定义
String issueAddr = admin.getAddress();
// 构建权益定义参数
IssueOption issueOption = IssueOptionBuilder.builder()
                .withAmount(num)
                .withNoteNoPrefix(notePreFix)
                .withNoteNoSize(noteNoSize)
                .withIssuer(issueAddr)
                .withOperator(operatorAddr)
                .withDesc("desc")
                .withEffectiveDate(effectiveDate)
                .withExpirationDate(expireDate).build();
//交易序列号 
BigInteger nonce = authCenterSDK.getNonceFromAccount(admin.getAddress()).getResult();
// 组装参数并进行hash
byte[] message = NonFungibleAssetService.computeIssueMsg(contractAddress, issueOption,nonce);
// 对hash进行签名
ECDSASignatureResult sign = OpenLedgerUtils.sign(admin, message);
// 调用权益定义接口 返回结果列表
ResponseData<List<IssueNoteResult>> response = nonFungibleAssetService.issue(issueOption, message, sign);
```

#### 权益所有权变更

当发生权益所有权变更时,可通过调用登记权益所有权变更接口,将变更相关的账户、权益信息记录到账本上.

```java
// 交易编号列表
BigInteger noteNo1 = new BigInteger("2021008");
List<BigInteger> noteNos = new ArrayList<>();
noteNos.add(noteNo1);
// 操作账户地址 自定义
String operatorAddr = admin.getAddress();
// 转出账户地址 自定义
String fromAddr = admin.getAddress();
// 接收账户地址 自定义
String toAddr = user.getAddress();
//资产合约地址
String assetAddr = "0x";
// 获取账户交易序号
BigInteger nonce = authCenterSDK.getNonceFromAccount(admin.getAddress()).getResult();
//组装参数并计算hash
byte[] message = NonFungibleAssetService.computeTransferMsg(assetAddr, operatorAddr, fromAddr, toAddr, noteNos, "desc", nonce);
// 对参数签名
ECDSASignatureResult sign = OpenLedgerUtils.sign(admin, message);
// 调用登记权益所有权变更接口
ResponseData<List<TransferNoteResult>> response = nonFungibleAssetService.transfer(operatorAddr, fromAddr, toAddr, noteNos, "desc", message, sign);
```



#### 查询权益明细

通过权益编码查询资产权益定义方、拥有方、失效时间等信息，仅权益定义方与拥有方有权限调用。

```java
// 资产编码
String noteNo = "2021009";
// 获取交易序列号
BigInteger nonce = authCenterSDK.getNonceFromAccount(admin.getAddress()).getResult();
// 计算hash
byte[] message = OpenLedgerUtils.getBytes32(nonce.toByteArray());
// 对hash签名
ECDSASignatureResult sign = OpenLedgerUtils.sign(admin, message);
// 调用查询接口
Note response = nonFungibleAssetService.getNoteDetail(new BigInteger(noteNo), message, sign);
```



#### 获取账户权益列表

通过账户地址获取账户拥有的权益编码列表，该操作支持管理员与权益拥有者操作。

```java
// 获取交易序列号
BigInteger nonce = authCenterSDK.getNonceFromAccount(admin.getAddress()).getResult();
// 计算hash
byte[] message = OpenLedgerUtils.getBytes32(nonce.toByteArray());
//对hash进行签名
ECDSASignatureResult sign = OpenLedgerUtils.sign(admin, message);
// 查询账户外部地址
String accountAddr=user.getAddress();
BigInteger startIndex = BigInteger.valueOf(0);
BigInteger endIndex = BigInteger.valueOf(100);
// 调用查询接口
List<BigInteger> response = nonFungibleAssetService.getAccountNotes(accountAddr, startIndex,endIndex,message, sign);
```



#### 更新权益编号

定义权益后，定义方可对权益的编号进行修改。

```java
// 原权益编码
BigInteger noteNo1 = new BigInteger("2021004");
// 新权益编码
BigInteger noteNo2 = new BigInteger("3021003");
// 操作账户地址：权益定义方地址
String accountAddr ="0x";
// 获取交易序列号
BigInteger nonce = authCenterSDK.getNonceFromAccount(admin.getAddress()).getResult();
// 组装参数并计算hash
byte[] message = OpenLedgerUtils.computeKeccak256Hash(OpenLedgerUtils.concatByte(OpenLedgerUtils.getBytes32(noteNo1.toByteArray()), OpenLedgerUtils.getBytes32(noteNo2.toByteArray()), OpenLedgerUtils.convertStringToAddressByte(accountAddr), OpenLedgerUtils.getBytes32(nonce.toByteArray())));
// 对hash签名
ECDSASignatureResult sign = OpenLedgerUtils.sign(admin, message);
// 调用更新接口
ResponseData<Boolean> response = nonFungibleAssetService.updateNoteNo(noteNo1, noteNo2, accountAddr, message, sign);

```

#### 更新权益属性

OpenLedger支持自定义权益属性。

```java
// 权益编号
BigInteger noteNo = new BigInteger("2021004");
// 自定义属性列表
HashMap<String, Object> items = new HashMap<>();
items.put("name", "world");
BigInteger nonce = authCenterSDK.getNonceFromAccount(admin.getAddress()).getResult();
// 组装参数并计算hash
byte[] args = OpenLedgerUtils.getBytes32(noteNo.toByteArray());
ValueModel vm = null;
for (Map.Entry<String, Object> entry : items.entrySet()) {
    String mapKey = entry.getKey();
    Object mapValue = entry.getValue();
    vm = new ValueModel(mapValue);
    args = OpenLedgerUtils.concatByte(args, mapKey.getBytes(Charset.defaultCharset()), ValueModel.getByteVal(vm));
}
byte[] message = OpenLedgerUtils.computeKeccak256Hash(OpenLedgerUtils.concatByte(args, OpenLedgerUtils.convertStringToAddressByte(admin.getAddress()), OpenLedgerUtils.getBytes32(nonce.toByteArray())));
// 对参数进行签名
ECDSASignatureResult sign = OpenLedgerUtils.sign(admin, message);
// 调用更新接口
ResponseData<Map<String, Object>> response = nonFungibleAssetService.updateNoteProperties(admin.getAddress(), noteNo, items, message, sign);
```

#### 查询权益属性

根据权益编号查询权益属性。

```java
// 获取交易序列号
BigInteger nonce = authCenterSDK.getNonceFromAccount(admin.getAddress()).getResult();
// 权益编号
BigInteger noteNo = new BigInteger("2021004");
// 组装签名
byte[] message = OpenLedgerUtils.getBytes32(nonce.toByteArray());
ECDSASignatureResult sign = OpenLedgerUtils.sign(admin, message);

// 调用查询权益属性接口
Map<String, Object> response = nonFungibleAssetService.getNoteProperties(noteNo, user.getAddress(), message, sign);

```



#### 更新权益批次信息

每定义一次权益为一个批次，批次包含权益的起效日期、失效日期、发行方、批次数量等信息。

##### 更新生效日期

调用示例：

```java
BigInteger batchNo = BigInteger.valueOf(1);
BigInteger nonce = authCenterSDK.getNonceFromAccount(admin.getAddress()).getResult();
Date expireDate = DateUtils.addYears(new Date(), 2);
byte[] args = OpenLedgerUtils.concatByte(OpenLedgerUtils.getBytes32(batchNo.toByteArray()),
        OpenLedgerUtils.getBytes32(BigInteger.valueOf(effectiveDate.getTime()).toByteArray()),
        OpenLedgerUtils.convertStringToAddressByte(admin.getAddress()),
        OpenLedgerUtils.getBytes32(nonce.toByteArray())
);
byte[] message = OpenLedgerUtils.computeKeccak256Hash(args);
ECDSASignatureResult sign = OpenLedgerUtils.sign(admin, message);
ResponseData<Boolean> responseData = nonFungibleAssetService.updateEffectiveDate(batchNo, effectiveDate, admin.getAddress(), message, sign);
```

##### 更新失效日期

调用示例：

```java
BigInteger batchNo = BigInteger.valueOf(1);
BigInteger nonce = authCenterSDK.getNonceFromAccount(admin.getAddress()).getResult();
Date expireDate = DateUtils.addYears(new Date(), 2);
byte[] args = OpenLedgerUtils.concatByte(OpenLedgerUtils.getBytes32(batchNo.toByteArray()),
        OpenLedgerUtils.getBytes32(BigInteger.valueOf(expireDate.getTime()).toByteArray()),
        OpenLedgerUtils.convertStringToAddressByte(admin.getAddress()),
        OpenLedgerUtils.getBytes32(nonce.toByteArray())
);
byte[] message = OpenLedgerUtils.computeKeccak256Hash(args);
ECDSASignatureResult sign = OpenLedgerUtils.sign(admin, message);

ResponseData<Boolean> responseData = nonFungibleAssetService.updateExpirationDate(batchNo, expireDate, admin.getAddress(), message, sign);
```

#### 启用批次权益

权益定义后，权益状态皆为冻结状态，需要手动调用启用批次接口，使批次内的权益生效。

调用示例：

```java
// 权益批次号
BigInteger batchNo = BigInteger.valueOf(2);
BigInteger nonce = authCenterSDK.getNonceFromAccount(admin.getAddress()).getResult();

byte[] message = OpenLedgerUtils.computeKeccak256Hash(OpenLedgerUtils.concatByte(OpenLedgerUtils.getBytes32(batchNo.toByteArray()),
                OpenLedgerUtils.getBytes32(nonce.toByteArray())
        ));
ECDSASignatureResult sign = OpenLedgerUtils.sign(admin, message);
// 调用启用接口
ResponseData<Boolean> responseData = nonFungibleAssetService.effectBatch(batchNo, message, sign);
```



#### 冻结权益

冻结权益后，权益无法交易与修改。该操作仅权益定义方有权限

```java
BigInteger nonce = authCenterSDK.getNonceFromAccount(admin.getAddress()).getResult();
// 冻结权益编码
BigInteger noteNo = new BigInteger("2021004");
// 操作用户
String accountAddr = admin.getAddress();
byte[] message = OpenLedgerUtils.computeKeccak256Hash(OpenLedgerUtils.concatByte(OpenLedgerUtils.getBytes32(noteNo.toByteArray()),
        OpenLedgerUtils.convertStringToAddressByte(accountAddr),
        OpenLedgerUtils.getBytes32(nonce.toByteArray())
));
// 对hash签名
ECDSASignatureResult sign = OpenLedgerUtils.sign(admin, message);
// 调用冻结接口
ResponseData<Boolean> response = nonFungibleAssetService.freezeNote(noteNo, accountAddr, message, sign);
```

#### 解冻权益

对冻结权益进行解冻，解冻后权益能正常交易与修改。

```java
BigInteger nonce = authCenterSDK.getNonceFromAccount(admin.getAddress()).getResult();
// 解冻资产编号
BigInteger noteNo = new BigInteger("2021001");
// 操作用户
String accountAddr = admin.getAddress();
// 组装参数并进行hash
byte[] message = OpenLedgerUtils.computeKeccak256Hash(OpenLedgerUtils.concatByte(OpenLedgerUtils.getBytes32(noteNo.toByteArray()),
        OpenLedgerUtils.convertStringToAddressByte(accountAddr),
        OpenLedgerUtils.getBytes32(nonce.toByteArray())
));
// 对hash进行签名
ECDSASignatureResult sign = OpenLedgerUtils.sign(admin, message);
// 调用解冻接口
ResponseData<BigInteger> response = nonFungibleAssetService.unfreezeNote(noteNo,accountAddr, message, sign);
```

#### 销毁权益

销毁权益后权益将无法再进行交易与修改，该操作仅支持权益定义方。

```java
BigInteger nonce = authCenterSDK.getNonceFromAccount(admin.getAddress()).getResult();
//销毁权益编码
BigInteger noteNo = new BigInteger("2021001");
// 操作用户
String accountAddr = admin.getAddress();
// 组装参数并进行hash
byte[] message = OpenLedgerUtils.computeKeccak256Hash(OpenLedgerUtils.concatByte(OpenLedgerUtils.getBytes32(noteNo.toByteArray()),
        OpenLedgerUtils.convertStringToAddressByte(accountAddr),
        OpenLedgerUtils.getBytes32(nonce.toByteArray())
));
// 对hash进行签名
ECDSASignatureResult sign = OpenLedgerUtils.sign(admin, message);
// 调用销毁接口
ResponseData<Boolean> response = nonFungibleAssetService.tearNote(noteNo, accountAddr, message, sign);
```

#### 获取销毁权益列表

```java
// 获取交易序列号
BigInteger nonce = authCenterSDK.getNonceFromAccount(admin.getAddress()).getResult();
// 查询账户
String accountAddr = admin.getAddress();
// 组装参数并进行hash
byte[] message = OpenLedgerUtils.computeKeccak256Hash(OpenLedgerUtils.concatByte(
        OpenLedgerUtils.convertStringToAddressByte(accountAddr),
        OpenLedgerUtils.getBytes32(nonce.toByteArray())
));
// 对hash进行签名
ECDSASignatureResult sign = OpenLedgerUtils.sign(admin, message);
// 调用获取销毁权益列表接口
List<BigInteger> response = nonFungibleAssetService.getTearNotes(accountAddr, message, sign);
```

#### 查询台账

OpenLedger支持多维查询账本,其中参数主要包括账期号、交易序列号、转出账户地址、接收账户地址、权益编号，转出以及接收账户具有查询个人账户的账本记录的权限，组织管理员具有查询全部台账记录的权限。

```java
// 交易发起方公私钥对 可以是资产拥有方以及组织管理员
CryptoKeyPair operator = new CryptoSuite(CryptoType.ECDSA_TYPE, "operator private key").getCryptoKeyPair(); 

//账期号
BigInteger termNo = BigInteger.valueOf(1);
// 交易记账序列号
BigInteger seqNo = BigInteger.valueOf(1);
// 权益编号
BigInteger noteNo =BigInteger.valueOf(203100099);
// 交易转出方账户外部地址
String fromAddress = "0x698bc024c1127cae3c108f1e2bc49e51b067cc56"
// 交易接收方账户外部地址
String toAddress = "0x793bd024c1127cae3c108f1e2bc49e51b067cc56";
List<BigInteger> limits = new ArrayList<>();
limits.add(BigInteger.valueOf(0));
limits.add(BigInteger.valueOf(500));
/**
 * 构建查询条件对象 其中5个参数在保证存在一个非空的参数值的前提下，其余皆可为空。
 *  查询某账期的前500条记录
 *  eg: NonFungibleCondition condition = new NonFungibleCondition(termNo, null, null, null,null,limits);
 *  查询某账户前500条记录转出记录
 *  eg: NonFungibleCondition condition = new NonFungibleCondition(null, null, fromAddress, limits);
 *  查询某账户前500条记录收入记录
 *  eg: NonFungibleCondition condition = new NonFungibleCondition(null, null, null, toAddress,null,limits);
 *  查询某两个账户之间的前500条记录交易记录
 *  eg: NonFungibleCondition condition = new NonFungibleCondition(null, null, fromAddress, toAddress,null,limits);
 *  根据交易记账序列号查询某条交易记录
 *  eg: NonFungibleCondition condition = new NonFungibleCondition(null, seqNo, null, null，null,null);
*/
NonFungibleCondition condition = new NonFungibleCondition(termNo, seqNo, fromAddress, toAddress,noteNo,limits);
// 获取交易发起方的交易序列号
BigInteger nonce = authCenterService.getNonceFromAccount(admin.getAddress()).getResult();
// 对序列号进行hash
byte[] message = OpenLedgerUtils.computeKeccak256HashFromBigInteger(nonce);
// 对hash进行签名
ECDSASignatureResult rs = OpenLedgerUtils.sign(admin, message);
// 调用查询接口获取返回
List<NonFungibleAssetRecord> recordEntities  = nonFungibleAssetService.query(condition, message, rs);
```

#### 设置权益价值

仅有组织管理员组下的用户有权限操作。

```java
// 组织管理员公私钥对 
CryptoKeyPair admin = new CryptoSuite(CryptoType.ECDSA_TYPE, "admin private key").getCryptoKeyPair(); 
// 获取交易发起方交易序号
BigInteger nonce = authCenterService.getNonceFromAccount(admin.getAddress()).getResult();
// 价值
BigInteger assetPrice = BigInteger.valueOf(100);
// 组装参数并进行hash
byte[] messagePrice = OpenLedgerUtils.computeKeccak256Hash(OpenLedgerUtils.concatByte(OpenLedgerUtils.getBytes32(assetPrice.toByteArray()), OpenLedgerUtils.getBytes32(nonce.toByteArray())));
// 对hash进行签名
ECDSASignatureResult sign = OpenLedgerUtils.sign(admin, messagePrice);
// 调用设置价值接口
ResponseData<BigInteger> responsePriceData = nonFungibleAssetService.setPrice(assetPrice, messagePrice, OpenLedgerUtils.sign(admin, messagePrice));
```



#### 查询权益价值

```java
nonFungibleAssetService.getAsset().price()
```

