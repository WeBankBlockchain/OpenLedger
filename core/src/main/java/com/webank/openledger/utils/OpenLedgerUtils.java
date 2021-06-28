/*
 *   Copyright (C) @2021 Webank Group Holding Limited
 *   <p>
 *   Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at
 *  <p>
 *  http://www.apache.org/licenses/LICENSE-2.0
 *   <p>
 *   Unless required by applicable law or agreed to in writing, software distributed under the License
 *   is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 *  or implied. See the License for the specific language governing permissions and limitations under
 *  he License.
 *
 */

package com.webank.openledger.utils;

import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.Security;
import java.util.ArrayList;
import java.util.List;

import com.webank.openledger.core.exception.OpenLedgerBaseException;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec;
import org.fisco.bcos.sdk.abi.datatypes.Address;
import org.fisco.bcos.sdk.crypto.CryptoSuite;
import org.fisco.bcos.sdk.crypto.hash.Keccak256;
import org.fisco.bcos.sdk.crypto.keypair.CryptoKeyPair;
import org.fisco.bcos.sdk.crypto.signature.ECDSASignatureResult;
import org.fisco.bcos.sdk.model.CryptoType;

import static com.webank.openledger.core.constant.OpenLedgerConstant.ALGORITHM;
import static com.webank.openledger.core.constant.OpenLedgerConstant.CURVE_TYPE;
import static com.webank.openledger.core.constant.OpenLedgerConstant.HEX_HEADER;
import static com.webank.openledger.core.constant.OpenLedgerConstant.PATH_SEPARATOR;
import static com.webank.openledger.core.constant.OpenLedgerConstant.PRIVATE_KEY_DESC;
import static com.webank.openledger.core.constant.OpenLedgerConstant.PRIVATE_KEY_SUFFIX;
import static com.webank.openledger.core.constant.OpenLedgerConstant.PUBLIC_KEY_DESC;
import static com.webank.openledger.core.constant.OpenLedgerConstant.PUBLIC_KEY_SUFFIX;


@Slf4j
public class OpenLedgerUtils {
    public static CryptoSuite ecdsaCryptoSuite = new CryptoSuite(CryptoType.ECDSA_TYPE);
    ;

    /**
     * @param filePath output pem file path.
     * @throws OpenLedgerBaseException OpenLedgerBaseException
     */
    public static void genPemFile(String filePath) throws OpenLedgerBaseException {
        if (StringUtils.isBlank(filePath)) {
            throw new OpenLedgerBaseException("");
        }
        try {
            BouncyCastleProvider prov = new BouncyCastleProvider();
            Security.addProvider(prov);

            ECNamedCurveParameterSpec ecSpec = ECNamedCurveTable.getParameterSpec(CURVE_TYPE);
            KeyPairGenerator generator = KeyPairGenerator.getInstance(ALGORITHM, prov.getName());
            generator.initialize(ecSpec, new SecureRandom());
            KeyPair pair = generator.generateKeyPair();
            String pubKey = pair.getPublic().toString();
            String account = HEX_HEADER + pubKey.substring(pubKey.indexOf("[") + 1, pubKey.indexOf("]")).replace(":", "");

            PemFile privatePemFile = new PemFile(pair.getPrivate(), PRIVATE_KEY_DESC);
            PemFile publicPemFile = new PemFile(pair.getPublic(), PUBLIC_KEY_DESC);


            System.out.println(filePath + PATH_SEPARATOR + account + PRIVATE_KEY_SUFFIX);
            privatePemFile.write(filePath + PATH_SEPARATOR + account + PRIVATE_KEY_SUFFIX);
            publicPemFile.write(filePath + PATH_SEPARATOR + account + PUBLIC_KEY_SUFFIX);
        } catch (IOException | NoSuchProviderException | NoSuchAlgorithmException | InvalidAlgorithmParameterException e) {
            log.error("generate pem file error");
            throw new OpenLedgerBaseException("ErrorCode.FILE_GEN_PEM_BC_FAILED");
        }
    }


    public static List<byte[]> convertSignToByte(byte[] message, ECDSASignatureResult signatureResult) {
        List<byte[]> byteList = new ArrayList<>();
        byte[] vbyte = OpenLedgerUtils.getBytes32(BigInteger.valueOf(signatureResult.getV() + 27).toByteArray());
        byte[] r = signatureResult.getR();
        byte[] s = signatureResult.getS();
        byteList.add(message);
        byteList.add(vbyte);
        byteList.add(r);
        byteList.add(s);
        return byteList;
    }


