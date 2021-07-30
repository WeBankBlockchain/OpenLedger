pragma solidity ^0.4.25;
pragma experimental ABIEncoderV2;

contract INonFungibleStorage {

    /*******************check functions************************************************/
    function checkEffectiveAndUpdate(uint256 noteNo) public constant returns (bool isEffective);

    function checkOwner(uint256 noteNo, address account) public view returns (bool isOwner);

    function checkIssuer(uint256 noteNo, address account) public view returns (bool isIssuer);

    function checkEffect(uint256 noteNo) public view returns (bool);

    function checkExpire(uint256 noteNo) public constant returns (bool isExpire);

    function isExistNoteDefined(uint256 noteNoPrefix, uint256 noteNoSize) public view returns (bool);

    function isExistNote(uint256 noteNo) public view returns (bool);

    function checkIssuerByBatchNo(uint256 batchNo, address account) public returns (bool);


    /*********************** global parameter set *********************************************/
    // init storage
    function init(address _book) public;

    function getBook(string assetName) public view returns (address);

    // update price
    function setPrice(uint256 _price) public returns (bool);
    // get price
    function getPrice() public returns (uint256);

    /***********************account‘s function*********************************************/
    function addAccount(address account) public;

    function getAccounts() public view returns (address[]);

    function existAccount(address account) public view returns (bool);

    //***********************note‘s function*********************************************/
    function updateExistNote(uint256 noteNoPrefix, uint256 noteNoSize) public;

    function createNote(uint256 noteId, uint256 noteNo, address owner, uint256 batchNo) public;

    function generateNoteId() public returns (uint256);


    function getNote(uint256 noteNo) public view returns (address[], uint256[], uint[], uint8);

    function getNoteByAccount(address account, uint256 start, uint256 end) public view returns (uint256[]);

    function getAccountNoteSize(address account) public constant returns (uint256);

    function updateNoteNo(uint256 oldNoteNo, uint256 newNoteNo) public returns (bool isUpdate);

    function getStatusByNote(uint256 noteNo) public view returns (uint8);

    function updateNoteProperties(uint256 noteNo, bytes[] keys, bytes[] values) public returns (bytes[] noteKeys, bytes[] noteValues);

    function getNoteProperties(uint256 noteNo) public constant returns (bytes[] noteKeys, bytes[] noteValues);

    function removeNoteFromAccount(uint256 noteNo, address account) public returns (uint256);

    function addNoteToAccount(uint256 noteNo, address account) public returns (uint256);

    function exchangeNote(address from, address to, uint256 noteNo) public returns (uint256);

    function accountHoldNote(address account, uint256 noteNo) public view returns (bool isContain);

    function getNoteIdByNo(uint256 noteNo) public view returns (uint256);

    function getNoteNoByNoteId(uint256 noteId) public constant returns (uint256);

    function updateNoteStatus(uint256 noteNo, uint8 status) public returns (uint256);


    //***********************batch‘s function*********************************************/

    function createBatch(uint effectiveDate, uint expirationDate, uint256 amount, address issuer) public returns (uint256, uint256);

    function updateNoteBatch(uint256 batchNo, uint date, bool isEffectiveDate) public returns (bool isUpdate);

    function enableBatch(uint256 batchNo) public returns (bool);

    //***********************totalNoteSize‘s function*********************************************/
    function addTotalNoteSize(uint256 amount) public;

    function getTotalNoteSize() public view returns (uint256);

    function mulTotalNoteSize(uint256 amount) public;

    //***********************tearNote‘s function*********************************************/

    function addTearNote(uint256 noteNo) public;


    function getTearNotes() public view returns (uint256[]);

}