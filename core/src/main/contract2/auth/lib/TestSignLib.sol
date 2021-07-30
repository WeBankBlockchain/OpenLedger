pragma solidity ^0.4.24;
pragma experimental ABIEncoderV2;

import "./SignLib.sol";

library TestSignLib
{
    using SignLib for *;

    function t_checkSign(bytes args, bytes32[4] sign) internal view returns (address){
        return sign.checkSign();
    }
}