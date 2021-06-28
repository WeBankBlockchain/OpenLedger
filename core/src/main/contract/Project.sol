pragma solidity ^0.4.25;
pragma experimental ABIEncoderV2;

import "./lib/UtilLib.sol";
import "./govAccount/AccountManager.sol";
import "./govAuth/IAuthControl.sol";
import "./storage/AddressSetLib.sol";
import "./storage/AddressMapLib.sol";
import "./interface/IAuthCenter.sol";
import "./Organization.sol";
import "./Constant.sol";
import "./Term.sol";
import "./interface/IAssetManager.sol";
import "./interface/ICurrencyManager.sol";
import  "./interface/IAssetPoolManager.sol";


contract Project is Constant {
    using AddressSetLib for AddressSetLib.Set;
    using AddressMapLib for AddressMapLib.Map;

    address owner;
    address currency;
    address assetPoolManager;
    AddressSetLib.Set organizationList;
    AddressMapLib.Map fungibleAssetList;
    AddressMapLib.Map nonFungibleAssetList;

    AccountManager accountManager;
    IAuthCenter authCenter;
    Term term;
    IAssetManager fungibleAssetManager;
    IAssetManager nonFungibleAssetManager;
    ICurrencyManager currencyManager;

    modifier onlyOwner() {
        require(msg.sender == owner, "Project: only owner is authorized.");
        _;
    }

    // address[]: accountManagerAddress,  authCenterAddress,  assetManager, nonFungibleManager, currencyManagerAddr,assetPoolManager
    constructor (address[] managers) {
        require(managers.length==6,"param not verify,require managers.length =6");
        owner = msg.sender;
        accountManager = AccountManager(managers[0]);

        authCenter = IAuthCenter(managers[1]);
        term = new Term(owner);
        fungibleAssetManager = IAssetManager(managers[2]);
        nonFungibleAssetManager = IAssetManager(managers[3]);
        currencyManager = ICurrencyManager(managers[4]);
        assetPoolManager = IAssetPoolManager(managers[5]);
    }

    function getOwner() public view returns (address) {
        return owner;
    }

    function getAccountManager() public view returns (address){
        return address(accountManager);
    }

    function getAuthManager() public  view returns (address){
        return address(authCenter.getAuthManager());
    }

    function getAuthCenter() public view  returns (address){
        return address(authCenter);
    }

    function createOrganization() public onlyOwner returns (address){
        Organization organization = new Organization(address(this), address(authCenter));
        require(address(0)!=address(organization),"create org failed");
        organizationList.insert(organization);
        return organization;
    }

    function createAddOrgAdmin(address orgAddress, address externalAccount, bytes[] keyList, bytes[] valueList) public onlyOwner returns (bool, address){
        require(organizationList.contains(orgAddress), "org not exist");
        require(!accountManager.hasAccount(externalAccount), "account has exist");

        bool ret;
        address admin;
        (ret, admin) = Organization(orgAddress).createAccount(externalAccount, keyList, valueList);
        if (!ret) {
            return (ret, admin);
        }
        ret = Organization(orgAddress).addAdmin(externalAccount, ADMIN);
        return (ret, admin);
    }

    function addOrgAdmin(address orgAddress, address externalAccount) public onlyOwner returns (bool){
        require(organizationList.contains(orgAddress), "org not exist");
        require(accountManager.hasAccount(externalAccount), "account not exist");

        return Organization(orgAddress).addAdmin(externalAccount, ADMIN);
    }

    //only org admin
    function syncAddAsset(address asset, address orgAddress,bool isFungible) public returns (bool){
        require(msg.sender == orgAddress, "msg.sender is not this org");
        if(isFungible){
            fungibleAssetList.insert(asset, orgAddress);
        }else{
            nonFungibleAssetList.insert(asset, orgAddress);
        }
        return true;
    }

    function getAllOrg() public returns (address[]) {
        return organizationList.getAll();
    }

    function hasAccount(address a) public returns (bool){
        return accountManager.hasAccount(a);
    }

    function getAllAsset(bool isFungible) public onlyOwner returns (address[] memory, address[] memory, uint retNum) {
        if(isFungible){
            fungibleAssetList.getAll();
        }else{
            nonFungibleAssetList.getAll();
        }
    }

    function getTerm() public view returns (address) {
        return address(term);
    }

    function getFingibleAssetManager() public view returns (address){
        return address(fungibleAssetManager);
    }

    function getNonFingibleAssetManager() public view returns (address){
        return address(nonFungibleAssetManager);
    }

    function getCurrencyManager() public view returns (address){
        return currencyManager;
    }
    function setCurrency(address currencyAddr) public returns (bool){
        currency = currencyAddr;
        return true;
    }
    function getCurrency() public view returns (address){
        return currency;
    }
    function getAssetPoolManager() public view returns (address){
        return assetPoolManager;
    }
}