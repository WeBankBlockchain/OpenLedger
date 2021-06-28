pragma solidity ^0.4.25;
pragma experimental ABIEncoderV2;


contract INonFungibleAsset  {

    function getNoteNoByNoteId(uint256 noteId) public constant returns (uint256 noteNo);
}