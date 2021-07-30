pragma solidity ^0.4.25;

pragma experimental ABIEncoderV2;

import "./core//storage/AddressMapLib.sol";
import "./core/lib/LibTypeConversion.sol";
import "./core/lib/UtilLib.sol";
import "./core//storage/AddressSetLib.sol";
import "./core/asset/interface/IAssetManager.sol";
import "./core/Term.sol";
import "./core/asset/interface/INonFungibleManager.sol";
import "./Organization.sol";
import "./core/asset/interface/INonFunStorageManager.sol";
import "./core/asset/interface/ICurrencyManager.sol";
import "./core/Constant.sol";
import "./core/asset/interface/IAssetPoolManager.sol";

contract Project is Constant {
    using AddressSetLib for AddressSetLib.Set;
    using AddressMapLib for AddressMapLib.Map;

    address owner;
    address currency;
    address assetPoolManager;
    AddressSetLib.Set organizationList;
    AddressMapLib.Map fungibleAssetList;
    AddressMapLib.Map nonFungibleAssetList;

    IAccountManager accountManager;
    Term term;
    IAssetManager fungibleAssetManager;
    INonFungibleManager nonFungibleAssetManager;
    ICurrencyManager currencyManager;
    INonFunStorageManager nonFungibleStorageManager;
    address aclManager;
    modifier onlyOwner() {
        require(msg.sender == owner, "Project: only owner is authorized.");
        _;
    }

    // address[]: accountManagerAddress, aclManager assetManager, nonFungibleManager, currencyManagerAddr,assetPoolManager
    constructor (address[] managers) {
        require(managers.length == 7, "param not verify,require managers.length =7");
        owner = msg.sender;
        accountManager = IAccountManager(managers[0]);
        term = new Term(owner);
        aclManager = managers[1];
        fungibleAssetManager = IAssetManager(managers[2]);
        nonFungibleAssetManager = INonFungibleManager(managers[3]);
        currencyManager = ICurrencyManager(managers[4]);
        assetPoolManager = IAssetPoolManager(managers[5]);
        nonFungibleStorageManager = INonFunStorageManager(managers[6]);
    }

    function getOwner() public view returns (address) {
        return owner;
    }

    function getAccountManager() public view returns (address){
        return address(accountManager);
    }

    function getAclManager() public view returns (address){
        return aclManager;
    }

    function createOrganization(address adminExternal) public returns (address){
        Organization organization = new Organization(address(this), address(accountManager));
        require(address(0) != address(organization), "create org failed");
        organizationList.insert(address(organization));
        address holderId = organization.createHolder(adminExternal);
        require(address(0) != holderId, "create org's holder failed");
        return organization;
    }

    function initRolesByProject(address org) public  returns (bool){
        Organization organization = Organization(org);
        bytes32[4] nullBytes;
        return organization.initRoles(nullBytes);
    }

    //only org admin
    function syncAddAsset(address asset, address orgAddress, bool isFungible) public returns (bool){
        require(msg.sender == orgAddress, "msg.sender is not this org");
        if (isFungible) {
            fungibleAssetList.insert(asset, orgAddress);
        } else {
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
        if (isFungible) {
            return fungibleAssetList.getAll();
        } else {
            return nonFungibleAssetList.getAll();
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