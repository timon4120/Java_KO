package Labs;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;



public class Mandelbrot4
{
    public static class Complex
    {
        private double realis,imaginalis;

        Complex(double real, double imagin)
        {
            this.realis = real;
            this.imaginalis = imagin;
        }
        void Update(double re, double im)
        {
            this.realis = re;
            this.imaginalis = im;
        }

        double Get_realis() {return this.realis;}
        double Get_imaginalis() {return this.imaginalis;}

        double Get_ABS() {return Math.sqrt(Math.pow(this.realis,2)+Math.pow(this.imaginalis,2));}
        double [] Get_power()
        {
            double re = Math.pow(this.realis,2) - Math.pow(this.imaginalis,2);
            double im = 2.0 * this.realis * this.imaginalis;
            return new double[] {re,im};
        }
    }

    public static class Start_Mandelbrot
    {
        public Start_Mandelbrot(){}

        public int Compute_N(Complex c, int iters)
        {
            Complex z_n = new Complex(0.0,0.0);
            int N = 0;
            while((z_n.Get_ABS() <= 2.0) && (N < iters))
            {
                double [] z_pow = z_n.Get_power();
                z_n.Update(z_pow[0] + c.Get_realis(),z_pow[1] + c.Get_imaginalis());
                N++;
            }
            return N;
        }

        public void Make_picture(int szer, int wys, double [] rogi, int max_it, String tit, BufferedImage im, int m,int k)
        {
            double x_min = rogi[0];
            double y_min = rogi[1];
            double x_max = rogi[2];
            double y_max = rogi[3];
//            BufferedImage image = new BufferedImage (szer, wys, BufferedImage.TYPE_INT_RGB); //Jak zrobię 4 osobne obrazki, to nic dziwnego, że generują się poprawnie.
            for(int i = 0; i < szer; i++)
            {
                for(int j = 0; j < wys; j++)
                {
                    double rea = x_min + ((double) i / szer) * (x_max - x_min);
                    double ima = y_min + ((double) j / wys) * (y_max - y_min);
                    Complex cc = new Complex(rea,ima);
                    int numb = this.Compute_N(cc, max_it);
                    float color = 255 - (int)(numb * 255 / max_it);
                    if(tit.equals("NULL"))
                    {
                        im.setRGB((int)(i+m*szer),(int)(j+k*wys), Color.HSBtoRGB(color,color,color));
                    }
                    else
                    {
                        im.setRGB((int)(i+m*szer),(int)j, Color.HSBtoRGB(color,color,color));
                    }
                }
            }
        }
    }

    public static class Exec implements Runnable
    {
        int x,y,N,i,k;
        double [] pics;
        String tit;
        BufferedImage im;
        double cnt = 0;
        public Exec(int x, int y, int N, double [] pics, String tit, BufferedImage im, int i, int k)
        {
            this.x = x;
            this.y = y;
            this.N = N;
            this.pics = pics;
            this.tit = tit;
            this.im = im;
            this.i = i;
            this.k = k;
        }
        @Override
        public void run()
        {
            Start_Mandelbrot Mandelbrot = new Start_Mandelbrot();
            Mandelbrot.Make_picture(this.x, this.y, this.pics, 500, this.tit, this.im, this.i, this.k);
        }
    }

    public static void Save(int [] pixels, double [] times, String tit)
    {
        try
        {
            FileWriter myWriter = new FileWriter("Pool_dane_" + tit + ".txt");
            for (int i = 0; i < times.length; i++)
            {
                myWriter.write(Math.round(pixels[i] * 100.0) / 100.0 + "," +Math.round(times[i] * 100.0) / 100.0 + "\n");
            }
            myWriter.close();
        }
        catch (IOException e) {e.printStackTrace(); }
    }

    public static double Repeat(int N, int cores, int x, int y, int block_size, String titt) throws InterruptedException
    {
        double cnt = 0;
        for (int m = 0; m < N; m++)
        {
            ExecutorService ex = Executors.newFixedThreadPool((x*y) / block_size);
            double [] part = new double[]{-2.2,-1.2, 0.6, 1.2};

            long start = System.nanoTime();
            BufferedImage image = new BufferedImage(x, y, BufferedImage.TYPE_INT_RGB);
            int number = (x*y) / block_size;
            if (block_size < y)
            {
                double dx = (part[2] - part[0]) / x;
                int YY = y / block_size;
                double dy = (part[3] - part[1]) / YY;
                for (int i = 0; i < x; i++)
                {
                    for (int j = 0; j < YY; j++)
                    {
                        Runnable worker = new Exec(1, block_size, 5, new double[]{part[0],part[1],part[0] + dx, part[1] + dy}, "NULL", image, i, j);
                        ex.execute(worker);
                        part[1] += dy;
                    }
                    part[1] = -1.2;
                    part[0] += dx;
                }
            }
            else if(block_size == y) //działa
            {
                double dx = (part[2] - part[0]) / x;
                for (int i = 0; i < x; i++)
                {
                    Runnable worker = new Exec(1, block_size, 5, new double[]{part[0],part[1],part[0] + dx, part[3]}, "Many_rys_" + (i+1) + ".bmp", image, i,0);
                    ex.execute(worker);
                    part[0] += dx;
                }
            }
            else //działa
            {
                int rozmiarx = block_size / y;
                double dx = (part[2] - part[0]) / number;
                for (int i = 0; i < number; i++)
                {
                    Runnable worker = new Exec(rozmiarx, y, 5, new double[]{part[0],part[1],part[0] + dx, part[3]}, "Many_rys_" + (i+1) + ".bmp", image, i,0);
                    ex.execute(worker);
                    part[0] += dx;
                }
            }
            ex.shutdown();
            ex.awaitTermination(1, TimeUnit.MINUTES);
            try {
                ImageIO.write(image, "png", new File(".\\Manys\\Many_rys_" + titt + "_" + x + ".png"));
            } catch (IOException e) {
                e.printStackTrace();
            }
            long stop = System.nanoTime();
            cnt += (stop - start)/1e9;
        }

        return cnt / N;
    }

    public static void main(String[] args) throws InterruptedException
    {
        int cores = Runtime.getRuntime().availableProcessors();
        int [] pics = new int []{32, 64, 128, 256, 512, 1024, 2048, 4096, 8192};
        double [] fin = new double[pics.length];
//        ExecutorService ex = Executors.newFixedThreadPool(cores*4);
        int [] block = {4, 8, 16, 32, 64, 128};
        int b = 5;
        for (int i = 0; i < pics.length; i++)
        {
            double time = Repeat(1, cores, pics[i], pics[i], block[b],"_" + block[b]);
            fin[i] = time;
            System.out.println("Rozmiar" + pics[i] + "_Z_" + block[b] + ": " + time);
        }
        Save(pics, fin, "Z_" + block[b]);
    }
}
