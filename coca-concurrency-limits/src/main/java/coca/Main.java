package coca;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;

public class Main {
    public static void main(String[] args) {
        ByteBuffer buffer = ByteBuffer.allocate(64).order(ByteOrder.nativeOrder());
        buffer.put("Helldd;9.13\n".getBytes(Charset.defaultCharset()));
        buffer.flip();

        int pos = 0;
        long value = buffer.getLong(pos);
        long valueSepMark = valueSepMark(value);
        SimpleHashMap simpleHashMap = new SimpleHashMap();
        if (valueSepMark != 0) {
            ByteBuffer buf = ByteBuffer.allocate(16);
            buf.putLong(value);
            System.out.println("String value: " + new String(buf.array()));
            int tailBits = tailBits(valueSepMark);
            pos += valueOffset(tailBits);
            System.out.println("pos is " + pos);
            byte[] cityBuffer = new byte[128];
            byte lastPositionByte = '\n';
            boolean negative = false;
            long dValue = 0;
            System.arraycopy(buffer.array(), 0, cityBuffer, 0, pos-1);
            System.out.println(new String(cityBuffer,Charset.defaultCharset()));
            buffer.position(pos);
            while (buffer.hasRemaining()) {
                byte positionByte = buffer.get();
                if (positionByte == '\r' || positionByte == '\n') {
                    lastPositionByte = positionByte;
                    break;
                }
                else if (positionByte == '-') {
                    negative = true;
                }
                else if (positionByte != '.') {
                    int digit = positionByte - 48;
                    dValue = dValue * 10 + digit;
                }
            }

            if (negative) {
                dValue = -dValue;
            }
            System.out.println(dValue);
        }


    }
    static class SimpleHashMap {
        // 100-byte key + 4-byte hash + 4-byte size +
        // 2-byte min + 2-byte max + 8-byte sum + 8-byte count
        private static final int KEY_SIZE = 128;
        private static final int CAPACITY = 1024 * 1024;
        private static final int INDEX_MASK = CAPACITY - 1;
        private static final int OFF_COUNT = 0;
        private static final int OFF_SUM = 1;
        private static final int OFF_MIN = 2;
        private static final int OFF_MAX = 3;
        private final int[] index;
        private final long[] values;

        public SimpleHashMap() {
            index = new int[CAPACITY];
            values = new long[CAPACITY * KEY_SIZE];
        }
        public void put(ByteBuffer buf, int hash, int value){
            int idx = hash & INDEX_MASK;

        }

    }
    private static long valueSepMark(long keyLong) {
        // Seen this trick used in multiple other solutions.
        // Nice breakdown here: https://graphics.stanford.edu/~seander/bithacks.html#ZeroInWord
        long match = keyLong ^ 0x3B3B3B3B_3B3B3B3BL; // 3B == ';'
        match = (match - 0x01010101_01010101L) & (~match & 0x80808080_80808080L);
        return match;
    }
    private static int tailBits(long valueSepMark) {
        return Long.numberOfTrailingZeros(valueSepMark >>> 7);
    }
    private static int valueOffset(int tailBits) {
        return (int) (tailBits >>> 3) + 1;
    }
//    private static long tailAndLen(int tailBits, long keyLong, long keyLen) {
//        long tailMask = ~(-1L << tailBits);
//        long tail = keyLong & tailMask;
//        return (tail << 8) | ((keyLen >> 3) & 0xFF);
//    }
//
//    private static int decimalSepMark(long value) {
//        // Seen this trick used in multiple other solutions.
//        // Looks like the original author is @merykitty.
//
//        // The 4th binary digit of the ascii of a digit is 1 while
//        // that of the '.' is 0. This finds the decimal separator
//        // The value can be 12, 20, 28
//        return Long.numberOfTrailingZeros(~value & 0x10101000);
//    }
//    private static int nextKeyOffset(int decimalSepMark) {
//        return (decimalSepMark >>> 3) + 3;
//    }
//    private static int decimalValue(int decimalSepMark, long value) {
//        // Seen this trick used in multiple other solutions.
//        // Looks like the original author is @merykitty.
//
//        int shift = 28 - decimalSepMark;
//        // signed is -1 if negative, 0 otherwise
//        long signed = (~value << 59) >> 63;
//        long designMask = ~(signed & 0xFF);
//        // Align the number to a specific position and transform the ascii code
//        // to actual digit value in each byte
//        long digits = ((value & designMask) << shift) & 0x0F000F0F00L;
//
//        // Now digits is in the form 0xUU00TTHH00 (UU: units digit, TT: tens digit, HH: hundreds digit)
//        // 0xUU00TTHH00 * (100 * 0x1000000 + 10 * 0x10000 + 1) =
//        // 0x000000UU00TTHH00 +
//        // 0x00UU00TTHH000000 * 10 +
//        // 0xUU00TTHH00000000 * 100
//        // Now TT * 100 has 2 trailing zeroes and HH * 100 + TT * 10 + UU < 0x400
//        // This results in our value lies in the bit 32 to 41 of this product
//        // That was close :)
//        long absValue = ((digits * 0x640a0001) >>> 32) & 0x3FF;
//        return (int) ((absValue ^ signed) - signed);
//    }
}