pragma solidity ^0.4.25;
pragma experimental ABIEncoderV2;

import "./lib/UtilLib.sol";
import "./asset/nonfungible/NonFungibleAsset.sol";
import "./asset/interface/IBaseAsset.sol";
import "./asset/interface/INonFunStorageManager.sol";

contract NonFungibleAssetManager {
    modifier onlyOwner() {
        require(msg.sender == owner, "Project: only owner is authorized.");
        _;
    }
    modifier onlyCustodian(address asset){
        require(IBaseAsset(asset).isCustodian(msg.sender), "FungibleAssetManager:required custodian called!");
        _;
    }
    modifier onlyAccountHolder(address asset){
        require(IBaseAsset(asset).containsAccount(msg.sender), "FungibleAssetManager:required accountHolder called!");
        _;
    }
    modifier onlyCustodianOrHolder(address asset){
        require(IBaseAsset(asset).containsAccount(msg.sender)||IBaseAsset(asset).isCustodian(msg.sender), "FungibleAssetManager:required accountHolder or custodian called!");
        _;
    }
    using UtilLib for *;
    struct AssetVersion {
        uint256 versionIndex;
        address assetAddr;
        address storageAddr;
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
    function createAsset(string assetName) public returns (bool, address){
        require(assetVersions[assetName].length == 0, "asset has been created,please use other assetName");
        address assetAddr;
        address assetStorage = INonFunStorageManager(storageManager).createStorage();
        NonFungibleAsset nonFungibleAsset = new NonFungibleAsset(assetName, msg.sender, true, assetStorage);
        assetAddr = address(nonFungibleAsset);
        if (address(0) == assetAddr) {
            return (false, address(0));
        }

        AssetVersion  memory version = AssetVersion(1, assetAddr, assetStorage, msg.sender);
        assetVersions[assetName].push(version);
        assetMap[assetAddr] = assetName;
        return (true, assetAddr);
    }

    function upgradeAsset(address publicAccount, string assetName) public returns (bool, address){
        require(assetVersions[assetName].length > 0, "asset has note been created,please run createAsset");
        uint256 versionSize = assetVersions[assetName].length;
        AssetVersion current = assetVersions[assetName][versionSize - 1];

        NonFungibleAsset nonFungibleAsset = new NonFungibleAsset(assetName, current.orgAddress, false, current.storageAddr);
        address assetAddr = address(nonFungibleAsset);

        if (address(0) == assetAddr) {
            return (false, address(0));
        }
        AssetVersion memory version = AssetVersion(versionSize + 1, assetAddr, current.storageAddr, current.orgAddress);
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
        address[] memory assetList = new address[](assetVersionList.length);

        for (uint i = 0; i < assetVersionList.length; i++) {
            versions[i] = assetVersionList[i].versionIndex;
            assetList[i] = assetVersionList[i].assetAddr;
        }
        return (versions, assetList);
    }

    function setPrice(address asset, uint256 _price) onlyCustodian(asset) public returns (uint256){
        return NonFungibleAsset(asset).setPrice(_price);
    }

    function openAccount(address asset,address account) onlyCustodian(asset) public returns (bool){
        return NonFungibleAsset(asset).openAccount(account);
    }

    function issue(address asset, address[] transactionAddress, uint256[] uint256Args, bytes[] stringValueList) onlyCustodian(asset) public returns (uint256, uint256[], uint[]){
        return NonFungibleAsset(asset).issue(transactionAddress, uint256Args, stringValueList);
    }

    function getNoteDetail(address asset, uint256 noteNo) onlyCustodian(asset) public returns (address[], uint256[], uint[], uint8){
        return NonFungibleAsset(asset).getNoteDetail(noteNo, msg.sender);
    }

    function transfer(address asset, address[] transactionAddress, uint[] noteNos, bytes[] stringValueList) onlyAccountHolder(asset) public returns (bool, uint[]){
        return NonFungibleAsset(asset).transfer(transactionAddress, noteNos, stringValueList);
    }

    function accountHoldNote(address asset, uint256 noteNo) onlyAccountHolder(asset) public constant returns (bool ){
        return NonFungibleAsset(asset).accountHoldNote(msg.sender, noteNo);
    }

    function getAccountNotes(address asset, address account,uint256 start, uint256 end) onlyCustodianOrHolder(asset) public constant returns (uint256[]){
        return NonFungibleAsset(asset).getAccountNotes(account, start, end);
    }

    function getAccountNoteSize(address asset,address account) public onlyCustodianOrHolder(asset) constant returns (uint256){
        return NonFungibleAsset(asset).getAccountNoteSize(account);
    }

    function updateNoteNo(address asset, uint256 oldNoteNo, uint256 newNoteNo) onlyCustodian(asset) public returns (bool){
        return NonFungibleAsset(asset).updateNoteNo(oldNoteNo, newNoteNo, msg.sender);
    }

    function updateNoteProperties(address asset, uint256 noteNo, bytes[] keys, bytes[] values) onlyCustodian(asset) public returns (bytes[] , bytes[] ){
        return NonFungibleAsset(asset).updateNoteProperties(noteNo, keys, values, msg.sender);
    }

    function getNoteProperties(address asset, uint256 noteNo) onlyCustodian(asset) public constant returns (bytes[] , bytes[] ){
        return NonFungibleAsset(asset).getNoteProperties(noteNo, msg.sender);
    }

    function updateNoteBatch(address asset, uint256 batchNo, uint date, bool isEffectiveDate) onlyCustodian(asset) public returns (bool){
        return NonFungibleAsset(asset).updateNoteBatch(batchNo, date, isEffectiveDate, msg.sender);
    }

    function freezeNote(address asset, uint256 noteNo) onlyCustodian(asset) public returns (bool){
        return NonFungibleAsset(asset).freezeNote(noteNo,msg.sender);
    }

    function unfreezeNote(address asset, uint256 noteNo) onlyCustodian(asset) public returns (uint8){
        return NonFungibleAsset(asset).unfreezeNote(noteNo,msg.sender);
    }

    function queryBook(address asset, uint[] uintCondition, address[] addressCondition, int[] limit) onlyCustodian(asset) public constant returns (string[] memory){
        return NonFungibleAsset(asset).queryBook(uintCondition, addressCondition, limit);
    }

    function getTotalNoteSize(address asset) public onlyCustodian(asset) constant returns (uint256){
        return NonFungibleAsset(asset).getTotalNoteSize();
    }

    function getHolders(address asset) public onlyCustodian(asset) constant returns (address[]){
        return NonFungibleAsset(asset).getHolders();
    }

    function tearNote(address asset, uint256 noteNo) public returns (bool){
        return NonFungibleAsset(asset).tearNote(noteNo,msg.sender);
    }

    function getTearNotes(address asset) onlyCustodian(asset) public view returns (uint256[]){
        return NonFungibleAsset(asset).getTearNotes(msg.sender);
    }

    function addBook(address asset) public onlyCustodian(asset) returns (uint256){
        return NonFungibleAsset(asset).addBook();
    }

    function enableBatch(address asset, uint256 batchNo) public onlyCustodian(asset) returns (bool ){
        return NonFungibleAsset(asset).enableBatch(batchNo);
    }


}