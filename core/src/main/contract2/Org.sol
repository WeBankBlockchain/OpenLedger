pragma solidity ^0.4.25;
pragma experimental ABIEncoderV2;

import "./auth/IdInterface.sol";
import "./interface/ICustody.sol";
import "./interface/IOrgAdmin.sol";
import "./FungibleAsset.sol";
import "./AddressArray.sol";
import "./Identity.sol";
//import "./R.sol";


contract Org is IOrgAdmin, ICustody,
{
    uint public MAX_ADMIN = 16;
    uint public MAX_MEMBER = 1024;

    uint public MAX_ASSET = 16;
    uint public MAX_NONFUNGIBLE_ASSET = 16;
    AddressArray admins;  //本机构管理员

    AddressArray members; //本机构成员，不包括管理员

    AddressArray asset;  //本机构托管的资产
    AddressArray uonFungibleAsset;  //本机构托管的不可分资产

//    Deposit_Resource;

    constructor () public {
        admins = new AddressArray(MAX_ADMIN);
        members = new AddressArray(MAX_MEMBER);

        asset = new AddressArray(MAX_ASSET);
        uonFungibleAsset = new AddressArray(MAX_NONFUNGIBLE_ASSET);

//        createRole("DEPOSIT", sign);
    }

    //implementation of IOrgAdmin interfaces
    function registerAdmin(address admin) public returns(bool) {
        return admins.add(admin);
    }

    function unregisterAdmin(address admin) public returns(bool) {
        return admins.remove(admin);
    }

    function registerMember(address member) public returns(bool) {
        return members.remove(member);
    }

    function unregisterMember(address member) public returns(bool) {
        return members.remove(member);
    }


    //implementation of ICustody interfaces
    function registerAsset(address admin) public returns(bool) {
        return admins.add(admin);
    }

    function registerUonFungibleAsset(address admin) public returns(bool) {
        return admins.remove(admin);
    }

    //授予某个ID"存款"资产的权限
    //\param[in] grantor 授权人
    //\param[in] grantee 被授权人
    //\param[in] amount 授权数量， 允许被授权人存款数量
    function grantedDespoit(address grantor, address grantee, address asset, uint256 amount, byte32[4] sign) external returns(bool) {
        if ( ! admins.memeberOf(grantor)) {
            return false;
        }

        if ( ! members.memeberOf(grantee)) {
            return false;
        }

        //todo
        grantID("DESPOSIT", grantee, sign);

    }

    function invoke(string fun, ....)

    //撤销某个ID"存款"资产的权限，撤销之后被授权人没有存款该资产的权限
    //\param[in] grantor 授权人
    //\param[in] grantee 被授权人
    function revokeDespoit(address grantor, address grantee, address asset) external returns(uint256);

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
    
    
    
    
    
