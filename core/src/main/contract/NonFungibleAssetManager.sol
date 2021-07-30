pragma solidity ^0.4.25;

import "./interface/IAuthCenter.sol";
import "./NonFungibleAsset.sol";
import "./interface/INonFunStorageManager.sol";

contract NonFungibleAssetManager {
    modifier onlyOwner() {
        require(msg.sender == owner, "Project: only owner is authorized.");
        _;
    }

    using UtilLib for *;
    struct AssetVersion {
        uint256 versionIndex;
        address assetAddr;
        address storageAddr;
        address authCenterAddress;
        address orgAddress;
    }

    string [] assets;
    mapping(string => AssetVersion[]) assetVersions;
    mapping(address => string) assetMap;
    address storageManager;
    address owner;
    constructor () {
        owner = msg.sender;
    }
    function setStorageManager(address _storageManager) onlyOwner public {
        storageManager = _storageManager;
    }

    function createAssetWithSign(address externalAccount, string assetName, bytes32[4] sign, address authCenterAddress, address orgAddress) public returns (bool, address){
        require(assetVersions[assetName].length == 0, "asset has been created,please use other assetName");
        checkAuth(externalAccount, authCenterAddress, orgAddress, sign);
        address assetAddr;
        address assetStorage = INonFunStorageManager(storageManager).createStorage();
        NonFungibleAsset nonFungibleAsset = new NonFungibleAsset(assetName, authCenterAddress, orgAddress, true, assetStorage);
        assetAddr = address(nonFungibleAsset);
        if (address(0) == assetAddr) {
            return (false, address(0));
        }

        AssetVersion  memory version = AssetVersion(1, assetAddr, assetStorage, authCenterAddress, orgAddress);
        assetVersions[assetName].push(version);
        assetMap[assetAddr] = assetName;
        return (true, assetAddr);
    }

    function upgradeAsset(address externalAccount, string assetName, bytes32[4] sign) public returns (bool, address){
        require(assetVersions[assetName].length > 0, "asset has note been created,please  run createAsset");
        uint256 versionSize = assetVersions[assetName].length;
        AssetVersion current = assetVersions[assetName][versionSize - 1];
        checkAuth(externalAccount, current.authCenterAddress, current.orgAddress, sign);

        NonFungibleAsset nonFungibleAsset = new NonFungibleAsset(assetName, current.authCenterAddress, current.orgAddress, false, current.storageAddr);
        address assetAddr = address(nonFungibleAsset);

        if (address(0) == assetAddr) {
            return (false, address(0));
        }
        AssetVersion memory version = AssetVersion(versionSize + 1, assetAddr, current.storageAddr, current.authCenterAddress, current.orgAddress);
        assetVersions[assetName].push(version);
        assetMap[assetAddr] = assetName;

        return (true, assetAddr);
    }

    function checkStorageCallee(address sender, address assetStorage) public view returns (bool){
        AssetVersion[] assetVersionList = assetVersions[assetMap[sender]];

        require(assetVersionList.length > 0, "checkStorageCallee:sender is not verify!");
        for (uint i = 0; i < assetVersionList.length; i++) {
            if (assetVersionList[i].assetAddr == sender && assetVersionList[i].storageAddr == assetStorage) {
                return true;
            }
        }
        return false;
    }
    function checkUpgrade(string assetName) public view returns (bool){
        AssetVersion[] assetVersionList = assetVersions[assetName];
        require(assetVersionList.length > 0, "checkUpgrade:sender is not verify!");
        return true;
    }

    function listAssetVersion(string assetName) public view returns (uint256[], address[]){
        AssetVersion[] assetVersionList = assetVersions[assetName];
        uint256[] memory versions = new uint256[](assetVersionList.length);
        address[] memory assets = new address[](assetVersionList.length);

        for (uint i = 0; i < assetVersionList.length; i++) {
            versions[i] = assetVersionList[i].versionIndex;
            assets[i] = assetVersionList[i].assetAddr;
        }
        return (versions, assets);
    }

    function checkAuth(address externalAccount, address authcenterAddress, address orgAddress, bytes32[4] sign) internal {
        bytes memory args;
        args = args.bytesAppend(externalAccount);

        address txOrigin;
        bool check;
        (txOrigin, check) = IAuthCenter(authcenterAddress).check2WithSign(orgAddress, this, "createAsset", args, sign);
        require(check, "createAsset Forbidden!");
    }

}