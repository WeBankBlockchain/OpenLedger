pragma solidity ^0.4.24;
pragma experimental ABIEncoderV2;

// refer: https://github.com/ethereum/dapp-bin/blob/master/library/iterable_mapping.sol
contract Storage
{
    struct Map
    {
        mapping(bytes => Value) data;
        Key[] keys;
        uint size;
    }

    event LogHistory(address txOrigin, address msgSender, bytes cmd, bytes key, bytes value);

    struct Value {uint keyIndex; bytes value;}

    struct Key {bytes key; bool deleted;}


    bytes constant private  COMMAND_MODIFY = "MODIFY";
    bytes constant private  COMMAND_DELETE = "DELETE";

    Map map;

    function Storage(){
    }

    function add(bytes key, bytes value) returns (bool ret)
    {
        uint keyIndex = map.data[key].keyIndex;
        if (keyIndex > 0) {
            return false;
        }
        else
        {
            map.data[key].value = value;
            keyIndex = map.keys.length++;
            map.data[key].keyIndex = keyIndex + 1;
            map.keys[keyIndex].key = key;
            map.size++;
            return true;
        }
    }

    function set(bytes key, bytes value) returns (bool ret)
    {
        uint keyIndex = map.data[key].keyIndex;
        if (keyIndex > 0) {
            emit LogHistory(tx.origin, msg.sender, COMMAND_MODIFY, key, map.data[key].value);
            map.data[key].value = value;
            return true;
        }
        else
        {
            return false;
        }
    }

    function insert(bytes key, bytes value) returns (bool replaced)
    {
        uint keyIndex = map.data[key].keyIndex;
        if (keyIndex > 0) {
            emit LogHistory(tx.origin, msg.sender, COMMAND_MODIFY, key, map.data[key].value);
            map.data[key].value = value;
            return true;
        }
        else
        {
            map.data[key].value = value;
            keyIndex = map.keys.length++;
            map.data[key].keyIndex = keyIndex + 1;
            map.keys[keyIndex].key = key;
            map.size++;
            return false;
        }
    }

    function remove(bytes key) returns (bool)
    {
        uint keyIndex = map.data[key].keyIndex;
        if (keyIndex == 0)
            return false;
        emit LogHistory(tx.origin, msg.sender, COMMAND_DELETE, key, map.data[key].value);
        delete map.data[key];
        map.keys[keyIndex - 1].deleted = true;
        map.size --;
        return true;
    }

    function contains(bytes key) constant returns (bool)
    {
        return map.data[key].keyIndex > 0;
    }

    function get(bytes key) constant returns (bytes)
    {
        return map.data[key].value;
    }

    function size() constant returns (uint)
    {
        return map.size;
    }

    function iterate_start() constant returns (uint keyIndex)
    {
        return iterate_next(uint(- 1));
    }

    function iterate_valid(uint keyIndex) constant returns (bool)
    {
        return keyIndex < map.keys.length;
    }

    function iterate_next(uint keyIndex) constant returns (uint r_keyIndex)
    {
        keyIndex++;
        while (keyIndex < map.keys.length && map.keys[keyIndex].deleted)
            keyIndex++;
        return keyIndex;
    }

    function iterate_get(uint keyIndex) constant returns (bytes key, bytes value)
    {
        key = map.keys[keyIndex].key;
        value = map.data[key].value;
    }


    function insertBatch(bytes[] kvList) returns (uint size)
    {
        uint length = kvList.length;
        for (uint i = 0; i < length; i += 2)
        {
            bool replaced = insert(kvList[i], kvList[i + 1]);
            if (replaced == true) {
                size++;
            }
        }
    }

    function getBatch(bytes[] keys) constant returns (bytes [] memory kvList)
    {
        uint length = keys.length;
        kvList = new bytes[](length * 2);
        for (uint i = 0; i < length; i++) {
            var value = get(keys[i]);
            kvList[i * 2] = keys[i];
            kvList[i * 2 + 1] = value;
        }

    }

    // num need to limit
    // index is not sequential
    // nextIndex = 0 means the end
    function getByIndex(uint start, uint num) constant returns (bytes[] memory kvList, uint retNum, uint nextIndex)
    {
        kvList = new bytes[](num * 2);

        uint i = 0;
        uint j = 0;
        retNum = 0;
        nextIndex = 0;
        for (i = start; iterate_valid(i); i = iterate_next(i))
        {
            var (key, value) = iterate_get(i);
            kvList[j] = key;
            kvList[j + 1] = value;
            j += 2;
            retNum++;
            if (retNum >= num)
            {
                nextIndex = iterate_next(i);
                if (!iterate_valid(nextIndex))
                {
                    nextIndex = 0;
                }
                break;
            }
        }
    }


    function getAll() constant returns (bytes[] memory kvList, uint retNum)
    {
        uint nextIndex;
        (kvList, retNum, nextIndex) = getByIndex(uint(0), size());
    }

    // Computes the sum of all stored data.
    function toStr() constant returns (bytes s)
    {
        for (var i = iterate_start(); iterate_valid(i); i = iterate_next(i))
        {
            var (key, value) = iterate_get(i);
            s = bytesConcat(s, key);
            s = bytesConcat(s, value);
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

}
