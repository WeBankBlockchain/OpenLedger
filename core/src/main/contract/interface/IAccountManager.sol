pragma solidity ^0.4.26;

import "./IAccount.sol";
pragma experimental ABIEncoderV2;

//账户管理
contract IAccountManager
{
    //开通账户
    //\[param[in] holder, 账户所有人
    function openAccount(IIdentity holder) external returns (address);

    //冻结账户
    //\[param[in] account, 目标账户
    function freezeAccount(IAccount account) external returns (address);

    //解冻账户
    //\[param[in] account, 目标账户
    function unfreezeAccount(IAccount account) external returns (address);

    //移除一个组织成员
    //\[param[in] account, 关闭账户
    function closeAccount(IAccount account) external returns (address);
}