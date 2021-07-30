pragma solidity ^0.4.25;


contract WEBasicAccount {
    address public _owner;

    event LogSetOwner(address indexed owner, address indexed contractAddress);

    constructor() public {
        _owner = msg.sender;
    }

    function setOwner(address owner) public onlyOwner {
        _owner = owner;
        emit LogSetOwner(owner, this);
    }

    modifier onlyOwner() {
        require(msg.sender == _owner, "WEBasicAccount: only owner is authorized.");
        _;
    }
}
