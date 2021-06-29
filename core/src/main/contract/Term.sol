pragma solidity ^0.4.25;

import "./Sequence.sol";
import "./lib/SignLib.sol";
/** term of project */
contract Term {
    using SignLib for bytes32[4];

    Sequence seq;
    uint current; //currentTermNo
    string name;
    uint startSeq;
    struct Range {
        string termname;
        uint start;
        uint end;
    }

    address owner;

    mapping(uint => Range) terms; //term [startno, endno]映射，theNumberAtWhichEachTermBeginsAndEnds。

    constructor(address projectOwner) {
        seq = new Sequence();
        owner = projectOwner;
    }

    function newTerm(string termname, bytes32[4] sign) public returns (uint) {//新term
        address txOrigin;
        txOrigin = sign.checkSign();
        require(owner == txOrigin, "Term:only project owner is authorized.");
        require(bytes(termname).length > 0 && bytes(termname).length < 64, "termname  should be not null and less than 64 long");

        endCurTerm();
        current = current + 1;
        name = termname;
        startSeq = seq.get();
        terms[current] = Range(termname, startSeq, startSeq);
        return current;
    }

    function getTerm() public view returns (uint, string){
        return (current, name);
    }


    function getSeqNo() public returns (uint) {
        return seq.get();
    }

    // endsTheCurrentPaymentDays
    function endCurTerm() private {
        uint end_seq = seq.getCurrent();
        if (current > 1) {
            terms[current] = Range(terms[current].termname, terms[current].start, end_seq);
        }
    }
    // getsTheAddressOfTheCurrentContract
    function getAddress() public view returns (address) {
        return address(this);
    }
}
