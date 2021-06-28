pragma solidity ^0.4.25;

import "./LibWeightMap.sol";

contract WEBasicAuth {
    address public  _owner;

    event LogSetOwner(address indexed owner, address indexed oldOwner, address indexed contractAddress);

    constructor() public {
        _owner = msg.sender;
        emit LogSetOwner(msg.sender, address(0), this);
    }

    function setOwner(address owner)
    public
    onlyOwner
    {
        _owner = owner;
        emit LogSetOwner(owner, _owner, this);
    }

    modifier onlyOwner() {
        require(msg.sender == _owner, "WEBasicAuth: only owner is authorized.");
        _;
    }
}

contract WEVoteRequest is WEBasicAuth {
    uint256 private _id;
    uint16 private _threshold;
    mapping(address => bool) _sigMapping;
    uint16 private  _weight;
    // 0-init, 1-approved
    uint8 private _status;
    uint8 private _txType;
    address public _requestAddress;

    event LogApprove(address indexed src, uint16 indexed weight, address contractAddress);
    event LogUnapprove(address indexed src, uint16 indexed weight, address contractAddress);
    event LogSetStatus(uint8 indexed status, address indexed contractAddress);

    constructor(uint256 id, uint8 txType, address requestAddress, uint16 threshold) public {
        _id = id;
        _threshold = threshold;
        _weight = 0;
        _status = 0;
        _txType = txType;
        _requestAddress = requestAddress;
    }

    function getVoteRequest()
    public
    view
    returns (uint256, address, uint16, address, uint16, uint8, uint8)
    {
        return (_id, _requestAddress, _threshold, this, _weight, _txType, _status);
    }

    function getApprovement(address src) public view returns (bool){
        return _sigMapping[src];
    }

    function getTxType() public view returns (uint8, address) {
        return (_txType, _requestAddress);
    }

    function approve(address src, uint16 weight) public onlyOwner returns (bool, uint16){
        bool b = _sigMapping[src];
        if (b == false) {
            _weight = _weight + weight;
            _sigMapping[src] = true;
        } else {
            return (false, _weight);
        }
        emit LogApprove(src, weight, this);
        if (_weight >= _threshold) {
            setStatus(1);
        }
        return (true, _weight);
    }

    function setStatus(uint8 status) public onlyOwner {
        emit LogSetStatus(status, this);
        _status = status;
    }

    function canCall(
        uint256 id
    ) public view returns (bool) {
        return _weight >= _threshold;
    }
}

contract WEVoteWeight is WEBasicAuth {
    using LibWeightMap for LibWeightMap.Map;
    LibWeightMap.Map  _addressWeightMap;
    uint16 public _threshold;

    event LogSetWeight(address indexed who, uint16 weight, address contractAddress);

    function setThreshold(uint16 threshold) public onlyOwner returns (bool) {
        _threshold = threshold;
        return true;
    }

    function setWeight(address who, uint16 weight) public onlyOwner returns (bool) {
        _addressWeightMap.put(who, weight);
        emit LogSetWeight(who, weight, this);
        return true;
    }

    function setVoteWeight(address[] whos, uint16 weight, uint16 threshold) public onlyOwner returns (bool) {
        require(whos.length > 0, "Illegal whos");
        require(weight > 0, "Illegal weight!");
        _threshold = threshold;
        for (uint i = 0; i < whos.length; i++) {
            _addressWeightMap.put(whos[i], weight);
            emit LogSetWeight(whos[i], weight, this);
        }
        return true;
    }

    function setBoardWeight(address[] whos, uint16[] weights, uint16 threshold) public onlyOwner returns (bool) {
        require(whos.length > 0 && whos.length == weights.length, "Illegal arrays");
        _threshold = threshold;
        for (uint i = 0; i < whos.length; i++) {
            require(weights[i] > 0, "Illegal weight!");
            _addressWeightMap.put(whos[i], weights[i]);
            emit LogSetWeight(whos[i], weights[i], this);
        }
        return true;
    }

    function getWeight(address who)
    public
    view
    returns (uint16, uint16)
    {
        return (_addressWeightMap.getValue(who), _threshold);
    }

    function getVoteWeight() public view
    returns (uint16, address[], uint16[])
    {
        (address[] memory a, uint16[] memory u) = _addressWeightMap.getMap();
        return (_threshold, a, u);
    }

}


