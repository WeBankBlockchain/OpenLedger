## 账期管理

### 新建账期

在项目过程中，在记账前先新建账期，后续可根据实际账期继续创建，仅有项目的拥有方，也就是初始化项目的用户才有权限进行操作。

调用示例：

```java
// 项目管理员公私钥对
CryptoKeyPair account = new CryptoSuite(CryptoType.ECDSA_TYPE, "project admin private key").getCryptoKeyPair();
//账期名称
String termName = "202101-202103";
// 对参数进行hash
byte[] message = OpenLedgerUtils.computeKeccak256Hash(termName.getBytes());
// 使用项目管理用户(owner)对hash进行签名
ECDSASignatureResult rs = OpenLedgerUtils.sign(admin, message);
//调用新建账期接口
ResponseData<BigInteger> newTermNo = termService.newTerm(termName, message, rs);
// 返回当前账期号
BigInteger termNo = newTermNo.getResult();
```



