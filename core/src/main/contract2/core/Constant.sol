pragma solidity ^0.4.24;
pragma experimental ABIEncoderV2;


contract Constant {
    bytes public TYPE_PUBLIC = "public"; //all can read, cannot write
    bytes public TYPE_OWNER = "owner";  //owner can read, cannot write
    bytes public TYPE_ADMIN = "admin";  //admin can read, can write

    bytes public MODE_R = "_r";
    bytes public MODE_W = "_w";

    bytes public KEY_ALL = "all";
    bytes public ID = "id";
    bytes public ADDR = "addr";
    bytes public TYPE = "type";
    bytes public ROLE = "role";
    bytes public PUB_KEY = "pub_key";
    bytes public DATA = "data";

    bytes public ROLE_R = "role_r";
    bytes public ROLE_W = "role_w";


    bytes public OPENING_BANK = "opening_bank";
    bytes public ASSETS = "assets";
    bytes public BALANCE = "balance";
    bytes public RECORD = "record";


    bytes public ADMIN = "admin";
    bytes public OPERATOR = "operator";
    bytes public OWNER = "owner";
    bytes public USER = "user";

    address public ADMIN_ADDR = 0x10001;
    address public OPERATOR_ADDR = 0x10002;
    address public OWNER_ADDR = 0x10003;
    address public USER_ADDR = 0x10004;
}