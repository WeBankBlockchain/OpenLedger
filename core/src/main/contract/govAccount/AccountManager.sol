pragma solidity ^0.4.25;

import "./WEBasicAccount.sol";
import "./BaseAccount.sol";
import "./UserAccount.sol";

contract AccountManager is WEBasicAccount {
    mapping(address => address) _externalAccountMapping;
    mapping(address => address) _userAccountMapping;

    event LogManageNewAccount(
        address indexed externalAccount,
        address indexed userAccount,
        address accountManagerAddress
    );
    event LogManageExternalAccount(
        address indexed newExternalAccount,
        address indexed oldExternalAccount,
        address accountManagerAddress
    );
    event LogManageFreeze(
        address indexed externalAccount,
        address accountManagerAddress
    );
    event LogManageUnfreeze(
        address indexed externalAccount,
        address accountManagerAddress
    );
    event LogManageCancel(
        address indexed externalAccount,
        address accountManagerAddress
    );

    modifier accountNotExisted(address externalAccount) {
        require(
            _externalAccountMapping[externalAccount] == 0x0,
            "AccountManager: externalAccount already existed."
        );
        _;
    }

    modifier accountExisted(address externalAccount) {
        require(
            _externalAccountMapping[externalAccount] != 0x0,
            "AccountManager: externalAccount not existed."
        );
        _;
    }

    function hasAccount(address externalAccount) public view returns (bool) {
        return _externalAccountMapping[externalAccount] != 0x0;
    }

    function isExternalAccountNormal(address externalAccount)
    public
    view
    returns (bool)
    {
        address account = _externalAccountMapping[externalAccount];
        if (account == 0x0) {
            return false;
        }
        BaseAccount a = BaseAccount(account);
        return a.isNormal();
    }

    function newAccount(address externalAccount, address data)
    public
    accountNotExisted(externalAccount)
    returns (bool, address)
    {
        UserAccount userAccount = new UserAccount(this, msg.sender, externalAccount, data);
        address ua = address(userAccount);
        _externalAccountMapping[externalAccount] = ua;
        _userAccountMapping[ua] = externalAccount;
        emit LogManageNewAccount(externalAccount, ua, this);
        return (true, ua);
    }

    function setAccountData(address externalAccount, address data)
    internal
    accountExisted(externalAccount) onlyOwner
    returns (bool)
    {
        UserAccount userAccount = UserAccount(_externalAccountMapping[externalAccount]);
        return userAccount.setData(data);
    }

    function getExternalAccount(address userAddress) public view returns (address) {
        return _userAccountMapping[userAddress];
    }

    function setExternalAccount(
        address newExternalAccount,
        address oldExternalAccount
    ) internal accountExisted(oldExternalAccount) returns (bool) {
        address userAccount = _externalAccountMapping[oldExternalAccount];
        _externalAccountMapping[newExternalAccount] = userAccount;
        _externalAccountMapping[oldExternalAccount] = 0x0;
        _userAccountMapping[userAccount] = newExternalAccount;
        emit LogManageExternalAccount(
            newExternalAccount,
            oldExternalAccount,
            this
        );
        UserAccount ac = UserAccount(userAccount);
        ac.setOwnerByManager(newExternalAccount);
        return true;
    }

    function setExternalAccountByGovernance(
        address newExternalAccount,
        address oldExternalAccount
    ) public onlyOwner returns (bool) {
        return setExternalAccount(newExternalAccount, oldExternalAccount);
    }

    function setExternalAccountByUser(address newExternalAccount)
    public
    returns (bool)
    {
        return setExternalAccount(newExternalAccount, msg.sender);
    }

    function setExternalAccountByAdmin(
        address newExternalAccount,
        address oldExternalAccount
    ) public accountExisted(oldExternalAccount) returns (bool) {
        address userAccount = _externalAccountMapping[oldExternalAccount];
        UserAccount ac = UserAccount(userAccount);
        require(ac.getAccountAdmin() == msg.sender, "AccountManager: only account admin is authorized.");

        _externalAccountMapping[newExternalAccount] = userAccount;
        _externalAccountMapping[oldExternalAccount] = 0x0;
        _userAccountMapping[userAccount] = newExternalAccount;
        emit LogManageExternalAccount(
            newExternalAccount,
            oldExternalAccount,
            this
        );
        ac.setOwnerByManager(newExternalAccount);
        return true;
    }

    function getUserAccount(address externalAccount)
    public
    view
    returns (address)
    {
        return _externalAccountMapping[externalAccount];
    }

    function freeze(address externalAccount) public onlyOwner returns (bool) {
        address account = _externalAccountMapping[externalAccount];
        BaseAccount a = BaseAccount(account);
        emit LogManageFreeze(externalAccount, this);
        return a.freeze();
    }

    function freezeByAdmin(address externalAccount) public returns (bool) {
        address account = _externalAccountMapping[externalAccount];
        BaseAccount a = BaseAccount(account);
        require(a.getAccountAdmin() == msg.sender, "AccountManager: only admin is authorized.");

        emit LogManageFreeze(externalAccount, this);
        return a.freeze();
    }

    function unfreeze(address externalAccount) public onlyOwner returns (bool) {
        address account = _externalAccountMapping[externalAccount];
        BaseAccount a = BaseAccount(account);
        emit LogManageUnfreeze(externalAccount, this);
        return a.unfreeze();
    }

    function unfreezeByAdmin(address externalAccount) public returns (bool) {
        address account = _externalAccountMapping[externalAccount];
        BaseAccount a = BaseAccount(account);
        require(a.getAccountAdmin() == msg.sender, "AccountManager: only admin is authorized.");

        emit LogManageUnfreeze(externalAccount, this);
        return a.unfreeze();
    }


    function cancel(address externalAccount) internal accountExisted(externalAccount) returns (bool b)
    {
        address account = _externalAccountMapping[externalAccount];
        _externalAccountMapping[externalAccount] = 0x0;
        _userAccountMapping[account] = 0x0;
        BaseAccount a = BaseAccount(account);
        emit LogManageCancel(externalAccount, this);
        b = a.cancel();
    }

    function cancelByGovernance(address externalAccount) onlyOwner returns (bool)
    {
        return cancel(externalAccount);
    }

    function cancelByUser() returns (bool) {
        return cancel(msg.sender);
    }

    function cancelByAdmin(address externalAccount) public accountExisted(externalAccount) returns (bool b)
    {
        address account = _externalAccountMapping[externalAccount];
        BaseAccount a = BaseAccount(account);
        require(a.getAccountAdmin() == msg.sender, "AccountManager: only admin is authorized.");

        _externalAccountMapping[externalAccount] = 0x0;
        _userAccountMapping[account] = 0x0;
        emit LogManageCancel(externalAccount, this);
        b = a.cancel();
    }
}