contract WEBoardVoteGuard {
    uint256 _currentId;
    WEVoteWeight public _voteWeight;
    mapping(uint256 => address) _requestMapping;

    event LogSetVoteWeight(address indexed voteWeight, address contractAddress);
    event LogRegister(uint256  indexed id, uint8 indexed txType, address indexed requestAddress, uint16 threshold, address contractAddress);
    event LogUnregister(uint256  indexed id, bool indexed result, address contractAddress);
    event LogApprove(uint256  indexed id, address indexed who, bool b, address contractAddress);
    event LogUnapprove(uint256  indexed id, address indexed who, bool b, address contractAddress);
    event LogCanCall(uint256  indexed id, address indexed requestAddress, address contractAddress);


    constructor() public {
        _currentId = 100;
        _voteWeight = new WEVoteWeight();
        emit LogSetVoteWeight(_voteWeight, this);
    }

    function setThreshold(uint16 threshold) internal {
        _voteWeight.setThreshold(threshold);
    }

    function setWeight(address who, uint16 weight) internal {
        _voteWeight.setWeight(who, weight);
    }

    function getWeight(address who) public view returns (uint16, uint16) {
        return _voteWeight.getWeight(who);
    }

    function setVoteWeight(address voteWeight) internal {
        _voteWeight = WEVoteWeight(voteWeight);
        emit LogSetVoteWeight(voteWeight, this);
    }

    function getVoteWeight()
    public
    view
    returns (address)
    {
        return _voteWeight;
    }

    function getRequestInfo(uint256 id) public view
    returns (uint256, address, uint16, address, uint16, uint8, uint8) {
        WEVoteRequest voteRequest = WEVoteRequest(_requestMapping[id]);
        return voteRequest.getVoteRequest();
    }

    function getRequestTxType(uint256 id) public view
    returns (uint8, address) {
        WEVoteRequest voteRequest = WEVoteRequest(_requestMapping[id]);
        return voteRequest.getTxType();
    }

    function register(uint8 txType, address requestAddress) internal returns (WEVoteRequest, uint256) {
        _currentId++;
        return (register(_currentId, txType, requestAddress), _currentId);
    }

    function register(uint256 id, uint8 txType, address requestAddress) internal returns (WEVoteRequest) {
        if (_requestMapping[id] != 0x00) {
            WEVoteRequest req = WEVoteRequest(_requestMapping[id]);
            return req;
        }
        uint16 threshold = _voteWeight._threshold();
        WEVoteRequest voteRequest = new WEVoteRequest(id, txType, requestAddress, threshold);
        _requestMapping[id] = voteRequest;
        emit LogRegister(id, txType, requestAddress, threshold, this);
        return voteRequest;
    }

    function unregister(uint256 id) internal returns (bool) {
        bool r = true;
        if (_requestMapping[id] == 0x00) {
            r = false;
        } else {
            WEVoteRequest voteRequest = WEVoteRequest(_requestMapping[id]);
            //set the request status: done.
            voteRequest.setStatus(2);
            _requestMapping[id] = 0x00;

        }
        emit LogUnregister(id, r, this);
        return r;
    }

    function approve(uint256 id) internal returns (bool) {
        address who = msg.sender;
        (uint16 weight, uint16 t) = _voteWeight.getWeight(who);
        bool b = true;
        uint16 u = 0;
        if (weight == 0) {
            b = false;
        } else {
            address a = _requestMapping[id];
            if (a == 0x00) {
                b = false;
            } else {
                WEVoteRequest voteRequest = WEVoteRequest(a);
                //emit LogRegister(id, weight, who );
                (b, u) = voteRequest.approve(who, weight);
            }
        }
        emit LogApprove(id, who, b, this);
        return b;
    }


    function canCall(
        uint256 id
    ) internal view returns (bool) {
        if (_requestMapping[id] == 0x00) {
            return false;
        } else {
            WEVoteRequest voteRequest = WEVoteRequest(_requestMapping[id]);
            bool b = voteRequest.canCall(id);
            if (b == true) {
                emit LogCanCall(id, _requestMapping[id], this);
            }
            return b;
        }
    }

}

contract SingletonVoter is WEBoardVoteGuard {

    mapping(uint8 => uint256) _txTypeReq;


    modifier preRegister(uint8 txType){
        require(_txTypeReq[txType] == 0, "request already active");
        (WEVoteRequest _, uint256 id) = super.register(txType, msg.sender);
        require(id > 0, "Register id failed");
        _txTypeReq[txType] = id;
        _;
    }

    modifier canExecute(uint8 txType){
        require(_txTypeReq[txType] != 0);
        require(super.canCall(_txTypeReq[txType]));
        _;
        require(super.unregister(_txTypeReq[txType]));
        delete _txTypeReq[txType];
    }

    event ApproveSingle(uint8 indexed txType);

    function approveSingleImpl(uint8 txType) internal {
        require(_txTypeReq[txType] != 0);
        require(super.approve(_txTypeReq[txType]));
        emit ApproveSingle(txType);
    }

    event DeleteSingle(uint8 txType);

    function deleteSingleImpl(uint8 txType) internal {
        require(_txTypeReq[txType] != 0);
        require(super.unregister(_txTypeReq[txType]));
        delete _txTypeReq[txType];
        emit DeleteSingle(txType);
    }

    function getRequestSingle(uint8 txType) public view returns (uint256, address, uint16, address, uint16, uint8, uint8) {
        require(_txTypeReq[txType] != 0);
        return super.getRequestInfo(_txTypeReq[txType]);
    }

}