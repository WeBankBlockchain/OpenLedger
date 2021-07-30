pragma solidity ^0.4.24;
pragma experimental ABIEncoderV2;

import "./UtilLib.sol";

library SignLib
{
    using UtilLib for bytes32;
    using UtilLib for bytes;

    //    function checkSign(bytes args, bytes32 message, uint8 v, bytes32 r, bytes32 s) internal returns (address){
    //        bytes argBytes;
    //        for(uint i=0;i<args.length; i++){
    //
    //        }
    //        bytes32 argsHash;
    //        if (!argsHash.bytes32EQ(message)) {
    //            return false;
    //        }
    //        return ecrecover(message,v,r,s);
    //    }

    function checkSign(bytes32[4] sign) internal returns (address){
        bytes32 message = sign[0];
        bytes32 bv = sign[1];
        uint8 v = uint8(bv.bytes322uint());
        bytes32 r = sign[2];
        bytes32 s = sign[3];

        return ecrecover(message, v, r, s);
    }

}