pragma solidity ^0.4.25;
pragma experimental ABIEncoderV2;

import "../base/BaseAsset.sol";
import "../../lib/UtilLib.sol";
import "../../lib/LibSafeMath.sol";
import "../../lib/LibTypeConversion.sol";
import "./FungibleBook.sol";
import "../../account/interface/IAccountHolder.sol";
import "../../org/interface/IOrganization.sol";

/**  @title standardAssetDefinition */
contract StandardAsset is BaseAsset {
    using LibSafeMath for uint256;
    using UtilLib for *;
    using LibTypeConversion for address;
    using LibTypeConversion for string;


    event Transfer(address _from, address _to, uint256 amount);
    event Deposit(address account, uint256 amount);
    event WithDrawal(address account, uint256 amount);
    event InsertResult(uint256 termNo, uint256 seqNo, address from, address to, uint256 amount);

    modifier onlyOwner(address caller){
        require(containsAccount(caller), "StandardAsset:required owner account called!");
        _;
    }
    modifier verifyTxArgs(uint256 amount, bytes[] detailList) {
        require(amount > 0, "amount<=0 is not verify");
        require(bytes(detailList[0]).length > 0 && bytes(detailList[0]).length < 255, "The length of desc bytes is within 255");
        if (detailList.length > 1) {
            require(bytes(detailList[1]).length > 0 && bytes(detailList[1]).length < 255, "The length of subject bytes is within 255");
        }
        _;
    }
    //ledger
    FungibleBook  book;
    //theTypeOfTransaction deposit
    int constant TRANSACTION_TYPE_INCOME = 0;
    //theTypeOfTransaction withdrawl
    int constant TRANSACTION_TYPE_SPEND = 1;
    //theTypeOfTransaction transfer
    int constant TRANSACTION_TYPE_TRANSFER = 2;
    // 账户地址 => 资产余额
    mapping(address => uint256) balances;
    constructor(string tableName, address orgAddr) public BaseAsset(orgAddr) {
        require(bytes(tableName).length > 0 && bytes(tableName).length < 64, "assetName should be not null and less than 64 long");
        book = new FungibleBook(IOrganization(orgAddr).getProjectTerm(), tableName, orgAddr);
        isFungible = true;
    }

    //    queryholders
    function getHolders() public onlyManager constant returns (address[])
    {
        return getAccounts();
    }

    //queryBalance
    function getBalance(address account) public onlyManager onlyOwner(account) constant returns (uint256)
    {
        return balances[account];
    }

    function openAccount(address account) public onlyManager returns (bool){
        IAccountHolder(account).addAsset(isFungible, address(this));
        return addAccount(account);
    }


    function deposit(address[] transactionAddress, uint256 amount, int[] typeList, bytes[] detailList)
    public
    onlyManager
    onlyOwner(transactionAddress[3])
//    onlyAccountNormal(transactionAddress[3])
    returns (bool, uint[2])
    {
        verifyTxArgsFunc(amount, detailList);
        address account = transactionAddress[3];
        balances[account] = balances[account].add(amount);

        int[] memory typeDetail = new int[](2);
        typeDetail[0] = TRANSACTION_TYPE_INCOME;
        typeDetail[1] = typeList[0];
        emit Deposit(account, amount);
        bool isWrite;
        uint[2] memory result;
        (isWrite, result) = book.write(transactionAddress, amount, detailList, typeDetail);
        emit InsertResult(result[0], result[1], transactionAddress[2], transactionAddress[3], amount);
        return (isWrite, result);
    }


    function withdrawal(address[] transactionAddress, uint256 amount, int[] typeList, bytes[] detailList)
    public
    onlyManager
    onlyOwner(transactionAddress[2])
//    onlyAccountNormal(transactionAddress[2])
    returns (bool, uint[2])
    {
        verifyTxArgsFunc(amount, detailList);
        address account = transactionAddress[2];
        balances[account] = balances[account].sub(amount);
        int[] memory typeDetail = new int[](2);
        typeDetail[0] = TRANSACTION_TYPE_SPEND;
        typeDetail[1] = typeList[0];
        emit WithDrawal(account, amount);
        bool isWrite;
        uint[2] memory result;
        (isWrite, result) = book.write(transactionAddress, amount, detailList, typeDetail);
        emit InsertResult(result[0], result[1], transactionAddress[2], transactionAddress[3], amount);
        return (isWrite, result);
    }


    function transfer(address[] transactionAddress, uint256 amount, int[] typeList, bytes[] detailList)
    public
    onlyManager
    returns (bool, uint[2])
    {
        verifyTxArgsFunc(amount, detailList);

        balances[transactionAddress[2]] = balances[transactionAddress[2]].sub(amount);
        balances[transactionAddress[3]] = balances[transactionAddress[3]].add(amount);

        int[] memory typeDetail = new int[](2);
        typeDetail[0] = TRANSACTION_TYPE_TRANSFER;
        typeDetail[1] = typeList[0];

        emit Transfer(transactionAddress[2], transactionAddress[3], amount);
        bool isWrite;
        uint[2] memory result;
        (isWrite, result) = book.write(transactionAddress, amount, detailList, typeDetail);
        emit InsertResult(result[0], result[1], transactionAddress[2], transactionAddress[3], amount);
        return (isWrite, result);

    }


    // theAddressOfTheCurrentContract
    function getAddress() public constant returns (address) {
        return address(this);
    }


    //query ledger
    function queryBook(uint[] uintCondition, address[] addressCondition, int[] limit) public onlyManager constant returns (string[] memory){
        require(limit.length == 2 && limit[0] < limit[1], "limit not verify,limit size should equals 2 and limit[0]<limit[1]");
        if (holders.size() == 0) {
            string[] memory result;
            return result;
        }
        return book.query(uintCondition, addressCondition, limit);
    }


    // add book no
    function addBook() public onlyManager returns (uint256){
        return book.addBook();
    }

    // acquisitionOfTotalAssets
    function getTotalBalance() public onlyManager constant returns (uint256){
        uint256 totalBalance;
        for (uint index = 0; index < holders.size(); index++) {
            totalBalance = totalBalance.add(balances[holders.get(index)]);
        }
        return totalBalance;
    }


    function genTransactionArgs(address[] transactionAddress, uint256 amount, int[] typeList, bytes[] detailList) internal view returns (bytes){
        bytes memory args;
        for (uint i = 0; i < transactionAddress.length; i++) {
            args = args.bytesAppend(transactionAddress[i]);
        }
        args = args.bytesAppend(amount);
        for (uint j = 0; j < typeList.length; j++) {
            uint256 _type = (uint256)(typeList[j]);
            args = args.bytesAppend(_type);
        }
        for (uint k = 0; k < detailList.length; k++) {
            args = args.bytesAppend(bytes(detailList[k]));
        }
        return args;

    }

    function genKey(string funtionName) internal view returns (bytes){
        bytes memory key;
        key = key.bytesAppend(address(this));
        return key;
    }


    function verifyTxArgsFunc(uint256 amount, bytes[] detailList) internal returns (bool){
        require(amount > 0, "amount<=0 is not verify");
        require(bytes(detailList[0]).length > 0 && bytes(detailList[0]).length < 255, "The length of desc bytes is within 255");
        if (detailList.length > 1) {
            require(bytes(detailList[1]).length > 0 && bytes(detailList[1]).length < 255, "The length of subject bytes is within 255");
        }
        return true;
    }

}