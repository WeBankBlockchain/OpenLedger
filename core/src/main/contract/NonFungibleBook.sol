pragma solidity ^0.4.25;
pragma experimental ABIEncoderV2;

import "./lib/Table.sol";
import "./lib/LibTypeConversion.sol";
import "./Term.sol";
import "./interface/IOrganization.sol";
import "./interface/INonFungibleAsset.sol";
import "./lib/UtilLib.sol";

/** @title ledger contract */
contract NonFungibleBook {
    event InsertResult(address[] transactionAddress, uint256 noteId, string[] stringValueList, int256 count);

    using LibTypeConversion for uint256;
    using LibTypeConversion for uint;
    using LibTypeConversion for address;
    using LibTypeConversion for string;
    using UtilLib for *;
    // term of prject
    Term term;
    TableFactory tableFactory;
    // booknos of ledger
    uint[] bookNos;
    // numberOfTableParameters
    uint constant  ITEM_SIZE = 8;


    // Maintain the relationship between booknum and termno termno=>booknum
    mapping(uint => uint[]) termNoMap;
    // Maintain the relationship between booknum and seqNo  seq=>booknum
    mapping(uint => uint) seqMap;
    // Maintain the relationship between booknum and from  from=>booknum
    mapping(address => uint[]) fromMap;
    // Maintain the relationship between booknum and to   to=>booknum
    mapping(address => uint[]) toMap;
    // Maintain the relationship between booknum and note  booknum=>noteIds
    mapping(uint256 => uint[]) noteIdMap;
    // from=>(bookNum=>true)
    mapping(uint => mapping(uint => bool)) termNoExistBookNos;
    // from=>(bookNum=>true)
    mapping(address => mapping(uint => bool)) fromMapExistBookNos;
    // to=>(bookNum=>true)
    mapping(address => mapping(uint => bool)) toMapExistBookNos;
    // noteId=>(bookNum=>true)
    mapping(uint256 => mapping(uint => bool)) noteIdMapExistBookNos;

    string tableName;
    address orgAddress;
    address asset;


    constructor(address termAddress, string assetName, address org) public {
        term = Term(termAddress);
        //从1开始
        bookNos.push(1);
        tableFactory = TableFactory(0x1001);

        tableName = assetName;
        tableFactory.createTable(tableName, "book_no", "term_no,seq,from,to,noteId,asset,operator,desc");
        orgAddress = org;
        asset = msg.sender;
    }

    // record
    // transactionAddress[operator(nonull),asset(nonull),from,to,related_asset]
    // typeList[transaction_type,operation_type]
    // stringValueList [desc(nonull),subject]
    function write(address[] transactionAddress, uint256 noteId, string[] memory stringValueList) public returns (bool, uint[2]){
        require(transactionAddress.length >= 4, "Params is not compliance");
        uint termNo;
        string  memory termName;
        (termNo, termName) = term.getTerm();
        uint seqNo = term.getSeqNo();
        // 账本记录
        Entry recordEntry;


        recordEntry = setRecordEntry(termNo, seqNo, transactionAddress, noteId, stringValueList);
        Table table = tableFactory.openTable(tableName);
        int256 count = table.insert(bookNos[bookNos.length - 1].uint2str(), recordEntry);
        emit InsertResult(transactionAddress, noteId, stringValueList, count);

        if (!termNoExistBookNos[termNo][bookNos[bookNos.length - 1]]) {
            termNoMap[termNo].push(bookNos[bookNos.length - 1]);
            termNoExistBookNos[termNo][bookNos[bookNos.length - 1]] = true;
        }
        seqMap[seqNo] = bookNos[bookNos.length - 1];
        if (!fromMapExistBookNos[transactionAddress[2]][bookNos[bookNos.length - 1]] && address(0) != transactionAddress[2]) {
            fromMap[transactionAddress[2]].push(bookNos[bookNos.length - 1]);
            fromMapExistBookNos[transactionAddress[2]][bookNos[bookNos.length - 1]] = true;
        }
        if (!toMapExistBookNos[transactionAddress[3]][bookNos[bookNos.length - 1]] && address(0) != transactionAddress[3]) {
            toMap[transactionAddress[3]].push(bookNos[bookNos.length - 1]);
            toMapExistBookNos[transactionAddress[3]][bookNos[bookNos.length - 1]] = true;
        }
        if (!noteIdMapExistBookNos[noteId][bookNos[bookNos.length - 1]]) {
            noteIdMap[noteId].push(bookNos[bookNos.length - 1]);
            noteIdMapExistBookNos[noteId][bookNos[bookNos.length - 1]] = true;
        }
        uint[2] memory result = [termNo, seqNo];

        return (count == 1, result);
    }


    // query: uintCondition:[term_no,seq] addressConditon:[from,to]
    function query(uint[] uintCondition, address[] addressConditon, int[] limit) public constant returns (string[] memory){
        require(uintCondition.length > 0 || addressConditon.length > 0, "Params is not compliance");

        Table table = tableFactory.openTable(tableName);
        bool[] memory isExistBookNos;
        uint existNum;

        (isExistBookNos, existNum) = handleCondiction(uintCondition, addressConditon);
        string[] memory itemList;

        Condition condition = table.newCondition();

        // //统计符合结果的记录数
        int256 totalRecordSize = 0;

        if (uintCondition.length > 0 && uintCondition[0] != 0) {
            condition.EQ("term_no", uintCondition[0].uint2str());
        }
        if (uintCondition.length > 1 && uintCondition[1] != 0) {
            condition.EQ("seq", uintCondition[1].uint2str());
        }
        if (uintCondition.length > 2 && uintCondition[2] != 0) {
            condition.EQ("noteId", uintCondition[2].uint2str());
        }

        if (addressConditon.length > 0 && addressConditon[0] != address(0)) {
            condition.EQ("from", addressConditon[0].addressToString());
        }

        if (addressConditon.length > 1 && addressConditon[1] != address(0)) {
            condition.EQ("to", addressConditon[1].addressToString());
        }

        condition.limit(limit[0], limit[1]);

        Entries[] memory resultEntries;
        (resultEntries, totalRecordSize) = sumRecords(isExistBookNos, existNum, condition);

        if (resultEntries.length > 0 && totalRecordSize > 0) {
            itemList = handleEntries(resultEntries, totalRecordSize);
        }
        return itemList;

    }

    function setRecordEntry(uint termNo, uint seqNo, address[] transactionAddress, uint256 noteId, string[] stringValueList) internal returns (Entry){
        Table table = tableFactory.openTable(tableName);

        Entry recordEntry = table.newEntry();
        recordEntry.set("book_no", bookNos[bookNos.length - 1].uint2str());
        recordEntry.set("seq", seqNo.uint2str());
        recordEntry.set("term_no", termNo.uint2str());
        recordEntry.set("from", transactionAddress[2].addressToString());
        recordEntry.set("to", transactionAddress[3].addressToString());
        recordEntry.set("asset", transactionAddress[1]);
        recordEntry.set("noteId", noteId.uint2str());
        recordEntry.set("operator", transactionAddress[0]);
        recordEntry.set("desc", stringValueList[0]);

        return (recordEntry);
    }

    function sumRecords(bool[] isExistBookNos, uint existNum, Condition condition) internal constant returns (Entries[], int256){
        Table table = tableFactory.openTable(tableName);
        int256 totalRecordSize = 0;

        Entries[] memory resultEntries = new Entries[](existNum);
        uint arrayIndex = 0;
        for (uint bindex = 0; bindex < isExistBookNos.length; bindex++) {
            if (isExistBookNos[bindex]) {
                Entries entries = table.select((bindex + 1).uint2str(), condition);
                totalRecordSize += entries.size();
                resultEntries[arrayIndex++] = entries;
            }
        }
        return (resultEntries, totalRecordSize);
    }


    // processingReturnedData
    function handleEntries(Entries[] resultEntries, int256 totalRecordSize) internal constant returns (string[]memory){
        string[] memory itemStringList = new string[](uint256(totalRecordSize) * ITEM_SIZE);

        for (uint ridx = 0; ridx < resultEntries.length; ridx++) {

            Entries rEntries = resultEntries[ridx];
            uint256 j = 0;

            for (int didx = 0; didx < rEntries.size(); didx++) {
                Entry recordEntry = rEntries.get(didx);
                j = 0;

                string[] memory accountAddress = genExtenalAddress(recordEntry.getString("from"), recordEntry.getString("to"), recordEntry.getAddress("operator"));
                require(accountAddress.length == 3, "account not verify");
                itemStringList[ITEM_SIZE * uint256(didx) + j] = recordEntry.getString("term_no");
                itemStringList[ITEM_SIZE * uint256(didx) + (++j)] = recordEntry.getString("seq");
                itemStringList[ITEM_SIZE * uint256(didx) + (++j)] = accountAddress[0];
                itemStringList[ITEM_SIZE * uint256(didx) + (++j)] = accountAddress[1];
                itemStringList[ITEM_SIZE * uint256(didx) + (++j)] = INonFungibleAsset(asset).getNoteNoByNoteId(recordEntry.getString("noteId").str2uint()).uint2str();
                itemStringList[ITEM_SIZE * uint256(didx) + (++j)] = recordEntry.getString("desc");

                itemStringList[ITEM_SIZE * uint256(didx) + (++j)] = recordEntry.getAddress("asset").addressToString();
                itemStringList[ITEM_SIZE * uint256(didx) + (++j)] = accountAddress[2];


            }
        }
        return (itemStringList);
    }

    // concatenationQueryCondition
    function handleCondiction(uint[] uintCondition, address[] addressConditon) public constant returns (bool[], uint existNum) {
        bool[] memory isExist = new bool[](bookNos.length);

        if (uintCondition.length > 1 && uintCondition[1] != 0) {
            if (seqMap[uintCondition[1]] != 0) {
                isExist[seqMap[uintCondition[1]] - 1] = true;
            }
        }
        if (uintCondition.length > 0 && uintCondition[0] != 0) {
            for (uint t = 0; t < termNoMap[uintCondition[0]].length; t++) {
                if (!isExist[termNoMap[uintCondition[0]][t] - 1]) {
                    isExist[termNoMap[uintCondition[0]][t] - 1] = true;
                }
            }
        }
        if (uintCondition.length > 2 && uintCondition[2] != 0) {
            for (uint n = 0; n < noteIdMap[uintCondition[2]].length; n++) {
                if (!isExist[noteIdMap[uintCondition[2]][n] - 1]) {
                    isExist[noteIdMap[uintCondition[2]][n] - 1] = true;
                }
            }
        }
        if (addressConditon.length > 0 && addressConditon[0] != address(0)) {
            for (uint f = 0; f < fromMap[addressConditon[0]].length; f++) {
                if (!isExist[fromMap[addressConditon[0]][f] - 1]) {
                    isExist[fromMap[addressConditon[0]][f] - 1] = true;
                }
            }
        }
        if (addressConditon.length > 1 && addressConditon[1] != address(0)) {
            for (uint tm = 0; tm < toMap[addressConditon[1]].length; tm++) {
                if (!isExist[toMap[addressConditon[1]][tm] - 1]) {
                    isExist[toMap[addressConditon[1]][tm] - 1] = true;
                }
            }
        }

        uint booknums = 0;
        for (uint i = 0; i < isExist.length; i++) {
            if (isExist[i]) {
                booknums++;
            }
        }
        return (isExist, booknums);

    }


    // add book no
    function addBook() public returns (uint256){
        bookNos.push(bookNos.length + 1);
        return bookNos[bookNos.length - 1];
    }

    function getAddress() public returns (address) {
        return address(this);
    }

    function genExtenalAddress(string _from, string _to, address operator) internal view returns (string[]){
        string[] memory innerAddress = new string[](3);
        bool isCheck;
        address externalAddress;
        if (bytes(_from).length > 0 && address(0) != stringToAddress(_from)) {
            externalAddress = IOrganization(orgAddress).getExternalAccount(stringToAddress(_from));
            require(address(0) != externalAddress, "from account not found");
            innerAddress[0] = externalAddress.addressToString();
        } else {
            innerAddress[0] = _from;
        }
        if (bytes(_to).length > 0 && address(0) != stringToAddress(_to)) {
            externalAddress = IOrganization(orgAddress).getExternalAccount(stringToAddress(_to));
            require(address(0) != externalAddress, "to account not found");
            innerAddress[1] = externalAddress.addressToString();
        } else {
            innerAddress[1] = _to;
        }
        externalAddress = IOrganization(orgAddress).getExternalAccount(operator);
        require(address(0) != externalAddress, "operator account not found");
        innerAddress[2] = externalAddress.addressToString();
        return innerAddress;
    }

    function stringToAddress(string memory s) internal returns (address) {
        bytes memory b = bytes(s);
        uint result = 0;
        for (uint i = 0; i < b.length; i++) {
            if (uint8(b[i]) >= 48 && uint8(b[i]) <= 57) {
                result = result * 10 + (uint8(b[i]) - 48);
            }
        }
        return address(result);
    }

}


