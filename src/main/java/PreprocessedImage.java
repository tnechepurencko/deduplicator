// Java program to demonstrate colored
// to red colored image conversion

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;


public class PreprocessedImage {
    public static File preprocessedImage(String filename, String initialFolder, String finalFolder, String extension, int whiteRatio) {
//        tuneContrast(filename, initialFolder, finalFolder);
        reduceNoise(filename, initialFolder, finalFolder, extension, whiteRatio);
        return new File(finalFolder + filename);
    }

    private static void tuneContrast(String filename, String initialFolder, String finalFolder) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        Mat src = Imgcodecs.imread(initialFolder + filename, Imgcodecs.IMREAD_COLOR);
        Mat dest = new Mat(src.rows(), src.cols(), src.type());
        src.convertTo(dest, -1, 0.5, 0);
        Imgcodecs.imwrite(finalFolder + filename, dest);
    }

    private static void reduceNoise(String filename, String initialFolder, String finalFolder, String extension, int whiteRatio) {
        BufferedImage img;
        File f = new File(initialFolder + filename);

        try {
            img = ImageIO.read(f);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        int width = img.getWidth();
        int height = img.getHeight();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int p = img.getRGB(x, y);

                int a = (p >> 24) & 0xff;
                int r = (p >> 16) & 0xff;
                int g = (p >> 8) & 0xff;
                int b = p & 0xff;

                int value = (r * r) + (g * g) + (b * b);
                if (value > whiteRatio) {
                    r = 255;
                    g = 255;
                    b = 255;
                }
                else {
                    r = 0;
                    g = 0;
                    b = 0;
                }

                p = (a << 24) | (r << 16) | (g << 8) | b;
                img.setRGB(x, y, p);
            }
        }

        f = new File(finalFolder + filename);
        try {
            ImageIO.write(img, extension, f);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
