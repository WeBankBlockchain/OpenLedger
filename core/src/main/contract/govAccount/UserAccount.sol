pragma solidity ^0.4.25;

import "./AccountManager.sol";
import "./BaseAccount.sol";


contract UserAccount is BaseAccount {
    // data address
    address private _data;

    constructor(address accountManager, address admin, address owner, address data)
    public
    BaseAccount(accountManager, admin)
    {
        setOwner(owner);
        _data = data;
    }

    function setOwnerByManager(address owner)
    public
    onlyAccountManager
    returns (bool)
    {
        _owner = owner;
        return true;
    }

    function setData(address data) public onlyAccountManager returns (bool) {
        _data = data;
        return true;
    }

    function getData() public returns (address) {
        return _data;
    }


}
