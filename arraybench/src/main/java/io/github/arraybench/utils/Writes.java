package io.github.arraybench.utils;

import java.util.ArrayList;
import java.util.Arrays;

public class Writes {
    public void resetStatistics() {}
    public void changeAuxWrites(int value) {}
    public void changeWrites(int value) {}
    public void changeAllocAmount(int value) {}
    public void clearAllocAmount() {}
    public void changeReversals(int value) {}

    public void swap(int[] array, int a, int b, double pause, boolean mark, boolean auxwrite) {
        int tmp = array[a];
        array[a] = array[b];
        array[b] = tmp;
    }

    public void multiSwap(int[] array, int pos, int to, double sleep, boolean mark, boolean auxwrite) {
        if (to - pos > 0) {
            for (int i = pos; i < to; i++) {
                swap(array, i, i + 1, 0, mark, auxwrite);
            }
        } else {
            for (int i = pos; i > to; i--) {
                swap(array, i, i - 1, 0, mark, auxwrite);
            }
        }
    }

    public void reversal(int[] array, int start, int length, double sleep, boolean mark, boolean auxwrite) {
        while (start < length) {
            swap(array, start++, length--, sleep, mark, auxwrite);
        }
    }

    public void write(int[] array, int at, int equals, double pause, boolean mark, boolean auxwrite) {
        array[at] = equals;
    }

    public <T> void write(T[] array, int at, T equals, double pause, boolean mark) {
        array[at] = equals;
    }

    public void visualClear(int[] array, int index) {}
    public void visualClear(int[] array, int index, double delay) {}

    public void multiDimWrite(int[][] array, int x, int y, int equals, double pause, boolean mark, boolean auxwrite) {
        array[x][y] = equals;
    }

    public <T> void multiDimWrite(T[][] array, int x, int y, T equals, double pause, boolean mark) {
        array[x][y] = equals;
    }

    public void mockWrite(int length, int pos, int val, double pause) {}

    public void transcribe(int[] array, ArrayList<Integer>[] registers, int start, boolean mark, boolean auxwrite) {
        int total = start;
        for (ArrayList<Integer> register : registers) {
            for (Integer x : register) {
                array[total++] = x;
            }
            register.clear();
        }
    }

    public void transcribeMSD(int[] array, ArrayList<Integer>[] registers, int start, int min, double sleep, boolean mark, boolean auxwrite) {
        int total = start;
        for (ArrayList<Integer> register : registers) {
            for (Integer x : register) {
                array[total++] = x;
            }
            register.clear();
        }
    }

    public void fancyTranscribe(int[] array, int length, ArrayList<Integer>[] registers, double sleep) {
        int total = 0;
        for (ArrayList<Integer> register : registers) {
            for (Integer x : register) {
                array[total++] = x;
            }
            register.clear();
        }
    }

    public void arraycopy(int[] src, int srcPos, int[] dest, int destPos, int length, double sleep, boolean mark, boolean aux) {
        System.arraycopy(src, srcPos, dest, destPos, length);
    }

    public int[] copyOfArray(int[] original, int newLength) {
        return Arrays.copyOf(original, newLength);
    }

    public int[] copyOfRangeArray(int[] original, int from, int to) {
        return Arrays.copyOfRange(original, from, to);
    }

    public void reversearraycopy(int[] src, int srcPos, int[] dest, int destPos, int length, double sleep, boolean mark, boolean aux) {
        System.arraycopy(src, srcPos, dest, destPos, length);
    }

    public int[] createExternalArray(int length) {
        return new int[length];
    }

    public void deleteExternalArray(int[] array) {}

    public void arrayListAdd(ArrayList<Integer> aList, int value) {
        aList.add(value);
    }

    public void arrayListAdd(ArrayList<Integer> aList, int value, boolean mockWrite, double sleep) {
        aList.add(value);
    }

    public void arrayListRemoveAt(ArrayList<Integer> aList, int index) {
        aList.remove(index);
    }

    public void deleteArrayList(ArrayList<Integer> aList) {}
    public void deleteExternalArray(ArrayList<Integer>[] array) {}
    public void addTime(long milliseconds) {}
    public void setTime(long milliseconds) {}
    public void startLap() {}
    public void stopLap() {}
}
