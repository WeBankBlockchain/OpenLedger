pragma solidity ^0.4.25;

import "./LibAddressSet.sol";
import "./SingletonVoter.sol";
import "./AclManager.sol";
import "./IAuthControl.sol";

contract GovManager is SingletonVoter {

    address[] private _accountsCache;
    uint16[] private _weightsCache;
    uint16 private _thresholdCache;

    modifier validRequest(uint256 id) {
        require(canCall(id), "WEGovernance: valid request failed.");
        _;
        unregister(id);
    }

    modifier onlyGovs(){
        require(inGovs(), "you are not governors");
        _;
    }

    event InitWeightData(address[] accounts, uint16[] weights, uint16 threshold);

    function initWeightData(address[] accounts, uint16[] weights, uint16 threshold) internal {
        _voteWeight.setBoardWeight(accounts, weights, threshold);
        _accountsCache = accounts;
        _weightsCache = weights;
        _thresholdCache = threshold;
        emit InitWeightData(accounts, weights, threshold);
    }

    function getGovs() public view returns (address[], uint16[], uint16){
        return (_accountsCache, _weightsCache, _thresholdCache);
    }

    //--RESET THRESHOLD
    uint16 _pendingThreshold;
    uint8 constant private SET_THRESHOLD_TXTYPE = 1;

    event RequestSetThreshold(uint16 threshold);

    function requestSetThreshold(uint16 newThreshold) public onlyGovs preRegister(SET_THRESHOLD_TXTYPE) {
        _pendingThreshold = newThreshold;
        emit RequestSetThreshold(newThreshold);
    }

    event ExecuteSetThreshold();

    function executeSetThreshold() public onlyGovs canExecute(SET_THRESHOLD_TXTYPE) {
        _voteWeight.setThreshold(_pendingThreshold);
        _thresholdCache = _pendingThreshold;
        emit ExecuteSetThreshold();
    }

    //---RESET GOVERNORS----
    uint _resetGovernorsReqId;
    address[] _pendingGovernors;
    uint16[] _pendingWeights;
    uint8 constant private RESET_GOVERNORS_TXTYPE = 2;

    event RequestResetGovernors(address[] governors, uint16[] weights);

    function requestResetGovernors(address[] governors, uint16[] weights) public onlyGovs preRegister(RESET_GOVERNORS_TXTYPE) {
        _pendingGovernors = governors;
        _pendingWeights = weights;
        emit RequestResetGovernors(governors, weights);
    }

    event ExecuteResetGovernAccounts();

    function executeResetGovernAccounts() public onlyGovs canExecute(RESET_GOVERNORS_TXTYPE) {
        _voteWeight = new WEVoteWeight();
        _voteWeight.setBoardWeight(_pendingGovernors, _pendingWeights, _thresholdCache);
        _accountsCache = _pendingGovernors;
        _weightsCache = _pendingWeights;
        emit ExecuteResetGovernAccounts();
    }


    //---ADD GOVERNOR---
    mapping(address => uint) private _createGovernorAccountReqs;
    LibAddressSet.AddressSet private _pendingAccountsToAdd;

    event RequestAddGovernor(address account, uint reqId);

    function requestAddGovernor(address account) public onlyGovs {
        require(_createGovernorAccountReqs[account] == 0);
        (WEVoteRequest _, uint256 reqId) = super.register(11, address(0));
        _createGovernorAccountReqs[account] = reqId;
        LibAddressSet.add(_pendingAccountsToAdd, account);
        emit RequestAddGovernor(account, reqId);
    }

    event DeleteAddGovernorReq(address account);

    function deleteAddGovernorReq(address account) public onlyGovs {
        uint reqId = _createGovernorAccountReqs[account];
        require(reqId > 0, "account not pending");
        require(unregister(reqId));
        LibAddressSet.remove(_pendingAccountsToAdd, account);
        delete _createGovernorAccountReqs[account];
        emit DeleteAddGovernorReq(account);
    }

    event ApproveAddGovernorReq(address account);

    function approveAddGovernorReq(address account) public onlyGovs {
        uint reqId = _createGovernorAccountReqs[account];
        require(reqId > 0, "account not pending");
        approve(reqId);
        emit ApproveAddGovernorReq(account);
    }

    event ExecuteAddGovernorReq(address account);

    function executeAddGovernorReq(address account) public onlyGovs {
        uint reqId = _createGovernorAccountReqs[account];
        require(reqId > 0, "account not exist");
        addGovernorByVote(reqId, account);
        LibAddressSet.remove(_pendingAccountsToAdd, account);
        delete _createGovernorAccountReqs[account];
        _accountsCache.push(account);
        _weightsCache.push(1);
        emit ExecuteAddGovernorReq(account);
    }

    function addGovernorByVote(uint256 id, address account) private validRequest(id) {
        _voteWeight.setWeight(account, 1);
    }

    function getGovernorsToAdd() public view returns (address[]){
        return LibAddressSet.getAll(_pendingAccountsToAdd);
    }

    function getAddGovRequest(address account) public view returns (uint256, address, uint16, address, uint16, uint8, uint8) {
        uint reqId = _createGovernorAccountReqs[account];
        require(reqId > 0, "account not exist");
        return super.getRequestInfo(reqId);
    }

    //---REMOVE GOVERNOR---
    mapping(address => uint) private _removeGovernorAccountReqs;
    LibAddressSet.AddressSet private _pendingAccountsToRemove;

    event RequestRemoveGovernor(address account);

    function requestRemoveGovernor(address account) public onlyGovs {
        require(_removeGovernorAccountReqs[account] == 0);
        (WEVoteRequest _, uint256 reqId) = super.register(12, address(0));
        _removeGovernorAccountReqs[account] = reqId;
        LibAddressSet.add(_pendingAccountsToRemove, account);
        emit RequestRemoveGovernor(account);
    }

    event DeleteRemoveGovernorReq(address account);

    function deleteRemoveGovernorReq(address account) public onlyGovs {
        uint reqId = _removeGovernorAccountReqs[account];
        require(reqId > 0);
        require(unregister(reqId));
        LibAddressSet.remove(_pendingAccountsToRemove, account);
        delete _removeGovernorAccountReqs[account];
        emit DeleteRemoveGovernorReq(account);
    }

    event ApproveRemoveGovernorReq(address account);

    function approveRemoveGovernorReq(address account) public onlyGovs {
        uint reqId = _removeGovernorAccountReqs[account];
        require(reqId > 0);
        approve(reqId);
        emit ApproveRemoveGovernorReq(account);
    }

    event ExecuteRemoveGovernorReq(address account);

    function executeRemoveGovernorReq(address account) public onlyGovs {
        uint reqId = _removeGovernorAccountReqs[account];
        require(reqId > 0);
        removeGovernorByVote(reqId, account);
        LibAddressSet.remove(_pendingAccountsToRemove, account);
        _removeGovernorAccountReqs[account];
        for (uint i = 0; i < _accountsCache.length; i++) {
            if (_accountsCache[i] == account) {
                _accountsCache[i] = _accountsCache[_accountsCache.length - 1];
                _weightsCache[i] = _weightsCache[_weightsCache.length - 1];
                _accountsCache.length--;
                _weightsCache.length--;
            }
        }

        emit ExecuteRemoveGovernorReq(account);
    }

    function removeGovernorByVote(uint256 id, address account) private validRequest(id) {
        _voteWeight.setWeight(account, 0);
    }

    function getGovernorsToRemove() public view returns (address[]){
        return LibAddressSet.getAll(_pendingAccountsToRemove);
    }

    function getRemoveGovRequest(address account) public view returns (uint256, address, uint16, address, uint16, uint8, uint8) {
        uint reqId = _removeGovernorAccountReqs[account];
        require(reqId > 0, "account not exist");
        return super.getRequestInfo(reqId);
    }

    function inGovs() public view returns (bool){
        (uint16 weight, uint16 _) = _voteWeight.getWeight(msg.sender);
        return weight > 0;
    }
}


