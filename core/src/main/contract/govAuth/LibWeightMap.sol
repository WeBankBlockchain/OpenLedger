pragma solidity ^0.4.25;

library LibWeightMap {

    struct Map {
        mapping(address => uint256) index;
        address[] keys;
        uint16[] values;
    }

    function put(Map storage map, address key, uint16 value) internal {
        uint256 idx = map.index[key];
        if (idx == 0) {
            map.keys.push(key);
            map.values.push(value);
            map.index[key] = map.keys.length;
        }
        else {
            map.values[idx - 1] = value;
        }
    }

    function getKey(Map storage map, uint16 index) internal view returns (address){
        require(map.keys.length > index);
        address key = map.keys[index - 1];
        return key;
    }

    function getValue(Map storage map, address key) internal view returns (uint16){
        uint256 idx = map.index[key];
        uint16 value = map.values[idx - 1];
        return value;
    }

    function size(Map storage self) internal view returns (uint256) {
        return self.keys.length;
    }

    function getMap(Map storage self) internal view returns (address[], uint16[]) {
        return (self.keys, self.values);
    }
}















