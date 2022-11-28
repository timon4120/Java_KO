package Labs;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Mandelbrot2
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

        public void Make_picture(int szer, int wys, double [] rogi, int max_it, String tit, BufferedImage im, int m)
        {
            double x_min = rogi[0];
            double y_min = rogi[1];
            double x_max = rogi[2];
            double y_max = rogi[3];
            BufferedImage image = new BufferedImage (szer, wys, BufferedImage.TYPE_INT_RGB);
            for(int i = 0; i < szer; i++)
            {
                for(int j = 0; j < wys; j++)
                {
                    double rea = x_min + ((double) i / szer) * (x_max - x_min);
                    double ima = y_min + ((double) j / wys) * (y_max - y_min);
                    Complex cc = new Complex(rea,ima);
                    int numb = this.Compute_N(cc, max_it);
                    float color = 255 - (int)(numb * 255 / max_it);
                    im.setRGB((int)(i+m*szer),(int)j, Color.HSBtoRGB(color,color,color));
                } //tutaj coś ewidentnie nie działa, bo wątki walczą o dostęp do ustawiania RGB i psują się wzajemnie.
            }
//            try {ImageIO.write(image, "bmp", new File(tit));}
//            catch (IOException e) {e.printStackTrace();}
        }
    }

    public static class Exec extends Thread
    {
        int x,y,N,i;
        double [] pics;
        String tit;
        double cnt;
        BufferedImage im;
        public Exec(int x, int y, int N, double [] pics, String tit, BufferedImage im, int i)
        {
            this.x = x;
            this.y = y;
            this.N = N;
            this.pics = pics;
            this.tit = tit;
            this.im = im;
            this.i = i;
        }
        @Override
        public void run()
        {
            Start_Mandelbrot Mandelbrot = new Start_Mandelbrot();
            Mandelbrot.Make_picture(this.x, this.y, this.pics, 500, this.tit, this.im, this.i);
        }
    }

    public static void Save(int [] pixels, double [] times)
    {
        try
        {
            FileWriter myWriter = new FileWriter("dane.txt");
            for (int i = 0; i < times.length; i++)
            {
                myWriter.write(Math.round(pixels[i] * 100.0) / 100.0 + "," +Math.round(times[i] * 100.0) / 100.0 + "\n");
            }
            myWriter.close();
        }
        catch (IOException e) {e.printStackTrace(); }
    }

    public static double Repeat(int N, int cores, int x, int y, Exec [] exs) throws InterruptedException
    {
        double cnt = 0;
        for (int m = 0; m < N; m++)
        {
            double [] part = new double[]{-2.2,-1.2, 0.6, 1.2};
            double dx = (part[2] - part[0]) / cores;
            long start = System.nanoTime();
            BufferedImage image = new BufferedImage(x, y, BufferedImage.TYPE_INT_RGB);
            for (int i = 0; i < cores; i++) {
                exs[i] = new Exec(x / cores, y, 5, new double[]{part[0], part[1], part[0] + dx, part[3]}, "Rys_" + (i + 1) + ".bmp", image, i);
                exs[i].start();
                part[0] += dx;
            }
            try {
                ImageIO.write(image, "png", new File("Elo_" + x + ".png"));
            } catch (IOException e) {
                e.printStackTrace();
            }
            for (Exec e : exs)
                e.join();
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
        Exec [] exs = new Exec[cores];
        for (int i = 0; i < pics.length; i++)
        {
            double time = Repeat(10, cores, pics[i],pics[i], exs);
            fin[i] = time;
        }
        Save(pics,fin);
    }
}
