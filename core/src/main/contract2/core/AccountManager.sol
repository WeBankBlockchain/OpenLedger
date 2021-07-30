pragma solidity ^0.4.25;


import "./govAccount/WEBasicAccount.sol";
import "./govAccount/BaseAccount.sol";
import "./storage/AddressMapLib.sol";
import "./account/base/BaseAccountHolder.sol";



contract AccountManager is WEBasicAccount {
    using AddressMapLib for AddressMapLib.Map;
    AddressMapLib.Map accounts;
    event LogManageNewAccount(
        address indexed externalAccount,
        address indexed userAccount,
        address accountAddrManagerAddress
    );
    event LogManageExternalAccount(
        address indexed newExternalAccount,
        address indexed oldExternalAccount,
        address accountAddrManagerAddress
    );
    event LogManageFreeze(
        address indexed externalAccount,
        address accountAddrManagerAddress
    );
    event LogManageUnfreeze(
        address indexed externalAccount,
        address accountAddrManagerAddress
    );
    event LogManageCancel(
        address indexed externalAccount,
        address accountAddrManagerAddress
    );

    modifier accountNotExisted(address accountAddr) {
        require(
            !accounts.contains(accountAddr) ,
            "AccountManager: externalAccount already existed."
        );
        _;
    }

    modifier accountExisted(address accountAddr) {
        require(
            accounts.contains(accountAddr) ,
            "AccountManager: externalAccount not existed."
        );
        _;
    }


    modifier onlyAcccountAdmin(address accountAddr) {
        require(
            BaseAccount(accountAddr).isAccountAdmin(msg.sender),
            "AccountManager: required account admin!"
        );
        _;
    }
    function hasAccount(address accountAddr) public view returns (bool) {
        return  accounts.contains(accountAddr);
    }

    function isExternalAccountNormal(address accountAddr)
    public
    view
    returns (bool)
    {
        if (accountAddr == 0x0) {
            return false;
        }
        BaseAccount a = BaseAccount(accountAddr);
        return a.isNormal();
    }
    // data is
    function newAccount(address project,address holderId)
    public
    returns (bool, address)
    {
        BaseAccountHolder account  = new BaseAccountHolder(project,holderId,address(this),msg.sender);
        accounts.insert(msg.sender,address(account));
        return (true, account);
    }



    function freeze(address accountAddr) public onlyAcccountAdmin(accountAddr) accountExisted(accountAddr) returns (bool) {
        BaseAccount a = BaseAccount(accountAddr);
        emit LogManageFreeze(accountAddr, this);
        return a.freeze();
    }

    function freezeByAdmin(address accountAddr) public onlyAcccountAdmin(accountAddr) accountExisted(accountAddr)returns (bool) {
        BaseAccount a = BaseAccount(accountAddr);
        require(a.getAccountAdmin() == msg.sender, "AccountManager: only admin is authorized.");

        emit LogManageFreeze(accountAddr, this);
        return a.freeze();
    }

    function unfreeze(address accountAddr) public onlyAcccountAdmin(accountAddr) accountExisted(accountAddr) returns (bool) {
        BaseAccount a = BaseAccount(accountAddr);
        emit LogManageUnfreeze(accountAddr, this);
        return a.unfreeze();
    }

    function unfreezeByAdmin(address accountAddr) public onlyAcccountAdmin(accountAddr)  accountExisted(accountAddr) returns (bool) {
        BaseAccount a = BaseAccount(accountAddr);
        require(a.getAccountAdmin() == msg.sender, "AccountManager: only admin is authorized.");

        emit LogManageUnfreeze(accountAddr, this);
        return a.unfreeze();
    }


    function cancel(address accountAddr) internal accountExisted(accountAddr)  returns (bool b)
    {
        BaseAccount a = BaseAccount(accountAddr);
        emit LogManageCancel(accountAddr, this);
        b = a.cancel();
    }

    function cancelByGovernance(address accountAddr) onlyOwner returns (bool)
    {
        return cancel(accountAddr);
    }

    function cancelByUser() returns (bool) {
        return cancel(msg.sender);
    }

    function cancelByAdmin(address accountAddr) public accountExisted(accountAddr) onlyAcccountAdmin(accountAddr) returns (bool b)
    {
        BaseAccount a = BaseAccount(accountAddr);
        require(a.getAccountAdmin() == msg.sender, "AccountManager: only admin is authorized.");
        emit LogManageCancel(accountAddr, this);
        b = a.cancel();
    }
}
