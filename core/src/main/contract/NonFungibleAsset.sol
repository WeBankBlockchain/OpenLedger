pragma solidity ^0.4.25;
pragma experimental ABIEncoderV2;

import "./lib/LibSafeMath.sol";
import "./lib/UtilLib.sol";
import "./lib/LibTypeConversion.sol";

import "./Sequence.sol";
import "./NonFungibleBook.sol";
import "./Identity.sol";
import "./interface/IOrganization.sol";
import "./interface/IAccount.sol";


// nonHomogeneousAssets
contract NonFungibleAsset is Identity {

    using LibSafeMath for uint256;
    using UtilLib for *;
    using LibTypeConversion for uint;

    event LogAccountList(address account, uint256[]);
    event LogNoteStatus(uint256 noteNo, uint256 noteID, uint8 status);
    modifier onlyAccountNormal(address account) {
        require(authCenter.checkAccount(account), "Auth:only account status is normal.");
        _;
    }
    // price
    uint256 public price;
    uint8 constant INIT_STATUS = 1;
    uint8 constant EFFECTIVE_STATUS = 2;
    uint8 constant EXPIRATE_STATUS = 3;
    uint8 constant FORZEN_STATUS = 4;
    uint8 constant TEAR_STATUS = 5;

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

    Sequence noteSeq;
    Sequence batchSeq;
    //allocatesSpaceForMapping
    Note storageVar;
    uint256 totalNoteSize;
    NonFungibleBook book;
    uint256[] tearNotes;

    constructor(string assetName, address authCenterAddr, address orgAddress) public Identity(authCenterAddr, orgAddress)
    {
        require(bytes(assetName).length > 0 && bytes(assetName).length < 64, "assetName should be not null and less than 64 ");
        noteSeq = new Sequence();
        batchSeq = new Sequence();
        book = new NonFungibleBook(IOrganization(orgAddress).getProjectTerm(), assetName, orgAddress);
    }

    function setPrice(uint256 _price, bytes32[4] sign) public returns (uint256){
        bytes memory args;
        args = args.bytesAppend(_price);
        address txOrigin;
        bool check;
        (txOrigin, check) = authCenter.check2WithSign(org, this, "setNonFungiblePrice", args, sign);
        require(check, "setNonFungiblePrice Forbidden");

        price = _price;
        return price;
    }

    function openAccount(address account, bytes32[4] sign) onlyAccountNormal(account) public returns (bool){
        bytes memory args;
        args = args.bytesAppend(account);
        address txOrigin;
        bool check;
        (txOrigin, check) = authCenter.check2WithSign(org, this, "openAccount", args, sign);
        require(check, "Forbidden openAccount");

        require(authCenter.checkAccount(txOrigin), "Auth:only account status is normal.");
        address innerAddress = IOrganization(org).getInnerAccount(account);
        require(!accountMap[innerAddress], "the account has been open");

        IAccount userAccount = IAccount(IOrganization(org).getAccount(account));
        userAccount.addAsset(this, org, false);

        accountList.push(innerAddress);
        accountMap[innerAddress] = true;
        return true;
    }


    //uint256[] amount,noteNoPrefix, noteNoSize,effectiveDate,expirationDate
    function issue(address[] transactionAddress, uint256[] uint256Args, string[] stringValueList, bytes32[4] sign) public returns (uint256, uint256[], uint[]){
        checkArgs(transactionAddress, uint256Args, stringValueList);
        transactionAddress = checkAuth(transactionAddress, uint256Args, stringValueList, sign);

        uint256 startId = 1;
        uint256 batchNo = 1;
        address issuer = transactionAddress[3];

        existNoteNo[uint256Args[1]][uint256Args[2]] = true;
        (startId, batchNo) = createBatch(uint256Args[3], uint256Args[4], uint256Args[0], issuer);
        uint256[] memory noteNos = new uint256[](uint256Args[0]);
        uint[] memory result = new uint[](uint256Args[0] * 2);
        noteNos = issueNotes(transactionAddress, uint256Args, startId, batchNo);
        result = writeNotes(noteNos, transactionAddress, stringValueList);
        emit LogAccountList(issuer, accountNoteList[issuer]);
        totalNoteSize = totalNoteSize.add(uint256Args[0]);

        //        return (getAccountNoteLimit(issuer, 0, uint256Args[0] > 100 ? 100 : uint256Args[0]), result);
        return (batchNo, noteNos, result);
    }


    function getNoteDetail(uint256 noteNo, address account, bytes32[4] sign) public constant returns (address[], uint256[], uint[], uint8){
        checkGetAuth(noteNo, account, "getNoteDetail", sign);
        Note memory note = notes[noIdMap[noteNo]];
        address[] memory addressList = new address[](2);
        uint256[] memory uint256List = new  uint256[](2);
        uint[] memory uintList = new uint[](2);
        addressList[0] = IOrganization(org).getExternalAccount(note.owner);
        uint256List[0] = note.noteNo;
        uint256List[1] = note.batchNo;
        NoteBatch memory noteBatch = noteBatchs[note.batchNo];
        uintList[0] = noteBatch.effectiveDate;
        uintList[1] = noteBatch.expirationDate;
        addressList[1] = IOrganization(org).getExternalAccount(noteBatch.issuer);

        return (addressList, uint256List, uintList, note.status);
    }


    function transfer(address[] transactionAddress, uint[] noteNos, string[] stringValueList, bytes32[4] sign) public returns (bool, uint[]){
        transactionAddress = checkTransferAuth(transactionAddress, noteNos, stringValueList, sign);
        Note memory note;
        uint[] memory result = new uint[](3 * noteNos.length);
        bool isWriteSuccess;
        for (uint i = 0; i < noteNos.length; i++) {
            require(noIdMap[noteNos[i]] > 0, "noteNo ".strConcat(noteNos[i].uint2str()).strConcat(" doesn't exist!"));
            note = notes[noIdMap[noteNos[i]]];
            require(!checkExpire(note.id), "noteNo ".strConcat(noteNos[i].uint2str()).strConcat(" has been expired"));
            require(checkEffectiveAndUpdate(note.id), "note is not effective");
            require(checkOwnerSingle(noteNos[i], transactionAddress[2]), "Forbidden transfer because from isn't owner");
        }
        for (uint j = 0; j < noteNos.length; j++) {
            note = notes[noIdMap[noteNos[j]]];
            accountNotes[transactionAddress[2]][note.id] = false;
            accountNoteList[transactionAddress[2]] = removeNoteFromAccount(transactionAddress[2], note.id);
            accountNotes[transactionAddress[3]][note.id] = true;
            accountNoteList[transactionAddress[3]].push(note.id);
            note.owner = transactionAddress[3];
            notes[noIdMap[noteNos[j]]] = note;
            uint[2] memory bookResult;
            (isWriteSuccess, bookResult) = book.write(transactionAddress, note.id, stringValueList);
            require(isWriteSuccess, "write book fail!");

            result[3 * j] = bookResult[0];
            result[3 * j + 1] = bookResult[1];
            result[3 * j + 2] = noteNos[j];

        }
        return (true, result);
    }


    function accountHoldNote(address account, uint256 noteNo, bytes32[4] sign) onlyAccountNormal(account) public constant returns (bool isContain){
        bytes memory args;
        address txOrigin;
        bool check;
        (txOrigin, check) = authCenter.check2WithSign(org, this, "accountContainsNote", args, sign);
        require(check, "Forbidden accountContainsNote");
        require(authCenter.checkAccount(txOrigin), "Auth:only account status is normal.");
        address innerAddress = IOrganization(org).getInnerAccount(account);

        uint256 noteId = noIdMap[noteNo];
        require(noteId > 0, "note doesn't exist!");
        isContain = accountNotes[innerAddress][noteId];
    }

    function getAccountNotes(address account, uint256 start, uint256 end, bytes32[4] sign) onlyAccountNormal(account) public constant returns (uint256[]){
        require(start < end, "require start < end");
        address innerAddress = IOrganization(org).getInnerAccount(account);
        bytes memory args;
        address txOrigin;
        bool check;
        (txOrigin, check) = authCenter.check2WithSign(org, innerAddress, "getAccountNotes", args, sign);
        require(check, "Forbidden getAccountNotes");
        require(authCenter.checkAccount(txOrigin), "Auth:only account status is normal.");

        return getAccountNoteLimit(innerAddress, start, end);
    }

    function getAccountNoteSize(address account, bytes32[4] sign) onlyAccountNormal(account) public constant returns (uint256){
        address innerAddress = IOrganization(org).getInnerAccount(account);
        bytes memory args;
        address txOrigin;
        bool check;
        (txOrigin, check) = authCenter.check2WithSign(org, innerAddress, "getAccountNotes", args, sign);
        require(check, "Forbidden getAccountNotes");
        require(authCenter.checkAccount(txOrigin), "Auth:only account status is normal.");

        uint256[] noteIds = accountNoteList[innerAddress];
        return noteIds.length;
    }

    function getAccountNoteLimit(address innerAddress, uint256 start, uint256 end) internal returns (uint256[]){
        uint256[] noteIds = accountNoteList[innerAddress];
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
        for (uint i = start; i < resultLength; i++) {
            noteNos[i] = notes[noteIds[i]].noteNo;
        }
        return noteNos;
    }

    function updateNoteNo(uint256 oldNoteNo, uint256 newNoteNo, address account, bytes32[4] sign) onlyAccountNormal(account) public returns (bool isUpdate){
        require(noIdMap[oldNoteNo] > 0, "note doesn't exist!");
        require(noIdMap[newNoteNo] == 0, "newNoteNo has been issue!");
        bytes memory args;
        args = args.bytesAppend(oldNoteNo).bytesAppend(newNoteNo).bytesAppend(account);
        address txOrigin;
        bool check;
        address innerAccount = IOrganization(org).getInnerAccount(account);
        (txOrigin, check) = authCenter.check2WithSign(org, this, "updateNoteNo", args, sign);
        require(check && checkIssuer(oldNoteNo, innerAccount), "Forbidden updateNoteNo");
        require(authCenter.checkAccount(txOrigin), "Auth:only account status is normal.");
        checkEffectiveAndUpdate(noIdMap[oldNoteNo]);
        require(notes[noIdMap[oldNoteNo]].status != FORZEN_STATUS && notes[noIdMap[oldNoteNo]].status != TEAR_STATUS, "note is forzen or tear");

        noIdMap[newNoteNo] = noIdMap[oldNoteNo];
        noIdMap[oldNoteNo] = 0;
        Note memory note = notes[noIdMap[newNoteNo]];
        note.noteNo = newNoteNo;
        notes[noIdMap[newNoteNo]] = note;
        isUpdate = true;
    }

    function updateNoteProperties(uint256 noteNo, bytes[] keys, bytes[] values, address account, bytes32[4] sign) onlyAccountNormal(account) public returns (bytes[] noteKeys, bytes[] noteValues){
        checkUpdateNoteNoAuth(noteNo, keys, values, account, sign);

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

    function getNoteProperties(uint256 noteNo, address account, bytes32[4] sign) public constant returns (bytes[] noteKeys, bytes[] noteValues){
        require(noIdMap[noteNo] > 0, "note doesn't exist!");

        bytes memory args;
        address txOrigin;
        bool check;
        address innerAccount = IOrganization(org).getInnerAccount(account);
        (txOrigin, check) = authCenter.check2WithSign(org, this, "getNoteProperties", args, sign);
        require(check && checkIssuer(noteNo, innerAccount), "Forbidden getNoteProperties");
        require(authCenter.checkAccount(txOrigin), "Auth:only account status is normal.");

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

    function updateNoteBatch(uint256 batchNo, uint date, bool isEffectiveDate, address account, bytes32[4] sign) onlyAccountNormal(account) public returns (bool isUpdate){
        require(date > getDate(), "require date > now");

        bytes memory args;
        args = args.bytesAppend(batchNo).bytesAppend(date).bytesAppend(account);
        checkUpdateBatchAuth(batchNo, args, account, sign);

        NoteBatch memory noteBatch = noteBatchs[batchNo];
        if (isEffectiveDate) {
            require(date < noteBatch.expirationDate, "require effectiveDate< expirationDate");
            noteBatch.effectiveDate = date;
        } else {
            if (noteBatch.effectiveDate > 0) {
                require(date > noteBatch.effectiveDate, "require effectiveDate< expirationDate");
            }
            noteBatch.expirationDate = date;
        }
        noteBatchs[batchNo] = noteBatch;
        isUpdate = true;
    }

    function checkUpdateBatchAuth(uint256 batchNo, bytes args, address account, bytes32[4] sign) internal returns (bool){
        address txOrigin;
        bool check;
        address innerAccount = IOrganization(org).getInnerAccount(account);
        (txOrigin, check) = authCenter.check2WithSign(org, this, "updateNoteBatch", args, sign);
        require(check && checkIssuerByBatchNo(batchNo, innerAccount), "Forbidden updateNoteBatch");
        require(authCenter.checkAccount(txOrigin), "Auth:only account status is normal.");
        return true;
    }

    function freezeNote(uint256 noteNo, address account, bytes32[4] sign) onlyAccountNormal(account) public returns (bool isForzen){
        require(noIdMap[noteNo] > 0, "note doesn't exist!");

        bytes memory args;
        args = args.bytesAppend(noteNo).bytesAppend(account);
        address txOrigin;
        bool check;
        address innerAccount = IOrganization(org).getInnerAccount(account);
        (txOrigin, check) = authCenter.check2WithSign(org, this, "freezeNote", args, sign);
        require(check && checkIssuer(noteNo, innerAccount), "Forbidden forzenNote");
        require(authCenter.checkAccount(txOrigin), "Auth:only account status is normal.");

        require(noIdMap[noteNo] > 0, "note doesn't exist");
        require(notes[noIdMap[noteNo]].status == EFFECTIVE_STATUS, "note is not effective");

        Note memory note = notes[noIdMap[noteNo]];
        NoteBatch noteBatch = noteBatchs[note.batchNo];
        note.status = FORZEN_STATUS;
        notes[noIdMap[noteNo]] = note;
        isForzen = true;
        emit LogNoteStatus(noteNo, note.id, FORZEN_STATUS);
    }

    function unfreezeNote(uint256 noteNo, address account, bytes32[4] sign) onlyAccountNormal(account) public returns (uint8){
        require(noIdMap[noteNo] > 0, "note doesn't exist!");

        bytes memory args;
        args = args.bytesAppend(noteNo).bytesAppend(account);
        address txOrigin;
        bool check;
        address innerAccount = IOrganization(org).getInnerAccount(account);
        (txOrigin, check) = authCenter.check2WithSign(org, this, "unfreezeNote", args, sign);
        require(check && checkIssuer(noteNo, innerAccount), "Forbidden unfreezeNote");
        require(authCenter.checkAccount(txOrigin), "Auth:only account status is normal.");
        require(notes[noIdMap[noteNo]].status == FORZEN_STATUS, "note is not forzen");

        Note memory note = notes[noIdMap[noteNo]];
        NoteBatch noteBatch = noteBatchs[note.batchNo];
        require(!checkExpire(note.id), "noteNo ".strConcat(noteNo.uint2str()).strConcat(" has been expired"));
        note.status = EFFECTIVE_STATUS;
        notes[noIdMap[noteNo]] = note;
        emit LogNoteStatus(noteNo, note.id, note.status);
        return note.status;
    }


    function queryBook(uint[] uintCondition, address[] addressCondition, int[] limit, bytes32[4] sign) public constant returns (string[] memory){
        bytes memory args;
        address txOrigin;
        bool check;
        (txOrigin, check) = authCenter.check2WithSign(org, this, "queryBook", args, sign);
        require(check, "Forbidden queryBook");
        require(authCenter.checkAccount(txOrigin), "Auth:only account status is normal.");

        require(limit.length == 2 && limit[0] < limit[1], "limit not verify,limit size should equals 2 and limit[0]<limit[1]");

        bool isOwner;
        if (addressCondition.length > 0) {
            for (uint256 i = 0; i < addressCondition.length; i++) {
                if (txOrigin == addressCondition[i]) {
                    isOwner = true;
                    break;
                }
            }

        }
        if (!isOwner) {
            isOwner = IOrganization(org).isExternalAccountAdmin(txOrigin);
        }
        require(isOwner, "Forbidden nonFungibleQueryBook because you aren't owner");

        if (accountList.length == 0) {
            string[] memory result;
            return result;
        }

        if (addressCondition.length > 0 && addressCondition[0] != address(0)) {
            address inner = IOrganization(org).getInnerAccount(addressCondition[0]);
            require(inner != address(0), "from account not exist");
            addressCondition[0] = inner;
        }

        if (addressCondition.length > 1 && addressCondition[1] != address(0)) {
            address innnerTo = IOrganization(org).getInnerAccount(addressCondition[1]);
            require(innnerTo != address(0), "to account not exist");
            addressCondition[1] = innnerTo;
        }
        if (uintCondition.length > 2 && uintCondition[2] != 0) {
            require(noIdMap[uintCondition[2]] > 0, "note doesn't exist!");
            uintCondition[2] = noIdMap[uintCondition[2]];
        }

        return book.query(uintCondition, addressCondition, limit);
    }


    function getTotalNoteSize() public constant returns (uint256){
        return totalNoteSize;
    }

    function getNoteNoByNoteId(uint256 noteId) public constant returns (uint256){
        require(notes[noteId].noteNo > 0, "note doesn't exist");
        return notes[noteId].noteNo;
    }


    //queryAccountList
    function getHolders(bytes32[4] sign) public constant returns (address[])
    {
        bytes memory args;
        address txOrigin;
        bool check;
        (txOrigin, check) = authCenter.check2WithSign(org, this, "getHolders", args, sign);
        require(check, "Forbidden getHolders");
        require(authCenter.checkAccount(txOrigin), "Auth:only account status is normal.");


        address[] memory externalAccountList = new address[](accountList.length);
        for (uint i = 0; i < accountList.length; i++) {
            externalAccountList[i] = IOrganization(org).getExternalAccount(accountList[i]);
        }
        return externalAccountList;
    }


    function tearNote(uint256 noteNo, address account, bytes32[4] sign) onlyAccountNormal(account) public returns (bool){
        require(noIdMap[noteNo] > 0, "note doesn't exist!");

        bytes memory args;
        args = args.bytesAppend(noteNo).bytesAppend(account);
        address txOrigin;
        bool check;
        (txOrigin, check) = authCenter.check2WithSign(org, this, "tearNote", args, sign);
        address innerAccount = IOrganization(org).getInnerAccount(account);
        require((check), "Forbidden tearNote");

        Note note = notes[noIdMap[noteNo]];
        note.status = TEAR_STATUS;
        notes[note.id] = note;

        accountNotes[innerAccount][note.id] = false;
        accountNoteList[innerAccount] = removeNoteFromAccount(innerAccount, note.id);
        tearNotes.push(noteNo);
        totalNoteSize -= 1;
        emit LogNoteStatus(noteNo, note.id, TEAR_STATUS);

        return true;
    }

    function getTearNotes(address account, bytes32[4] sign) public view returns (uint256[]){
        address innerAccount = IOrganization(org).getInnerAccount(account);
        bytes memory args;
        address txOrigin;
        bool check;
        (txOrigin, check) = authCenter.check2WithSign(org, this, "getTearNotes", args, sign);
        require(check, "Forbidden getTearNotes");

        return tearNotes;
    }

    // increaseLedgerNumber
    function addBook(bytes32[4] sign) public returns (uint256){
        bytes memory args;
        address txOrigin;
        bool check;
        (txOrigin, check) = authCenter.check2WithSign(org, this, "addBook", args, sign);
        require(check, "Forbidden addBook");
        require(authCenter.checkAccount(txOrigin), "Auth:only account status is normal.");

        return book.addBook();
    }

    function enableBatch(uint256 batchNo, bytes32[4] sign) public returns (bool){
        bytes memory args;
        args = args.bytesAppend(batchNo);
        address txOrigin;
        bool check;
        (txOrigin, check) = authCenter.check2WithSign(org, this, "enableBatch", args, sign);
        require(check, "Forbidden enableBatch");
        require(authCenter.checkAccount(txOrigin), "Auth:only account status is normal.");

        NoteBatch batch = noteBatchs[batchNo];
        require(batch.endId > 0, "batch not verify");
        if (batch.effectiveDate > 0) {
            require(batch.effectiveDate < getDate(), "batch can not be effective,because effectiveDate>now");
        }
        bool isEffective = batch.effectiveDate == 0 || batch.effectiveDate < getDate();
        batch.effectiveDate=getDate();
        noteBatchs[batchNo]=batch;
        if (isEffective) {
            for (uint i = batch.startId; i <= batch.endId; i++) {
                activeNote(i);
            }
        }

        return isEffective;
    }

    function ownerOf(uint256 _noteId) public view returns (address) {
        address owner = notes[_noteId].owner;
        require(owner != address(0));
        return owner;
    }


    function getDate() internal returns (uint){
        uint time = now;
        return (time);
    }

    function checkGetAuth(uint256 noteNo, address account, string key, bytes32[4] sign){
        bytes memory args;
        address txOrigin;
        bool check;
        address innerAddress = IOrganization(org).getInnerAccount(account);

        (txOrigin, check) = authCenter.check2WithSign(org, innerAddress, "getNoteDetail", args, sign);

        require(check && (checkOwnerSingle(noteNo, innerAddress) || checkIssuer(noteNo, innerAddress)), "Forbbiden getNoteDetail");
    }

    function checkTransferAuth(address[] transactionAddress, uint[] noteNos, string[] stringValueList, bytes32[4] sign) public view returns (address[]){
        require(transactionAddress[2] != address(0) && transactionAddress[3] != address(0), "from/to address not verify!");
        bool isCheck;
        bytes memory args = genTransferArgs(transactionAddress, noteNos, stringValueList);
        (isCheck, transactionAddress) = checkAndHandleTransactionAddress(transactionAddress);
        require(isCheck, "operator or account is not normal");
        require(accountMap[transactionAddress[2]], "the account has not been open");
        require(accountMap[transactionAddress[3]], "the account has not been open");

        address txOrigin;
        bool check;
        (txOrigin, check) = authCenter.check2WithSign(org, transactionAddress[2], "transfer", args, sign);
        require(check, "Forbidden nonFungibleTransfer");
        return transactionAddress;
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

    function removeNoteFromAccount(address account, uint256 noteId) internal returns (uint256[]){
        require(accountNotes[account][noteId] = true, "The account is not owned this note!");
        uint256[] orignalNoteIds = accountNoteList[account];
        uint256[] memory newNoteIds = new uint256[](orignalNoteIds.length - 1);
        bool isIndex;
        for (uint i = 0; i < newNoteIds.length; i++) {
            if (!isIndex && orignalNoteIds[i] == noteId) {
                isIndex = true;
            }
            if (isIndex) {
                newNoteIds[i] = orignalNoteIds[i + 1];
            } else {
                newNoteIds[i] = orignalNoteIds[i];
            }

        }
        return newNoteIds;
    }

    function checkAuth(address[] transactionAddress, uint256[] uint256Args, string[] stringValueList, bytes32[4] sign) internal returns (address[]) {
        bool isCheck;
        bytes memory args = genIssueArgs(transactionAddress, uint256Args, stringValueList);
        (isCheck, transactionAddress) = checkAndHandleTransactionAddress(transactionAddress);
        require(isCheck, "operator or account is not normal");
        address issuer = transactionAddress[3];
        require(accountMap[issuer], "the account has not been open");
        address txOrigin;
        (txOrigin, isCheck) = authCenter.check2WithSign(org, this, "issueNonFungible", args, sign);
        require(isCheck, "Forbidden issueNonFungible");
        return transactionAddress;
    }
    //uint256[] amount,noteNoPrefix  uint[] noteNoSize,effectiveDate,expirationDate

    function checkArgs(address[] transactionAddress, uint256[] uint256Args, string[] stringValueList) internal {
        require(transactionAddress.length == 4 && uint256Args.length == 5 && stringValueList.length > 0, "params not verify");
        require(uint256Args[2] > 0 && uint256Args[2] < 10 && uint256Args[4] > getDate(), "require 10 > noteNoSize >0 and expirationDate >now");
        if (uint256Args[3] > 0) {
            require(uint256Args[3] > getDate(), "require effectiveDate>now");
            require(uint256Args[4] > uint256Args[3], "require expirationDate >  effectiveDate");
        }
        require(uint256Args[0] > 0 && uint256Args[0] < 501, "require 500>=amount>0");
        require(uint256Args[1] > 0, "require noteNoPrefix>0");

        require(bytes(stringValueList[0]).length > 0 && bytes(stringValueList[0]).length <= 255, "desc bytes are between 1 and 255 in length");
        require(!existNoteNo[uint256Args[1]][uint256Args[2]], "notes has been defined please change noteNoPrefix or noteNoSize");
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
                noteId = noteSeq.get().sub(1);
            }
            accountNotes[transactionAddress[3]][noteId] = true;
            accountNoteList[transactionAddress[3]].push(noteId);

            noteNos[i] = createNote(transactionAddress[3], batchNo, noteId, uint256Args[1], uint256Args[2], i);

        }

        return noteNos;
    }

    function writeNotes(uint256[] noteNos, address[] transactionAddress, string[] stringValueList) internal returns (uint[]){
        uint[] memory result = new uint[](noteNos.length * 2);
        bool isWriteSuccess;
        uint256 noteId;
        uint[2] memory bookResult;

        for (uint i = 0; i < noteNos.length; i++) {
            noteId = noIdMap[noteNos[i]];
            (isWriteSuccess, bookResult) = book.write(transactionAddress, noteId, stringValueList);

            result[2 * i] = bookResult[0];
            result[2 * i + 1] = bookResult[1];
            require(isWriteSuccess, "write book fail!");
        }
        return result;

    }

    function createBatch(uint effectiveDate, uint expirationDate, uint256 amount, address issuer) public returns (uint256, uint256){
        require(expirationDate > getDate(), "require expirationDate > now");
        if (effectiveDate > 0) {
            require(effectiveDate >= getDate(), "require effectiveDate >= now");
        }

        uint256 startId = noteSeq.get();
        uint256 batchNo = batchSeq.get().sub(1);
        NoteBatch memory noteBatch = NoteBatch(batchNo, issuer, startId, startId.add(amount - 1), effectiveDate, expirationDate);
        noteBatchs[batchNo] = noteBatch;
        return (startId, batchNo);
    }

    function createNote(address owner, uint256 batchNo, uint256 noteId, uint256 noteNoPrefix, uint noteNoSize, uint index) public returns (uint256 noteNo){
        noteNo = (noteNoPrefix * (10 ** noteNoSize)).add(index + 1);
        bytes[]  memory itemkeys = new bytes[](1);
        Note memory note = Note(noteId, owner, noteNo, batchNo, INIT_STATUS, itemkeys);
        notes[noteId] = note;
        noIdMap[noteNo] = noteId;
    }

    function checkOwner(uint256[] noteNos, address _owner) internal returns (bool){
        for (uint i = 0; i < noteNos.length; i++) {
            if (!checkOwnerSingle(noteNos[i], _owner)) {
                return false;
            }
        }
        return true;
    }

    function checkIssuer(uint256 noteNo, address account) internal returns (bool){
        uint256 noteId = noIdMap[noteNo];
        require(noteId > 0, "note doesn't exist!");
        address owner = notes[noteId].owner;
        NoteBatch noteBatch = noteBatchs[notes[noteId].batchNo];
        if (address(0) == account || noteBatch.issuer != account) {
            return false;
        }
        return true;
    }

    function checkIssuerByBatchNo(uint256 batchNo, address account) internal returns (bool){
        require(noteBatchs[batchNo].batchNo > 0, "batch doesn't exist!");
        NoteBatch noteBatch = noteBatchs[batchNo];
        if (address(0) == account || noteBatch.issuer != account) {
            return false;
        }
        return true;
    }

    function checkOwnerSingle(uint noteNo, address _owner) public returns (bool isOwner){
        uint256 noteId = noIdMap[noteNo];
        require(noteId > 0, "note doesn't exist!");
        address owner = notes[noteId].owner;
        NoteBatch noteBatch = noteBatchs[notes[noteId].batchNo];
        if (owner == _owner) {
            isOwner = true;
        }
    }


    function checkUpdateNoteNoAuth(uint256 noteNo, bytes[] keys, bytes[] values, address account, bytes32[4] sign) internal {
        require(noIdMap[noteNo] > 0, "note doesn't exist!");
        bytes memory args;
        args = args.bytesAppend(noteNo);
        for (uint n = 0; n < keys.length; n++) {
            args = args.bytesAppend(keys[n]);
            args = args.bytesAppend(values[n]);
        }
        args = args.bytesAppend(account);
        address txOrigin;
        bool check;
        (txOrigin, check) = authCenter.check2WithSign(org, this, "updateNoteProperties", args, sign);
        address innerAccount = IOrganization(org).getInnerAccount(account);
        require(check && checkIssuer(noteNo, innerAccount), "Forbidden updateNoteProperties");
        require(authCenter.checkAccount(txOrigin), "Auth:only account status is normal.");
        require(notes[noIdMap[noteNo]].status != FORZEN_STATUS && notes[noIdMap[noteNo]].status != TEAR_STATUS, "note is forzen or tear");
    }


    function checkAndHandleTransactionAddress(address[] transactionAddress) internal returns (bool, address[]){
        address[] memory innerAddress = new address[](transactionAddress.length);
        bool isCheck;
        address inner;
        for (uint i = 0; i < transactionAddress.length; i++) {
            if (i == 1 || address(0) == transactionAddress[i]) {
                innerAddress[i] = transactionAddress[i];
                continue;
            }

            (inner, isCheck) = authCenter.getInnerAccountAndStatus(transactionAddress[i]);
            if (!isCheck) {
                return (false, innerAddress);
            }
            innerAddress[i] = inner;
        }
        return (isCheck, innerAddress);
    }


    function checkExpire(uint256 noteId) internal constant returns (bool isExpire){
        Note memory note = notes[noteId];
        if (EXPIRATE_STATUS == note.status) {
            isExpire = true;
            return;
        }

        NoteBatch noteBatch = noteBatchs[note.batchNo];
        if (noteBatch.expirationDate < getDate()) {
            note.status = EXPIRATE_STATUS;
            notes[noteId] = note;
            isExpire = true;
            return;
        }

    }

    function checkEffectiveAndUpdate(uint256 noteId) internal constant returns (bool isEffective){
        Note memory note = notes[noteId];
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
            notes[noteId] = note;
            isEffective = true;
            return;
        }


    }

    function activeNote(uint256 noteId) internal constant returns (bool isEffective){
        Note note = notes[noteId];
        note.status = EFFECTIVE_STATUS;
        notes[noteId] = note;
        isEffective=true;
    }
}