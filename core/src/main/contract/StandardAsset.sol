pragma solidity ^0.4.25;
pragma experimental ABIEncoderV2;

import "./lib/LibSafeMath.sol";
import "./Identity.sol";
import "./FungibleBook.sol";
import "./interface/IOrganization.sol";
import "./lib/SignLib.sol";
import "./lib/UtilLib.sol";
import "./interface/IAccount.sol";
import "./lib/LibTypeConversion.sol";


/**  @title standardAssetDefinition */
contract StandardAsset is Identity {

    using LibSafeMath for uint256;
    using UtilLib for *;
    using SignLib for bytes32[4];
    using LibTypeConversion for address;
    using LibTypeConversion for string;


    event Transfer(address _from, address _to, uint256 amount);
    event Deposit(address account, uint256 amount);
    event WithDrawal(address account, uint256 amount);
    event InsertResult(uint256 termNo, uint256 seqNo, address from, address to, uint256 amount);

    modifier onlyAccountNormal(address account) {
        require(authCenter.checkAccount(account), "Auth:only account status is normal.");
        _;
    }

    modifier verifyTxArgs(uint256 amount, string[] detailList) {
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

    //theAccountAddress
    address[] accountList;
    //trueIfTheAccountExists
    mapping(address => bool) accountMap;

    // 账户地址 => 资产余额
    mapping(address => uint256) balances;

    mapping(address => uint256) innerAndExternal;


    constructor(string tableName, address authCenterAddr, address orgAddr) public Identity(authCenterAddr, orgAddr) {
        require(bytes(tableName).length > 0 && bytes(tableName).length < 64, "assetName should be not null and less than 64 long");
        book = new FungibleBook(IOrganization(orgAddr).getProjectTerm(), tableName, orgAddr);

    }

    //    queryAccountList
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

    function getExtenalAccount(address account) internal view returns (address){
        return IOrganization(org).getExternalAccount(account);
    }

    //queryBalance
    function getBalance(address account, bytes32[4] sign) public constant returns (uint256)
    {
        address innerAccount = IOrganization(org).getInnerAccount(account);
        require(accountMap[innerAccount], "the account has not been open");

        bytes memory args;
        address txOrigin;
        bool check;
        (txOrigin, check) = authCenter.check2WithSign(org, innerAccount, "getBalance", args, sign);
        require(check, "Forbidden getBalance");
        require(authCenter.checkAccount(txOrigin), "Auth:only account status is normal.");

        return balances[innerAccount];
    }



    function deposit(address[] transactionAddress, uint256 amount, int[] typeList, string[] detailList, bytes32[4] sign) public returns (bool, uint[2])
    {
        transactionAddress = checkAuth(transactionAddress, amount, typeList, detailList, sign, "deposit");
        address account = transactionAddress[3];
        require(accountMap[account], "the account has not been open");
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

    function checkAuth(address[] transactionAddress, uint256 amount, int[] typeList, string[] detailList, bytes32[4] sign, string key) internal returns (address[]){
        verifyTxArgsFunc(amount, detailList);
        bool isCheck;
        bytes memory args = genTransactionArgs(transactionAddress, amount, typeList, detailList);
        (isCheck, transactionAddress) = checkAndHandleTransactionAddress(transactionAddress);
        require(isCheck, "operator or account is not normal");


        address txOrigin;
        bool check;
        (txOrigin, check) = authCenter.check2WithSign(org, this, bytes(key), args, sign);
        require(check, "Forbidden ".strConcat(key));
        return transactionAddress;

    }

    function checkTransferAuth(address[] transactionAddress, uint256 amount, int[] typeList, string[] detailList, bytes32[4] sign, string key) internal returns (address[]){
        verifyTxArgsFunc(amount, detailList);
        bool isCheck;
        bytes memory args = genTransactionArgs(transactionAddress, amount, typeList, detailList);
        (isCheck, transactionAddress) = checkAndHandleTransactionAddress(transactionAddress);
        require(isCheck, "operator or account is not normal");


        address txOrigin;
        bool check;
        (txOrigin, check) = authCenter.check2WithSign(org, transactionAddress[2], bytes(key), args, sign);
        require(check, "Forbidden ".strConcat(key));
        return transactionAddress;

    }
    function withdrawal(address[] transactionAddress, uint256 amount, int[] typeList, string[] detailList, bytes32[4] sign) public returns (bool, uint[2])
    {
        transactionAddress = checkAuth(transactionAddress, amount, typeList, detailList, sign, "withdrawal");
        address account = transactionAddress[2];
        require(accountMap[account], "the account has not been open");

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


    function transfer(address[] transactionAddress, uint256 amount, int[] typeList, string[] detailList, bytes32[4] sign) public returns (bool, uint[2])
    {
        transactionAddress = checkTransferAuth(transactionAddress, amount, typeList, detailList, sign, "transfer");
        require(accountMap[transactionAddress[2]], "the account has not been open");
        require(accountMap[transactionAddress[3]], "the account has not been open");

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


        require(isOwner, "Forbidden queryBook because you aren't owner");

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
        return book.query(uintCondition, addressCondition, limit);
    }


    // add book no
    function addBook(bytes32[4] sign) public returns (uint256){
        bytes memory args;
        address txOrigin;
        bool check;
        (txOrigin, check) = authCenter.check2WithSign(org, this, "addBook", args, sign);
        require(check, "Forbidden addBook");
        require(authCenter.checkAccount(txOrigin), "Auth:only account status is normal.");

        return book.addBook();
    }

    // acquisitionOfTotalAssets
    function getTotalBalance(bytes32[4] sign) public constant returns (uint256){
        bytes memory args;
        address txOrigin;
        bool check;
        (txOrigin, check) = authCenter.check2WithSign(org, this, "getTotalBalance", args, sign);
        require(check, "getTotalBalance getTotalBalance");
        require(authCenter.checkAccount(txOrigin), "Auth:only account status is normal.");

        uint256 totalBalance;
        for (uint index = 0; index < accountList.length; index++) {
            totalBalance = totalBalance.add(balances[accountList[index]]);
        }
        return totalBalance;
    }

    function openAccount(address account, bytes32[4] sign) onlyAccountNormal(account) public returns (bool){
        address innerAddress = IOrganization(org).getInnerAccount(account);
        require(!accountMap[innerAddress], "the account has been open");
        bytes memory args;
        args = args.bytesAppend(account);
        address txOrigin;
        bool check;
        (txOrigin, check) = authCenter.check2WithSign(org, this, "openAccount", args, sign);
        require(check, "Forbidden openAccount");
        require(authCenter.checkAccount(txOrigin), "Auth:only account status is normal.");

        IAccount userAccount = IAccount(IOrganization(org).getAccount(account));
        userAccount.addAsset(this, org, true);

        accountList.push(innerAddress);
        accountMap[innerAddress] = true;
        return true;
    }


    function genTransactionArgs(address[] transactionAddress, uint256 amount, int[] typeList, string[] detailList) internal view returns (bytes){
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

    function checkAdminAuth(string functionName, bytes32[4] sign) internal view returns (bool){
        bytes memory args;
        args = args.bytesAppend(bytes(functionName));
        address txOrigin;
        bool check;
        (txOrigin) = sign.checkSign();
        check = IOrganization(org).isExternalAccountAdmin(txOrigin);
        check = authCenter.checkAccount(txOrigin);

        if (check) {
            check = authCenter.checkNonce(args, sign[0], txOrigin);
        }
        return check;
    }

    function checkAndHandleTransactionAddress(address[] transactionAddress)   returns (bool, address[]){
        address[] memory innerAddress = new address[](transactionAddress.length);
        bool isCheck;
        address inner;
        for (uint i = 0; i < transactionAddress.length; i++) {
            if (i == 1 || (transactionAddress.length == 5 && i == 4 || address(0) == transactionAddress[i])) {
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

    function verifyTxArgsFunc(uint256 amount, string[] detailList) internal returns (bool){
        require(amount > 0, "amount<=0 is not verify");
        require(bytes(detailList[0]).length > 0 && bytes(detailList[0]).length < 255, "The length of desc bytes is within 255");
        if (detailList.length > 1) {
            require(bytes(detailList[1]).length > 0 && bytes(detailList[1]).length < 255, "The length of subject bytes is within 255");
        }
        return true;
    }

}