contract WEAdmin {

    address private _admin;

    function initAdmin() internal {
        _admin = msg.sender;
    }

    modifier onlyAdmin(){
        require(msg.sender == _admin, "You are not admin");
        _;
    }


    event TransferAdminAuth(address indexed oldAdmin, address indexed newAdmin);

    function transferAdminAuth(address newAdminAddr) external onlyAdmin {
        address oldAdmin = _admin;
        _admin = newAdminAddr;
        emit TransferAdminAuth(oldAdmin, newAdminAddr);
    }

    function getAdmin() public view returns (address){
        return _admin;
    }

    function isAdmin() public view returns (bool){
        return _admin == msg.sender;
    }
}

contract AuthManager is WEAdmin, GovManager, AclManager, IAuthControl {

    uint private _mode;

    /**
    * 1 - ADMIN
    * 2 - VOTE
    **/
    constructor(uint mode, address[] accounts, uint16[] weights, uint16 threshold) public {
        _mode = mode;
        if (_mode == 1) {
            super.initAdmin();
        }
        if (_mode == 2) {
            super.initWeightData(accounts, weights, threshold);
        }
    }

    function opMode() public view returns (uint){
        return _mode;
    }

    modifier canCallFunc(address contractAddr, bytes4 funcSig, address caller) {
        require(canCallFunction(contractAddr, funcSig, caller), "Forbidden");
        _;
    }

    function approveSingle(uint8 txType) public onlyGovs {
        super.approveSingleImpl(txType);
    }

    function deleteSingle(uint8 txType) public onlyGovs {
        super.deleteSingleImpl(txType);
    }

    //By admin
    function createGroup(string group, uint8 mode) public onlyAdmin {
        super.createGroupImpl(group, mode);
    }

    function addAccountToGroup(address account, string group) public onlyAdmin {
        super.addAccountToGroupImpl(account, group);
    }

    function addFunctionToGroup(address contractAddr, string func, string group) public onlyAdmin {
        super.addFunctionToGroupImpl(contractAddr, func, group);
    }

    function removeAccountFromGroup(address account, string group) public onlyAdmin {
        super.removeAccountFromGroupImpl(account, group);
    }

    function removeFunctionFromGroup(address contractAddr, string func, string group) public onlyAdmin {
        super.removeFunctionFromGroupImpl(contractAddr, func, group);
    }

    //By Vote
    uint8 constant private CREATE_GROUP = 3;
    string private _createGroupGroup;
    uint8 private _createGroupMode;

    event RequestCreateGroup(string group, uint8 mode);

    function requestCreateGroup(string group, uint8 mode) public onlyGovs preRegister(CREATE_GROUP) {
        _createGroupGroup = group;
        _createGroupMode = mode;
        emit RequestCreateGroup(group, mode);
    }

    function viewCreateGroup() public onlyGovs view returns (string, uint8){
        return (_createGroupGroup, _createGroupMode);
    }

    function executeCreateGroup() public onlyGovs canExecute(CREATE_GROUP) {
        super.createGroupImpl(_createGroupGroup, _createGroupMode);
    }

    uint8 constant private ADD_ACCOUNT_TO_GROUP = 4;
    address private _addAccountToGroupAccount;
    string private _addAccountToGroupGroup;

    event RequestAddAccountToGroup(address account, string group);

    function requestAddAccountToGroup(address account, string group) public onlyGovs preRegister(ADD_ACCOUNT_TO_GROUP) {
        _addAccountToGroupAccount = account;
        _addAccountToGroupGroup = group;
        emit RequestAddAccountToGroup(account, group);
    }

    function viewAddAccountToGroup() public view onlyGovs returns (address, string){
        return (_addAccountToGroupAccount, _addAccountToGroupGroup);
    }

    function executeAddAccountToGroup() public onlyGovs canExecute(ADD_ACCOUNT_TO_GROUP) {
        super.addAccountToGroupImpl(_addAccountToGroupAccount, _addAccountToGroupGroup);
    }

    uint8 constant private ADD_FUNCTION_TO_GROUP = 5;
    address private _addFunctionToGroupContract;
    string private _addFunctionToGroupFunc;
    string private _addFunctionToGroupGroup;

    event RequestAddFunctionToGroup(address contractAddr, string func, string group);

    function requestAddFunctionToGroup(address contractAddr, string func, string group) public onlyGovs preRegister(ADD_FUNCTION_TO_GROUP) {
        _addFunctionToGroupContract = contractAddr;
        _addFunctionToGroupFunc = func;
        _addFunctionToGroupGroup = group;
        emit RequestAddFunctionToGroup(contractAddr, func, group);
    }

    function viewAddFunctionToGroup() public view onlyGovs returns (address, string, string){
        return (_addFunctionToGroupContract, _addFunctionToGroupFunc, _addFunctionToGroupGroup);
    }

    function executeAddFunctionToGroup() public onlyGovs canExecute(ADD_FUNCTION_TO_GROUP) {
        super.addFunctionToGroupImpl(_addFunctionToGroupContract, _addFunctionToGroupFunc, _addFunctionToGroupGroup);
    }

    uint8 constant private REMOVE_ACCOUNT_FROM_GROUP = 6;
    address private _removeAccountFromGroupAccount;
    string private _removeAccountFromGroupGroup;

    event RequestRemoveAccountFromGroup(address account, string group);

    function requestRemoveAccountFromGroup(address account, string group) public onlyGovs preRegister(REMOVE_ACCOUNT_FROM_GROUP) {
        _removeAccountFromGroupAccount = account;
        _removeAccountFromGroupGroup = group;
        emit RequestRemoveAccountFromGroup(account, group);
    }

    function viewRemoveAccountToGroup() public view onlyGovs returns (address, string){
        return (_removeAccountFromGroupAccount, _removeAccountFromGroupGroup);
    }

    function executeRemoveAccountFromGroup() public onlyGovs canExecute(REMOVE_ACCOUNT_FROM_GROUP) {
        super.removeAccountFromGroupImpl(_removeAccountFromGroupAccount, _removeAccountFromGroupGroup);
    }

    uint8 constant private REMOVE_FUNCTION_FROM_GROUP = 7;
    address private _removeFunctionFromGroupContract;
    string private _removeFunctionFromGroupFunc;
    string private _removeFunctionFromGroupGroup;

    event RequestRemoveFunctionFromGroup(address contractAddr, string func, string group);

    function requestRemoveFunctionFromGroup(address contractAddr, string func, string group) public onlyGovs preRegister(REMOVE_FUNCTION_FROM_GROUP) {
        _removeFunctionFromGroupContract = contractAddr;
        _removeFunctionFromGroupFunc = func;
        _removeFunctionFromGroupGroup = group;
        emit RequestRemoveFunctionFromGroup(contractAddr, func, group);
    }

    function viewRemoveFunctionToGroup() public view onlyGovs returns (address, string, string){
        return (_removeFunctionFromGroupContract, _removeFunctionFromGroupFunc, _removeFunctionFromGroupGroup);
    }

    function executeRemoveFunctionFromGroup() public onlyGovs canExecute(REMOVE_FUNCTION_FROM_GROUP) {
        super.removeFunctionFromGroupImpl(_removeFunctionFromGroupContract, _removeFunctionFromGroupFunc, _removeFunctionFromGroupGroup);
    }


    function canCallFunction(address contractAddr, bytes4 sig, address caller) public view returns (bool){
        //Not configured for this function
        string groupName = _functionToGroups[contractAddr][sig];
        Group storage group = _groups[groupName];
        if (group.mode == 0) return false;
        //Take function mode
        uint8 mode = group.mode;
        if (mode == ALL) return true;

        //Where member in group
        bool memberInGroup = group.accList[caller];
        if (mode == WHITE) return memberInGroup;
        return !memberInGroup;
    }
}


















