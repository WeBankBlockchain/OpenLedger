pragma solidity ^0.4.24;
pragma experimental ABIEncoderV2;

library AddressSetLib {

    struct Set {
        mapping(address => uint) IndexList;
        address[] keyList; //keyList[index-1]
    }


    //Returns: true if the set contained the specified element
    function insert(Set storage self, address key) internal returns (bool) {
        require(key != 0x0, "AddressSet: Key cannot be 0x0");
        if (contains(self, key)) {
            return true;
        }
        self.IndexList[key] = self.keyList.push(key);
        return false;
    }

    //Returns: true if the set contained the specified element
    function remove(Set storage self, address key) internal returns (bool) {
        //        require(exists(self, key), "AddressSet: Address (key) does not exist in the set.");
        if (!contains(self, key)) {
            return false;
        }

        //step 1. move the lastKey to removedIndex
        //step 2. delete index
        address lastKey = self.keyList[self.keyList.length - 1];
        uint toBeRemovedIndex = self.IndexList[key];
        self.IndexList[lastKey] = toBeRemovedIndex;
        self.keyList[toBeRemovedIndex - 1] = lastKey;
        delete self.IndexList[key];
        self.keyList.length--;
        return true;
    }

    function size(Set storage self) internal view returns (uint) {
        return self.keyList.length;
    }

    function contains(Set storage self, address key) internal view returns (bool) {
        return self.IndexList[key] != 0;
    }

    function get(Set storage self, uint index) internal view returns (address) {
        return self.keyList[index];
    }

    function geti(Set storage self, address key) internal view returns (uint) {
        return self.IndexList[key];
    }

    function getAll(Set storage self) internal view returns (address[]) {
        return self.keyList;
    }

    function getByIndex(Set storage self, uint start, uint num) internal view returns (address[] memory keyList) {
        uint sumNum = size(self);
        if (sumNum == 0) {
            return keyList;
        }
        require(start <= sumNum - 1, "index out of bounds");

        uint endIndex = start + num - 1;
        if (endIndex >= sumNum) {
            endIndex = sumNum - 1;
        }
        require(start <= endIndex, "num is too large");

        uint retNum = endIndex - start + 1;
        keyList = new address[](retNum);

        uint i = 0;
        uint j = 0;
        for (i = start; i <= endIndex; i++)
        {
            keyList[j] = self.keyList[i];
        }
    }

    //only for test
    function log(Set storage self) internal view returns (address[] memory a, uint[] memory b) {
        uint num = size(self);
        a = new address[](num);
        b = new uint[](num);

        for (uint i = 0; i < num; i++) {
            a[i] = get(self, i);
            b[i] = geti(self, a[i]);
        }

    }

}
