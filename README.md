# WeBankBlockchain-OpenLedger

WeBankBlockchain-OpenLedger(以下简称OpenLedger)是一套聚焦链上的“个人、组织、账户、权益"，承载数字化权益的完整生命周期,与业务场景深度融合、拥抱安全合规的企业级分布式账本解决方案，


## 关键特性
- 职责分离的托管人机制

    托管人负责权益上链登记、保管账本，业务提供方负责维护账户，账户所有人通过私钥签名操作自己的账户。

- 灵活的账户体系

    OpenLedger提供了由个人账户、企业账户组成的灵活账户体系，组织可以授权多名业务员操作企业账户。
    
- 安全可信的双重签名机制

    交易可以由业务方和用户联合签名，用户在链下对交易签名，业务方在上链环节签名，以保障用户权益和资产管理安全性。

- 双模式记账体系

   支持基于余额数量变化的记账模式和基于所有权变化的记账模式。

## 环境要求

在使用本项目前，请确认系统环境已安装相关依赖软件，清单如下：

| 依赖软件   | 说明                                                         | 备注 |
| ---------- | ------------------------------------------------------------ | ---- |
| FISCO-BCOS       | >= 2.7.2 |      |
| Java       | \>= JDK[1.8]                                                 |      |
| Git        | 下载的安装包使用Git                                          |      |

## 文档
- [**开发手册**](https://openledger.readthedocs.io/zh_CN/latest/)

## 贡献代码
欢迎参与本项目的社区建设：
- 如项目对您有帮助，欢迎点亮我们的小星星(点击项目左上方Star按钮)。
- 欢迎提交代码(Pull requests)。
- [提问和提交BUG](https://github.com/WeBankBlockchain/OpenLedger/issues) 
- 如果发现代码存在安全漏洞，请在[这里](https://security.webank.com)上报。


## License
![license](http://img.shields.io/badge/license-Apache%20v2-blue.svg)

开源协议为[Apache License 2.0](http://www.apache.org/licenses/). 详情参考[LICENSE](LICENSE)。

