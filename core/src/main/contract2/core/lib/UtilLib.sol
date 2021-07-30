pragma solidity ^0.4.24;
pragma experimental ABIEncoderV2;

library UtilLib
{
    function isStrEmpty(string a) internal view returns (bool){
        return bytes(a).length == 0;
    }

    function strEQ(string a, string b) internal view returns (bool){
        if (bytes(a).length != bytes(b).length) {
            return false;
        } else {
            return keccak256(a) == keccak256(b);
        }
    }

    function bytesEQ(bytes a, bytes b) internal view returns (bool){
        if (a.length != b.length) {
            return false;
        } else {
            return keccak256(a) == keccak256(b);
        }
    }

    function bytesConcat(bytes _a, bytes _b) internal returns (bytes){
        bytes memory _ba = bytes(_a);
        bytes memory _bb = bytes(_b);
        bytes memory ret = new bytes(_ba.length + _bb.length);
        bytes memory bret = bytes(ret);
        uint k = 0;
        for (uint i = 0; i < _ba.length; i++) bret[k++] = _ba[i];
        for (i = 0; i < _bb.length; i++) bret[k++] = _bb[i];
        return bytes(ret);
    }

    function strConcat(string _a, string _b) internal returns (string){
        bytes memory _ba = bytes(_a);
        bytes memory _bb = bytes(_b);
        bytes memory ret = new bytes(_ba.length + _bb.length);
        bytes memory bret = bytes(ret);
        uint k = 0;
        for (uint i = 0; i < _ba.length; i++) bret[k++] = _ba[i];
        for (i = 0; i < _bb.length; i++) bret[k++] = _bb[i];
        return string(ret);
    }

    //https://ethereum.stackexchange.com/questions/4170/how-to-convert-a-uint-to-bytes-in-solidity
    function uint2bytes(uint256 x) internal returns (bytes b) {
        b = new bytes(32);
        assembly {mstore(add(b, 32), x)}
    }


    function bytes2uint(bytes memory b) internal view returns (uint256){
        uint256 number = 0;
        for (uint i = 0; i < b.length; i++) {
            number = number + uint8(b[i]) * (2 ** (8 * (b.length - (i + 1))));
        }
        return number;
    }

    function bytes322uint(bytes32 b) internal view returns (uint256){
        uint256 number = 0;
        for (uint i = 0; i < 32; i++) {
            number = number + uint8(b[i]) * (2 ** (8 * (32 - (i + 1))));
        }
        return number;
    }


    function addr2bytes(address addr) internal returns (bytes c){
        bytes20 b = bytes20(addr);
        c = new bytes(20);
        for (uint i = 0; i < 20; i++) {
            c[i] = b[i];
        }
    }

    function bytes2addr(bytes b) internal returns (address addr){
        require(b.length == 20, "bytes not valid address");

        uint160 num = 0;
        for (uint i = 0; i < b.length; i++) {
            num = num + uint160(uint8(b[i]) * (2 ** (8 * (b.length - (i + 1)))));
        }
        return num;
    }


    function bytesAppend(bytes b1, bytes b2) internal returns (bytes) {
        return bytesConcat(b1, b2);
    }

    function bytesAppend(bytes _a, bytes32 _b) internal returns (bytes) {
        bytes memory _ba = bytes(_a);
        bytes memory ret = new bytes(_ba.length + _b.length);
        bytes memory bret = bytes(ret);
        uint k = 0;
        for (uint i = 0; i < _ba.length; i++) bret[k++] = _ba[i];
        for (i = 0; i < _b.length; i++) bret[k++] = _b[i];
        return bytes(ret);
    }

    function bytesAppend(bytes b1, address a) internal returns (bytes) {
        return bytesConcat(b1, addr2bytes(a));
    }

    function bytesAppend(bytes b1, uint a) internal returns (bytes) {
        return bytesConcat(b1, uint2bytes(a));
    }

    function bytesAppend(bytes b1, string s) internal returns (bytes) {
        return bytesConcat(b1, bytes(s));
    }

    function equal(bytes memory self, bytes memory other) internal pure returns(bool){
        if(self.length != other.length){
            return false;
        }
        uint selfLen = self.length;
        for(uint i=0;i<selfLen;i++){
            if(self[i] != other[i]) return false;
        }
        return true;
    }

}