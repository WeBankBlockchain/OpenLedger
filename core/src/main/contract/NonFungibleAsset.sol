pragma solidity ^0.4.25;
pragma experimental ABIEncoderV2;

import "./lib/LibSafeMath.sol";
import "./lib/UtilLib.sol";
import "./lib/LibTypeConversion.sol";
import "./NonFungibleBook.sol";
import "./Identity.sol";
import "./interface/IOrganization.sol";
import "./interface/IAccount.sol";
import "./interface/INonFungibleStorage.sol";

// nonHomogeneousAssets
contract NonFungibleAsset is Identity {
    using LibSafeMath for uint256;
    using UtilLib for *;
    using LibTypeConversion for uint;

    event LogNoteStatus(uint256 noteNo, uint256 noteID, uint8 status);
    modifier onlyAccountNormal(address account) {
        require(authCenter.checkAccount(account), "Auth:only account status is normal.");
        _;
    }
    //    uint256 public price;
    uint8 constant INIT_STATUS = 1;
    uint8 constant EFFECTIVE_STATUS = 2;
    uint8 constant EXPIRATE_STATUS = 3;
    uint8 constant FORZEN_STATUS = 4;
    uint8 constant TEAR_STATUS = 5;
    NonFungibleBook book;
    INonFungibleStorage  assetStorage;

    constructor(string assetName, address authCenterAddr, address orgAddress, bool isCreate, address _storage) public Identity(authCenterAddr, orgAddress)
    {
        require(bytes(assetName).length > 0 && bytes(assetName).length < 64, "assetName should be not null and less than 64 ");
        assetStorage = INonFungibleStorage(_storage);
        if (isCreate) {
            book = new NonFungibleBook(IOrganization(orgAddress).getProjectTerm(), assetName, orgAddress);
            assetStorage.init(address(book));
        } else {
            book = NonFungibleBook(assetStorage.getBook(assetName));
        }
    }

    function setPrice(uint256 _price, bytes32[4] sign) public returns (uint256){
        bytes memory args;
        args = args.bytesAppend(_price);
        address txOrigin;
        bool check;
        (txOrigin, check) = authCenter.check2WithSign(org, this, "setNonFungiblePrice", args, sign);
        require(check, "setNonFungiblePrice Forbidden");
        assetStorage.setPrice(_price);
        return assetStorage.getPrice();
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
        require(!assetStorage.existAccount(innerAddress), "the account has been open");

        IAccount userAccount = IAccount(IOrganization(org).getAccount(account));
        userAccount.addAsset(this, org, false);

        assetStorage.addAccount(innerAddress);
        return true;
    }


    //uint256[] amount,noteNoPrefix, noteNoSize,effectiveDate,expirationDate
    function issue(address[] transactionAddress, uint256[] uint256Args, string[] stringValueList, bytes32[4] sign) public returns (uint256, uint256[], uint[]){
        checkArgs(transactionAddress, uint256Args, stringValueList);
        transactionAddress = checkAuth(transactionAddress, uint256Args, stringValueList, sign);

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


    function getNoteDetail(uint256 noteNo, address account, bytes32[4] sign) public constant returns (address[], uint256[], uint[], uint8){
        checkGetAuth(noteNo, account, "getNoteDetail", sign);
        address[] memory addressList = new address[](2);
        uint256[] memory uint256List = new  uint256[](2);
        uint[] memory uintList = new uint[](2);
        uint8 noteStatus;
        (addressList, uint256List, uintList, noteStatus) = assetStorage.getNote(noteNo);
        addressList[0] = IOrganization(org).getExternalAccount(addressList[0]);
        addressList[1] = IOrganization(org).getExternalAccount(addressList[1]);

        return (addressList, uint256List, uintList, noteStatus);
    }


    function transfer(address[] transactionAddress, uint[] noteNos, string[] stringValueList, bytes32[4] sign) public returns (bool, uint[]){
        transactionAddress = checkTransferAuth(transactionAddress, noteNos, stringValueList, sign);
        uint[] memory result = new uint[](3 * noteNos.length);
        bool isWriteSuccess;
        for (uint i = 0; i < noteNos.length; i++) {
            require(assetStorage.isExistNote(noteNos[i]), "noteNo ".strConcat(noteNos[i].uint2str()).strConcat(" doesn't exist!"));
            require(!assetStorage.checkExpire(noteNos[i]), "noteNo ".strConcat(noteNos[i].uint2str()).strConcat(" has been expired"));
            require(assetStorage.checkEffectiveAndUpdate(noteNos[i]), "note is not effective");
            require(assetStorage.checkOwner(noteNos[i], transactionAddress[2]), "Forbidden transfer because from isn't owner");
        }
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


    function accountHoldNote(address account, uint256 noteNo, bytes32[4] sign) onlyAccountNormal(account) public constant returns (bool isContain){
        bytes memory args;
        address txOrigin;
        bool check;
        (txOrigin, check) = authCenter.check2WithSign(org, this, "accountContainsNote", args, sign);
        require(check, "Forbidden accountContainsNote");
        require(authCenter.checkAccount(txOrigin), "Auth:only account status is normal.");
        address innerAddress = IOrganization(org).getInnerAccount(account);

        isContain = assetStorage.accountHoldNote(innerAddress, noteNo);
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

        return assetStorage.getNoteByAccount(innerAddress, start, end);
    }

    function getAccountNoteSize(address account, bytes32[4] sign) onlyAccountNormal(account) public constant returns (uint256){
        address innerAddress = IOrganization(org).getInnerAccount(account);
        bytes memory args;
        address txOrigin;
        bool check;
        (txOrigin, check) = authCenter.check2WithSign(org, innerAddress, "getAccountNotes", args, sign);
        require(check, "Forbidden getAccountNotes");
        require(authCenter.checkAccount(txOrigin), "Auth:only account status is normal.");

        return assetStorage.getAccountNoteSize(innerAddress);
    }


    function updateNoteNo(uint256 oldNoteNo, uint256 newNoteNo, address account, bytes32[4] sign) onlyAccountNormal(account) public returns (bool isUpdate){
        require(assetStorage.isExistNote(oldNoteNo), "note doesn't exist!");
        require(!assetStorage.isExistNote(newNoteNo), "newNoteNo has been issue!");
        bytes memory args;
        args = args.bytesAppend(oldNoteNo).bytesAppend(newNoteNo).bytesAppend(account);
        address txOrigin;
        bool check;
        address innerAccount = IOrganization(org).getInnerAccount(account);
        (txOrigin, check) = authCenter.check2WithSign(org, this, "updateNoteNo", args, sign);
        require(check && assetStorage.checkIssuer(oldNoteNo, innerAccount), "Forbidden updateNoteNo");
        require(authCenter.checkAccount(txOrigin), "Auth:only account status is normal.");
        require(assetStorage.getStatusByNote(oldNoteNo) != FORZEN_STATUS && assetStorage.getStatusByNote(oldNoteNo) != TEAR_STATUS, "note is forzen or tear");

        isUpdate = assetStorage.updateNoteNo(oldNoteNo, newNoteNo);
    }

    function updateNoteProperties(uint256 noteNo, bytes[] keys, bytes[] values, address account, bytes32[4] sign) onlyAccountNormal(account) public returns (bytes[] noteKeys, bytes[] noteValues){
        checkUpdateNoteNoAuth(noteNo, keys, values, account, sign);
        return assetStorage.updateNoteProperties(noteNo, keys, values);
    }

    function getNoteProperties(uint256 noteNo, address account, bytes32[4] sign) public constant returns (bytes[] noteKeys, bytes[] noteValues){
        require(assetStorage.isExistNote(noteNo), "note doesn't exist!");

        bytes memory args;
        address txOrigin;
        bool check;
        address innerAccount = IOrganization(org).getInnerAccount(account);
        (txOrigin, check) = authCenter.check2WithSign(org, this, "getNoteProperties", args, sign);
        require(check && assetStorage.checkIssuer(noteNo, innerAccount), "Forbidden getNoteProperties");
        require(authCenter.checkAccount(txOrigin), "Auth:only account status is normal.");

        return assetStorage.getNoteProperties(noteNo);
    }

    function updateNoteBatch(uint256 batchNo, uint date, bool isEffectiveDate, address account, bytes32[4] sign) onlyAccountNormal(account) public returns (bool isUpdate){
        require(date > getDate(), "require date > now");

        bytes memory args;
        args = args.bytesAppend(batchNo).bytesAppend(date).bytesAppend(account);
        checkUpdateBatchAuth(batchNo, args, account, sign);

        isUpdate = assetStorage.updateNoteBatch(batchNo, date, isEffectiveDate);
    }

    function checkUpdateBatchAuth(uint256 batchNo, bytes args, address account, bytes32[4] sign) internal returns (bool){
        address txOrigin;
        bool check;
        address innerAccount = IOrganization(org).getInnerAccount(account);
        (txOrigin, check) = authCenter.check2WithSign(org, this, "updateNoteBatch", args, sign);
        require(check && assetStorage.checkIssuerByBatchNo(batchNo, innerAccount), "Forbidden updateNoteBatch");
        require(authCenter.checkAccount(txOrigin), "Auth:only account status is normal.");
        return true;
    }

    function freezeNote(uint256 noteNo, address account, bytes32[4] sign) onlyAccountNormal(account) public returns (bool isForzen){
        require(assetStorage.isExistNote(noteNo), "note doesn't exist!");

        bytes memory args;
        args = args.bytesAppend(noteNo).bytesAppend(account);
        address txOrigin;
        bool check;
        address innerAccount = IOrganization(org).getInnerAccount(account);
        (txOrigin, check) = authCenter.check2WithSign(org, this, "freezeNote", args, sign);
        require(check && assetStorage.checkIssuer(noteNo, innerAccount), "Forbidden forzenNote");
        require(authCenter.checkAccount(txOrigin), "Auth:only account status is normal.");

        require(assetStorage.isExistNote(noteNo), "note doesn't exist");
        require(assetStorage.checkEffect(noteNo), "note is not effective");

        isForzen=true;
        uint256 noteId = assetStorage.updateNoteStatus(noteNo,FORZEN_STATUS);
        emit LogNoteStatus(noteNo, noteId, FORZEN_STATUS);
    }

    function unfreezeNote(uint256 noteNo, address account, bytes32[4] sign) onlyAccountNormal(account) public returns (uint8){
        require(assetStorage.isExistNote(noteNo), "note doesn't exist!");

        bytes memory args;
        args = args.bytesAppend(noteNo).bytesAppend(account);
        address txOrigin;
        bool check;
        address innerAccount = IOrganization(org).getInnerAccount(account);
        (txOrigin, check) = authCenter.check2WithSign(org, this, "unfreezeNote", args, sign);
        require(check && assetStorage.checkIssuer(noteNo, innerAccount), "Forbidden unfreezeNote");
        require(authCenter.checkAccount(txOrigin), "Auth:only account status is normal.");
        require(assetStorage.getStatusByNote(noteNo) == FORZEN_STATUS, "note is not forzen");

        uint256 noteId = assetStorage.updateNoteStatus(noteNo,EFFECTIVE_STATUS);
        emit LogNoteStatus(noteNo, noteId, EFFECTIVE_STATUS);
        return EFFECTIVE_STATUS;
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

        if (assetStorage.getTotalNoteSize() == 0) {
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
            require(assetStorage.isExistNote(uintCondition[2]), "note doesn't exist!");
            uintCondition[2] = assetStorage.getNoteIdByNo(uintCondition[2]);
        }

        return book.query(uintCondition, addressCondition, limit);
    }


    function getTotalNoteSize() public constant returns (uint256){
        return assetStorage.getTotalNoteSize();
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

        address[]  memory accountList = assetStorage.getAccounts();
        address[] memory externalAccountList = new address[](accountList.length);
        for (uint i = 0; i < accountList.length; i++) {
            externalAccountList[i] = IOrganization(org).getExternalAccount(accountList[i]);
        }
        return externalAccountList;
    }


    function tearNote(uint256 noteNo, address account, bytes32[4] sign) onlyAccountNormal(account) public returns (bool){
        require(assetStorage.isExistNote(noteNo), "note doesn't exist!");

        bytes memory args;
        args = args.bytesAppend(noteNo).bytesAppend(account);
        address txOrigin;
        bool check;
        (txOrigin, check) = authCenter.check2WithSign(org, this, "tearNote", args, sign);
        address innerAccount = IOrganization(org).getInnerAccount(account);
        require((check), "Forbidden tearNote");

        assetStorage.updateNoteStatus(noteNo, TEAR_STATUS);
        uint256 noteId = assetStorage.removeNoteFromAccount(noteNo, innerAccount);
        assetStorage.addTearNote(noteNo);
        assetStorage.mulTotalNoteSize(1);
        emit LogNoteStatus(noteNo, noteId, TEAR_STATUS);
        return true;
    }

    function getTearNotes(address account, bytes32[4] sign) public view returns (uint256[]){
        address innerAccount = IOrganization(org).getInnerAccount(account);
        bytes memory args;
        address txOrigin;
        bool check;
        (txOrigin, check) = authCenter.check2WithSign(org, this, "getTearNotes", args, sign);
        require(check, "Forbidden getTearNotes");

        return assetStorage.getTearNotes();
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

    function enableBatch(uint256 batchNo, bytes32[4] sign) public returns (bool isEffective){
        bytes memory args;
        args = args.bytesAppend(batchNo);
        address txOrigin;
        bool check;
        (txOrigin, check) = authCenter.check2WithSign(org, this, "enableBatch", args, sign);
        require(check, "Forbidden enableBatch");
        require(authCenter.checkAccount(txOrigin), "Auth:only account status is normal.");
        isEffective = assetStorage.enableBatch(batchNo);
        return isEffective;
    }


    function checkGetAuth(uint256 noteNo, address account, string key, bytes32[4] sign){
        bytes memory args;
        address txOrigin;
        bool check;
        address innerAddress = IOrganization(org).getInnerAccount(account);

        (txOrigin, check) = authCenter.check2WithSign(org, innerAddress, "getNoteDetail", args, sign);

        require(check && assetStorage.checkIssuer(noteNo, innerAddress), "Forbbiden getNoteDetail");
    }

    function checkTransferAuth(address[] transactionAddress, uint[] noteNos, string[] stringValueList, bytes32[4] sign) public view returns (address[]){
        require(transactionAddress[2] != address(0) && transactionAddress[3] != address(0), "from/to address not verify!");
        bool isCheck;
        bytes memory args = genTransferArgs(transactionAddress, noteNos, stringValueList);
        (isCheck, transactionAddress) = checkAndHandleTransactionAddress(transactionAddress);
        require(isCheck, "operator or account is not normal");
        require(assetStorage.existAccount(transactionAddress[2]), "the account has not been open");
        require(assetStorage.existAccount(transactionAddress[3]), "the account has not been open");

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


    function checkAuth(address[] transactionAddress, uint256[] uint256Args, string[] stringValueList, bytes32[4] sign) internal returns (address[]) {
        bool isCheck;
        bytes memory args = genIssueArgs(transactionAddress, uint256Args, stringValueList);
        (isCheck, transactionAddress) = checkAndHandleTransactionAddress(transactionAddress);
        require(isCheck, "operator or account is not normal");
        address issuer = transactionAddress[3];
        require(assetStorage.existAccount(issuer), "the account has not been open");
        address txOrigin;
        (txOrigin, isCheck) = authCenter.check2WithSign(org, this, "issueNonFungible", args, sign);
        require(isCheck, "Forbidden issueNonFungible");
        return transactionAddress;
    }
    //uint256[] amount,noteNoPrefix noteNoSize,effectiveDate,expirationDate

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

    function writeNotes(uint256[] noteNos, address[] transactionAddress, string[] stringValueList) internal returns (uint[]){
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

    function createBatch(uint effectiveDate, uint expirationDate, uint256 amount, address issuer) public returns (uint256, uint256){
        require(expirationDate > getDate(), "require expirationDate > now");
        if (effectiveDate > 0) {
            require(effectiveDate >= getDate(), "require effectiveDate >= now");
        }

        return assetStorage.createBatch(effectiveDate, expirationDate, amount, issuer);
    }

    function createNote(address owner, uint256 batchNo, uint256 noteId, uint256 noteNoPrefix, uint noteNoSize, uint index) public returns (uint256 noteNo){
        noteNo = (noteNoPrefix * (10 ** noteNoSize)).add(index + 1);
        assetStorage.createNote(noteId, noteNo, owner, batchNo);
    }


    function checkUpdateNoteNoAuth(uint256 noteNo, bytes[] keys, bytes[] values, address account, bytes32[4] sign) internal {
        require(assetStorage.isExistNote(noteNo), "note doesn't exist!");
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
        require(check && assetStorage.checkIssuer(noteNo, innerAccount), "Forbidden updateNoteProperties");
        require(authCenter.checkAccount(txOrigin), "Auth:only account status is normal.");
        require(assetStorage.getStatusByNote(noteNo) != FORZEN_STATUS && assetStorage.getStatusByNote(noteNo) != TEAR_STATUS, "note is forzen or tear");
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


    function getDate() internal returns (uint){
        uint time = now;
        return (time);
    }

    function getNoteNoByNoteId(uint256 noteId) public returns (uint256){
        return assetStorage.getNoteNoByNoteId(noteId);
    }
}