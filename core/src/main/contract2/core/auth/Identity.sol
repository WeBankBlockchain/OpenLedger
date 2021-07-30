pragma solidity ^0.4.25;
pragma experimental ABIEncoderV2;

import "./interface/IAclManager.sol";
import "./roleRes/ResourceGroup.sol";
import "./roleRes/Role.sol";
import "../storage/AddressMapLib.sol";
import "../storage/AddressSetLib.sol";
import "./interface/IResource.sol";
import "../lib/LibSafeMath.sol";
import "../lib/SignLib.sol";

/**
   Identity support two ways to set permissions:
   - 1.Base on role and resourceGroup
   - 2.Base on allowId、resource、operation、detail

   Base on role and resource:
     Identity can create roles and resourceGroup.
     The role can be set as org's admin、operator which including some id.
     The resourceGroup including some resources、operations and detail.
     Call 'addResGroupToRole' can be set the resourceGroup of role,every role has only one resourceGroup.
     When call 'check' function,Identity will check the callerId boolean is in role,and check the caller's resource、operation and detail  boolean is in resourceGroup of role.

   Base on allowId、resource、operation、detail:
     The difference with the previous approach is that this way isn't  distinguish the role,it's the way authorization to individuals.
     Such as the individual account authorize another individual account to transfer his asset.
     When call 'check' function,Identity will check the callerId、resource、operation and detail boolean is in AuthTable.
     when detail is null,it means allowId can do operation for the all of the resource.
**/
contract Identity {
    using LibSafeMath for uint256;
    using SignLib for bytes32[4];
    using AddressSetLib for AddressSetLib.Set;
    using AddressMapLib for AddressMapLib.Map;
    using UtilLib for *;

    string constant DEFAULT_GRANT_VALUE = "grant";
    string constant DEFAULT_REVOKE_VALUE = "revoke";

    modifier checkSign(bytes32[4] sign) {
        require(sign.checkSign() == externalAddress, "Identity:required owner called");
        _;
    }
    modifier checkNonce(bytes args, bytes32 msg) {
        require(checkNonceAndArg(args, msg), "Identity: args or nonce not verify");
        _;
    }
    modifier checkHolder(address resource){
        require(resource == address(this) || IResource(resource).isHolder(address(this)), "Identity:id is not the resource's holder");
        _;
    }

    modifier checkPermission(string operation, bytes32[4] sign){
        bytes memory detail;
        require(check(genAuthByTableArgs(address(this), sign), operation, detail), "Identity:Forbbiden ".strConcat(operation));

        _;
    }

    IAclManager aclManager;
    address orgAddress;
    AddressMapLib.Map roleResource;
    AddressSetLib.Set rolesManage;
    AddressSetLib.Set resourceManage;
    address externalAddress;
    uint256 nonce;

    constructor(address _aclAddress, address _externalAddress) public {
        aclManager = IAclManager(_aclAddress);
        orgAddress = msg.sender;
        externalAddress = _externalAddress;
    }



    function createRole(string name, bytes32[4] sign) public checkPermission(DEFAULT_GRANT_VALUE, sign) returns (address){
        Role role = new Role(name);
        rolesManage.insert(address(role));
        return address(role);
    }


    function createResourceGroup(bytes32[4] sign) public checkPermission(DEFAULT_GRANT_VALUE, sign) returns (address){
        ResourceGroup res = new ResourceGroup();
        resourceManage.insert(address(res));
        return address(res);
    }

    function addIdToRole(address idAddr, address roleAddr, bytes32[4] sign) public checkPermission(DEFAULT_GRANT_VALUE, sign) returns (bool){
        require(rolesManage.contains(roleAddr), "role is not exist!");
        Role role = Role(roleAddr);
        role.addAccount(idAddr);
        return true;
    }

    function removeIdFromRole(address idAddr, address roleAddr, bytes32[4] sign) public checkPermission(DEFAULT_REVOKE_VALUE, sign) returns (bool){
        require(rolesManage.contains(roleAddr), "role is not exist!");
        Role role = Role(roleAddr);
        role.removeAccount(idAddr);
        return true;
    }

    function addOperationToRes(address resourceGroup, address resource, string[] operations, bytes32[4] sign) public checkPermission(DEFAULT_GRANT_VALUE, sign) returns (bool){
        require(resourceManage.contains(resourceGroup), "resource is not exist!");
        ResourceGroup res = ResourceGroup(resourceGroup);
        for (uint i = 0; i < operations.length; i++) {
            res.addResource(resource, operations[i]);
        }
        return true;
    }

    function addDetailToRes(address resourceGroup, address resource, string operation, bytes detail, bytes32[4] sign) public checkPermission(DEFAULT_GRANT_VALUE, sign) returns (bool){
        require(resourceManage.contains(resourceGroup), "resource is not exist!");
        ResourceGroup res = ResourceGroup(resourceGroup);
        res.addResource(resource, operation, detail);
        return true;
    }

    function removeOperationFromRes(address resourceGroup, address resource, string operation, bytes32[4] sign) public checkPermission(DEFAULT_REVOKE_VALUE, sign) returns (bool){
        require(resourceManage.contains(resourceGroup), "resource is not exist!");
        ResourceGroup res = ResourceGroup(resourceGroup);
        res.removeOperation(resource, operation);

        return true;
    }

    function removeDetailTFromRes(address resourceGroup, address resource, string operation, bytes detail, bytes32[4] sign) public checkPermission(DEFAULT_REVOKE_VALUE, sign) returns (bool){
        require(resourceManage.contains(resourceGroup), "resource is not exist!");
        ResourceGroup res = ResourceGroup(resourceGroup);
        res.removeDetail(resource, operation, detail);
        return true;
    }

    function addResGroupToRole(address role, address resourceGroup, bytes32[4] sign) public checkPermission(DEFAULT_GRANT_VALUE, sign) returns (bool){
        require(resourceManage.contains(resourceGroup), "resource is not exist!");
        require(rolesManage.contains(role), "role is not exist!");
        roleResource.insert(role, resourceGroup);
        return true;
    }


    function grant(address resource, address allowId, string operation, bytes detail, bytes32[4] sign) public checkPermission(DEFAULT_GRANT_VALUE, sign) returns (bool){
        return aclManager.grant(resource, allowId, operation, detail);
    }


    function revoke(address resource, address allowId, string operation, bytes detail, bytes32[4] sign) public checkPermission(DEFAULT_REVOKE_VALUE, sign) returns (bool){
        return aclManager.revoke(resource, allowId, operation, detail);
    }

    function allowGrant(address allowId, bytes32[4] sign) public checkPermission(DEFAULT_GRANT_VALUE, sign) returns (bool){
        bytes memory detail;
        return aclManager.grant(address(this), allowId, DEFAULT_GRANT_VALUE, detail);
    }

    function allowRevoke(address allowId, bytes32[4] sign) public checkPermission(DEFAULT_GRANT_VALUE, sign) returns (bool){
        bytes memory detail;
        return aclManager.grant(address(this), allowId, DEFAULT_GRANT_VALUE, detail);
    }

    // check the permission from Id
    // @param addressList The list of address.
    //        When check the role and resourceGroup,the addressList is including roleAddress,resourceAddress, callerIdAddress
    //        The other way  is including resourceAddress, callerIdAddress
    // @param  stringList The List including operaiton and detail,operation can not be null.
    function check(address[] addressList, string operation, bytes detail) public view returns (bool){
        if (addressList.length > 2) {
            return checkByGroup(addressList, operation, detail);
        } else {
            return checkByAuthTable(addressList, operation, detail);
        }
    }

    //address resource, address allowId, string operation, bytes detail
    function checkByAuthTable(address[] addressList, string operation, bytes detail) internal view returns (bool){
        return aclManager.check(addressList[0], addressList[1], operation, detail);
    }

    //address roleAddress, address resource, address allowId,string operation, bytes detail
    function checkByGroup(address[] addressList, string operation, bytes detail) internal view returns (bool){
        bool isCheck;
        if (addressList[2] == address(this)) {
            return true;
        }
        require(rolesManage.contains(addressList[0]), "role is not exist!");
        if (roleResource.get(addressList[0]) != address(0) && Role(addressList[0]).existAccount(addressList[2])) {
            isCheck = ResourceGroup(roleResource.get(addressList[0])).hasResource(addressList[1], operation);

            if (!isCheck && detail.length > 0) {
                isCheck = ResourceGroup(roleResource.get(addressList[0])).hasResource(addressList[1], operation, detail);
            }
        }
        return isCheck;
    }

    function setExternalAddress(address _external) public {
        externalAddress = _external;
    }

    function getExternalAddress() public view returns (address){
        return externalAddress;
    }

    function getAcl() public view returns (address){
        return address(aclManager);
    }

    function getNonce() public view returns (uint256){
        return nonce;
    }

    function updateNonce() internal returns (uint256){
        nonce = nonce.add(1);
        return nonce;
    }

    function getResGroupByRole(address role) public view returns (address){
        return roleResource.get(role);
    }

    function checkNonceAndArg(bytes args, bytes32 message) internal returns (bool){
        bytes memory origin = args.bytesAppend(nonce);
        bytes32 argsHash = keccak256(origin);
        if (argsHash != message) {
            return false;
        }
        updateNonce();
        return true;
    }

    function genAuthByTableArgs(address resAddress, bytes32[4] sign) public view returns (address[]){
        bytes32 nullByte;
        address callerId;
        if (keccak256(sign[0]) == keccak256(nullByte)) {
            callerId = IResource(msg.sender).getHolder();
            require(address(0) != callerId, "Identity:caller is not holder");

        } else {
            address txOrgin = sign.checkSign();
            callerId = aclManager.getIdByExternal(txOrgin);
        }
        address[] memory addressList = new address[](2);
        addressList[0] = resAddress;
        addressList[1] = callerId;
        return addressList;
    }

    function genAuthByRoleArgs(address roleAddress, address resAddress, bytes32[4] sign) public view returns (address[]){
        bytes32 nullByte;
        address callerId;
        if (keccak256(sign[0]) == keccak256(nullByte)) {
            callerId = IResource(msg.sender).getHolder();
            require(address(0) != callerId, "Identity:caller is not holder");

        } else {
            address txOrgin = sign.checkSign();
            callerId = aclManager.getIdByExternal(txOrgin);
        }
        address[] memory addressList = new address[](3);
        addressList[0] = roleAddress;
        addressList[1] = resAddress;
        addressList[2] = callerId;
        return addressList;
    }

}
