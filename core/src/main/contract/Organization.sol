pragma solidity ^0.4.25;
pragma experimental ABIEncoderV2;

import "./Constant.sol";
import "./interface/IAuthCenter.sol";
import "./lib/UtilLib.sol";

import "./govAccount/AccountManager.sol";
import "./govAuth/AuthManager.sol";
import "./Account.sol";
import "./Identity.sol";

import "./storage/BytesMapLib.sol";
import "./storage/Address2BytesMapLib.sol";
import "./storage/AddressSetLib.sol";

import "./lib/SignLib.sol";
import "./Term.sol";
import "./interface/IAssetManager.sol";
import "./interface/IProject.sol";
import "./interface/ICurrencyManager.sol";
import "./interface/IAssetPoolManager.sol";
import "./interface/INonFungibleManager.sol";


contract Organization is Constant, Identity
{
    using Address2BytesMapLib for Address2BytesMapLib.Map;
    using UtilLib for *;
    using SignLib for bytes[];

    //organizationManager
    Address2BytesMapLib.Map admins;
    //accountInformationUnderTheOrganization
    address[] accountList;
    //theEntireProjectGlobalList
    address project;
    //fungible asset management within the organization
    address[] fungibleAssetList;
    //Non-fungible asset management within the organization
    address[] nonFungibleAssetList;
    address[] assetPoolAddressList;
    //assetsManagement
    IAssetManager assetManager;
    INonFungibleManager nonFungibleAssetManager;



    modifier onlyOrgAdmin() {
        require(this.isExternalAccountAdmin(msg.sender), "Organization: only admins is authorized.");
        _;
    }

    modifier onlyOrgAdminOrSuper() {
        bool ok = false;
        if (isExternalAccountAdmin(msg.sender) || msg.sender == address(project) || tx.origin == IProject(project).getOwner()) {
            ok = true;
        }
        require(ok, "Organization: only admins or project admin is authorized.");
        _;
    }

    modifier onlySuperAdmin() {
        bool ok = false;
        if (tx.origin == IProject(project).getOwner() || msg.sender == project) {
            ok = true;
        }
        require(ok, "Organization: only admins or project admin is authorized.");
        _;
    }

    constructor(address projectAddress, address authCenterAddress) public Identity(authCenterAddress, address(0x0)) {
        project = projectAddress;
        assetManager = IAssetManager(IProject(project).getFingibleAssetManager());
        nonFungibleAssetManager = INonFungibleManager(IProject(project).getNonFingibleAssetManager());
    }

    //account manager
    function createAccount(address externalAccount, bytes[] keyList, bytes[] valueList) public onlyOrgAdminOrSuper returns (bool, address) {
        bool ret;
        address uaAddr;
        (ret, uaAddr) = innerCreateAccount(externalAccount, keyList, valueList);

        accountList.push(uaAddr);
        return (true, uaAddr);
    }

    function cancel(address externalAccount) public onlyOrgAdmin returns (bool)  {
        return AccountManager(accountManager).cancelByAdmin(externalAccount);
    }

    function freeze(address externalAccount) public onlyOrgAdmin returns (bool) {
        return AccountManager(accountManager).freezeByAdmin(externalAccount);
    }

    function unfreeze(address externalAccount) public onlyOrgAdmin returns (bool) {
        return AccountManager(accountManager).unfreezeByAdmin(externalAccount);
    }

    function changeExternalAccount(address oldAccount, address newAccount) public onlyOrgAdmin returns (bool){
        return AccountManager(accountManager).setExternalAccountByAdmin(newAccount, oldAccount);
    }

    //administratorManagementInterface
    function addAdmin(address externalAccount, bytes role) public onlyOrgAdminOrSuper returns (bool) {
        address addr = AccountManager(accountManager).getUserAccount(externalAccount);
        require(addr != 0x0, "account not exist");

        admins.insert(addr, role);

        UserAccount ua = UserAccount(addr);
        Account a = Account(ua.getData());
        a.insert(ROLE, role);

        return true;
    }

    function removeAdmin(address externalAccount) public onlyOrgAdmin returns (bool) {
        address addr = AccountManager(accountManager).getUserAccount(externalAccount);
        require(addr != 0x0, "account not exist");

        admins.remove(addr);

        UserAccount ua = UserAccount(addr);
        Account a = Account(ua.getData());
        a.remove(ROLE);
        return true;
    }

    function isExternalAccountAdmin(address externalAccount) public view returns (bool) {
        address internalAccount = AccountManager(accountManager).getUserAccount(externalAccount);
        if (internalAccount == 0x0) {
            return false;
        }
        return isInternalAccountAdmin(internalAccount);
    }

    function isInternalAccountAdmin(address internalAccount) public view returns (bool) {
        if (internalAccount == 0x0) {
            return false;
        }

        bytes memory r = admins.get(internalAccount);
        if (r.length == 0) {
            return false;
        }
        return true;
    }

    function listAdmins() public view returns (address[] memory, bytes[]memory, uint){
        return admins.getAll();
    }

    function getAllAccounts() public view returns (address[]) {
        return accountList;
    }


    //theFollowingIsTheProxyInterface
    function createAccountWithSign(address externalAccount, bytes[] keyList, bytes[] valueList, bytes32[4] sign) public returns (bool, address) {
        require(keyList.length == valueList.length, "key value num not equal.");

        bytes memory args;
        args = args.bytesAppend(externalAccount);
        for (uint i = 0; i < keyList.length; i++) {
            args = args.bytesAppend(keyList[i]);
            args = args.bytesAppend(valueList[i]);
        }

        address txOrigin;
        bool check;
        (txOrigin, check) = authCenter.check2WithSign(org, address(0x0), "createAccount", args, sign);
        require(check, "createAccount Forbidden!");


        bool ret;
        address uaAddr;
        (ret, uaAddr) = innerCreateAccount(externalAccount, keyList, valueList);

        //        accountList.insert(uaAddr);
        accountList.push(uaAddr);
        return (true, uaAddr);
    }

    function cancelWithSign(address externalAccount, bytes32[4] sign) public returns (bool)  {
        bytes memory args;
        args = args.bytesAppend(externalAccount);

        address txOrigin;
        bool check;
        (txOrigin, check) = authCenter.check2WithSign(org, address(0x0), "cancel", args, sign);
        require(check, "cancel Forbidden!");

        return AccountManager(accountManager).cancelByAdmin(externalAccount);
    }

    function freezeWithSign(address externalAccount, bytes32[4] sign) public returns (bool) {
        bytes memory args;
        args = args.bytesAppend(externalAccount);

        address txOrigin;
        bool check;
        (txOrigin, check) = authCenter.check2WithSign(org, address(0x0), "freeze", args, sign);
        require(check, "freeze Forbidden!");

        return AccountManager(accountManager).freezeByAdmin(externalAccount);
    }

    function unfreezeWithSign(address externalAccount, bytes32[4] sign) public returns (bool) {
        bytes memory args;
        args = args.bytesAppend(externalAccount);

        address txOrigin;
        bool check;
        (txOrigin, check) = authCenter.check2WithSign(org, address(0x0), "unfreeze", args, sign);
        require(check, "unfreeze Forbidden!");

        return AccountManager(accountManager).unfreezeByAdmin(externalAccount);
    }

    function changeExternalAccountWithSign(address oldAccount, address newAccount, bytes32[4] sign) public returns (bool){
        bytes memory args;
        args = args.bytesAppend(oldAccount).bytesAppend(newAccount);

        address txOrigin;
        bool check;
        (txOrigin, check) = authCenter.check2WithSign(org, address(0x0), "changeExternalAccount", args, sign);
        require(check, "changeExternalAccount Forbidden!");

        return AccountManager(accountManager).setExternalAccountByAdmin(newAccount, oldAccount);
    }

    //administratorManagementInterface
    function addAdminWithSign(address externalAccount, bytes role, bytes32[4] sign) public returns (bool) {
        bytes memory args;
        args = args.bytesAppend(externalAccount).bytesAppend(role);

        address txOrigin;
        bool check;
        (txOrigin, check) = authCenter.check2WithSign(org, address(0x0), "addAdmin", args, sign);
        require(check, "addAdmin Forbidden!");


        address addr = AccountManager(accountManager).getUserAccount(externalAccount);
        require(addr != 0x0, "account not exist");

        admins.insert(addr, role);

        UserAccount ua = UserAccount(addr);
        Account a = Account(ua.getData());
        a.insert(ROLE, role);

        return true;
    }

    function removeAdminWithSign(address externalAccount, bytes32[4] sign) public onlyOrgAdmin returns (bool) {
        bytes memory args;
        args = args.bytesAppend(externalAccount);

        address txOrigin;
        bool check;
        (txOrigin, check) = authCenter.check2WithSign(org, address(0x0), "removeAdmin", args, sign);
        require(check, "removeAdmin Forbidden!");

        address addr = AccountManager(accountManager).getUserAccount(externalAccount);
        require(addr != 0x0, "account not exist");

        admins.remove(addr);

        UserAccount ua = UserAccount(addr);
        Account a = Account(ua.getData());
        a.remove(ROLE);
        return true;
    }

    function createAssetWithSign(address externalAccount, string assetName, bool isFungible, bytes32[4] sign) public returns (address, bool){
        address assetAddress;
        bool isCreate;
        address storageAddress;

        if (isFungible) {
            (isCreate, assetAddress) = assetManager.createAssetWithSign(externalAccount, assetName, sign, address(authCenter), address(this));
            if (address(0) == assetAddress) {
                return (address(0), false);
            }
            fungibleAssetList.push(assetAddress);
        } else {
            (isCreate, assetAddress) = nonFungibleAssetManager.createAssetWithSign(externalAccount, assetName, sign, address(authCenter), address(this));
            if (address(0) == assetAddress) {
                return (address(0), false);
            }
            nonFungibleAssetList.push(assetAddress);
        }

        IProject(project).syncAddAsset(assetAddress, address(this), isFungible);
        return (assetAddress, true);
    }

    function upgradeAsset(address externalAccount,string assetName, bool isFungible, bytes32[4] sign) public returns (address, bool){
        require(!isFungible, "upgrade not support fungibleAsset");
        address assetAddress;
        bool isUpgrade;
        address storageAddress;
        (isUpgrade, assetAddress) = nonFungibleAssetManager.upgradeAsset(externalAccount, assetName, sign);
        return (assetAddress, true);
    }

    function listAssetVersion(string assetName) public view returns (uint256[], address[]){
        return nonFungibleAssetManager.listAssetVersion(assetName);
    }

    function getAllAssets(bool isFungible) public view returns (address[]) {
        if (isFungible) {
            return fungibleAssetList;
        } else {
            return nonFungibleAssetList;
        }
    }

    function getAccount(address account) public returns (address){
        //获取被调方所属组织
        address innerCallee = AccountManager(accountManager).getUserAccount(account);
        if (innerCallee == 0x0) {
            return 0;
        }
        UserAccount calleeUA = UserAccount(innerCallee);
        Account calleeAccount = Account(calleeUA.getData());
        return address(calleeAccount);
    }

    function getProjectTerm() public view returns (address){
        return IProject(project).getTerm();
    }

    function getExternalAccount(address userAddress) public view returns (address) {
        return AccountManager(accountManager).getExternalAccount(userAddress);
    }

    function getInnerAccount(address account) public returns (address){
        //getsTheOrganizationOfTheCalledParty
        address innerCallee = AccountManager(accountManager).getUserAccount(account);
        if (innerCallee == 0x0) {
            return 0;
        }
        UserAccount calleeUA = UserAccount(innerCallee);
        return address(calleeUA);
    }


    function innerCreateAccount(address externalAccount, bytes[] keyList, bytes[] valueList) private returns (bool, address) {
        Account a = new Account(address(authCenter), this);
        require(keyList.length == valueList.length, "key value num not equal.");

        for (uint i = 0; i < keyList.length; i++) {
            a.insert(keyList[i], valueList[i]);
        }
        return AccountManager(accountManager).newAccount(externalAccount, a);
    }


    function createCurrency(string currencyName, string currencySymbol, uint8 decimals) public onlySuperAdmin returns (address){
        address assetAddress;

        assetAddress = ICurrencyManager(IProject(project).getCurrencyManager()).createCurrency(currencyName, currencySymbol, decimals, address(authCenter), address(this));
        require(address(0) != assetAddress, "create currency fail");
        IProject(project).setCurrency(assetAddress);
        return assetAddress;
    }

    function createAssetPool() public onlyOrgAdminOrSuper returns (address){
        address assetPool = IAssetPoolManager(IProject((project)).getAssetPoolManager()).createAssetPool(address(authCenter), address(this));
        require(address(0) != assetPool, "create assetPool fail");
        assetPoolAddressList.push(assetPool);
        return assetPool;
    }

    function getAssetPools() public onlyOrgAdminOrSuper returns (address[]){
        return assetPoolAddressList;
    }


}

    
    
    
    
    
    
