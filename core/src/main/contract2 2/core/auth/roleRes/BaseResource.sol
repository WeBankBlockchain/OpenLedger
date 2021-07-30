pragma solidity ^0.4.25;
pragma experimental ABIEncoderV2;

import "../interface/IdInterface.sol";
import "../interface/IAclManager.sol";
import "../../lib/SignLib.sol";

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


    function grant(address allowExternalAddr, string operation, bytes detail, bytes32[4] sign) public returns (bool){
        address allowId = IAclManager(IdInterface(holder).getAcl()).getIdByExternal(allowExternalAddr);
        return IdInterface(holder).grant(address(this), allowId, operation, detail, sign);
    }

    function revoke(address revokeExternalAddr, string operation, bytes detail, bytes32[4] sign) public returns (bool){
        address revokeId = IAclManager(IdInterface(holder).getAcl()).getIdByExternal(revokeExternalAddr);
        return IdInterface(holder).revoke(address(this), revokeId, operation, detail, sign);
    }

    function checkAuth(bytes32[4] sign, string functionName, bytes detail) internal view returns (bool isCheck){
        address txOrgin = sign.checkSign();
        address callerId = IAclManager(IdInterface(holder).getAcl()).getIdByExternal(txOrgin);
        address[] memory addressList = new address[](2);
        addressList[0] = address(this);
        addressList[1] = callerId;
        isCheck = IdInterface(holder).check(addressList, functionName, detail);
    }
}
