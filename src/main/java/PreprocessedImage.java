import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

public class PreprocessedImage extends Image {
    public PreprocessedImage(String filename, String initialFolder, String finalFolder, String extension) {
        super(filename, initialFolder, finalFolder, extension);
    }
    public File preprocessImage(int whiteRatio) {
//        this.tuneContrast(2);
        this.reduceNoise(whiteRatio);
        return new File(this.finalFolder + this.filename);
    }

    private void tuneContrast(int alpha) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        Mat src = Imgcodecs.imread(this.initialFolder + this.filename, Imgcodecs.IMREAD_COLOR);
        Mat dest = new Mat(src.rows(), src.cols(), src.type());
        src.convertTo(dest, -1, alpha, 0);
        Imgcodecs.imwrite(this.finalFolder + this.filename, dest);
    }

    private void reduceNoise(int whiteRatio) {
        BufferedImage img;
        File f = new File(this.initialFolder + this.filename);

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

        f = new File(this.finalFolder + this.filename);
        try {
            ImageIO.write(img, this.extension, f);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
