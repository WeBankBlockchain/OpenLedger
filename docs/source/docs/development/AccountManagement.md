## 账户管理

### 获取所有资产

获取某账户下的所有资产

调用示例：

```java
// 组织管理员公私钥对
CryptoKeyPair account = new CryptoSuite(CryptoType.ECDSA_TYPE, "admin private key").getCryptoKeyPair();
// 获取交易调用方交易序号
BigInteger nonce = (BigInteger) authCentertService.getNonceFromAccount(account.getAddress()).getResult();
// 获取交易调用方交易序号
byte[] message = OpenLedgerUtils.computeKeccak256HashFromBigInteger(nonce);
// 使用交易发起方签名
ECDSASignatureResult sign=OpenLedgerUtils.sign(account, message);
// 设置获取资产类型 同质化资产为true,非同质化资产为false
Boolean isFungible = true;
// 调用获取account的所有资产
ResponseData<HashMap> responseData = accountService.getAllAssets(isFungible,message, sign);
// 返回资产列表 <资产合约地址，组织合约地址>
HashMap<String, String> map = responseData.getResult();
```

### 新增/修改/查询Account基础信息

- 1. 获取Account基础信息Service

     调用示例：

     ```java
     IdentitytService identity = accountService.getIdentity();
     ```

     

  2. 调用Account基础信息tService

  ​      请参考请参考[账户基础信息操作](./IdentityManagement.html)搭建。



  

