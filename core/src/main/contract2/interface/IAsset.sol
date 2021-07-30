pragma solidity ^0.4.24;
pragma experimental ABIEncoderV2;

interface IAsset {

    //资产存款操作
    //\param[in] someone 交易发起人
    //\param[in] to 目标账户
    //\param[in] amount 存款数量
    function deposit(IIdentity someone, IAccount to, uint256 amount) external returns (uint256);

    //资产取款操作
    //\param[in] someone 交易发起人
    //\param[in] to 目标账户
    //\param[in] amount 存款数量
    function withdraw(IIdentity someone, IAccount to, uint256 amount) external returns (uint256);

    //资产转账操作
    //\param[in] someone 交易发起人
    //\param[in] from 源账户
    //\param[in] to 目标账户
    //\param[in] amount 转账数量
    function transfer(IIdentity someone, IAccount from, IAccount to, uint256 amount) external returns (uint256);

    //准许账户持有资产
    //\param[in] grantor 授权人
    //\param[in] asset 资产
    //\param[in] account 目标账户
    function authenticateAccount(IIdentity grantor, IAsset asset, IAccount account) external returns(uint256);

    //撤销账户持有资产
    //\param[in] grantor 授权人
    //\param[in] asset 资产
    //\param[in] account 目标账户
    //账户资产不会为空时怎么处理？
    function unauthenticateAccount(IIdentity grantor, IAsset asset, IAccount account) external returns(uint256);
}