pragma solidity ^0.4.24;
pragma experimental ABIEncoderV2;
import "./BaseIdentity.sol";

contract IOrganization is BaseIdentity
{

    function innerCreateAccount(address externalAccount, bytes[] keyList, bytes[] valueList) private returns (bool, address) ;

    function createAccount(address externalAccount, bytes[] keyList, bytes[] valueList) public returns (bool, address) ;

    function cancel(address externalAccount) public returns (bool)  ;

    function freeze(address externalAccount) public returns (bool) ;

    function unfreeze(address externalAccount) public returns (bool) ;

    function changeExternalAccount(address oldAccount, address newAccount) public returns (bool);

    function addAdmin(address externalAccount, bytes role) public returns (bool) ;

    function removeAdmin(address externalAccount) public returns (bool) ;

    function isExternalAccountAdmin(address externalAccount) public view returns (bool) ;

    function isInternalAccountAdmin(address internalAccount) public view returns (bool) ;

    function listAdmins() public view returns (address[] memory, bytes[]memory, uint);

    function getAllAccounts() public view returns (address[]) ;

    function createAccountWithSign(address externalAccount, bytes[] keyList, bytes[] valueList, bytes32[4] sign) public returns (bool, address) ;

    function cancelWithSign(address externalAccount, bytes32[4] sign) public returns (bool)  ;

    function freezeWithSign(address externalAccount, bytes32[4] sign) public returns (bool) ;

    function unfreezeWithSign(address externalAccount, bytes32[4] sign) public returns (bool) ;

    function changeExternalAccountWithSign(address oldAccount, address newAccount, bytes32[4] sign) public returns (bool);

    function addAdminWithSign(address externalAccount, bytes role, bytes32[4] sign) public returns (bool) ;

    function removeAdminWithSign(address externalAccount, bytes32[4] sign) public returns (bool) ;

    function createAssetWithSign(address externalAccount, string assetName, bool isFungible, bytes32[4] sign) public returns (address, bool);

    function getAllAssets() public view returns (address[]) ;

    function getAccount(address account) public returns (address);

    function getProjectTerm() public view returns (address);

    function getExternalAccount(address userAddress) public view returns (address);

    function getInnerAccount(address account) public returns (address);
}


  