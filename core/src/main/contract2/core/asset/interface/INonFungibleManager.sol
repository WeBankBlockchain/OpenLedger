pragma solidity ^0.4.0;
pragma experimental ABIEncoderV2;

import "./IAssetManager.sol";

contract INonFungibleManager  {

    function createAsset(string assetName) public returns (bool, address);

    function upgradeAsset(string assetName) public returns (bool, address);

    function checkStorageCallee(address sender, address assetStorage) public view returns (bool);

    function listAssetVersion(string assetName) public view returns (uint256[], address[]);

    function checkUpgrade(string assetName) public view returns (bool);

    function setPrice(address asset, uint256 _price)  public returns (uint256);

    function openAccount(address asset,address account)  public returns (bool);

    function issue(address asset, address[] transactionAddress, uint256[] uint256Args, bytes[] stringValueList)  public returns (uint256, uint256[], uint[]);

    function getNoteDetail(address asset,uint256 noteNo)  public view returns (address[], uint256[], uint[], uint8);

    function transfer(address asset, address[] transactionAddress, uint[] noteNos, bytes[] stringValueList) public returns (bool, uint[]);

    function accountHoldNote(address asset, uint256 noteNo) public constant returns (bool );

    function getAccountNotes(address asset, address account,uint256 start, uint256 end) public constant returns (uint256[]);

    function getAccountNoteSize(address asset,address account) public constant returns (uint256);

    function updateNoteNo(address asset, uint256 oldNoteNo, uint256 newNoteNo)  public returns (bool);

    function updateNoteProperties(address asset, uint256 noteNo, bytes[] keys, bytes[] values)  public returns (bytes[] , bytes[] );

    function getNoteProperties(address asset, uint256 noteNo)  public constant returns (bytes[] , bytes[] );

    function updateNoteBatch(address asset, uint256 batchNo, uint date, bool isEffectiveDate)  public returns (bool);

    function freezeNote(address asset, uint256 noteNo)  public returns (bool);

    function unfreezeNote(address asset, uint256 noteNo)  public returns (uint8);

    function queryBook(address asset, uint[] uintCondition, address[] addressCondition, int[] limit)  public constant returns (string[] memory);

    function getTotalNoteSize(address asset) public  constant returns (uint256);

    function getHolders(address asset) public  constant returns (address[]);

    function tearNote(address asset, uint256 noteNo) public returns (bool);

    function getTearNotes(address asset)  public view returns (uint256[]);

    function addBook(address asset) public  returns (uint256);

    function enableBatch(address asset, uint256 batchNo) public  returns (bool );
}
