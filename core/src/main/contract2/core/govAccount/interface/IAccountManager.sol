pragma solidity ^0.4.25;

import "./IWEBasicAccount.sol";


contract IAccountManager is IWEBasicAccount {
    function hasAccount(address externalAccount) public view returns (bool);

    function isExternalAccountNormal(address externalAccount) public view returns (bool);

    function newAccount(address project, address holderId) public returns (bool, address);

    function getExternalAccount(address userAddress) public view returns (address);

    function setExternalAccountByGovernance(address newExternalAccount, address oldExternalAccount) public returns (bool);

    function setExternalAccountByUser(address newExternalAccount) public returns (bool);

    function setExternalAccountByAdmin(address newExternalAccount, address oldExternalAccount) public returns (bool);

    function getUserAccount(address externalAccount) public view returns (address);

    function freeze(address externalAccount) public returns (bool);

    function freezeByAdmin(address externalAccount) public returns (bool);

    function unfreeze(address externalAccount) public returns (bool);

    function unfreezeByAdmin(address externalAccount) public returns (bool);

    function cancelByGovernance(address externalAccount)  returns (bool);

    function cancelByUser() returns (bool);

    function cancelByAdmin(address externalAccount) public returns (bool b);

}
