pragma solidity ^0.4.25;
pragma experimental ABIEncoderV2;

import "./lib/SignLib.sol";
import "./interface/IAuthCenter.sol";
import "./lib/UtilLib.sol";

/** @title assetPoolContract */
contract AssetPool {
    event AssetStatusTransform(address _asset, uint8 fromStatus, uint8 toStatus);
    using SignLib for bytes32[4];
    using UtilLib for *;

    // assetPoolStatus
    struct PoolStatus {
        // statusValue key
        uint8 status;
        // theNameOfTheState
        string name;
        // stateDescription
        string desc;
    }

    // maintains the current state of the asset assetAddress => statusAddress
    mapping(address => PoolStatus) assetStatusMap;
    // stateList
    PoolStatus[]  statusList;
    //assetsList
    address[]  assetList;
    // status=>poolStatus
    mapping(uint8 => PoolStatus) poolStatusMap;
    // The asset address is in the index of the asset List
    mapping(address => uint256) assetIndexMap;
    // 0-normal, 1-frozen
    uint8 constant NORMAL_STATUS = 1;
    uint8 constant FROZEN_STATUS = 2;
    uint8 public status = NORMAL_STATUS;

    address org;
    IAuthCenter authCenter;

    constructor(address authCenterAddr, address orgAddr) {
        org = orgAddr;
        PoolStatus  memory poolStatus = PoolStatus(0, "init", "init");
        statusList.push(poolStatus);
        poolStatusMap[0] = poolStatus;
        authCenter = IAuthCenter(authCenterAddr);
    }


    // addTheAsset and init the state
    function addAsset(address assetAddress, bytes32[4] sign) public returns (address[]){
        bytes memory args;
        address txOrigin;
        bool check;
        args = args.bytesAppend(assetAddress);

        (txOrigin, check) = authCenter.check2WithSign(org, this, "addAsset", args, sign);
        require(check, "Forbidden addAsset");

        require(status == NORMAL_STATUS, "Current Status is FORZEN");
        require(!checkAsset(assetAddress), "Asset has been added");

        assetList.push(assetAddress);
        assetStatusMap[assetAddress] = poolStatusMap[0];
        assetIndexMap[assetAddress] = assetList.length - 1;

        return assetList;
    }

    // Add state Add state before adding assets to the asset pool
    function addStatus(uint8 status, string name, string desc, bytes32[4] sign) public returns (uint256){
        require(bytes(name).length > 0 && bytes(name).length < 64, "name should not be null and less than 64");
        bytes memory args;
        address txOrigin;
        bool check;
        uint256 uintstatus = (uint256)(status);
        args = args.bytesAppend(uintstatus);
        args = args.bytesAppend(bytes(name));
        args = args.bytesAppend(bytes(desc));

        (txOrigin, check) = authCenter.check2WithSign(org, this, "addStatus", args, sign);
        require(check, "Forbidden addStatus");

        require(status == NORMAL_STATUS, "Current Status is FORZEN");
        require(!checkStatus(status), "Status has been added");

        PoolStatus  memory poolStatus = PoolStatus(status, name, desc);
        statusList.push(poolStatus);
        poolStatusMap[status] = poolStatus;
        return statusList.length;
    }


    function getAssetList(bytes32[4] sign) public constant returns (address[]){
        bytes memory args;
        address txOrigin;
        bool check;
        (txOrigin, check) = authCenter.check2WithSign(org, this, "getAssetList", args, sign);
        require(check, "Forbidden getAssetList");

        return assetList;
    }


    // updateAssetStatus
    function moveAsset(address assetAddress, uint8 toStatus, bytes32[4] sign) public returns (uint8){
        bytes memory args;
        address txOrigin;
        bool check;

        uint256 uintstatus = (uint256)(toStatus);
        args = args.bytesAppend(assetAddress);
        args = args.bytesAppend(uintstatus);

        (txOrigin, check) = authCenter.check2WithSign(org, this, "moveAsset", args, sign);
        require(check, "Forbidden moveAsset");

        require(status == NORMAL_STATUS, "Current Status is FORZEN");
        require(checkAsset(assetAddress), "Asset has not been added");
        require(checkStatus(toStatus), "Status has not been added");
        require(assetStatusMap[assetAddress].status != toStatus, "Status has  been set to the asset");

        emit AssetStatusTransform(assetAddress, assetStatusMap[assetAddress].status, toStatus);
        assetStatusMap[assetAddress] = poolStatusMap[toStatus];

        return assetStatusMap[assetAddress].status;
    }

    //removeTheAsset
    function removeAsset(address assetAddress, bytes32[4] sign) public returns (address[]) {
        require(checkAsset(assetAddress), "Asset has not  been added");
        bytes memory args;
        address txOrigin;
        bool check;
        args = args.bytesAppend(assetAddress);
        (txOrigin, check) = authCenter.check2WithSign(org, this, "removeAsset", args, sign);
        require(check, "Forbidden removeAsset");

        require(status == NORMAL_STATUS, "Current Status is FORZEN");

        uint256 index = assetIndexMap[assetAddress];
        if (index >= assetList.length) return;

        for (uint i = index; i < assetList.length - 1; i++) {
            assetList[i] = assetList[i + 1];
        }
        delete assetList[assetList.length - 1];
        assetList.length--;
        return assetList;
    }

    // freeze asset pool
    function freezePool(bytes32[4] sign) public returns (uint8){
        bytes memory args;
        address txOrigin;
        bool check;
        (txOrigin, check) = authCenter.check2WithSign(org, this, "freezePool", args, sign);
        require(check, "Forbidden freezePool");

        require(status == NORMAL_STATUS, "Current Status is FORZEN");
        status = FROZEN_STATUS;
        return status;
    }

    //unfreeze asset pool
    function unfreezePool(bytes32[4] sign) public returns (uint8){
        bytes memory args;
        address txOrigin;
        bool check;
        (txOrigin, check) = authCenter.check2WithSign(org, this, "unfreezePool", args, sign);
        require(check, "Forbidden unfreezePool");

        require(status == FROZEN_STATUS, "Current Status is NORMAL");
        status = NORMAL_STATUS;
        return status;
    }

    function checkAsset(address assetAddress) internal constant returns (bool){
        for (uint i = 0; i < assetList.length; i++) {
            if (assetList[i] == assetAddress) {
                return true;
            }
        }
        return false;
    }

    function checkStatus(uint8 status) internal constant returns (bool){
        for (uint i = 0; i < statusList.length; i++) {
            if (statusList[i].status == status) {
                return true;
            }
        }
        return false;
    }

    function genKey(string funtionName) internal view returns (bytes){
        bytes memory key;
        key = key.bytesAppend(address(this));
        return key;
    }

}

