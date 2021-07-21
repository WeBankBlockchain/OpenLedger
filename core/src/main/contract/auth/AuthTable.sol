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
        tableFactory = TableFactory(0x1001);
        tableFactory.createTable(tableName, tableId, columns);
        manager = msg.sender;
    }

    function grant(address ownerId, address resource, address allowId, string operation, string detail) public onlyManager returns (bool) {
        bytes32 ownerResource = keccak256(ownerId, resource);
        require(selectEntryByCallerId(ownerResource, allowId, operation, detail) == 0, "allowId has been granted!");
        return createAuthEntry(ownerResource, allowId, operation, detail) == 1;
    }

    function check(address ownerId, address resource, address caller, string operation, string detail) public onlyManager returns (bool){
        if (ownerId == caller) {
            return true;
        }
        bytes32 ownerResource = keccak256(ownerId, resource);
        return selectEntryByCallerId(ownerResource, caller, operation, detail) > 0;
    }

    function revoke(address ownerId, address resource, address caller, string operation, string detail) public onlyManager returns (bool){
        bytes32 ownerResource = keccak256(ownerId, resource);
        require(selectEntryByCallerId(ownerResource, caller, operation, detail) != 0, "allowId has not  been granted!");
        return deleteAuthEntry(ownerResource, caller, operation, detail) > 0;
    }

    function createAuthEntry(bytes32 ownerResource, address allowId, string operation, string detail) internal returns (int256){
        require(bytes(operation).length > 0, "required operation can not be null");
        Table table = tableFactory.openTable(tableName);
        Entry authEntry = table.newEntry();
        authEntry.set("owner_res", ownerResource.bytes32ToString());
        authEntry.set("operation", operation);
        if (bytes(detail).length > 0) {
            authEntry.set("detail", detail);
        }
        authEntry.set("allowId", allowId.addressToString());
        int256 result = table.insert(ownerResource.bytes32ToString(), authEntry);
        return result;
    }

    function deleteAuthEntry(bytes32 ownerResource, address allowId, string operation, string detail) internal returns (int256){
        require(bytes(operation).length > 0, "required operation can not be null");
        Table table = tableFactory.openTable(tableName);
        Condition condition = table.newCondition();
        condition.EQ("operation", operation);
        if (bytes(detail).length > 0) {
            condition.EQ("detail", detail);
        }
        condition.EQ("allowId", allowId.addressToString());
        int256 result = table.remove(ownerResource.bytes32ToString(), condition);
        return result;
    }

    function selectEntryByCallerId(bytes32 ownerResource, address callerId, string operation, string detail) internal returns (int256){
        require(bytes(operation).length > 0, "required operation can not be null");
        Table table = tableFactory.openTable(tableName);
        Condition condition = table.newCondition();
        condition.EQ("operation", operation);
        if (bytes(detail).length > 0) {
            condition.EQ("detail", detail);
        }
        condition.EQ("allowId", callerId.addressToString());
        condition.limit(0, 1);
        Entries result = table.select(ownerResource.bytes32ToString(), condition);
        return result.size();
    }

}
