pragma solidity ^0.4.0;

import "./NonFungibleStorage.sol";

contract NonFunStorageManager {
    address manager;
    constructor(address _manager){
        manager = _manager;
    }
    function createStorage() public returns (address){
        require(msg.sender == manager, "msg.sender not verify");
        NonFungibleStorage nonFungibleStorage = new NonFungibleStorage(manager);
        return address(nonFungibleStorage);
    }


}
