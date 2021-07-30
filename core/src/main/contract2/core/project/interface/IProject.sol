pragma solidity ^0.4.25;
pragma experimental ABIEncoderV2;


contract IProject {

    function getOwner() public view returns (address);

    function getAccountManager() public returns (address);

    function getAuthManager() public returns (address);

    function getAuthCenter() public returns (address);

    function createOrganization() public returns (address);

    function createAddOrgAdmin(address org, address externalAccount, bytes[] keyList, bytes[] valueList) public returns (bool, address);

    function addOrgAdmin(address org, address externalAccount) public returns (bool);

    function syncAddAsset(address asset, address org, bool isFungible) public returns (bool);

    function getAllOrg() public returns (address[]);

    function hasAccount(address a) public returns (bool);

    function getAllAsset() public returns (address[] memory, address[] memory, uint retNum);

    function getTerm() public view returns (address);

    function getFingibleAssetManager() public view returns (address);

    function getNonFingibleAssetManager() public view returns (address);

    function getCurrencyManager() public view returns (address);

    function setCurrency(address currencyAddr) public returns (bool);

    function getCurrency() public view returns (address);

    function getAssetPoolManager() public view returns (address);

    function getAclManager() public view returns (address);

}