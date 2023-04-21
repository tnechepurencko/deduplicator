import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

import java.io.File;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        try {
            Database database = new Database();
        } catch (SQLException e) {
            Database.printSQLException(e);
            System.exit(0);
        }
        Tesseract tesseract = prepareTesseract();

        String initialFolder = "pictures/";
        String finalFolder = "preprocessed_pictures/";
        String extension = "jpg";

        File folder = new File(initialFolder);
        File[] listOfFiles = folder.listFiles();

        for (File file : listOfFiles) {
            if (file.isFile()) {
                try {
                    String gstin = processImage(file, tesseract, initialFolder, finalFolder, extension);
                    if (gstin != null) {
                        gstin = formatGSTIN(gstin);
                        System.out.println("Formatted GSTIN:\t" + gstin);
                    }

                } catch (TesseractException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public static Tesseract prepareTesseract() {
        Tesseract tesseract = new Tesseract();
        tesseract.setDatapath("tessdata_best-main");
        tesseract.setLanguage("eng");
        tesseract.setPageSegMode(1);
        tesseract.setOcrEngineMode(1);
        return tesseract;
    }

    public static String processImage(File file, Tesseract tesseract, String initialFolder, String finalFolder, String extension) throws TesseractException {
        System.out.println(file.getName());
        for (int i = 10000; i < 100000; i += 10000) {
            file = PreprocessedImage.preprocessedImage(file.getName(), initialFolder, finalFolder, extension, i);
            String result = tesseract.doOCR(file);
            if (result.contains("GSTIN")) {
                List<String> gstin = result.lines().filter(line -> line.contains("GSTIN")).toList();
                return gstin.get(0);
//            } else if (result.contains("GST")) {
//                result.lines().filter(line -> line.contains("GST")).forEach(System.out::println);
            }
        }
        System.out.printf("GSTIN not found in %s%n", file.getName());
        return null;
    }

    public static String formatGSTIN(String gstin) {
        return removeNonAlphanumeric(Arrays.stream(gstin.split("GSTIN")).toList().get(1));
    }

    public static String removeNonAlphanumeric(String str) {
        return str.replaceAll("[^a-zA-Z0-9]", "");
    }
}

