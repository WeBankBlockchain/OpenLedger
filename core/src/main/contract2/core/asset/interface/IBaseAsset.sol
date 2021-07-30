pragma solidity ^0.4.25;
pragma experimental ABIEncoderV2;

import "../../storage/AddressSetLib.sol";
import "../../govAccount/BaseAccountInterface.sol";

contract IBaseAsset {

    function isCustodian(address account) public view returns (bool);

    function containsAccount(address account) external view returns (bool);

    function boolFungible() public view returns(bool);
}
