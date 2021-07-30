pragma solidity ^0.4.24;
pragma experimental ABIEncoderV2;


// refer: https://github.com/ethereum/dapp-bin/blob/master/library/iterable_mapping.sol
// k: address , v: address
library Address2BytesMapLib
{
    struct Map
    {
        mapping(address => Value) data;
        Key[] keys;
        uint length;
    }


    struct Value {uint keyIndex; bytes value;}

    struct Key {address key; bool deleted;}

    bytes constant COMMAND_MODIFY = "MODIFY";
    bytes constant COMMAND_DELETE = "DELETE";

    event LogHistory(address txOrigin, address msgSender, bytes cmd, address key, bytes value);


    function add(Map storage self, address key, bytes value) internal returns (bool ret)
    {
        uint keyIndex = self.data[key].keyIndex;
        if (keyIndex > 0) {
            return false;
        }
        else
        {
            self.data[key].value = value;
            keyIndex = self.keys.length++;
            self.data[key].keyIndex = keyIndex + 1;
            self.keys[keyIndex].key = key;
            self.length++;
            return true;
        }
    }

    function set(Map storage self, address key, bytes value) internal returns (bool ret)
    {
        uint keyIndex = self.data[key].keyIndex;
        if (keyIndex > 0) {
            emit LogHistory(tx.origin, msg.sender, COMMAND_MODIFY, key, self.data[key].value);
            self.data[key].value = value;
            return true;
        }
        else
        {
            return false;
        }
    }

    function insert(Map storage self, address key, bytes value) internal returns (bool replaced)
    {
        uint keyIndex = self.data[key].keyIndex;
        if (keyIndex > 0) {
            emit LogHistory(tx.origin, msg.sender, COMMAND_MODIFY, key, self.data[key].value);
            self.data[key].value = value;
            return true;
        }
        else
        {
            self.data[key].value = value;
            keyIndex = self.keys.length++;
            self.data[key].keyIndex = keyIndex + 1;
            self.keys[keyIndex].key = key;
            self.length++;
            return false;
        }
    }

    function remove(Map storage self, address key) internal returns (bool)
    {
        uint keyIndex = self.data[key].keyIndex;
        if (keyIndex == 0)
            return false;
        emit LogHistory(tx.origin, msg.sender, COMMAND_DELETE, key, self.data[key].value);
        delete self.data[key];
        self.keys[keyIndex - 1].deleted = true;
        self.length --;
        return true;
    }

    function contains(Map storage self, address key) internal view returns (bool)
    {
        return self.data[key].keyIndex > 0;
    }

    function get(Map storage self, address key) internal view returns (bytes)
    {
        return self.data[key].value;
    }

    function size(Map storage self) internal view returns (uint)
    {
        return self.length;
    }

    function iterate_start(Map storage self) internal view returns (uint keyIndex)
    {
        return iterate_next(self, uint(- 1));
    }

    function iterate_valid(Map storage self, uint keyIndex) internal view returns (bool)
    {
        return keyIndex < self.keys.length;
    }

    function iterate_next(Map storage self, uint keyIndex) internal view returns (uint r_keyIndex)
    {
        keyIndex++;
        while (keyIndex < self.keys.length && self.keys[keyIndex].deleted)
            keyIndex++;
        return keyIndex;
    }

    function iterate_get(Map storage self, uint keyIndex) internal view returns (address key, bytes value)
    {
        key = self.keys[keyIndex].key;
        value = self.data[key].value;
    }


    function insertBatch(Map storage self, address[] keyList, bytes[] valList) internal returns (uint size)
    {
        require(keyList.length != 0 && keyList.length == valList.length, "key or value list not match");

        uint length = keyList.length;
        for (uint i = 0; i < length; i++)
        {
            bool replaced = insert(self, keyList[i], valList[i]);
            if (replaced == true) {
                size++;
            }
        }
    }

    function getBatch(Map storage self, address[] keys) internal view returns (bytes[] memory valList)
    {
        require(keys.length != 0, "key list is not allowed empty");

        uint length = keys.length;
        valList = new bytes[](length);
        for (uint i = 0; i < length; i++) {
            valList[i] = get(self, keys[i]);
        }

    }

    // num need to limit
    // index is not sequential
    // nextIndex = 0 means the end
    function getByIndex(Map storage self, uint start, uint num) internal view returns (address[] memory keyList, bytes[] memory valList, uint retNum, uint nextIndex)
    {
        keyList = new address[](num);
        valList = new bytes[](num);

        uint i = 0;
        uint j = 0;
        retNum = 0;
        nextIndex = 0;
        for (i = start; iterate_valid(self, i); i = iterate_next(self, i))
        {
            var (key, value) = iterate_get(self, i);
            keyList[j] = key;
            valList[j] = value;
            j += 1;
            retNum++;
            if (retNum >= num)
            {
                nextIndex = iterate_next(self, i);
                if (!iterate_valid(self, nextIndex))
                {
                    nextIndex = 0;
                }
                break;
            }
        }
    }


    function getAll(Map storage self) internal view returns (address[] memory keyList, bytes[] memory valList, uint retNum)
    {
        uint nextIndex = 0;
        (keyList, valList, retNum, nextIndex) = getByIndex(self, uint(0), size(self));
    }


    // Computes the sum of all stored data.
    //    function toStr() internal view returns (bytes s)
    //    {
    //        for (var i = iterate_start(); iterate_valid(i); i = iterate_next(i))
    //        {
    //            var (key, value) = iterate_get(i);
    //            s = bytesConcat(s, key);
    //            s = bytesConcat(s, value);
    //        }
    //    }

}
