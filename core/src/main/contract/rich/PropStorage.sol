pragma solidity ^0.4.26;

//属性存储合约，以KV形式存储属性值
contract PropStorage {
    address _owner;
    mapping(string => string) _props;

    constructor() public {
        _owner = msg.sender;
    }

    function set(string key, string value) public {
        _props[key] = value;
    }

    function get(string key) public returns(string) {
        return _props[key];
    }
}