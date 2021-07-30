pragma solidity ^0.4.25;
pragma experimental ABIEncoderV2;

import "./Interfaces.sol";

contract Org is IAccountHolder, IOrgAdmin, ICustody
{
    uint public MAX_ADMIN = 16;
    uint public MAX_MEMBER = 1024;

    uint public MAX_ASSET = 16;
    uint public MAX_NONFUNGIBLE_ASSET = 16;
//    AddressArray admins;  //本机构管理员
//    AddressArray members; //本机构成员，不包括管理员

    string constant roleAdmin = "ADMIN";  //本机构管理员
    string constant roleMember = "MEMBER"; //本机构成员，不包括管理员

    AddressArray asset;  //本机构托管的资产
    AddressArray uonFungibleAsset;  //本机构托管的不可分资产

//    Deposit_Resource;

    constructor () public {
//        admins = new AddressArray(MAX_ADMIN);
//        members = new AddressArray(MAX_MEMBER);

        asset = new AddressArray(MAX_ASSET);
        uonFungibleAsset = new AddressArray(MAX_NONFUNGIBLE_ASSET);

//        createRole("DEPOSIT", sign);
//        init();
    }

    function init(bytes32[4] sign) public {
        createRole(roleAdmin,sign);
        createRole(roleMember,sign);
    }

    //implementation of IAccountHolder interface
    function getHolder() public returns (address) {
        return this;
    }
    //允许谁将本账户的资产转到某个账户
    function allowTransfer(LegalEntity operator, address to, address Asset, uint amount) public returns (bool){
        return memberOf();
    }

    invoke(allowTransfer, llllll )
    //implementation of IOrgAdmin interfaces
    function registerAdmin(address admin, bytes32[4] sign) public {
        require(checkAuth(sign, roleAdmin, "registerAdmin", ""));
        grantId(roleAdmin, grantee, sign);
    }


    function unregisterAdmin(address admin, bytes32[4] sign) public {
        require(checkAuth(sign, roleAdmin, "unregisterAdmin", ""));
        revokeId(roleAdmin, admin, sign);
    }


    function registerMember(address member, bytes32[4] sign) public {
        require(checkAuth(sign, roleAdmin, "registerMember", ""));
        grantId(roleAdmin, grantee, sign);
    }

    function unregisterMember(address member, bytes32[4] sign) public {
        require(checkAuth(sign, roleAdmin, "unregisterMember", ""));
        grantId(roleAdmin, grantee, sign);
    }


    //implementation of ICustody interfaces
    function registerAsset(address asset, bytes32[4] sign) public {
        require(checkAuth(sign, roleAdmin, "registerAdmin", ""));
    }

    function registerUonFungibleAsset(address admin) public returns(bool) {
        return admins.remove(admin);
    }

    //授予某个ID"存款"资产的权限
    //\param[in] grantor 授权人
    //\param[in] grantee 被授权人
    //\param[in] amount 授权数量， 允许被授权人存款数量
    function grantedDeposit(address grantee, address asset, uint256 amount, byte32[4] sign) public {
        require(checkAuth(sign, roleAdmin, "grantedDeposit", ""));
        grantId(roleAdmin, grantee, sign);
    }

//    function invoke(string fun, ....)

    //撤销某个ID"存款"资产的权限，撤销之后被授权人没有存款该资产的权限
    //\param[in] grantor 授权人
    //\param[in] grantee 被授权人
    function revokeDeposit(address grantor, address grantee, address asset) external returns(uint256);

    //授予某个ID"取款"资产的权限
    //\param[in] grantor 授权人
    //\param[in] grantee 被授权人
    //\param[in] amount 授权数量， 允许被授权人取款数量
    function grantedWithdraw(address grantor, address grantee, address asset, uint256 amount) external returns(uint256);

    //撤销某个ID"取款"资产的权限，撤销之后被授权人没有对该资产的取款权限
    //\param[in] grantor 授权人
    //\param[in] grantee 被授权人
    function revokeWithdraw(address grantor, address grantee, address asset, uint256 amount) external returns(uint256);

    //准许账户持有资产
    //\param[in] grantor 授权人
    //\param[in] asset 资产
    //\param[in] account 目标账户
    //验权通过后调研资产对应的方法
    function authenticateAccount(address grantor, address asset, address account) external returns(uint256);

    //撤销账户持有资产
    //\param[in] grantor 授权人
    //\param[in] asset 资产
    //\param[in] account 目标账户
    //账户资产不会为空时怎么处理？
    //验权通过后调研资产对应的方法
    function unauthenticateAccount(address grantor, address asset, address account) external returns(uint256);

    function createID() public returns(address) {
     return new ID()
}
}


contract ID {
address _org;
constructor() {
 _org = getOrg();
}
}
    
    
    
    
    
