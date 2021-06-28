# WebankBlockchain-OpenLedger demo使用手册

OpenLedger-demo通过命令行方式提供基于WebankBlockchain-OpenLedger创建组织、创建账户、登记账本等操作。

## 环境准备

- 运行环境

  安装Java：JDK 14 （JDK1.8 至JDK 14都支持）

- 搭建一条FISCO BCOS链

    请参考[FISCO BCOS安装](https://fisco-bcos-documentation.readthedocs.io/zh_CN/latest/docs/installation.html)搭建。

## 准备demo
### 1.下载源码

```bash
git clone https://github.com/WeBankBlockchain/OpenLedger
```

打开`demo/build`目录

修改`sdk.toml`中节点的IP和端口，与您要连接的节点所匹配。

### 2.证书配置

参考[java Service证书配置](./configuration.html#id5)。


修改`sdk.toml`中节点的IP和端口，与您要连接的节点所匹配。

   ``` properties
   [network]
   peers=["127.0.0.1:20200"]
   ```


### 3.拷贝证书

从节点`nodes/${ip}/Service/` 目录下的证书拷贝到`conf`目录。

### 4.修改项目配置文件

修改`application.properties`文件中`blockchain.txKey`为`conf`目录下的账户私钥文件

示例：

``` properties
blockchain.txKey=0x11a070299af8ec007927188f91d40759d1a7c525.pem
```

## 体验demo
### 启动

控制台执行以下命令：

``` bash
java -jar openledger-demo-1.0.0-all.jar
```

启动完成后出现以下提示：

``` bash
Welcome to OpenLedger! please input 'init' to run a new project or 'help' to learn more,input 'quit' to exist.
OpenLedger-demo>
```



### 退出

输入`quit` 退出demo。

``` bash
OpenLedger-demo>quit
```



### 项目管理

#### 初始化项目

输入`init`初始化项目。

初始化成功后，控制台输出相关合约地址以及组织管理员地址与私钥，其中初始化完成后，程序默认登录组织管理员。

``` bash
project.addr:0x809185526e71a2cee6890185452cff883959434a
accountmanager.addr:0x44c064808dee8143dccd7d7560c1e92d8976980a
authmanager.addr:0xf95bad61af99e88f0ce3c6ac62cb25c9cd6f151d
authcenter.addr:0xe7dc3226d54b384c3e5f00a7b40ad62bdf800e09
org.addr:0xeedf3702b47144f512b454ad230aa68be1f5a842
orgAdmin.addr(logon):0xda86d395c99a846db5f76d6339fdde12415d75a2
orgAdmin.prikey:c0a82314219cc7951e76e8cd1b910c16ee87cb5892383da4b0a91c98c080b68f
create Project:0x809185526e71a2cee6890185452cff883959434a
```

#### 加载项目

demo的运行需要先加载项目，初始化后默认加载创建的项目，同时支持通过配置文件的方式加载项目。

将`project.addr`、`accountmanager.addr`、`authmanager.addr`、`authcenter.addr`、`org.addr` 配置到`application.propertis`。

例如：

```bash
project.addr:0x8b72183b20975c115a3e4335ceb668ed70828756
accountmanager.addr:0x270bf532e2be56a64b85a0ae81fdd4930d9e97cf
authmanager.addr:0xc32b2c8a3172462d895b4d7f5ff9223489d40c7b
authcenter.addr:0xe7846a2cc4ff9c3cd15294fae746a8d8ee8b73c3
org.addr:0xc0d82fd05ea37d473f4cc2a364ffdcc555bbfc6c
```

执行命令加载：

``` bash
OpenLedger-demo>load
```

执行完成后输出：

``` bash
load Project:0x8b72183b20975c115a3e4335ceb668ed70828756
```

### 账户管理

#### 登录账户

账户通过私钥登录到demo，作为后续交易签名方。

控制台执行`login`命令登录：

``` bash
OpenLedger-demo>login 99cdcc7bb81573229a6d0365c53c3cafa869fb067c87bace262f23685205b04d
```

`99cdcc7bb81573229a6d0365c53c3cafa869fb067c87bace262f23685205b04d` 为私钥。

#### 登出账户

登出账户后，无法进行交易。

``` bash
OpenLedger-demo>logout 
```

#### 查看当前登录账户

``` bash
OpenLedger-demo>whoami
```

#### 生成公私钥对

``` bash
OpenLedger-demo>generateAccount
```

返回地址以及公钥：

``` bash
address:0x50b5d4ee132159d4ba7029bce866b4131f3658d1
prikey:5c6b07518d24cb89d03e4cb83a7097afee3488c7bf5892a41d81db5c7a031f43
```

#### 创建组织账户

需要登录组织管理员创建组织账户。

执行`createAccount $accountAddress`命令创建账户(`$accountAddress`为账户地址)：

eg:

``` bash
OpenLedger-demo>createAccount 0x70b5d4ee132159d4ba7029bce866b4131f3658d1
```

`0x70b5d4ee132159d4ba7029bce866b4131f3658d1` 为账户地址，可通过`generateAccount`生成。

#### 冻结组织账户

需要登录组织管理员冻结组织账户。

执行`freeze $accountAddress`命令创建账户(`$accountAddress`为账户地址)：

eg:

``` bash
OpenLedger-demo>freeze 0x70b5d4ee132159d4ba7029bce866b4131f3658d1
```

`0x70b5d4ee132159d4ba7029bce866b4131f3658d1` 为账户地址。

#### 解冻组织账户

需要登录组织管理员解冻组织账户。

执行`unfreeze $accountAddress`命令创建账户(`$accountAddress`为账户地址)：

eg:

``` bash
OpenLedger-demo>unfreeze 0x70b5d4ee132159d4ba7029bce866b4131f3658d1
```

`0x70b5d4ee132159d4ba7029bce866b4131f3658d1` 为账户地址。

#### 注销组织账户

需要登录组织管理员解注销组织账户。

执行`cancel $accountAddress`命令创建账户(`$accountAddress`为账户地址)：

eg:

``` bash
OpenLedger-demo>cancel 0x70b5d4ee132159d4ba7029bce866b4131f3658d1
```

`0x70b5d4ee132159d4ba7029bce866b4131f3658d1` 为账户地址。

#### 替换组织账户外部地址

需要登录组织管理员替换组织账户外部地址

执行`changeExternalAccount $accountAddress`命令创建账户(`$accountAddress`为账户地址)：

eg:

``` bash
OpenLedger-demo>changeExternalAccount 0x70b5d4ee132159d4ba7029bce866b4131f3658d1
```

`0x70b5d4ee132159d4ba7029bce866b4131f3658d1` 为账户地址。

### 账本管理

#### 创建账本

需要登录组织管理员操作。

创建资产命令：`createAsset $isFungible $assetName`

- `isFungible`: `true`为同质化，`false`为非同质化。
- `assetName`: 账本名称自定义。 

 执行完成后返回账本地址。

eg:

- 创建登记资产数量变化模式的账本

  ``` bash
  OpenLedger-demo>createAsset true fungible1
  ```

  

- 创建登记权益所有权变更模式的账本

  ``` bash
  OpenLedger-demo>createAsset false nonfungible1
  ```

  

#### 加载账本

demo支持通过账本地址加载账本，创建账本后默认加载新创建的账本，加载账本后才能执行账本的所有操作。

命令：

`loadAsset $assetAddr $isFungible`

- `assetAddr`:  账本合约地址，可通过创建账本获取。
- `isFungible`: 是否为同质化资产，`true`为同质化，`false`为非同质化。

eg:

``` bash
OpenLedger-demo>loadAsset 0x70b3d4ee132159d4ba7029bce866b4131f3658d1 false
```

#### 登记账户

需要登录组织管理员操作账本登记账户。

命令：

`openAccount $accountAddr`

- `accountAddr`: 账户地址。

eg:

``` bash
OpenLedger-demo>openAccount 0x70b3d4ee132159d4ba7029bce866b4131f3658d2
```



#### 登记资产数量变化账户

##### 登记资产存入账目

需要登录组织管理员操作登记存入。

命令：

`deposit $accountAddr $amount`

- `accountAddr:` 已登记的账户地址。
- `amount:` 存入数量。

eg:

``` bash
OpenLedger-demo>deposit 0x70b3d4ee132159d4ba7029bce866b4131f3658d2 100
```

返回：

``` bash
deposit successfully! transaction result:TransferResult{isSuccees=true, termNo=1, seqNo=3}
```

`TransferResult`说明 ：

- `termNo`:  账期
- `seqNo`: 交易序列号



##### 登记资产提取账目

需要登录组织管理员操作提取记账。

命令：

`withdrawal $accountAddr $amount`

- `accountAddr:` 已登记的账户地址。
- `amount:` 提取数量。

eg:

``` bash
OpenLedger-demo>withdrawal 0x70b3d4ee132159d4ba7029bce866b4131f3658d2 100
```

返回：

``` bash
withdrawal successfully! transaction result:TransferResult{isSuccees=true, termNo=1, seqNo=3}
```

`TransferResult`说明 ：

- `termNo`:  账期
- `seqNo`: 交易序列号

#### 余额数量变化账目管理

账户仅有操作自己资产余额的权限。

###### 生成签名

交易前需要使用交易转出方(from)私钥进行签名。

- 切换登录账户,使用当前登录账户生成签名

  ```bash
  OpenLedger-demo>login $fromAddress
  ```

- 生成签名

  ``` bash
  OpenLedger-demo>generateTransferSign $from $to $amount
  ```

  返回签名字符串。

###### 使用签名交易

命令：

`transfer $from $to $amount $sign `

- `from`: 转出方账户地址。
- `to`: 接收方账户地址。
- `amount`: 交易数量。
- `sign`: 通过上面生成签名操作的结果。

`（上面1，2步骤可分离操作实现授权代理场景，即A客户端使用拥有者执行生成签名，把签名复制到B客户端使用组织管理员执行交易）`

返回：

``` bash
transfer successfully!  transaction result:TransferResult{isSuccees=true, termNo=1, seqNo=128}
```

- `isSuccees`: 操作是否成功，成功则为true,失败则为false。
- `termNo`: 台账账期号。
- `seqNo`: 交易序列号。

例子：

A端为拥有者账户：

```bash
OpenLedger-demo>login  $clientPrikey ## 登录拥有者账户
logon $clientAddr
OpenLedger-demo>generateTransferSign $from $to $amount  ## from等于clientAddr
result:xxx  ## 生成签名
OpenLedger-demo>transfer  $from $to $amount $resultSign  ##拥有者自己执行交易
```

B端为组织管理员：

`resultSign`为A端生成签名结果

``` bash
OpenLedger-demo>login  $adminPrikey  ## 登录组织管理员
OpenLedger-demo>transfer  $from $to $amount $resultSign  ##resultSign 为A端产生签名
```

eg:

``` bash
OpenLedger-demo>transfer 0xd1d99b85b912ffcfbe266ac317c6b88a3b06530b  0x3fe7b33c513555aa9f5b25e1e0f2c51da97e02ba 100 127918723897129878937223243242
```



##### 余额查询

账户可通过提交签名查询自己资产的余额。

- 1.使用当前登录账户生成签名

  ```bash
  OpenLedger-demo>generateQuerySign
  ```

  返回签名字符串。

- 2.执行查询余额命令

  命令：

  `getBalance $accountAddr $sign`

  - `accountAddr`： 账户地址。
  - `sign`: 签名。

`（上面1，2步骤可分离操作实现授权代理场景，即A客户端使用资产拥有者执行生成签名，把签名复制到B客户端使用组织管理员执行查询余额）`

例子：

A端为资产拥有者账户：

```bash
OpenLedger-demo>login  $clientPrikey ## 登录资产拥有者账户
logon $clientAddr
OpenLedger-demo>generateQuerySign  
result:xxx  ## 生成签名
OpenLedger-demo>getBalance $clientAddr resultSign  ##拥有者查询自己余额
result:100
```

B端为组织管理员：

`resultSign`为A端生成签名结果

``` bash
OpenLedger-demo>login  $adminPrikey  ## 登录组织管理员
OpenLedger-demo>getBalance  $clientAddr resultSign  ## 管理员执行查询client余额
result:100
```

#### 权益所有权变更账目管理

##### 权益定义

执行发行前调用`loadAsset`加载账本,该操作仅支持管理员。

命令：

`issue $issuerAddr $num $noteNoPrefix $noteNoSize $expirationDate`

-  `issuer`: 定义方地址，必须已开户账户。
- `num`: 权益数量，如100。
- `noteNoPrefix:` 权益编码前缀，如2021。
- `noteNoSize`: 权益编码除了前缀外的长度，如4，起始编码则为20210001。
- `expirationDate`: 权益过期时间,日期格式为0000-00-00

eg:

``` bash
OpenLedger-demo>issue 0xd1d99b85b912ffcfbe266ac317c6b88a3b06530b 100 2021 4 2023-10-01
```

返回：

``` bash
issue successfully! issue noteNos:[IssueNoteResult{batchNo=1,noteNo=20210001, termNo=1, SeqNo=25}]
```

- `noteNos`: 返回所有权益编码

  ​	1. batchNo: 批次号

  ​	2.noteNo: 资产编码。

  ​	3.台账账期号。
  
  ​	4. 交易序列号。
##### 启用权益

定义权益后，需要调用启用接口使权益转化为启用状态。

命令：

``` bash
OpenLedger-demo>activeBatch $batchNo
```

返回：

``` bash
result:true
```

返回启用是否成功。

eg:

``` bash
OpenLedger-demo>activeBatch 1
```



##### 登记权益所有权变更账目

账户拥有操作自己权益的权限。

###### 生成签名

交易前需要使用交易转出方(from)私钥进行签名。

- 切换登录账户,使用当前登录账户生成签名

  ```bash
  OpenLedger-demo>login $fromAddress
  ```

- 生成签名

  ``` bash
  OpenLedger-demo>generateTransferSign $from $to $noteNos
  ```

  - `from`: 交易转出方账户地址,from需要等于当前账户。
  - `to`: 交易接收方账户地址。
  - `noteNos`: 资产编码列表，多个使用逗号分割。

  返回签名字符串。

  eg：

  ``` bash
  OpenLedger-demo>generateTransferSign 0x9250cd52f7c41ed9e015b2cd453aa97b310b309f 0x7250cdf2f7c41ed9e015b2cd453aa97b310b3093 20210001,20210002
  ```

  

###### 执行签名交易

`transfer $from $to $noteNos $sign `

- `from`: 转出方账户地址。
- `to`: 接收方账户地址。
- `noteNos`: 权益编码列表，多个使用逗号分割。
- `sign`: 通过上面生成签名操作的结果。

`（上面1，2步骤可分离操作实现授权代理场景，即A客户端使用拥有者执行生成签名，把签名复制到B客户端使用组织管理员执行交易）`

返回值：

` transaction result:[TransferNoteResult(result=true, noteNo=20210003, termNo=1, SeqNo=127)]`

- `result`: 操作是否成功。
- `noteNo`: 变更权益编码。
- `termNo`: 台账账期。
- `seqNo`: 交易序列号。

例子：

A端为拥有者账户：

```bash
OpenLedger-demo>login  $clientPrikey ## 登录拥有者账户
logon $clientAddr
OpenLedger-demo>generateTransferSign $from $to $noteNos  ## from等于clientAddr
result:xxx  ## 生成签名
OpenLedger-demo>transfer  $from $to $noteNos $resultSign  ##拥有者自己执行交易
```

B端为组织管理员：

`resultSign`为A端生成签名结果

``` bash
OpenLedger-demo>login  $adminPrikey  ## 登录组织管理员
OpenLedger-demo>transfer  $from $to $noteNos $resultSign  ##组织管理员执行交易，resultSign 为A端产生签名
```

eg:

``` bash
OpenLedger-demo>transfer  0x9250cd52f7c41ed9e015b2cd453aa97b310b309f 0x3250cd52f7c41ed9e015b2cd453aa97b310b309f 20210001,20210003 112789897293472873979787989779
```



##### 查询账户权益

###### 查询账户是否拥有权益

需要登录管理员操作。

命令：

`isAccountHoldNote $clientAddr $noteNo` 

- `clientAddr`: 查询账户地址。
- `noteNo`: 权益编号。

返回Boolean类型，true则拥有，false则不拥有。

eg:

``` bash
OpenLedger-demo>isAccountHoldNote  0x9250cd52f7c41ed9e015b2cd453aa97b310b309f 20210001
```

###### 查询账户拥有的权益编号列表

查询账户的资产编号列表。

命令：

`getAccountNotes  $clientAddr $start $end`

- `clientAddr`: 账户地址。
- `start `: 查询列表起始值，从0开始。
- `end`: 查询列表结束值。

返回账户权益编号列表。

eg:

``` bash
OpenLedger-demo>getAccountNotes 0x9250cd52f7c41ed9e015b2cd453aa97b310b309f 0 10
```



#### 台账查询

资产拥有者具有查询自己资产台账的权限。

##### 生成签名

1.使用当前登录账户生成签名

```bash
OpenLedger-demo>generateQuerySign
```

返回签名字符串。

##### 执行签名交易

命令：

`queryBook $conditon $sign`

- `condition`: 查询条件列表

  - 同质化资产查询列表支持4个查询参数，分别为账期号、交易序列号、交易转出地址、交易接收地址，4个参数满足包含至少1个非0值即可, 若查询值为空则输入0。

    eg: 

    [1,0,0,0x9250cd52f7c41ed9e015b2cd453aa97b310b309f]

    查询账期号为1，0x9250cd52f7c41ed9e015b2cd453aa97b310b309f账户作为接收方的账本记录。

  - 非同质化资产查询列表支持5个查询参数，分别为账期号、交易序列号、交易转出地址、交易接收地址、权益编号，5个参数满足包含至少1个非0值即可, 若查询值为空则输入0。

    eg: 

    [1,0,0,0x9250cd52f7c41ed9e015b2cd453aa97b310b309f,0]

    查询账期号为1，0x9250cd52f7c41ed9e015b2cd453aa97b310b309f账户作为接收方的账本记录。

- `sign`: 上面生成的签名。

返回：

demo默认返回前10条记录。

``` bash
[NonFungibleAssetRecord(termNo=1, seq=32, from=0x0000000000000000000000000000000000000000, to=0xb7b1fa39fba52b6d5f248fb2b2c83779e617340a, noteNo=20210008, asset=0x3fe7b33c513555aa9f5b25e1e0f2c51da97e02ba, operator=0x688c9c9442001343aa44aa347a21112e605fc748, desc=desc)]
```

- `termNo`: 台账账期号。
- `seq`: 序列号。
- `from`: 转出地址。
- `to`: 存入地址。
- `noteNo`: 权益编码,仅非同质化返回。
- `amount:` 交易数量，仅同质化返回。
- `operator`: 操作账户。
- `desc`： 描述。





