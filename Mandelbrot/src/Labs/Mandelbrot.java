package Labs;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.Math;

public class Mandelbrot
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


        public void Make_picture(int szer, int wys, double [] rogi, int max_it)
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
                    image.setRGB((int)i,(int)j, Color.HSBtoRGB(color,color,color));

                }
            }
            try {ImageIO.write(image, "bmp", new File("XD.bmp"));}
            catch (IOException e) {e.printStackTrace();}
            System.out.println("The End");
        }

        public void Make_picture(int szer, int wys)
        {
            Make_picture(szer,wys,new double[]{-2.2, -1.2, 0.7, 1.2},800);
        }
    }

    public static void main(String[] args)
    {
        Start_Mandelbrot Mandelbrot = new Start_Mandelbrot();
        Mandelbrot.Make_picture(5000, 2700);
    }
}
