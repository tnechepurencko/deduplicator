import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProcessedImage extends Image {
    private static final Tesseract tesseract = prepareTesseract();

    public ProcessedImage(String filename, String initialFolder, String finalFolder, String extension) {
        super(filename, initialFolder, finalFolder, extension);
    }

    public static Tesseract prepareTesseract() {
        Tesseract tesseract = new Tesseract();
        tesseract.setDatapath("tessdata_best-main");
        tesseract.setLanguage("eng");
        tesseract.setPageSegMode(1);
        tesseract.setOcrEngineMode(1);
        return tesseract;
    }

    public Pair<String, String> processImage() throws TesseractException {
        System.out.println("\n" + this.filename);

        String gstinFound = null;
        String totalFound = null;
        String result;

        PreprocessedImage image = new PreprocessedImage(this.filename, this.initialFolder, this.finalFolder, this.extension);
        for (int i = 10000; i < 100000; i += 10000) {
            File file = image.preprocessImage(i);
            result = tesseract.doOCR(file).toLowerCase();

            String updGSTIN = this.updateGSTIN(gstinFound, result);
            if (updGSTIN != null) {
                gstinFound = updGSTIN;
            }

            String updTotal = this.updateTotal(totalFound, result);
            if (updTotal != null) {
                totalFound = updTotal;
            }

            if (gstinFound != null && totalFound != null && Double.parseDouble(totalFound) != 0) {
                return new MutablePair<>(gstinFound, totalFound);
            }
        }

        if (gstinFound != null && totalFound != null) {
            return new MutablePair<>(gstinFound, totalFound);
        }

        System.out.printf("BAD PICTURE '%s': error in text recognition%n", this.filename);
        return null;
    }

    private String updateGSTIN(String gstinFound, String result) {
        if (result.contains("gstin") && gstinFound == null) {
            List<String> gstin = result.lines().filter(line -> line.contains("gstin")).toList();
            return formatGSTIN(gstin.get(0));
        }
        return null;
    }

    private String updateTotal(String totalFound, String result) {
        String updTotal = findTotal(totalFound, result, "total");
        updTotal = findTotal(updTotal, result, "cash");
        updTotal = findTotal(updTotal, result, "net");
        return updTotal;
    }

    private String findTotal(String totalFound, String result, String naming) {
        if (result.contains(naming) && (totalFound == null || Double.parseDouble(totalFound) == 0)) {
            List<String> total = result.lines().filter(line -> line.contains(naming)).toList();
            return formatTotal(total.get(0));
        }
        return totalFound;
    }

    public static String formatGSTIN(String gstin) {
        return removeNonAlphanumeric(Arrays.stream(gstin.split("gstin")).toList().get(1));
    }

    public static String formatTotal(String total) {
        List<String> decimalNumsFound = findDecimalNums(total);
        if (decimalNumsFound.size() == 0) {
            return null;
        }

        for (String dn : decimalNumsFound) {
            if (dn.contains(".")) {
                return dn;
            }
        }

        return null;
    }

    public static List<String> findDecimalNums(String stringToSearch) {
        Pattern decimalNumPattern = Pattern.compile("-?\\d+(\\.\\d+)?");
        Matcher matcher = decimalNumPattern.matcher(stringToSearch);

        List<String> decimalNumList = new ArrayList<>();
        while (matcher.find()) {
            decimalNumList.add(matcher.group());
        }

        return decimalNumList;
    }

    public static String removeNonAlphanumeric(String str) {
        return str.replaceAll("[^a-zA-Z0-9]", "");
    }
}
