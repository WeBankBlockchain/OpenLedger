pragma solidity ^0.4.24;

pragma experimental ABIEncoderV2;

//参与系统的组织或者个人
interface ILegalEntity
{

}

interface IAccount
{
    //转账操作
    function transfer(IAsset asset, IAccount to, uint amount) public;
    //查询某种资产的余额
    function balanceOf(IAsset asset) public returns(uint);
    //转账某种资产
    function transfer(IAsset asset, IAccount from, IAccount to, uint amount) public;
    //所持有的的资产的列表
    function assetList() public returns(uint);
}

//账户管理接口，可以为其他组织或者个人开通Account
interface IAccountAdmin
{
    //开通账户
    function openAccount(string EOA) public returns(ILegalEntity);
    //冻结账户
    function freezeAccount(IAccount account) public returns(uint);
    //解冻账户
    function unfreezeAccount(IAccount account) public;
    //注销账户
    function closeAccount(IAccount account) public returns(uint);

    //重置私钥
    function resetEOA(IAccount account, string newEOA) public returns(uint);
}

//账户所有"人"，拥有账户的组织或者个人需实现此接口
interface IAccountHolder {
    //持有账户的地址
    function getHolder() public returns (address);
    //允许谁将本账户的资产转到某个账户
    function allowTransfer(ILegalEntity operator, IAccount to, IAsset Asset, uint amount) public returns (bool);
    //查询账户余额
    function balanceOf(ILegalEntity operator, IAccount account) public returns (uint);
}

//组织管理接口
interface IOrgAdmin
{
    //新增一个管理员
    //\[param[in] another, 新增管理员地址
    function registerAdmin(ILegalEntity another) external returns (bool);

    //新增一个组织成员
    //\[param[in] member, 新增组织成员地址
    function registerMember(ILegalEntity member) external returns (bool);

    //移除一个管理员
    //\[param[in] another, 目标管理员地址
    function unregisterAdmin(ILegalEntity another) external returns (bool);

    //移除一个组织成员
    //\[param[in] member, 目标成员地址
    function unregisterMember(ILegalEntity member) external returns (bool);
}

//普通资产接口
interface IAsset
{
    function deposit(IAccount account, uint amount) public;

    function withdraw(IAccount account, uint amount) public;

    function transfer(IAccount from, IAccount to, uint amount) public;
}

//不可拆分的资产
interface INonFungibleAsset
{
    function deposit(IAccount account, uint amount) public;

    function withdraw(IAccount account, uint amount) public;

    function transfer(IAccount from, IAccount to, uint amount) public;
}

//托管人接口
interface ICustody
{
    //注册一类新资产，注册成功后自动成为该资产的托管人。
    //暂不考虑别的机构注册资产的情形
    //\param[in] asset 注册资产的地址
    function registerAsset(IAsset asset) external returns (bool);
//    function registerUonFungibleAsset(IAsset asset) external returns (bool);

    function deposit(IAsset asset, IAccount account, uint amount) public;

    function withdraw(IAsset asset, IAccount account, uint amount) public;

    function transfer(IAsset asset, IAccount from, IAccount to, uint amount) public;

    //一下接口是托管人权限接口，可以不必定义，仅需在组织中实现

    //授予某个ID"存款"资产的权限
    //\param[in] grantor 授权人
    //\param[in] grantee 被授权人
    //\param[in] amount 授权数量， 允许被授权人存款数量
    function grantedDeposit(ILegalEntity grantor, ILegalEntity grantee, IAsset asset, uint256 amount) external returns(bool);

    //撤销某个ID"存款"资产的权限，撤销之后被授权人没有存款该资产的权限
    //\param[in] grantor 授权人
    //\param[in] grantee 被授权人
    function revokeDeposit(ILegalEntity grantor, ILegalEntity grantee, IAsset asset) external returns(uint256);

    //授予某个ID"取款"资产的权限
    //\param[in] grantor 授权人
    //\param[in] grantee 被授权人
    //\param[in] amount 授权数量， 允许被授权人取款数量
    function grantedWithdraw(ILegalEntity grantor, ILegalEntity grantee, IAsset asset, uint256 amount) external returns(uint256);

    //撤销某个ID"取款"资产的权限，撤销之后被授权人没有对该资产的取款权限
    //\param[in] grantor 授权人
    //\param[in] grantee 被授权人
    function revokeWithdraw(ILegalEntity grantor, ILegalEntity grantee, IAsset asset, uint256 amount) external returns(uint256);

    //准许账户持有资产
    //\param[in] grantor 授权人
    //\param[in] asset 资产
    //\param[in] account 目标账户
    //验权通过后调研资产对应的方法
    function authenticateAccount(ILegalEntity grantor, IAsset asset, IAccount account) external returns(uint256);

    //撤销账户持有资产
    //\param[in] grantor 授权人
    //\param[in] asset 资产
    //\param[in] account 目标账户
    //账户资产不会为空时怎么处理？
    //验权通过后调研资产对应的方法
    function unauthenticateAccount(ILegalEntity grantor, IAsset asset, IAccount account) external returns(uint256);
}