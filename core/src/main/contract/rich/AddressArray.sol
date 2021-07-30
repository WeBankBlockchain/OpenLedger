pragma solidity ^0.4.26;

contract AddressArray {
    uint _cap; // capacity
    uint _len; // current length
    address[] public members;
    address public _creator;  //owner

    constructor(uint cap) public {
        _creator = msg.sender;
        _cap = cap;
        _len = 0;
        members = new address[](_cap);
    }

    modifier onlyOwner {
        require(
            msg.sender == _creator,
            "Only owner can call this function."
        );
        _;
    }

    function memeberOf(address member) public onlyOwner view returns (bool) {
        for (uint i = 0; i < _cap; i++) {
            if (member == members[i]) {
                return true;
            }
        }
        return false;
    }

    //how many slot?
    function space() public onlyOwner view returns (uint) {
        return (_cap - _len);
    }

    function add(address member) public onlyOwner returns (bool) {
        if (space() > 0) {
            for (uint i = 0; i < _len; i++) {
                if (member == members[i]) {
                    return true;
                }
            }

            members[_len] = member;
            _len = _len + 1;
            return true;
        }
        return false;
    }

    function remove(address member) public onlyOwner returns (bool) {
        for (uint i = 0; i < _cap; i++) {
            if (member == members[i]) {
                for (uint j = i; j < _len - 1; j++) {
                    members[j] = members[j + 1];
                }
                _len = _len - 1;
                return true;
            }
        }
        return false;
    }

    function pop() public onlyOwner returns (address) {
        address _res = 0x0;
        if (_len > 0) {
            _res = members[0];

            for (uint i = 0; i < _len - 1; i++) {
                members[i] = members[i + 1];
            }

            _len = _len - 1;
        }
        return _res;
    }
}