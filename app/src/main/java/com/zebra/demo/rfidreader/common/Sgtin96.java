package com.zebra.demo.rfidreader.common;

import android.util.Log;

import java.math.BigInteger;

public class Sgtin96 {
    public static final Integer BIN = 2;
    public static final Integer HEX = 16;
    public static final String HEADER = "00110000";
    //public static final Integer Sgtin96LengthBits = 96;//original
    public static final Integer Sgtin96LengthBits = 496;
    //public static final Integer Sgtin96LengthHex = 24;
    public static final Integer Sgtin96LengthHex = 124;

    public enum Partition {
        SIX(20, 24), SEVEN(24, 20), EIGHT(27, 17), NINE(30, 14),
        TEN(34, 10), ELEVEN(37, 7), TWELVE(40, 4);

        private final int companyPrefixBits;
        private final int itemReferenceIndicatorBits;

        private Partition(int companyPrefixBits, int itemReferenceIndicatorBits) {
            this.companyPrefixBits = companyPrefixBits;
            this.itemReferenceIndicatorBits = itemReferenceIndicatorBits;
        }

        public int getCompanyPrefixLength() {
            return ordinal() + 6;
        }

        public int getPartitionValue() {
            return 6- ordinal();
        }

        public int getCompanyPrefixBits() {
            return companyPrefixBits;
        }

        public int getItemReferenceIndicatorBits() {
            return itemReferenceIndicatorBits;
        }

        public static Partition getByCompanyPrefixLength(int companyPrefixLength) {
            if (companyPrefixLength < 6 || companyPrefixLength > 12) {
                throw new IllegalArgumentException("Invalid company prefix length.");
                // or return null?
            }
            return values()[companyPrefixLength - 6];
        }

        public static Partition getByPartitionValue(int partitionValue) {
            if (partitionValue < 0 || partitionValue > 6) {
                throw new IllegalArgumentException("Invalid partition value.");
            }

            return values()[6 - partitionValue];
        }


    }

    private static String zeroFill(String s, int n) {
        int fill = n - s.length();
        String Zeroes = "";
        if (fill > 0) {
            Zeroes = new String(new char[fill]).replace("\0", "0");
        }
        return Zeroes + s;
    }

    private static String binaryToHex(String bin) {
        // bin_epc is 96 bits, so need to use BitInt for hex conversion
        return new BigInteger(bin, BIN).toString(HEX);
    }

    private static String hexToBinary(String hex) {
        return new BigInteger(hex, HEX).toString(BIN);
    }

    private static Long binaryToLong(String bin) {
        return Long.parseLong(bin, BIN);
    }

    private static Integer binaryToInt(String bin) {
        return Integer.parseInt(bin, BIN);
    }

    public static String decode(String sgtin96_epc) {
        Integer filterValue, partitionValue,
                companyPrefixBits, companyPrefixLength,
                itemReferenceBits, itemReferenceLength;
        Integer itemReferenceValue;
        Long companyPrefixValue, serialNumber;
        String companyPrefixFinalValue, itemReferenceFinalValue;

        if (sgtin96_epc.length() != Sgtin96LengthHex) {
            throw new IllegalArgumentException("EPC must be 24 characters long");
        }
        String binary = zeroFill(hexToBinary(sgtin96_epc), Sgtin96LengthBits);
        
        String header = binary.substring(0, 8);
        if(!header.equals(HEADER))
            throw new IllegalArgumentException("EPC header doesn't match with SGTIN-96 header");

        filterValue = binaryToInt(binary.substring(8, 11));
       
        partitionValue = binaryToInt(binary.substring(11, 14));
        
        if (partitionValue > 6) {
            throw new IllegalArgumentException("Partition value cannot be greater than 6");
        }
        companyPrefixBits = Partition.getByPartitionValue(partitionValue).getCompanyPrefixBits();
        companyPrefixLength = Partition.getByPartitionValue(partitionValue).getCompanyPrefixLength();
       
        companyPrefixValue = binaryToLong(binary.substring(14, 14 + companyPrefixBits));
       
        if (companyPrefixLength >= Math.pow(10, companyPrefixLength)) {
            throw new IllegalArgumentException("Company Prefix exceeded specified length");
        }
        companyPrefixFinalValue = zeroFill(companyPrefixValue.toString(), companyPrefixLength);
       
        itemReferenceLength = 13 - companyPrefixLength;
       
        itemReferenceValue = binaryToInt(binary.substring(14 + companyPrefixBits, 58));
        if (itemReferenceValue >= Math.pow(10, itemReferenceLength)) {
            throw new IllegalArgumentException("Item Reference and Indicator exceeded specified length");
        }
        itemReferenceFinalValue = zeroFill(itemReferenceValue.toString(), itemReferenceLength);
       
        serialNumber = binaryToLong(binary.substring(58));

        String rv = "urn:epc:tag:sgtin-96 : "
                + filterValue+"."
                +companyPrefixFinalValue+"."
                +itemReferenceFinalValue+"."
                +serialNumber;
        return rv;
    }


}

