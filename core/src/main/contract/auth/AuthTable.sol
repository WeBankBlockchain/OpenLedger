pragma solidity ^0.4.25;
pragma experimental ABIEncoderV2;

import "./lib/Table.sol";
import "./lib/LibTypeConversion.sol";
import "./lib/UtilLib.sol";

contract AuthTable {
    modifier onlyManager() {
        require(msg.sender == manager, "AuthTable:required manager called");
        _;
    }
    using LibTypeConversion for *;
    using UtilLib for *;

    string constant  columns = "detail,operation,allowId";
    // ownerId and resource hash result
    string constant   tableId = "owner_res";
    string constant  tableName = "ledger_auth";
    TableFactory tableFactory;
    address manager;

    constructor() public {
        tableFactory.createTable(tableName, tableId, columns);
        manager = msg.sender;
    }

    function grant(address ownerId, address resource, address allowId, string operation, string detail) public onlyManager returns (bool) {
        string[] data;
        if (bytes(detail).length == 0) {
            data.push(operation);
        } else {
            data.push(detail);
        }

        bytes32 ownerResource = keccak256(ownerId, resource);
        require(selectEntryByCallerId(ownerResource, allowId, data) == 0, "allowId has been granted!");
        return createAuthEntry(ownerResource, allowId, data) == 1;
    }

    function check(address ownerId, address resource, address caller, string operation, string detail) public onlyManager returns (bool){
        if(ownerId==caller){
            return true;
        }
        bytes32 ownerResource = keccak256(ownerId, resource);
        string[] condition;
        if (bytes(detail).length == 0) {
            condition.push(operation);
        } else {
            condition.push(detail);
        }
        return selectEntryByCallerId(ownerResource, caller, condition) > 0;
    }

    function revoke(address ownerId, address resource, address caller, string operation, string detail) public onlyManager returns (bool){
        bytes32 ownerResource = keccak256(ownerId, resource);
        string[] condition;
        if (bytes(detail).length == 0) {
            condition.push(operation);
        } else {
            condition.push(detail);
        }
        require(selectEntryByCallerId(ownerResource, caller, condition) != 0, "allowId has not  been granted!");
        return deleteAuthEntry(ownerResource, caller, condition) == 1;
    }

    function createAuthEntry(bytes32 ownerResource, address allowId, string[] data) internal returns (int256){
        Table table = tableFactory.openTable(tableName);
        Entry authEntry = table.newEntry();
        authEntry.set("owner_res", ownerResource.bytes32ToString());
        authEntry.set("operation", data[0]);
        if (data.length >= 2) {
            authEntry.set("detail", data[1]);
        } else {
            authEntry.set("detail", "*");
        }
        authEntry.set("allowId", allowId.addressToString());
        int256 result = table.insert(ownerResource.bytes32ToString(), authEntry);
        return result;
    }

    function deleteAuthEntry(bytes32 ownerResource, address allowId, string[] data) internal returns (int256){
        Table table = tableFactory.openTable(tableName);
        Condition condition = table.newCondition();
        condition.EQ("operation", data[0]);
        if (data.length >= 2) {
            condition.EQ("detail", data[1]);
        } else {
            condition.EQ("detail", "*");
        }
        condition.EQ("allowId", allowId.addressToString());
        int256 result = table.remove(ownerResource.bytes32ToString(), condition);
        return result;
    }

    function selectEntryByCallerId(bytes32 ownerResource, address callerId, string[] conditions) internal returns (int256){
        Table table = tableFactory.openTable(tableName);
        Condition condition = table.newCondition();
        condition.EQ("operation", conditions[0]);
        if (conditions.length >= 2) {
            condition.EQ("detail", conditions[1]);
        } else {
            condition.EQ("detail", "*");
        }
        condition.EQ("allowId", callerId.addressToString());
        condition.limit(0, 1);
        Entries result = table.select(ownerResource.bytes32ToString(), condition);
        return result.size();
    }

}