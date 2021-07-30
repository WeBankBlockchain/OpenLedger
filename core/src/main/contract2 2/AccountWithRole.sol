pragma solidity ^0.4.25;
pragma experimental ABIEncoderV2;

import "./core/storage/AddressMapLib.sol";
import "./core/lib/LibTypeConversion.sol";
import "./core/auth/roleRes/BaseResourceWithRole.sol";


// 假设账户下可操作账户的分为两种角色，可查询资产的与可发行资产的角色
contract AccountWithRole is BaseResourceWithRole {
    modifier onlyAssetCall(address asset) {
        require(msg.sender == asset, "Account: only asset is authorized.");
        _;
    }
    string constant roleA = "canRead";
    string constant roleB = "canIssue";
    using AddressMapLib for AddressMapLib.Map;
    using LibTypeConversion for *;
    AddressMapLib.Map fungibleAssetList;
    AddressMapLib.Map nonFungibleAssetList;
    address org;

     constructor(address creatorId, address orgAddr)  BaseResourceWithRole(creatorId) {
        holder = creatorId;
        org = orgAddr;
    }
    // 1.SDK call createRoleA
    function createRoleA(bytes32[4] sign) public {
        createRole(roleA,sign);
    }
    // 2.SDK call createRoleB
    function createRoleB(bytes32[4] sign) public {
        createRole(roleB,sign);
    }

    //3.SDK call grant Id1 to RoleA
    function addRoleA(address id, string[] operations, bytes32[4] sign){
        grantId(roleA, id, sign);
        // set defalut operation
        addOperationToResGroup(roleA, operations, sign);
    }
    //4.SDK call grant Id1 to RoleA
    function addRoleB(address id, bytes32[4] sign){
         grantId(roleB, id, sign);
    }

    function addAsset(address asset, address org, bool isFungible) public  returns (bool) {
        if (isFungible) {
            return fungibleAssetList.insert(asset, org);
        } else {
            return nonFungibleAssetList.insert(asset, org);
        }
    }

    // 6. 动态授权：SDK call: Account.addDetailToResGroup(roleB, "issue", address(asset), sign)

    // 7.执行鉴权
    function getAllAssets(bool isFungible, bytes32[4] sign) public view returns (address[], address[], uint) {
        require(checkAuth(sign, roleA, "getAllAssets", ""), "Account:Forbbiden getAllAsset!");
        if (isFungible) {
            return fungibleAssetList.getAll();
        } else {
            return nonFungibleAssetList.getAll();
        }
    }
    // 7.执行鉴权
    function issue(address asset, address[] transactionAddress, uint256[] uint256Args, string[] stringValueList, bytes32[4] sign) public returns (bool){
        require(checkAuth(sign, roleB, "issue", asset.addr2bytes()), "Account:Forbbiden issue!");
        return true;
    }


}
