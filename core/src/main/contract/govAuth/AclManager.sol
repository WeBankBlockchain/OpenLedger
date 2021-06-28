pragma solidity ^0.4.25;


contract AclManager {
    uint8 internal constant WHITE = 1;
    uint8 internal constant BLACK = 2;
    uint8 internal constant ALL = 3;

    struct Group {
        uint8 mode;
        uint256 accCount;
        uint256 functionCount;
        mapping(address => bool) accList;
        mapping(address => mapping(bytes4 => bool)) functions;
    }

    mapping(string => Group) internal _groups;
    mapping(address => mapping(bytes4 => string)) internal _functionToGroups;

    event CreateGroup(string group, uint8 mode);

    function createGroupImpl(string group, uint8 mode) internal {
        bytes memory groupNameBytes = bytes(group);
        require(groupNameBytes.length != 0);
        require(_groups[group].mode == 0, "group already exist");
        _groups[group] = Group(mode, 0, 0);
        emit CreateGroup(group, mode);
    }

    event AddAccountToGroup(address account, string group);

    function addAccountToGroupImpl(address account, string group) internal {
        require(_groups[group].mode != 0, "group not exist");
        require(!_groups[group].accList[account], "account already add");
        _groups[group].accList[account] = true;
        _groups[group].accCount++;
        emit AddAccountToGroup(account, group);
    }

    event AddFunctionToGroup(address contractAddr, string func, string group);

    function addFunctionToGroupImpl(address contractAddr, string func, string group) internal {
        //Checks
        Group storage g = _groups[group];
        require(g.mode != 0, "group not exist");
        bytes4 sig = bytes4(sha3(func));
        require(!g.functions[contractAddr][sig], "function already add");
        bytes memory groupNameBytes = bytes(_functionToGroups[contractAddr][sig]);
        require(groupNameBytes.length == 0);
        //Effects
        g.functions[contractAddr][sig] = true;
        g.functionCount++;
        _functionToGroups[contractAddr][sig] = group;
        emit AddFunctionToGroup(contractAddr, func, group);
    }

    event RemoveAccountFromGroup(address account, string group);

    function removeAccountFromGroupImpl(address account, string group) internal {
        require(_groups[group].accList[account], "account not exist");
        _groups[group].accList[account] = false;
        _groups[group].accCount--;
        bytes32 sha3Group = sha3(group);
        emit RemoveAccountFromGroup(account, group);
    }

    event RemoveFunctionFromGroup(address contractAddr, string func, string group);

    function removeFunctionFromGroupImpl(address contractAddr, string func, string group) internal {
        //Checks
        Group storage g = _groups[group];
        require(g.mode != 0, "group not exist");
        bytes4 sig = bytes4(sha3(func));
        require(g.functions[contractAddr][sig], "function not exist");
        //Effectss
        g.functions[contractAddr][sig] = false;
        g.functionCount--;
        _functionToGroups[contractAddr][sig] = "";
        emit RemoveFunctionFromGroup(contractAddr, func, group);
    }

    function getGroup(string group) public view returns (uint8, uint256, uint256){
        Group g = _groups[group];
        return (g.mode, g.accCount, g.functionCount);
    }

    function containsAccount(string group, address account) public view returns (bool){
        return _groups[group].accList[account];
    }

    function containsFunction(string group, address contractAddr, string func) public view returns (bool){
        return _groups[group].functions[contractAddr][bytes4(sha3(func))];
    }


}