    public static ECDSASignatureResult sign(CryptoKeyPair cryptoKeyPair, byte[] message) {
        // sign with secp256k1
        ECDSASignatureResult signatureResult =
                (ECDSASignatureResult)
                        ecdsaCryptoSuite.sign(
                                message, cryptoKeyPair);
        return signatureResult;
    }

    public static byte[] computeKeccak256Hash(byte[] buffer) {
        Keccak256 keccak256 = new Keccak256();
        return keccak256.hash(buffer);
    }

    public static byte[] computeKeccak256Hash(String buffer) {
        Keccak256 keccak256 = new Keccak256();
        return keccak256.hash(buffer).getBytes();
    }

    public static byte[] computeKeccak256HashFromAddress(String address) {
        Keccak256 keccak256 = new Keccak256();
        return keccak256.hash(convertStringToAddressByte(address));
    }

    public static byte[] computeKeccak256HashFromBigInteger(BigInteger num) {
        Keccak256 keccak256 = new Keccak256();
        return keccak256.hash(OpenLedgerUtils.getBytes32(num.toByteArray()));
    }


    public static byte[] concatByte(byte[]... params) {
        int length_byte = 0;
        for (int i = 0; i < params.length; i++) {
            length_byte += params[i].length;
        }
        byte[] all_byte = new byte[length_byte];
        int countLength = 0;
        for (int i = 0; i < params.length; i++) {
            byte[] b = params[i];
            System.arraycopy(b, 0, all_byte, countLength, b.length);
            countLength += b.length;
        }
        return all_byte;
    }

    public static byte[] concatByte(List<byte[]> bytesList) {
        int length_byte = 0;
        for (int i = 0; i < bytesList.size(); i++) {
            length_byte += bytesList.get(i).length;
        }
        byte[] all_byte = new byte[length_byte];
        int countLength = 0;
        for (int i = 0; i < bytesList.size(); i++) {
            byte[] b = bytesList.get(i);
            System.arraycopy(b, 0, all_byte, countLength, b.length);
            countLength += b.length;
        }
        return all_byte;
    }


    /**
     * convertByteToHexadecimal
     *
     * @param bytes
     * @return
     */
    public static String byte2Hex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        String temp = null;
        for (byte aByte : bytes) {
            temp = Integer.toHexString(aByte & 0xFF);
            if (temp.length() == 1) {
                // 1得到一位的进行补0操作
                sb.append("0");
            }
            sb.append(temp);
        }
        return sb.toString();
    }

    /**
     * get address hash byte
     *
     * @param addressString
     * @return
     */
    public static byte[] convertStringToAddressByte(String addressString) {
        Address address = new Address(addressString);
        byte[] addressIntBytes = address.toUint160().getValue().toByteArray();

        byte[] addressBytes = new byte[20];
        for (int i = 0; i < addressBytes.length; i++) {
            if (addressIntBytes.length == 20) {
                addressBytes[i] = addressIntBytes[i];
            } else if (Address.DEFAULT.equals(address)) {
                addressBytes[i] = (byte) 0;
            } else {
                addressBytes[i] = addressIntBytes[i + 1];
            }

        }
        return addressBytes;
    }

    /**
     * hexStringToByteArray
     *
     * @param inHex theHexStringToBeConverted
     * @return theResultOfTheConvertedByteArray
     */
    public static byte[] hexToByteArray(String inHex) {
        int hexlen = inHex.length();
        byte[] result;
        if (hexlen % 2 == 1) {
            //奇数
            hexlen++;
            result = new byte[(hexlen / 2)];
            inHex = "0" + inHex;
        } else {
            //偶数
            result = new byte[(hexlen / 2)];
        }
        int j = 0;
        for (int i = 0; i < hexlen; i += 2) {
            result[j] = hexToByte(inHex.substring(i, i + 2));
            j++;
        }
        return result;
    }

    /**
     * hexStringToByte
     *
     * @param inHex theHexStringToBeConverted
     * @return theConvertedByte
     */
    public static byte hexToByte(String inHex) {
        return (byte) Integer.parseInt(inHex, 16);
    }


    /**
     * Bytes [] to 32 bytes[] is typically used for conversions to uint, uint256 for solidity contracts
     *  e.g:
     * BigInteger amount;
     * OpenLedgerUtils.getBytes32(amount.toByteArray())
     *
     * @param test
     * @return
     */
    public static byte[] getBytes32(byte[] test) {
        int p = 32 - test.length % 32; // p：看最后要补多少位
        byte[] ret = new byte[test.length + p]; // ret：新的 32倍数组
        for (int i = 0; i < p; i++) {
            ret[i] = (byte) 0;
        }
        System.arraycopy(test, 0, ret, p, ret.length - p); // 原内容拷到新数组

        return ret;
    }
}
