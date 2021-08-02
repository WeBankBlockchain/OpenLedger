pragma solidity ^0.4.25;
pragma experimental ABIEncoderV2;

import "../../storage/AddressSetLib.sol";
import "../../govAccount/BaseAccountInterface.sol";

contract BaseAsset {
    using AddressSetLib for AddressSetLib.Set;
    modifier onlyManager(){
        require(msg.sender == manager, "BaseAsset:required manager called!");
        _;
    }
    modifier onlyCustodian(address caller){
        require(caller == custodian, "BaseAsset:required custodian called!");
        _;
    }

    modifier onlyAccountNormal(address account) {
        require(BaseAccountInterface(account).isNormal(), "Auth:only account status is normal.");
        _;
    }
    modifier existAccount(address account) {
        require(holders.contains(account), "BaseAsset:account is not exist.");
        _;
    }
    address manager;
    address custodian;
    AddressSetLib.Set holders;
    bool isFungible;
    constructor(address _custodian) public {
        custodian = _custodian;
        manager = msg.sender;
    }

    // account is BaseAccountHolder
    function addAccount(address account) public returns (bool){
        require(!holders.contains(account), "BaseAsset:account has been exist!");
        holders.insert(account);
        return true;
    }
    // account is BaseAccountHolder
    function containsAccount(address account) public view returns (bool){
        return holders.contains(account);
    }
    // account is BaseAccountHolder
    function getAccounts() public view returns (address[]){
        return holders.getAll();
    }
    // account is BaseAccountHolder
    function isCustodian(address account) public view returns (bool){
        return account == custodian;
    }
    function boolFungible() public view returns(bool){
        return isFungible;
    }
}
