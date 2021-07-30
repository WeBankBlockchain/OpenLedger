pragma solidity ^0.4.25;
pragma experimental ABIEncoderV2;


import "../../lib/Table.sol";
import "../../lib/LibTypeConversion.sol";
import "../../Term.sol";
import "../../lib/UtilLib.sol";

/** @title bookContract */
contract FungibleBook {
    event InsertResult(uint256 termNo, uint256 seqNo, address from, address to, uint256 amount);
    using LibTypeConversion for *;
    using UtilLib for *;

    // term of ledger
    Term term;
    TableFactory tableFactory;
    // bookNos of ledger
    uint[] bookNos;

    // Maintain the relationship between bookno and termno: termno=>booknum
    mapping(uint => uint[]) termNoMap;
    //  Maintain the relationship between bookno and seqno:  seq=>booknum
    mapping(uint => uint) seqMap;
    // Maintain the relationship between bookno and fromaddress:  from=>booknum
    mapping(address => uint[]) fromMap;
    // Maintain the relationship between bookno and toaddress:   to=>booknum
    mapping(address => uint[]) toMap;
    // from=>(bookNum=>true)
    mapping(uint => mapping(uint => bool)) termNoExistBookNos;
    // from=>(bookNum=>true)
    mapping(address => mapping(uint => bool)) fromMapExistBookNos;
    // to=>(bookNum=>true)
    mapping(address => mapping(uint => bool)) toMapExistBookNos;

    string tableName;
    address orgAddress;

    constructor(address termAddress, string assetName, address orgAddr) public {
        term = Term(termAddress);
        //从1开始
        bookNos.push(1);
        tableFactory = TableFactory(0x1001);

        tableName = assetName;
        tableFactory.createTable(tableName, "book_no", "term_no,seq,from,to,amount,asset,operator,desc,transaction_type,operation_type,subject,related_asset");
        orgAddress = orgAddr;
    }


    // record
    // transactionAddress[operator(nonull),asset(nonull),from,to,related_asset]
    // typeList[transaction_type,operation_type]
    // stringValueList [desc(nonull),subject]
    function write(address[] transactionAddress, uint256 amount, bytes[] memory stringValueList, int[] typeList) public returns (bool, uint[2]){
        require(transactionAddress.length >= 4 && typeList.length == 2, "Params is not compliance");
        uint _termno;
        string  memory _termname;
        (_termno, _termname) = term.getTerm();
        uint seqNo = term.getSeqNo();
        // 账本记录
        Entry recordEntry;

        recordEntry = setRecordEntry(_termno, seqNo, transactionAddress, amount, stringValueList, typeList);
        Table table = tableFactory.openTable(tableName);
        require(table.insert(bookNos[bookNos.length - 1].uint2str(), recordEntry) == 1, "write book failed");

        setIndex(_termno, seqNo, transactionAddress);

        uint[2] memory result = [_termno, seqNo];
        //        result[0] = _termno;
        //        result[1] = seqNo;
        emit InsertResult(_termno, seqNo, transactionAddress[2], transactionAddress[3], amount);
        return (true, result);
    }

    // queryledger uintCondition:[term_no,seq] addressCondtion:[from,to]
    function query(uint[] uintCondition, address[] addressCondtion, int[] limit) public constant returns (string[] memory){
        require(uintCondition.length > 0 || addressCondtion.length > 0, "Params is not compliance");

        Table table = tableFactory.openTable(tableName);
        bool[] memory isExistBookNos;
        uint existNum;

        (isExistBookNos, existNum) = handleCondiction(uintCondition, addressCondtion);
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

        if (addressCondtion.length > 0 && addressCondtion[0] != address(0)) {
            condition.EQ("from", addressCondtion[0].addressToString());
        }

        if (addressCondtion.length > 1 && addressCondtion[1] != address(0)) {
            condition.EQ("to", addressCondtion[1].addressToString());
        }
        condition.limit(limit[0], limit[1]);

        Entries[] memory resultEntries;
        (resultEntries, totalRecordSize) = sumRecords(isExistBookNos, existNum, condition);

        if (resultEntries.length > 0 && totalRecordSize > 0) {
            itemList = handleEntries(resultEntries, totalRecordSize);
        }
        return itemList;
    }

    function setIndex(uint256 termNo, uint256 seqNo, address[] transactionAddress) internal {
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

    function setRecordEntry(uint termNo, uint seqNo, address[] transactionAddress, uint256 amount, bytes[] stringValueList, int[] typeList) internal returns (Entry){
        Table table = tableFactory.openTable(tableName);

        Entry recordEntry = table.newEntry();
        recordEntry.set("book_no", bookNos[bookNos.length - 1].uint2str());
        recordEntry.set("seq", seqNo.uint2str());
        recordEntry.set("term_no", termNo.uint2str());
        recordEntry.set("from", transactionAddress[2].addressToString());
        recordEntry.set("to", transactionAddress[3].addressToString());
        recordEntry.set("asset", transactionAddress[1]);
        recordEntry.set("amount", amount.uint2str());
        recordEntry.set("operator", transactionAddress[0]);
        recordEntry.set("desc", keccak256(stringValueList[0]).bytes32ToString());
        recordEntry.set("transaction_type", typeList[0]);
        recordEntry.set("operation_type", typeList[1]);
        if (stringValueList.length > 1) {
            recordEntry.set("subject", stringValueList[1].bytes2addr());
        }
        recordEntry.set("related_asset", transactionAddress.length < 5 ? address(0) : transactionAddress[4]);

        return (recordEntry);
    }

    // numberOfTableParameters
    uint constant  ITEM_SIZE = 12;

    // processingReturnedData
    function handleEntries(Entries[] resultEntries, int256 totalRecordSize) internal constant returns (string[]memory){
        string[] memory itemStringList = new string[](uint256(totalRecordSize) * ITEM_SIZE);

        for (uint ridx = 0; ridx < resultEntries.length; ridx++) {

            Entries rEntries = resultEntries[ridx];
            uint256 j = 0;

            for (int didx = 0; didx < rEntries.size(); didx++) {
                Entry recordEntry = rEntries.get(didx);
                j = 0;
                itemStringList[ITEM_SIZE * uint256(didx) + j] = recordEntry.getString("term_no");
                itemStringList[ITEM_SIZE * uint256(didx) + (++j)] = recordEntry.getString("seq");
                itemStringList[ITEM_SIZE * uint256(didx) + (++j)] = recordEntry.getString("from");
                itemStringList[ITEM_SIZE * uint256(didx) + (++j)] = recordEntry.getString("to");
                itemStringList[ITEM_SIZE * uint256(didx) + (++j)] = recordEntry.getString("amount");
                itemStringList[ITEM_SIZE * uint256(didx) + (++j)] = recordEntry.getString("desc");

                itemStringList[ITEM_SIZE * uint256(didx) + (++j)] = recordEntry.getAddress("asset").addressToString();
                itemStringList[ITEM_SIZE * uint256(didx) + (++j)] = recordEntry.getAddress("operator").addressToString();
                itemStringList[ITEM_SIZE * uint256(didx) + (++j)] = uint256(recordEntry.getInt("transaction_type")).uint2str();
                itemStringList[ITEM_SIZE * uint256(didx) + (++j)] = uint256(recordEntry.getInt("operation_type")).uint2str();
                itemStringList[ITEM_SIZE * uint256(didx) + (++j)] = recordEntry.getAddress("related_asset").addressToString();
                itemStringList[ITEM_SIZE * uint256(didx) + (++j)] = recordEntry.getString("subject");

            }
        }
        return (itemStringList);
    }

    // concatenationQueryCondition
    function handleCondiction(uint[] uintCondition, address[] addressCondtion) private constant returns (bool[], uint existNum) {
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

        if (addressCondtion.length > 0 && addressCondtion[0] != address(0)) {
            for (uint f = 0; f < fromMap[addressCondtion[0]].length; f++) {
                if (!isExist[fromMap[addressCondtion[0]][f] - 1]) {
                    isExist[fromMap[addressCondtion[0]][f] - 1] = true;
                }
            }
        }
        if (addressCondtion.length > 1 && addressCondtion[1] != address(0)) {
            for (uint tm = 0; tm < toMap[addressCondtion[1]].length; tm++) {
                if (!isExist[toMap[addressCondtion[1]][tm] - 1]) {
                    isExist[toMap[addressCondtion[1]][tm] - 1] = true;
                }
            }
        }

        uint _existNum = 0;
        for (uint i = 0; i < isExist.length; i++) {
            if (isExist[i]) {
                _existNum++;
            }
        }
        return (isExist, _existNum);

    }


    // increase book no
    function addBook() public returns (uint256){
        bookNos.push(bookNos.length + 1);
        return bookNos[bookNos.length - 1];
    }


    function getAddress() public returns (address) {
        return address(this);
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


