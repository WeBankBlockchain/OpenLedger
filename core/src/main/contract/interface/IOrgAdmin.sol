pragma solidity ^0.4.26;

import "./IAccount.sol";
pragma experimental ABIEncoderV2;

//组织管理员
contract IOrgAdmin
{
    //新增一个管理员
    //\[param[in] another, 新增管理员地址
    function registerAdmin(IIdentity another) external returns (address);

    //新增一个组织成员
    //\[param[in] member, 新增组织成员地址
    function registerMember(IIdentity member) external returns (address);

    //移除一个管理员
    //\[param[in] another, 目标管理员地址
    function unregisterAdmin(IIdentity another) external returns (address);

    //移除一个组织成员
    //\[param[in] member, 目标成员地址
    function unregisterMember(IIdentity member) external returns (address);
}