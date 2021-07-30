pragma solidity ^0.4.25;


pragma experimental ABIEncoderV2;

import "../storage/AddressSetLib.sol";
import "../asset/interface/IAssetManager.sol";
import "../asset/interface/INonFungibleManager.sol";
import "../project/interface/IProject.sol";
import "../asset/interface/IBaseAsset.sol";
import "./interface/IOrgAdmin.sol";
import "../lib/UtilLib.sol";
import "../asset/interface/ICurrencyManager.sol";

contract BaseCustody {
    using AddressSetLib for AddressSetLib.Set;
    using UtilLib for *;

    modifier onlyMember(bytes32[4] sign, string operation, bytes detail) {
        require(IOrgAdmin(address(this)).isMember(sign, operation, detail), "BaseCustody:Forbbiden ".strConcat(operation));
        _;
    }
    modifier onlyMemberWithoutDetail(bytes32[4] sign, string operation) {
        bytes memory detail;
        require(IOrgAdmin(address(this)).isMember(sign, operation, detail), "BaseCustody:Forbbiden ".strConcat(operation));
        _;
    }


    //Non-fungible asset management within the organization
    AddressSetLib.Set nonFungibleAssetList;
    AddressSetLib.Set fungibleAssetList;
    //accountInformationUnderTheOrganization
    address[] assetPoolAddressList;
    INonFungibleManager nonFungibleAssetManager;
    IAssetManager assetManager;
    address project;

    constructor(address _project){
        assetManager = IAssetManager(IProject(_project).getFingibleAssetManager());
        nonFungibleAssetManager = INonFungibleManager(IProject(_project).getNonFingibleAssetManager());
        project = _project;
    }

    function registerAsset(string assetName, bool isFungible, bytes32[4] sign) public onlyMemberWithoutDetail(sign, "registerAsset") returns (address, bool){
        address assetAddress;
        bool isCreate;
        if (isFungible) {
            (isCreate, assetAddress) = assetManager.createAsset(assetName);
            if (isCreate) {
                fungibleAssetList.insert(assetAddress);
            }
        } else {
            (isCreate, assetAddress) = nonFungibleAssetManager.createAsset(assetName);
            if (isCreate) {
                nonFungibleAssetList.insert(assetAddress);
            }
        }
        return (assetAddress, isCreate);
    }

    function upgradeAsset(string assetName, bool isFungible, bytes32[4] sign) public onlyMemberWithoutDetail(sign, "upgradeAsset") returns (address, bool){
        require(!isFungible, "upgrade not support fungibleAsset");
        bool isUpgrade;
        address assetAddress;
        (isUpgrade, assetAddress) = nonFungibleAssetManager.upgradeAsset(assetName);

        return (assetAddress, isUpgrade);
    }


    function getAllAssets(bool isFungible) public view returns (address[]) {
        if (isFungible) {
            return fungibleAssetList.getAll();
        } else {
            return nonFungibleAssetList.getAll();
        }
    }

    function listAssetVersion(string assetName) public view returns (uint256[], address[]){
        return nonFungibleAssetManager.listAssetVersion(assetName);
    }

    function createCurrency(string currencyName, string currencySymbol, uint8 decimals) public returns (address){
        address assetAddress;
        assetAddress = ICurrencyManager(IProject(project).getCurrencyManager()).createCurrency(currencyName, currencySymbol, decimals, address(this));
        require(address(0) != assetAddress, "create currency fail");
        IProject(project).setCurrency(assetAddress);
        return assetAddress;
    }

    //    function createAssetPool() public returns (address){
    //        address assetPool = IAssetPoolManager(IProject((project)).getAssetPoolManager()).createAssetPool(address(0), address(this));
    //        require(address(0) != assetPool, "create assetPool fail");
    //        assetPoolAddressList.push(assetPool);
    //        return assetPool;
    //    }

    function getAssetPools() public returns (address[]){
        return assetPoolAddressList;
    }

    function setPrice(address asset, uint256 priceVal, bytes32[4] sign) onlyMember(sign, "setPrice", asset.addr2bytes()) public returns (uint256){
        if (IBaseAsset(asset).boolFungible()) {
            return assetManager.setPrice(asset, priceVal);
        } else {
            return nonFungibleAssetManager.setPrice(asset, priceVal);
        }
    }

    function setRate(address asset, uint256 rateVal, bytes32[4] sign) public onlyMember(sign, "setRate", asset.addr2bytes()) returns (uint256){
        return assetManager.setRate(asset, rateVal);
    }

    function getHolders(address asset, bytes32[4] sign) public onlyMember(sign, "getHolders", asset.addr2bytes()) constant returns (address[]){
        if (IBaseAsset(asset).boolFungible()) {
            return assetManager.getHolders(asset);
        } else {
            return nonFungibleAssetManager.getHolders(asset);
        }
    }

    function openAccountByCustody(address asset, address account, bytes32[4] sign)
    public
    onlyMember(sign, "openAccount", asset.addr2bytes())
    returns (bool){
        if (IBaseAsset(asset).boolFungible()) {
            return assetManager.registerAccount(asset, account);
        } else {
            return nonFungibleAssetManager.openAccount(asset, account);
        }
    }

    function deposit(address asset, address[] transactionAddress, uint256 amount, int[] typeList, bytes[] detailList, bytes32[4] sign)
    public
    onlyMember(sign, "deposit", asset.addr2bytes())
    returns (bool, uint[2]){
        return assetManager.deposit(asset, transactionAddress, amount, typeList, detailList);
    }

    function withdrawal(address asset, address[] transactionAddress, uint256 amount, int[] typeList, bytes[] detailList, bytes32[4] sign)
    onlyMember(sign, "withdrawal", asset.addr2bytes())
    public
    returns (bool, uint[2]){
        return assetManager.deposit(asset, transactionAddress, amount, typeList, detailList);
    }


    function addBookByCustody(address asset, bytes32[4] sign)
    onlyMember(sign, "addBook", asset.addr2bytes())
    public
    returns (uint256){
        if (IBaseAsset(asset).boolFungible()) {
            return assetManager.addBook(asset);
        } else {
            return nonFungibleAssetManager.addBook(asset);
        }
    }

    function getTotalBalance(address asset, bytes32[4] sign)
    public
    onlyMember(sign, "getTotalBalance", asset.addr2bytes())
    constant returns (uint256){
        return assetManager.getTotalBalance(asset);
    }


    function issue(address asset, address[] transactionAddress, uint256[] uint256Args, bytes[] stringValueList, bytes32[4] sign)
    onlyMember(sign, "issue", asset.addr2bytes())
    public
    returns (uint256, uint256[], uint[]){
        return nonFungibleAssetManager.issue(asset, transactionAddress, uint256Args, stringValueList);
    }

    function getNoteDetail(address asset, uint256 noteNo, bytes32[4] sign)
    onlyMember(sign, "getNoteDetail", asset.addr2bytes())
    public
    view
    returns (address[], uint256[], uint[], uint8){
        return nonFungibleAssetManager.getNoteDetail(asset, noteNo);
    }

    function getAccountNotes(address asset, address account, uint256 start, uint256 end, bytes32[4] sign)
    public
    onlyMember(sign, "getAccountNotes", asset.addr2bytes())
    constant returns (uint256[]){
        return nonFungibleAssetManager.getAccountNotes(asset, account, start, end);
    }

    function getAccountNoteSize(address asset, address account, bytes32[4] sign)
    public
    onlyMember(sign, "getAccountNoteSize", asset.addr2bytes())
    constant
    returns (uint256){
        return nonFungibleAssetManager.getAccountNoteSize(asset, account);
    }

    function updateNoteNo(address asset, uint256 oldNoteNo, uint256 newNoteNo, bytes32[4] sign)
    public
    onlyMember(sign, "updateNoteNo", asset.addr2bytes())
    returns (bool){
        return nonFungibleAssetManager.updateNoteNo(asset, oldNoteNo, newNoteNo);
    }

    function updateNoteProperties(address asset, uint256 noteNo, bytes[] keys, bytes[] values, bytes32[4] sign)
    onlyMember(sign, "updateNoteProperties", asset.addr2bytes())
    public
    returns (bytes[] noteKeys, bytes[] noteValues){
        return nonFungibleAssetManager.updateNoteProperties(asset, noteNo, keys, values);
    }

    function getNoteProperties(address asset, uint256 noteNo, bytes32[4] sign)
    onlyMember(sign, "getNoteProperties", asset.addr2bytes())
    public
    constant
    returns (bytes[] noteKeys, bytes[] noteValues){
        return nonFungibleAssetManager.getNoteProperties(asset, noteNo);
    }

    function updateNoteBatch(address asset, uint256 batchNo, uint date, bool isEffectiveDate, bytes32[4] sign)
    onlyMember(sign, "updateNoteBatch", asset.addr2bytes())
    public
    returns (bool){
        return nonFungibleAssetManager.updateNoteBatch(asset, batchNo, date, isEffectiveDate);
    }

    function freezeNote(address asset, uint256 noteNo, bytes32[4] sign)
    onlyMember(sign, "freezeNote", asset.addr2bytes())
    public
    returns (bool){
        return nonFungibleAssetManager.freezeNote(asset, noteNo);
    }

    function unfreezeNote(address asset, uint256 noteNo, bytes32[4] sign)
    public
    onlyMember(sign, "unfreezeNote", asset.addr2bytes())
    returns (uint8){
        return nonFungibleAssetManager.unfreezeNote(asset, noteNo);
    }

    function queryBookByCustody(address asset, uint[] uintCondition, address[] addressCondition, int[] limit, bytes32[4] sign)
    onlyMember(sign, "queryBook", asset.addr2bytes())
    public
    constant
    returns (string[] memory){
        if (IBaseAsset(asset).boolFungible()) {
            return assetManager.queryBook(asset, uintCondition, addressCondition, limit);
        } else {
            return nonFungibleAssetManager.queryBook(asset, uintCondition, addressCondition, limit);
        }
    }

    function getTotalNoteSize(address asset, bytes32[4] sign)
    public
    onlyMember(sign, "getTotalNoteSize", asset.addr2bytes())
    constant
    returns (uint256){
        return nonFungibleAssetManager.getTotalNoteSize(asset);
    }


    function tearNote(address asset, uint256 noteNo, bytes32[4] sign)
    public
    onlyMember(sign, "getTotalNoteSize", asset.addr2bytes())
    returns (bool){
        return nonFungibleAssetManager.tearNote(asset, noteNo);
    }

    function getTearNotes(address asset, bytes32[4] sign)
    onlyMember(sign, "getTearNotes", asset.addr2bytes())
    public
    view
    returns (uint256[]){
        return nonFungibleAssetManager.getTearNotes(asset);
    }


    function enableBatch(address asset, uint256 batchNo, bytes32[4] sign)
    public
    onlyMember(sign, "enableBatch", asset.addr2bytes())
    returns (bool isEffective){
        return nonFungibleAssetManager.enableBatch(asset, batchNo);
    }

    function getBalance(address asset, address account, bytes32[4] sign) public onlyMember(sign, "getBalance", asset.addr2bytes()) constant returns (uint256){
        return assetManager.getBalance(asset, account);
    }

}
