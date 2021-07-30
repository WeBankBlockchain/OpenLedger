pragma solidity ^0.4.24;
pragma experimental ABIEncoderV2;


contract History
{

    struct Record {
        bytes cmd;
        bytes value;
    }

    mapping(bytes => Record[]) history;

    function addHistory(bytes cmd, bytes key, bytes value) public {

        Record[] records = history[key];
        Record r;
        r.cmd = cmd;
        r.value = value;
        records.push(r);
    }

    function getHistory(bytes key) public constant returns (bytes[] memory results) {
        Record[] records = history[key];
        uint length = records.length;
        if (length <= 0) {
            return;
        }

        results = new bytes[](length * 2);
        for (uint i = 0; i < length; i++) {
            results[i * 2] = records[i].cmd;
            results[i * 2 + 1] = records[i].value;
        }
    }

}
