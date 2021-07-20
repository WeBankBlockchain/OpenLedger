pragma solidity ^0.4.26;

import "./IAccount.sol";
pragma experimental ABIEncoderV2;

contract ICustody
{
    //注册一类新资产，注册成功后自动成为该资产的托管人。
    //暂不考虑别的机构注册资产的情形
    //\param[in] asset 注册资产的地址
    function register(IAsset asset) external returns (address);

    //授予某个ID"存款"资产的权限
    //\param[in] grantor 授权人
    //\param[in] grantee 被授权人
    //\param[in] amount 授权数量， 允许被授权人存款数量
    function grantedDespoit(IIdentity grantor, IIdentity grantee, IAsset asset, uint256 amount) external returns(bool);

    //撤销某个ID"存款"资产的权限，撤销之后被授权人没有存款该资产的权限
    //\param[in] grantor 授权人
    //\param[in] grantee 被授权人
    function revokeDespoit(IIdentity grantor, IIdentity grantee, IAsset asset) external returns(uint256);

    //授予某个ID"取款"资产的权限
    //\param[in] grantor 授权人
    //\param[in] grantee 被授权人
    //\param[in] amount 授权数量， 允许被授权人取款数量
    function grantedWithdraw(IIdentity grantor, IIdentity grantee, IAsset asset, uint256 amount) external returns(uint256);

    //撤销某个ID"取款"资产的权限，撤销之后被授权人没有对该资产的取款权限
    //\param[in] grantor 授权人
    //\param[in] grantee 被授权人
    function revokeWithdraw(IIdentity grantor, IIdentity grantee, IAsset asset, uint256 amount) external returns(uint256);

    //准许账户持有资产
    //\param[in] grantor 授权人
    //\param[in] asset 资产
    //\param[in] account 目标账户
    //验权通过后调研资产对应的方法
    function authenticateAccount(IIdentity grantor, IAsset asset, IAccount account) external returns(uint256);

    //撤销账户持有资产
    //\param[in] grantor 授权人
    //\param[in] asset 资产
    //\param[in] account 目标账户
    //账户资产不会为空时怎么处理？
    //验权通过后调研资产对应的方法
    function unauthenticateAccount(IIdentity grantor, IAsset asset, IAccount account) external returns(uint256);
}