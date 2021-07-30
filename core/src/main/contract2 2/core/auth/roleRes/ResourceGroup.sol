pragma solidity ^0.4.25;

import "../../storage/BytesSetLib.sol";
import "../../storage/BytesMapLib.sol";
import "../../lib/UtilLib.sol";

contract ResourceGroup {
    using BytesSetLib for BytesSetLib.Set;
    using BytesMapLib for BytesMapLib.Map;
    using UtilLib for *;

    modifier onlyManager() {
        require(msg.sender == manager, "Resource:required manager called");
        _;
    }

    mapping(bytes32 => BytesSetLib.Set) resDetailAndOperMap;

    address manager;
    constructor() public {
        manager = msg.sender;
    }

    function addResource(address resource, string operation) public onlyManager {
        require(!(resDetailAndOperMap[keccak256(resource)]).contains(bytes(operation)), "Resource: has been added");
        resDetailAndOperMap[keccak256(resource)].insert(bytes(operation));
    }

    function addResource(address resource, string operation, bytes detail) public onlyManager {
        require(detail.length > 0 && bytes(operation).length > 0, "required detail and operation >0");
        require(!resDetailAndOperMap[keccak256(resource, detail)].contains(bytes(operation)), "Resource: has been added");
        resDetailAndOperMap[keccak256(resource, detail)].insert(bytes(operation));
    }

    function removeResource(address resource) public onlyManager {
        BytesSetLib.Set nullSet;
        resDetailAndOperMap[keccak256(resource)] = nullSet;
    }

    function removeOperation(address resource, string operation) public onlyManager {
        resDetailAndOperMap[keccak256(resource)].remove(bytes(operation));
    }

    function removeDetail(address resource, string operation, bytes detail) public onlyManager {
        resDetailAndOperMap[keccak256(resource, detail)].remove(bytes(operation));
    }

    function hasResource(address resource, string operation) public onlyManager view returns (bool){
        return resDetailAndOperMap[keccak256(resource)].contains(bytes(operation));
    }

    function hasResource(address resource, string operation, bytes detail) public onlyManager view returns (bool){
        return resDetailAndOperMap[keccak256(resource, detail)].contains(bytes(operation));
    }

}
