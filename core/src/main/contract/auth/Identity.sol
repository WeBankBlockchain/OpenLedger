pragma solidity ^0.4.25;
pragma experimental ABIEncoderV2;

import "./IAclManager.sol";
import "./ResourceGroup.sol";
import "./Role.sol";
import "./storage/AddressSetLib.sol";
import "./storage/AddressMapLib.sol";
import "./lib/LibSafeMath.sol";
import "./lib/SignLib.sol";
import "./lib/UtilLib.sol";

contract Identity {
    using LibSafeMath for uint256;
    using SignLib for bytes32[4];
    using AddressSetLib for AddressSetLib.Set;
    using AddressMapLib for AddressMapLib.Map;
    using UtilLib for *;

    modifier checkSign(bytes32[4] sign) {
        require(sign.checkSign() == externalAddress, "Identity:required owner called");
        _;
    }
    modifier checkNonce(bytes args, bytes32 msg) {
        require(checkNonceAndArg(args, msg), "Identity: args or nonce not verify");
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

    function createRole(string name, bytes32[4] sign) public checkSign(sign) returns (address){
        Role role = new Role(name);
        rolesManage.insert(address(role));
        return address(role);
    }

    function createResourceGroup(bytes32[4] sign) public checkSign(sign) returns (address){
        ResourceGroup res = new ResourceGroup();
        resourceManage.insert(address(res));
        return address(res);
    }

    function addIdToRole(address idAddr, address roleAddr, bytes32[4] sign) public checkSign(sign) returns (bool){
        require(rolesManage.contains(roleAddr), "role is not exist!");
        Role role = Role(roleAddr);
        role.addAccount(idAddr);
        return true;
    }

    function removeIdFromRole(address roleAddr, address idAddr, bytes32[4] sign) public checkSign(sign) returns (bool){
        require(rolesManage.contains(roleAddr), "role is not exist!");
        Role role = Role(roleAddr);
        require(!role.existAccount(idAddr), "id is not exist");
        role.removeAccount(idAddr);
        return true;
    }

    function addOperationToRes(address resourceGroup, address resource, string[] operations, bytes32[4] sign) public checkSign(sign) returns (bool){
        require(resourceManage.contains(resourceGroup), "resource is not exist!");
        ResourceGroup res = ResourceGroup(resourceGroup);
        for (uint i = 0; i < operations.length; i++) {
            res.addResource(resource, operations[i]);
        }
        return true;
    }

    function addDetailToRes(address resourceGroup, address resource, string operation, string detail, bytes32[4] sign) public checkSign(sign) returns (bool){
        require(resourceManage.contains(resourceGroup), "resource is not exist!");
        ResourceGroup res = ResourceGroup(resourceGroup);
        res.addResource(resource, operation, detail);
        return true;
    }

    function addResGroupToRole(address role, address resourceGroup, bytes32[4] sign) public checkSign(sign) returns (bool){
        require(resourceManage.contains(resourceGroup), "resource is not exist!");
        require(rolesManage.contains(role), "role is not exist!");
        roleResource.insert(role, resourceGroup);
        return true;
    }


    function grant(address resource, address allowId, string operation, string detail, bytes32[4] sign) public checkSign(sign) returns (bool){
        bool isGrant;
        if (bytes(detail).length == 0) {
            isGrant = aclManager.grant(resource, allowId, operation);
        } else {
            isGrant = aclManager.grant(resource, allowId, operation, detail);
        }
        return isGrant;
    }


    function revoke(address resource, address allowId, string operation, string detail, bytes32[4] sign) public checkSign(sign) returns (bool){
        bool isRevoke;
        if (bytes(detail).length == 0) {
            isRevoke = aclManager.revoke(resource, allowId, operation);
        } else {
            isRevoke = aclManager.revoke(resource, allowId, operation, detail);
        }
        return isRevoke;
    }

    function check(address resource, address allowId, string operation, string detail, bytes args, bytes32[4] sign) public checkSign(sign) view returns (bool){
        bool isCheck;
        isCheck = aclManager.check(resource, allowId, operation);
        if (!isCheck && bytes(detail).length > 0) {
            isCheck = aclManager.check(resource, allowId, operation, detail);
        }
        return isCheck;
    }

    function check(address roleAddress, address resource, string operation, address allowId, bytes args, bytes32[4] sign) public checkSign(sign) view returns (bool){
        bool isCheck;
        require(rolesManage.contains(roleAddress), "role is not exist!");
        if (roleResource.get(roleAddress) != address(0) && ResourceGroup(roleResource.get(roleAddress)).hasResource(resource, operation) && Role(roleAddress).existAccount(allowId)) {
            isCheck = true;
        }
        return isCheck;
    }

    function setExternalAddress(address _external) public {
        externalAddress = _external;
    }

    function getNonce() public view returns (uint256){
        return nonce;
    }

    function updateNonce() internal returns (uint256){
        nonce = nonce.add(1);
        return nonce;
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

}