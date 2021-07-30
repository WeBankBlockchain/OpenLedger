pragma solidity ^0.4.25;

import "./WEBasicAccount.sol";
import "./BaseAccountInterface.sol";

contract BaseAccount is  WEBasicAccount,BaseAccountInterface {
    address public _accountManager;
    address internal _accountAdmin;

    // 0-normal, 1-frozen, 2-closed
    uint8 public _status = 0;

    modifier onlyAccountManager() {
        require(msg.sender == _accountManager || msg.sender == _accountAdmin,
            "BaseAccount: only account manager.");
        _;
    }

    modifier onlyNormal() {
        require(isNormal(), "BaseAccount: only account status is normal.");
        _;
    }

    modifier onlyFrozen() {
        require(_status == 1, "BaseAccount: only account status is normal.");
        _;
    }

    modifier onlyAuthorized() {
        require(
            msg.sender == _accountManager || msg.sender == _owner || msg.sender == _accountAdmin,
            "BaseAccount: only authorized."
        );
        _;
    }

    constructor(address accountManager, address accountAdmin) public {
        _accountManager = accountManager;
        _accountAdmin = accountAdmin;// ==org
    }

    function isNormal() public returns (bool) {
        return _status == 0;
    }

    function cancel() public onlyAccountManager returns (bool) {
        _status = 2;
        emit LogBaseAccount("cancel", this);
        return true;
    }

    function freeze() public onlyAccountManager onlyNormal returns (bool) {
        _status = 1;
        emit LogBaseAccount("freeze", this);
        return true;
    }

    function unfreeze() public onlyAccountManager onlyFrozen returns (bool) {
        _status = 0;
        emit LogBaseAccount("unfreeze", this);
        return true;
    }

    function getAccountAdmin() public returns (address) {
        return _accountAdmin;
    }

    function isAccountAdmin(address caller) public view returns(bool){
        return caller == _accountAdmin;
    }
}
