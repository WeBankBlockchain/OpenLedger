pragma solidity ^0.4.25;
pragma experimental ABIEncoderV2;

import "./interface/INonFungibleManager.sol";
import "./Sequence.sol";
import "./lib/LibSafeMath.sol";

contract NonFungibleStorage {
    using LibSafeMath for uint256;

    modifier onlyOwner() {
        require(assetManager.checkStorageCallee(msg.sender, address(this)), "required owner called");
        _;
    }
    modifier onlyOnceInit() {
        require(isInit==false, "storage has been init");
        _;
    }
    modifier onlyUpgrade(string assetName) {
        require(assetManager.checkUpgrade(assetName), "upgrade not verify");
        _;
    }
    uint8 constant INIT_STATUS = 1;
    uint8 constant EFFECTIVE_STATUS = 2;
    uint8 constant EXPIRATE_STATUS = 3;
    uint8 constant FORZEN_STATUS = 4;
    uint8 constant TEAR_STATUS = 5;
    uint256 public price;
    //账户地址
    address[] accountList;
    //账户存在则true
    mapping(address => bool) accountMap;

    struct Note {
        uint256 id;
        address owner;
        uint256 noteNo;
        uint256 batchNo;
        uint8 status;
        mapping(bytes => bytes) items;
        bytes[] itemKeys;
    }

    struct NoteBatch {
        uint256 batchNo;
        address issuer;
        uint256 startId;
        uint256 endId;
        uint effectiveDate;
        uint expirationDate;
    }

    mapping(uint256 => Note) notes;
    mapping(uint256 => NoteBatch) noteBatchs;
    mapping(uint256 => uint256) noIdMap;
    mapping(address => mapping(uint256 => bool)) accountNotes;
    mapping(address => uint256[]) accountNoteList;
    mapping(uint256 => mapping(uint => bool)) existNoteNo;

    address noteSeq;
    address batchSeq;
    uint256 totalNoteSize;
    address book;
    uint256[] tearNotes;
    INonFungibleManager assetManager;

    constructor(address manager){
        assetManager = INonFungibleManager(manager);
    }


    /*******************check functions************************************************/
    function checkEffectiveAndUpdate(uint256 noteNo) public onlyOwner constant returns (bool isEffective){
        Note memory note = notes[noIdMap[noteNo]];
        if (EFFECTIVE_STATUS == note.status) {
            isEffective = true;
            return;
        }
        NoteBatch noteBatch = noteBatchs[note.batchNo];
        if (noteBatch.effectiveDate == 0) {
            isEffective = false;
            return;
        }
        if (noteBatch.effectiveDate < getDate()) {
            note.status = EFFECTIVE_STATUS;
            notes[note.id] = note;
            isEffective = true;
            return;
        }
    }

    function checkOwner(uint256 noteNo, address account) public onlyOwner view returns (bool isOwner){
        uint256 noteId = noIdMap[noteNo];
        require(noteId > 0, "note doesn't exist!");
        address owner = notes[noteId].owner;
        if (owner == account) {
            isOwner = true;
        }
    }

    function checkIssuer(uint256 noteNo, address account) public onlyOwner view returns (bool isIssuer){
        uint256 noteId = noIdMap[noteNo];
        require(noteId > 0, "note doesn't exist!");
        address owner = notes[noteId].owner;
        NoteBatch noteBatch = noteBatchs[notes[noteId].batchNo];
        if (address(0) == account || noteBatch.issuer != account) {
            isIssuer = false;
        }
        isIssuer = true;
    }

    function checkEffect(uint256 noteNo) public onlyOwner view returns (bool){
        Note memory note = notes[noIdMap[noteNo]];
        return note.status == EFFECTIVE_STATUS;
    }

    function checkExpire(uint256 noteNo) public onlyOwner constant returns (bool isExpire){
        Note memory note = notes[noIdMap[noteNo]];
        if (EXPIRATE_STATUS == note.status) {
            isExpire = true;
            return;
        }

        NoteBatch noteBatch = noteBatchs[note.batchNo];
        if (noteBatch.expirationDate < getDate()) {
            note.status = EXPIRATE_STATUS;
            notes[note.id] = note;
            isExpire = true;
            return;
        }

    }

    function isExistNoteDefined(uint256 noteNoPrefix, uint256 noteNoSize) public onlyOwner view returns (bool){
        return existNoteNo[noteNoPrefix][noteNoSize];
    }

    function isExistNote(uint256 noteNo) public view returns (bool){
        if (noIdMap[noteNo] > 0) {
            return true;
        }
        return false;
    }

    function checkIssuerByBatchNo(uint256 batchNo, address account) public returns (bool){
        require(noteBatchs[batchNo].batchNo > 0, "batch doesn't exist!");
        NoteBatch noteBatch = noteBatchs[batchNo];
        if (address(0) == account || noteBatch.issuer != account) {
            return false;
        }
        return true;
    }
    /*********************** global parameter set *********************************************/
    bool isInit;
    // init storage
    function init(address _book) public onlyOnceInit {
        Sequence _noteSeq = new Sequence();
        Sequence _batchSeq = new Sequence();
        book = _book;
        noteSeq = address(_noteSeq);
        batchSeq = address(_batchSeq);
        isInit = true;
    }

    function getBook(string assetName) public view onlyUpgrade(assetName) returns (address){
        return book;
    }
    // update price
    function setPrice(uint256 _price) public onlyOwner returns (bool){
        price = _price;
        return true;
    }

    function getPrice() public onlyOwner returns (uint256){
        return price;
    }

    /***********************account‘s function*********************************************/
    function addAccount(address account) public onlyOwner {
        accountList.push(account);
        accountMap[account] = true;
    }

    function existAccount(address account) public onlyOwner view returns (bool){
        return accountMap[account];
    }

    function getAccounts() public onlyOwner view returns (address[]){
        return accountList;
    }

    //***********************note‘s function*********************************************/
    function updateExistNote(uint256 noteNoPrefix, uint256 noteNoSize) public onlyOwner {
        existNoteNo[noteNoPrefix][noteNoSize] = true;
    }

    function createNote(uint256 noteId, uint256 noteNo, address owner, uint256 batchNo) public onlyOwner {
        bytes[]  memory itemkeys = new bytes[](1);
        Note memory note = Note(noteId, owner, noteNo, batchNo, INIT_STATUS, itemkeys);
        notes[noteId] = note;
        noIdMap[noteNo] = noteId;
        accountNotes[owner][noteId] = true;
        accountNoteList[owner].push(noteId);
    }

    function generateNoteId() public onlyOwner returns (uint256){
        return Sequence(noteSeq).get();
    }

    function getNote(uint256 noteNo) public onlyOwner view returns (address[], uint256[], uint[], uint8){
        Note memory note = notes[noIdMap[noteNo]];
        address[] memory addressList = new address[](2);
        uint256[] memory uint256List = new  uint256[](2);
        uint[] memory uintList = new uint[](2);
        addressList[0] = note.owner;
        uint256List[0] = note.noteNo;
        uint256List[1] = note.batchNo;
        NoteBatch memory noteBatch = noteBatchs[note.batchNo];
        uintList[0] = noteBatch.effectiveDate;
        uintList[1] = noteBatch.expirationDate;
        addressList[1] = noteBatch.issuer;

        return (addressList, uint256List, uintList, note.status);
    }

    function getNoteByAccount(address account, uint256 start, uint256 end) public onlyOwner view returns (uint256[]){
        uint256[] noteIds = accountNoteList[account];
        uint256 resultLength;
        uint256[] memory noteNos;
        if (noteIds.length == 0) {
            return noteIds;
        }
        require(start < noteIds.length, "require start less than account note size");
        if (end > noteIds.length) {
            resultLength = noteIds.length;
        } else {
            resultLength = end;
        }
        noteNos = new uint256[](resultLength <= 0 ? 0 : resultLength - start);
        uint j = 0;
        for (uint i = start; i < resultLength; i++) {
            noteNos[j++] = notes[noteIds[i]].noteNo;
        }
        return noteNos;
    }

    function getAccountNoteSize(address account) public onlyOwner constant returns (uint256){
        uint256[] noteIds = accountNoteList[account];
        return noteIds.length;
    }

    function updateNoteNo(uint256 oldNoteNo, uint256 newNoteNo) public onlyOwner returns (bool isUpdate){
        noIdMap[newNoteNo] = noIdMap[oldNoteNo];
        noIdMap[oldNoteNo] = 0;
        Note memory note = notes[noIdMap[newNoteNo]];
        note.noteNo = newNoteNo;
        notes[noIdMap[newNoteNo]] = note;
        isUpdate = true;
    }

    function getStatusByNote(uint256 noteNo) public onlyOwner view returns (uint8){
        Note memory note = notes[noIdMap[noteNo]];
        return note.status;
    }

    function updateNoteProperties(uint256 noteNo, bytes[] keys, bytes[] values) public onlyOwner returns (bytes[] noteKeys, bytes[] noteValues){
        uint256 noteId = noIdMap[noteNo];
        Note note = notes[noteId];
        for (uint i = 0; i < keys.length; i++) {
            note.items[keys[i]] = values[i];
            note.itemKeys.push(keys[i]);
        }
        notes[noteId] = note;
        noteKeys = new bytes[](note.itemKeys.length);
        noteValues = new bytes[](note.itemKeys.length);
        for (uint j = 0; j < note.itemKeys.length; j++) {
            if (j == 0) {
                continue;
            }

            noteKeys[j] = note.itemKeys[j];
            noteValues[j] = note.items[note.itemKeys[j]];
        }
    }

    function getNoteProperties(uint256 noteNo) public onlyOwner constant returns (bytes[] noteKeys, bytes[] noteValues){
        uint256 noteId = noIdMap[noteNo];
        Note note = notes[noteId];
        noteKeys = new bytes[](note.itemKeys.length);
        noteValues = new bytes[](note.itemKeys.length);
        for (uint j = 0; j < note.itemKeys.length; j++) {
            if (j == 0) {
                continue;
            }
            noteKeys[j] = note.itemKeys[j];
            noteValues[j] = note.items[note.itemKeys[j]];
        }
    }

    function removeNoteFromAccount(uint256 noteNo, address account) public onlyOwner returns (uint256){
        Note memory note = notes[noIdMap[noteNo]];
        accountNotes[account][note.id] = false;
        uint256[] orignalNoteIds = accountNoteList[account];
        uint256[] memory newNoteIds = new uint256[](orignalNoteIds.length - 1);
        bool isIndex;
        for (uint i = 0; i < newNoteIds.length; i++) {
            if (!isIndex && orignalNoteIds[i] == note.id) {
                isIndex = true;
            }
            if (isIndex) {
                newNoteIds[i] = orignalNoteIds[i + 1];
            } else {
                newNoteIds[i] = orignalNoteIds[i];
            }
        }
        accountNoteList[account] = newNoteIds;
        return note.id;
    }

    function addNoteToAccount(uint256 noteNo, address account) public onlyOwner returns (uint256){
        Note memory note = notes[noIdMap[noteNo]];
        accountNotes[account][note.id] = true;
        accountNoteList[account].push(note.id);
        note.owner = account;
        notes[noIdMap[noteNo]] = note;
        return note.id;
    }

    function exchangeNote(address from, address to, uint256 noteNo) public onlyOwner returns (uint256){
        Note memory note = notes[noIdMap[noteNo]];
        require(accountNotes[from][note.id] = true, "The account is not owned this note!");
        require(removeNoteFromAccount(noteNo, from) == note.id, "exchange fail");
        require(addNoteToAccount(noteNo, to) == note.id, "exchange fail");
        return note.id;
    }

    function accountHoldNote(address account, uint256 noteNo) public onlyOwner view returns (bool isContain){
        uint256 noteId = noIdMap[noteNo];
        require(noteId > 0, "note doesn't exist!");
        isContain = accountNotes[account][noteId];
    }

    function getNoteIdByNo(uint256 noteNo) public onlyOwner view returns (uint256){
        require(noIdMap[noteNo] > 0, "note doesn't exist!");
        return noIdMap[noteNo];
    }


    function getNoteNoByNoteId(uint256 noteId) public onlyOwner constant returns (uint256){
        require(notes[noteId].noteNo > 0, "note doesn't exist");
        return notes[noteId].noteNo;
    }

    function updateNoteStatus(uint256 noteNo, uint8 status) public onlyOwner returns (uint256){
        Note note = notes[noIdMap[noteNo]];
        note.status = status;
        notes[note.id] = note;
        return note.id;
    }


    //***********************batch‘s function*********************************************/

    function createBatch(uint effectiveDate, uint expirationDate, uint256 amount, address issuer) public onlyOwner returns (uint256, uint256){
        uint256 startId = Sequence(noteSeq).get();
        uint256 batchNo = Sequence(batchSeq).get().sub(1);
        NoteBatch memory noteBatch = NoteBatch(batchNo, issuer, startId-1, startId.add(amount - 1), effectiveDate, expirationDate);
        noteBatchs[batchNo] = noteBatch;
        return (startId, batchNo);
    }

    function updateNoteBatch(uint256 batchNo, uint date, bool isEffectiveDate) public onlyOwner returns (bool isUpdate){
        NoteBatch memory noteBatch = noteBatchs[batchNo];
        if (isEffectiveDate) {
            require(date < noteBatch.expirationDate, "require effectiveDate< expirationDate");
            require(notes[noteBatch.startId].status!=EFFECTIVE_STATUS,"batch has been effect");
            noteBatch.effectiveDate = date;
        } else {
            if (noteBatch.effectiveDate > 0) {
                require(date > noteBatch.effectiveDate, "require effectiveDate< expirationDate");
                require(notes[noteBatch.startId].status!=EXPIRATE_STATUS,"batch has been expirated");
            }
            noteBatch.expirationDate = date;
        }
        noteBatchs[batchNo] = noteBatch;
        isUpdate = true;
    }

    function enableBatch(uint256 batchNo) public onlyOwner returns (bool){
        NoteBatch batch = noteBatchs[batchNo];
        require(batch.endId > 0, "batch not verify");
        if (batch.effectiveDate > 0) {
            require(batch.effectiveDate < getDate(), "batch can not be effective,because effectiveDate>now");
        }
        bool isEffective = batch.effectiveDate == 0 || batch.effectiveDate < getDate();
        batch.effectiveDate = getDate();
        noteBatchs[batchNo] = batch;
        if (isEffective) {
            for (uint i = batch.startId; i <= batch.endId; i++) {
                Note note = notes[i];
                note.status = EFFECTIVE_STATUS;
                notes[i] = note;
            }
        }
        return isEffective;
    }

    //***********************totalNoteSize‘s function*********************************************/

    function addTotalNoteSize(uint256 amount) public onlyOwner {
        totalNoteSize = totalNoteSize + amount;
    }

    function getTotalNoteSize() public onlyOwner view returns (uint256){
        return totalNoteSize;
    }

    function mulTotalNoteSize(uint256 amount) public onlyOwner {
        totalNoteSize = totalNoteSize - amount;
    }

    //***********************tearNote‘s function*********************************************/

    function addTearNote(uint256 noteNo) public onlyOwner {
        tearNotes.push(noteNo);
    }

    function getTearNotes() public onlyOwner view returns (uint256[]){
        return tearNotes;
    }

    //***********************internal utiln*********************************************/

    function getDate() internal returns (uint){
        uint time = now;
        return (time);
    }
}