package com.example.pdfscanner;

import java.util.ArrayList;

public class QuickSort<T> {

    ArrayList<T> array;
    java.util.Comparator<? super  T> c;
    int high;
    int low;


    public QuickSort(ArrayList<T> arrayList, java.util.Comparator<? super  T> c){
        this.array = arrayList;
        this.c = c;
        this.high = arrayList.size() - 1;
        this.low = 0;
    }

    public QuickSort(ArrayList<T> arrayList, int low, int high, java.util.Comparator < ? super T > c){
        this.array = arrayList;
        this.c = c;
        this.high = high;
        this.low = low;
    }

    public void sort(){
        qsort(this.array, this.low, this.high, this.c);
    }

    public ArrayList<T> getArrayList() {
        return array;
    }

    void qsort (ArrayList < T > array,int low, int high, java.
            util.Comparator<? super T> c){
        if (low < high) {
            int p = partition(array, low, high, c);
            qsort(array, low, p - 1, c);
            qsort(array, p + 1, high, c);
        }
    }

    void swap_array_list (ArrayList <T> array, int pos1, int pos2){
        T temp = array.get(pos1);
        array.set(pos1, array.get(pos2));
        array.set(pos2, temp);
    }

    int partition (ArrayList < T > array,int low, int high, java.
            util.Comparator<? super T> c){
        T pivot = array.get(high);
        int index = low - 1;
        for (int jndex = low; jndex < high; jndex++) {
            try {
                if (c.compare(array.get(jndex), pivot) <= 0) {
                    index++;
                    swap_array_list(array, index, jndex);
                }
            } catch (NullPointerException | ClassCastException e) {
                e.printStackTrace();
            }
        }
        swap_array_list(array, index + 1, high);
        return index + 1;
    }

}