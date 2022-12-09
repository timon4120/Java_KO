package Labs;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

public class Sort_Alg
{
    public static void swap (int [] a, int i, int j)
    {
        int t = a[i];
        a[i] = a[j];
        a[j] = t;
    }

    public static void Quick(int [] arr, int left_cor, int right_cor)
    {
        if(right_cor <= left_cor) {return ;}
        int i = left_cor - 1;
        int j = right_cor + 1;
        int ith = (left_cor+right_cor)/2;
        int pivot = arr[ith];

        while (true)
        {
            while(pivot > arr[++i]);
            while(pivot < arr[--j]);
            if (i <= j) {swap(arr,i,j);}
            else {break;}
        }
        if(j > left_cor) {Quick(arr, left_cor, j);}
        if(i < right_cor) {Quick(arr, i, right_cor);}
    }

    public static void Quick2(int [] arr, int left_cor, int right_cor, ExecutorService ex, int n)
    {
        if(right_cor <= left_cor) {return ;}
        int i = left_cor - 1;
        int j = right_cor + 1;
        int ith = (left_cor+right_cor)/2;
        int pivot = arr[ith];

        while (true)
        {
            while(pivot > arr[++i]);
            while(pivot < arr[--j]);
            if (i <= j) {swap(arr,i,j);}
            else {break;}
        }
        if(j > left_cor)
        {
            Runnable worker = new Exec(arr, left_cor, j);
            ex.execute(worker);
        }
        if(i < right_cor)
        {
            Runnable worker = new Exec(arr, i, right_cor);
            ex.execute(worker);
        }
    }

    public static class Exec implements Runnable
    {
        int [] arr;
        int left_cor,right_cor;
        public Exec(int [] arr, int left_cor, int right_cor)
        {
            this.arr = arr;
            this.left_cor = left_cor;
            this.right_cor = right_cor;
        }

        @Override
        public void run()
        {
            Quick(this.arr,this.left_cor,this.right_cor);
        }
    }

    public static void main(String[] args) throws InterruptedException
    {
        int cnt = 3000000;
        for (int i = 0; i < 20; i++)
        {
            int[] arrayy = IntStream.generate(() -> new Random().nextInt(100000)).limit(cnt).toArray();
            int n = arrayy.length;
//            System.out.println("Sequential Quick sorting averaged 20 times");
            int cores = Runtime.getRuntime().availableProcessors();
            long start = System.nanoTime();
            Quick(arrayy, 0, n-1);
            long stop = System.nanoTime();
            System.out.println(cnt + "," + (stop - start) / 1e9);
            arrayy = null;
            cnt += 3000000;

//            System.out.println("ThreadPool Quick sorting averaged 20 times");
//            long start2 = System.nanoTime();
//            ExecutorService ex = Executors.newFixedThreadPool(n);
//            Quick2(arrayy, 0, n - 1, ex, n);
//            ex.shutdown();
//            ex.awaitTermination(1, TimeUnit.MINUTES);
//            long stop2 = System.nanoTime();
//            System.out.println(cnt + "," + (stop2 - start2) / 1e9);
//            arrayy = null;
//            cnt += 3000000;
        }
    }
}
