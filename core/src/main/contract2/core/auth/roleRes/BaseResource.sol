pragma solidity ^0.4.25;
pragma experimental ABIEncoderV2;

import "../interface/IdInterface.sol";
import "../interface/IAclManager.sol";
import "../../lib/SignLib.sol";
import "../../lib/UtilLib.sol";

contract BaseResource {
    modifier holderNotNull() {
        require(holder != address(0), "BaseResource:holder is null!");
        _;
    }
    using UtilLib for *;
    using SignLib for bytes32[4];
    address holder;
    constructor(address account) public {
        if (account != address(0)) {
            holder = account;
        }
    }

    function getHolder() public view returns (address){
        return holder;
    }

    function isHolder(address account) public view returns (bool){
        return holder == account;
    }


    function grant(address allowExternalAddr, string operation, bytes detail, bytes32[4] sign) public holderNotNull returns (bool){
        address allowId = IAclManager(IdInterface(holder).getAcl()).getIdByExternal(allowExternalAddr);
        return IdInterface(holder).grant(address(this), allowId, operation, detail, sign);
    }

    function revoke(address revokeExternalAddr, string operation, bytes detail, bytes32[4] sign) public holderNotNull returns (bool){
        address revokeId = IAclManager(IdInterface(holder).getAcl()).getIdByExternal(revokeExternalAddr);
        return IdInterface(holder).revoke(address(this), revokeId, operation, detail, sign);
    }

    function checkAuth(bytes32[4] sign, string functionName, bytes detail) internal holderNotNull view returns (bool isCheck){
        address txOrgin = sign.checkSign();
        address callerId = IAclManager(IdInterface(holder).getAcl()).getIdByExternal(txOrgin);
        address[] memory addressList = new address[](2);
        addressList[0] = address(this);
        addressList[1] = callerId;
        isCheck = IdInterface(holder).check(addressList, functionName, detail);
    }

    function checkHolderByAddress(address res, string operation, bytes32[4] sign) public returns (bool) {
        if (address(0) == res) {
            bytes memory detail;
            return checkAuth(sign, operation, detail);
        } else {
            return checkAuth(sign, operation, res.addr2bytes());
        }
    }
}
