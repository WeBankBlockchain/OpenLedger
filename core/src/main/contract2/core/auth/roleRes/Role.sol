pragma solidity ^0.4.25;

contract Role {
    modifier onlyManager() {
        require(msg.sender == manager, "Role:required manager called");
        _;
    }
    string name;
    mapping(address => bool) accMap;
    address[] accounts;
    address manager;

    constructor(string roleName) public {
        manager = msg.sender;
        name = roleName;
    }

    function addAccount(address account) public  onlyManager{
        require(!accMap[account], "Role:account has been added");
        accounts.push(account);
        accMap[account] = true;
    }

    function removeAccount(address account) public onlyManager {
        require(accMap[account], "Role:account has not been added");
        bool isIndex;
        address[] memory newAccounts=new address[](accounts.length - 1);
        for (uint i = 0; i < accounts.length-1; i++) {
            if (!isIndex&&accounts[i] == account) {
                isIndex = true;
            }
            if (isIndex) {
                newAccounts[i] = accounts[i + 1];
            } else {
                newAccounts[i] = accounts[i];
            }
        }
        accounts=newAccounts;
        accMap[account] = false;
    }

    function existAccount(address account) public   view returns(bool){
        return accMap[account];
    }

    function listAccount() public  onlyManager view returns(address[]){
        return accounts;
    }

    function getRole() public onlyManager view returns(string){
        return name;
    }

}
