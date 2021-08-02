pragma solidity ^0.4.25;

pragma experimental ABIEncoderV2;

import "../base/BaseAsset.sol";
import "./NonFungibleBook.sol";
import "../../lib/UtilLib.sol";
import "../../lib/LibSafeMath.sol";
import "../../lib/LibTypeConversion.sol";
import "../interface/INonFungibleStorage.sol";
import "../../account/interface/IAccountHolder.sol";
import "../../org/interface/IOrganization.sol";


// nonHomogeneousAssets
contract NonFungibleAsset is BaseAsset {
    using LibSafeMath for uint256;
    using UtilLib for *;
    using LibTypeConversion for uint;

    event LogNoteStatus(uint256 noteNo, uint256 noteID, uint8 status);

    //    uint256 public price;
    uint8 constant INIT_STATUS = 1;
    uint8 constant EFFECTIVE_STATUS = 2;
    uint8 constant EXPIRATE_STATUS = 3;
    uint8 constant FORZEN_STATUS = 4;
    uint8 constant TEAR_STATUS = 5;
    NonFungibleBook book;
    INonFungibleStorage  assetStorage;
    constructor(string assetName, address orgAddress, bool isCreate, address _storage) public BaseAsset(orgAddress)
    {
        require(bytes(assetName).length > 0 && bytes(assetName).length < 64, "assetName should be not null and less than 64 ");
        assetStorage = INonFungibleStorage(_storage);
        if (isCreate) {
            book = new NonFungibleBook(IOrganization(orgAddress).getProjectTerm(), assetName, orgAddress);
            assetStorage.init(address(book));
        } else {
            book = NonFungibleBook(assetStorage.getBook(assetName));
        }
        isFungible = false;
    }

    function setPrice(uint256 _price) public onlyManager returns (uint256){
        assetStorage.setPrice(_price);
        return assetStorage.getPrice();
    }

    function openAccount(address account) onlyAccountNormal(account) onlyManager public returns (bool){
        IAccountHolder(account).addAsset(isFungible, address(this));
        assetStorage.addAccount(account);
        return addAccount(account);

    }


    //uint256[] amount,noteNoPrefix, noteNoSize,effectiveDate,expirationDate
    function issue(address[] transactionAddress, uint256[] uint256Args, bytes[] stringValueList)
    public
    onlyManager
    onlyAccountNormal(transactionAddress[3])
    existAccount(transactionAddress[3])
    returns (uint256, uint256[], uint[]){
        checkArgs(transactionAddress, uint256Args, stringValueList);

        uint256 startId = 1;
        uint256 batchNo = 1;
        address issuer = transactionAddress[3];

        assetStorage.updateExistNote(uint256Args[1], uint256Args[2]);
        (startId, batchNo) = createBatch(uint256Args[3], uint256Args[4], uint256Args[0], issuer);
        uint256[] memory noteNos = new uint256[](uint256Args[0]);
        uint[] memory result = new uint[](uint256Args[0] * 2);
        noteNos = issueNotes(transactionAddress, uint256Args, startId, batchNo);
        result = writeNotes(noteNos, transactionAddress, stringValueList);
        assetStorage.addTotalNoteSize(uint256Args[0]);
        return (batchNo, noteNos, result);
    }


    function getNoteDetail(uint256 noteNo)
    public
    onlyManager
    constant
    returns (address[], uint256[], uint[], uint8){
        address[] memory addressList = new address[](2);
        uint256[] memory uint256List = new  uint256[](2);
        uint[] memory uintList = new uint[](2);
        uint8 noteStatus;
        (addressList, uint256List, uintList, noteStatus) = assetStorage.getNote(noteNo);

        return (addressList, uint256List, uintList, noteStatus);
    }


    function transfer(address[] transactionAddress, uint[] noteNos, bytes[] stringValueList)
    public
    onlyManager
    returns (bool, uint[]){
        uint[] memory result = new uint[](3 * noteNos.length);
        bool isWriteSuccess;
        checkTransfer(transactionAddress, noteNos);
        uint256 noteId;
        for (uint j = 0; j < noteNos.length; j++) {
            noteId = assetStorage.exchangeNote(transactionAddress[2], transactionAddress[3], noteNos[j]);
            uint[2] memory bookResult;
            (isWriteSuccess, bookResult) = book.write(transactionAddress, noteId, stringValueList);
            require(isWriteSuccess, "write book fail!");

            result[3 * j] = bookResult[0];
            result[3 * j + 1] = bookResult[1];
            result[3 * j + 2] = noteNos[j];

        }
        return (true, result);
    }

    function checkTransfer(address[] transactionAddress, uint[] noteNos)
    existAccount(transactionAddress[2])
    existAccount(transactionAddress[3])
    internal {
        for (uint i = 0; i < noteNos.length; i++) {
            require(assetStorage.isExistNote(noteNos[i]), "noteNo ".strConcat(noteNos[i].uint2str()).strConcat(" doesn't exist!"));
            require(!assetStorage.checkExpire(noteNos[i]), "noteNo ".strConcat(noteNos[i].uint2str()).strConcat(" has been expired"));
            require(assetStorage.checkEffectiveAndUpdate(noteNos[i]), "note is not effective");
            require(assetStorage.checkOwner(noteNos[i], transactionAddress[2]), "Forbidden transfer because from isn't owner");
        }
    }

    function accountHoldNote(address account, uint256 noteNo)
    onlyManager
    public
    constant
    returns (bool isContain){
        isContain = assetStorage.accountHoldNote(account, noteNo);
    }

    function getAccountNotes(address account, uint256 start, uint256 end) onlyManager  public constant returns (uint256[]){
        require(start < end, "require start < end");
        return assetStorage.getNoteByAccount(account, start, end);
    }

    function getAccountNoteSize(address account) public onlyManager constant returns (uint256){
        return assetStorage.getAccountNoteSize(account);
    }


    function updateNoteNo(uint256 oldNoteNo, uint256 newNoteNo, address account)
    onlyManager
    public
    returns (bool isUpdate){
        require(assetStorage.isExistNote(oldNoteNo), "note doesn't exist!");
        require(!assetStorage.isExistNote(newNoteNo), "newNoteNo has been issue!");
        require(assetStorage.getStatusByNote(oldNoteNo) != FORZEN_STATUS && assetStorage.getStatusByNote(oldNoteNo) != TEAR_STATUS, "note is forzen or tear");

        isUpdate = assetStorage.updateNoteNo(oldNoteNo, newNoteNo);
    }

    function updateNoteProperties(uint256 noteNo, bytes[] keys, bytes[] values, address account)
    onlyManager
    public
    returns (bytes[] noteKeys, bytes[] noteValues){
        checkUpdateNoteNoAuth(noteNo, keys, values, account);
        return assetStorage.updateNoteProperties(noteNo, keys, values);
    }

    function getNoteProperties(uint256 noteNo, address account)
    public
    onlyManager
    constant returns (bytes[] noteKeys, bytes[] noteValues){
        require(assetStorage.isExistNote(noteNo), "note doesn't exist!");
        require(assetStorage.checkIssuer(noteNo, account), "Forbidden getNoteProperties");
        return assetStorage.getNoteProperties(noteNo);
    }

    function updateNoteBatch(uint256 batchNo, uint date, bool isEffectiveDate, address account)
    onlyManager
    public
    returns (bool isUpdate){
        require(date > getDate(), "require date > now");
        bytes args;
        checkUpdateBatchAuth(batchNo, args, account);
        isUpdate = assetStorage.updateNoteBatch(batchNo, date, isEffectiveDate);
    }

    function checkUpdateBatchAuth(uint256 batchNo, bytes args, address account) internal returns (bool){
        require(assetStorage.checkIssuerByBatchNo(batchNo, account), "Forbidden updateNoteBatch");
        return true;
    }

    function freezeNote(uint256 noteNo, address account)
    onlyManager
    public
    returns (bool isForzen){
        require(assetStorage.isExistNote(noteNo), "note doesn't exist!");
        require(assetStorage.checkIssuer(noteNo, account), "Forbidden forzenNote");
        require(assetStorage.isExistNote(noteNo), "note doesn't exist");
        require(assetStorage.checkEffect(noteNo), "note is not effective");

        isForzen = true;
        uint256 noteId = assetStorage.updateNoteStatus(noteNo, FORZEN_STATUS);
        emit LogNoteStatus(noteNo, noteId, FORZEN_STATUS);
    }

    function unfreezeNote(uint256 noteNo, address account)
    onlyManager
    public
    returns (uint8){
        require(assetStorage.isExistNote(noteNo), "note doesn't exist!");
        require(assetStorage.checkIssuer(noteNo, account), "Forbidden unfreezeNote");
        require(assetStorage.getStatusByNote(noteNo) == FORZEN_STATUS, "note is not forzen");

        uint256 noteId = assetStorage.updateNoteStatus(noteNo, EFFECTIVE_STATUS);
        emit LogNoteStatus(noteNo, noteId, EFFECTIVE_STATUS);
        return EFFECTIVE_STATUS;
    }


    function queryBook(uint[] uintCondition, address[] addressCondition, int[] limit) public onlyManager constant returns (string[] memory){
        require(limit.length == 2 && limit[0] < limit[1], "limit not verify,limit size should equals 2 and limit[0]<limit[1]");
        if (assetStorage.getTotalNoteSize() == 0) {
            string[] memory result;
            return result;
        }

        if (uintCondition.length > 2 && uintCondition[2] != 0) {
            require(assetStorage.isExistNote(uintCondition[2]), "note doesn't exist!");
            uintCondition[2] = assetStorage.getNoteIdByNo(uintCondition[2]);
        }

        return book.query(uintCondition, addressCondition, limit);
    }


    function getTotalNoteSize() public onlyManager constant returns (uint256){
        return assetStorage.getTotalNoteSize();
    }


    //queryAccountList
    function getHolders() public onlyManager constant returns (address[])
    {
        return getAccounts();
    }


    function tearNote(uint256 noteNo, address account) onlyAccountNormal(account) onlyManager public returns (bool){
        require(assetStorage.isExistNote(noteNo), "note doesn't exist!");
        assetStorage.updateNoteStatus(noteNo, TEAR_STATUS);
        uint256 noteId = assetStorage.removeNoteFromAccount(noteNo, account);
        assetStorage.addTearNote(noteNo);
        assetStorage.mulTotalNoteSize(1);
        emit LogNoteStatus(noteNo, noteId, TEAR_STATUS);
        return true;
    }

    function getTearNotes(address account) onlyAccountNormal(account) onlyManager public view returns (uint256[]){
        return assetStorage.getTearNotes();
    }

    // increaseLedgerNumber
    function addBook() public onlyManager returns (uint256){
        return book.addBook();
    }

    function enableBatch(uint256 batchNo) public onlyManager returns (bool isEffective){
        isEffective = assetStorage.enableBatch(batchNo);
    }


    function genTransferArgs(address[] transactionAddress, uint[] noteNos, string[] stringValueList) internal view returns (bytes){
        bytes memory args;
        for (uint i = 0; i < transactionAddress.length; i++) {
            args = args.bytesAppend(transactionAddress[i]);
        }

        for (uint j = 0; j < noteNos.length; j++) {
            args = args.bytesAppend(noteNos[j]);
        }
        for (uint k = 0; k < stringValueList.length; k++) {
            args = args.bytesAppend(bytes(stringValueList[k]));
        }
        return args;
    }


    function checkArgs(address[] transactionAddress, uint256[] uint256Args, bytes[] stringValueList) internal {
        require(transactionAddress.length == 4 && uint256Args.length == 5 && stringValueList.length > 0, "params not verify");
        require(uint256Args[2] > 0 && uint256Args[2] < 10 && uint256Args[4] > getDate(), "require 10 > noteNoSize >0 and expirationDate >now");
        if (uint256Args[3] > 0) {
            require(uint256Args[3] > getDate(), "require effectiveDate>now");
            require(uint256Args[4] > uint256Args[3], "require expirationDate >  effectiveDate");
        }
        require(uint256Args[0] > 0 && uint256Args[0] < 501, "require 500>=amount>0");
        require(uint256Args[1] > 0, "require noteNoPrefix>0");

        require(stringValueList[0].length > 0 && stringValueList[0].length <= 255, "desc bytes are between 1 and 255 in length");
        require(!assetStorage.isExistNoteDefined(uint256Args[1], uint256Args[2]), "notes has been defined please change noteNoPrefix or noteNoSize");
    }

    function genIssueArgs(address[] transactionAddress, uint256[] uint256Args, string[] stringValueList) internal view returns (bytes){
        bytes memory args;
        for (uint i = 0; i < transactionAddress.length; i++) {
            args = args.bytesAppend(transactionAddress[i]);
        }
        for (uint j = 0; j < uint256Args.length; j++) {
            args = args.bytesAppend(uint256Args[j]);
        }

        for (uint k = 0; k < stringValueList.length; k++) {
            args = args.bytesAppend(bytes(stringValueList[k]));
        }
        return args;
    }

    function issueNotes(address[] transactionAddress, uint256[] uint256Args, uint256 startId, uint256 batchNo) internal returns (uint[]){
        uint256 noteId;
        uint[] memory bookResult;
        uint[] memory result = new uint[](noteNos.length * 2);

        uint256[] memory noteNos = new uint256[](uint256Args[0]);
        for (uint i = 0; i < uint256Args[0]; i++) {
            if (i == 0) {
                noteId = startId.sub(1);
            } else {
                noteId = assetStorage.generateNoteId().sub(1);
            }
            noteNos[i] = createNote(transactionAddress[3], batchNo, noteId, uint256Args[1], uint256Args[2], i);
        }

        return noteNos;
    }

    function writeNotes(uint256[] noteNos, address[] transactionAddress, bytes[] stringValueList) internal returns (uint[]){
        uint[] memory result = new uint[](noteNos.length * 2);
        bool isWriteSuccess;
        uint256 noteId;
        uint[2] memory bookResult;

        for (uint i = 0; i < noteNos.length; i++) {
            noteId = assetStorage.getNoteIdByNo(noteNos[i]);
            (isWriteSuccess, bookResult) = book.write(transactionAddress, noteId, stringValueList);

            result[2 * i] = bookResult[0];
            result[2 * i + 1] = bookResult[1];
            require(isWriteSuccess, "write book fail!");
        }
        return result;

    }

    function createBatch(uint effectiveDate, uint expirationDate, uint256 amount, address issuer) internal returns (uint256, uint256){
        require(expirationDate > getDate(), "require expirationDate > now");
        if (effectiveDate > 0) {
            require(effectiveDate >= getDate(), "require effectiveDate >= now");
        }

        return assetStorage.createBatch(effectiveDate, expirationDate, amount, issuer);
    }

    function createNote(address owner, uint256 batchNo, uint256 noteId, uint256 noteNoPrefix, uint noteNoSize, uint index) internal returns (uint256 noteNo){
        noteNo = (noteNoPrefix * (10 ** noteNoSize)).add(index + 1);
        assetStorage.createNote(noteId, noteNo, owner, batchNo);
    }


    function checkUpdateNoteNoAuth(uint256 noteNo, bytes[] keys, bytes[] values, address account) internal {
        require(assetStorage.isExistNote(noteNo), "note doesn't exist!");
        require(assetStorage.getStatusByNote(noteNo) != FORZEN_STATUS && assetStorage.getStatusByNote(noteNo) != TEAR_STATUS, "note is forzen or tear");
    }


    function getDate() internal returns (uint){
        uint time = now;
        return (time);
    }

    function getNoteNoByNoteId(uint256 noteId) public returns (uint256){
        return assetStorage.getNoteNoByNoteId(noteId);
    }
}