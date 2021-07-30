pragma solidity ^0.4.25;

import "./storage/BytesSetLib.sol";
import "./storage/BytesMapLib.sol";
import "./lib/UtilLib.sol";

contract ResourceGroup {
    using BytesSetLib for BytesSetLib.Set;
    using BytesMapLib for BytesMapLib.Map;
    using UtilLib for *;

    modifier onlyManager() {
        require(msg.sender == manager, "Resource:required manager called");
        _;
    }
    mapping(address => bool) resourceMap;
    mapping(address => BytesSetLib.Set) operationMap;

    mapping(address =>BytesMapLib.Map) detailMap;

    address manager;
    constructor() public {
        manager = msg.sender;
    }

    function addResource(address resource, string operation) public  onlyManager {
        require(!operationMap[resource].contains(bytes(operation)), "Resource: has been added");
        if (!resourceMap[resource]) {
            resourceMap[resource] = true;
        }
        operationMap[resource].insert(bytes(operation));
    }

    function addResource(address resource, string operation, string detail) public  onlyManager {
        require(!detailMap[resource].contains(bytes(detail)), "Resource: has been added");
        if (!resourceMap[resource]) {
            resourceMap[resource] = true;
        }
        if (!operationMap[resource].contains(bytes(operation))) {
            operationMap[resource].insert(bytes(operation));
        }
        detailMap[resource].insert(bytes(detail), bytes(operation));
    }

    function removeResource(address resource) public  onlyManager{
        require(resourceMap[resource], "resource is not exist");
        resourceMap[resource] = false;
        BytesSetLib.Set nullSet;
        BytesMapLib.Map nullMap;
        operationMap[resource] = nullSet;
        detailMap[resource] = nullMap;
    }

    function removeOperation(address resource, string operation) public onlyManager {
        require(resourceMap[resource], "resource is not exist");
        require(operationMap[resource].contains(bytes(operation)), "operation ist not exist");
        operationMap[resource].remove(bytes(operation));
    }

    function removeDetail(address resource, string operation, string detail) public  onlyManager {
        require(resourceMap[resource], "resource is not exist");
        require(operationMap[resource].contains(bytes(operation)), "operation is not exist");
        require(detailMap[resource].contains(bytes(detail)), "detail is not exist");
        detailMap[resource].remove(bytes(detail));
    }

    function hasResource(address resource, string operation) public onlyManager  view returns (bool){
        return operationMap[resource].contains(bytes(operation));
    }

    function hasResource(address resource, string operation, string detail) public onlyManager view returns (bool){
        if (detailMap[resource].contains(bytes(detail))  && detailMap[resource].get(bytes(detail)).equal(bytes(operation))) {

            return true;
        }
        return false;
    }

}