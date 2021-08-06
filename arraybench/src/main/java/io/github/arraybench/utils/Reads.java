package io.github.arraybench.utils;

public class Reads {
    public void resetStatistics() {}
    public void addComparison() {}
    public long getComparisons() { return 0; }
    public void setComparisons(long value) {}

    public int compareValues(int left, int right) {
        return Integer.compare(left, right);
    }

    public int compareOriginalValues(int left, int right) {
        return Integer.compare(left, right);
    }

    public int compareIndices(int[] array, int left, int right, double sleep, boolean mark) {
        return Integer.compare(array[left], array[right]);
    }

    public int compareOriginalIndices(int[] array, int left, int right, double sleep, boolean mark) {
        return Integer.compare(array[left], array[right]);
    }

    public int compareIndexValue(int[] array, int index, int value, double sleep, boolean mark) {
        return Integer.compare(array[index], value);
    }

    public int compareOriginalIndexValue(int[] array, int index, int value, double sleep, boolean mark) {
        return Integer.compare(array[index], value);
    }

    public int compareValueIndex(int[] array, int value, int index, double sleep, boolean mark) {
        return Integer.compare(value, array[index]);
    }

    public int compareOriginalValueIndex(int[] array, int value, int index, double sleep, boolean mark) {
        return Integer.compare(value, array[index]);
    }

    public int analyzeMax(int[] array, int length, double sleep, boolean mark) {
        int max = Integer.MIN_VALUE;
        for (int i = 0; i < length; i++) {
            int val = array[i];
            if (val > max) max = val;
        }
        return max;
    }

    public int analyzeMin(int[] array, int length, double sleep, boolean mark) {
        int min = Integer.MAX_VALUE;
        for (int i = 0; i < length; i++) {
            int val = array[i];
            if (val < min) min = val;
        }
        return min;
    }

    public int analyzeMaxLog(int[] array, int length, int base, double sleep, boolean mark) {
        int max = analyzeMax(array, length, sleep, mark);
        return (int)(Math.log(max) / Math.log(base));
    }

    public int analyzeMaxCeilingLog(int[] array, int length, int base, double sleep, boolean mark) { // These do the same thing?
        int max = analyzeMax(array, length, sleep, mark);
        return (int)(Math.log(max) / Math.log(base));
    }

    public int analyzeBit(int[] array, int length) {
        int max = Integer.MIN_VALUE;
        for (int i = 0; i < length; i++) {
            int val = array[i];
            if (val > max) max = val;
        }
        return 31 - Integer.numberOfLeadingZeros(max);
    }

    public int getDigit(int a, int power, int radix) {
        return (int)(a / Math.pow(radix, power)) % radix;
    }

    public boolean getBit(int n, int k) {
        return ((n >> k) & 1) == 1;
    }
}
