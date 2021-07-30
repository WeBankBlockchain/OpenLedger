pragma solidity ^0.4.25;
pragma experimental ABIEncoderV2;

import "./BaseResource.sol";
import "./storage/AddressSetLib.sol";
import "./interface/IdInterface.sol";
import "./lib/UtilLib.sol";
import "./lib/LibTypeConversion.sol";

contract BaseResourceWithRole is BaseResource {
    using AddressSetLib for AddressSetLib.Set;
    using UtilLib for *;
    using LibTypeConversion for *;

    mapping(string => address) roles;
    mapping(string => address) resourceGroup;
    constructor(address account) public BaseResource(account) {
    }

    function createRole(string roleName, bytes32[4] sign) public {
        require(roles[roleName] == address(0), "role has been create");
        address role = IdInterface(holder).createRole(roleName.strConcat(address(this).addressToString()),sign);
        roles[roleName] = role;
        address resGroup = IdInterface(holder).createResourceGroup(sign);
        resourceGroup[roleName] = resGroup;
        require(IdInterface(holder).addResGroupToRole(role, resGroup, sign), "create role failed");
    }

    function grantId(string roleName, address id, bytes32[4] sign) public returns (bool) {
        require(roles[roleName] != address(0), "role has not  been create");
        return IdInterface(holder).addIdToRole(id, roles[roleName], sign);
    }

    function revokeId(string roleName, address id, bytes32[4] sign) public returns (bool) {
        require(roles[roleName] != address(0), "role has not  been create");
        return IdInterface(holder).removeIdFromRole(id, roles[roleName], sign);
    }

    function addOperationToResGroup(string roleName,  string[] operations, bytes32[4] sign) public returns (bool) {
        require(roles[roleName] != address(0), "role has not  been create");
        return IdInterface(holder).addOperationToRes(resourceGroup[roleName], address(this), operations, sign);
    }

    function addDetailToResGroup(string roleName, string operation, string detail, bytes32[4] sign) public returns (bool ) {
        require(roles[roleName] != address(0), "role has not  been create");
        return  IdInterface(holder).addDetailToRes(resourceGroup[roleName], address(this), operation, detail, sign);
    }

    function removeOperation(string roleName, string operation,  bytes32[4] sign) public   returns (bool ) {
        require(roles[roleName] != address(0), "role has not  been create");
        return IdInterface(holder).removeOperationFromRes(resourceGroup[roleName], address(this), operation, sign);
    }

    function removeDetail(string roleName, string operation,  string detail,bytes32[4] sign) public returns (bool ) {
        require(roles[roleName] != address(0), "role has not  been create");
        return IdInterface(holder).removeDetailTFromRes(resourceGroup[roleName], address(this), operation, detail,sign);
    }

    function checkAuth(bytes32[4] sign, string roleName,string functionName,string detail) internal returns (bool isCheck){
        address txOrgin = sign.checkSign();
        address callerId = IAclManager(IdInterface(holder).getAcl()).getIdByExternal(txOrgin);
        address[] memory addressList = new address[](3);
        addressList[0] = roles[roleName];
        addressList[1] = address(this);
        addressList[2] = callerId;
        uint256 size = 0;
        if (bytes(detail).length == 0) {
            size = 1;
        } else {
            size = 2;
        }
        string[] memory stringList = new string[](size);
        stringList[0] = functionName;
        if (bytes(detail).length > 0) {
            stringList[0] = detail;
        }
        isCheck = IdInterface(holder).check(addressList, stringList);
    }


}
