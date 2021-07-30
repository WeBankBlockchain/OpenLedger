pragma solidity ^0.4.25;
pragma experimental ABIEncoderV2;

import "./interface/IdInterface.sol";
import "./lib/SignLib.sol";
import "./interface/IAclManager.sol";

contract BaseResource {
    using SignLib for bytes32[4];
    address holder;
    constructor(address account) public {
        holder = account;
    }

    function getHolder() public view returns (address){
        return holder;
    }

    function isHolder(address account) public view returns (bool){
        return holder == account;
    }


    function grant(address allowExternalAddr, string operation, string detail, bytes32[4] sign) public returns (bool){
        address allowId = IAclManager(IdInterface(holder).getAcl()).getIdByExternal(allowExternalAddr);
        return IdInterface(holder).grant(address(this), allowId, operation, detail, sign);
    }

    function revoke(address revokeExternalAddr, string operation, string detail, bytes32[4] sign) public returns (bool){
        address revokeId = IAclManager(IdInterface(holder).getAcl()).getIdByExternal(revokeExternalAddr);
        return IdInterface(holder).revoke(address(this), revokeId, operation, detail, sign);
    }

    function checkAuth(bytes32[4] sign, string functionName, string detail) internal view returns (bool isCheck){
        address txOrgin = sign.checkSign();
        address callerId = IAclManager(IdInterface(holder).getAcl()).getIdByExternal(txOrgin);
        address[] memory addressList = new address[](2);
        addressList[0] = address(this);
        addressList[1] = callerId;
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
