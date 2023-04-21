import net.sourceforge.tess4j.TesseractException;
import org.apache.commons.lang3.tuple.Pair;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;

/**
 * The principle of work:
 * 1. The pictures from the 'pictures/' repo go through preprocessing: there is a debugging of shadows on the image,
 * and the transformation of the picture into black and white. Preprocessed pictures are saved to the
 * 'preprocessed_pictures/' repo.
 * 2. The pictures from the 'preprocessed_pictures/' repo pass text recognition where the parameters 'GSTIN'
 * and 'total amount' passes to the database as unique terms of a receipt (Ideally, the time and date should also
 * be stored in the database, but the text recognizer has huge problems with them).
 * 3. If 'GSTIN' and 'total amount' of the receipt is already stored in the database, then the current receipt
 * is a duplicate.
 */
public class Main {
    public static void main(String[] args) {
        Database database = null;
        try {
            database = new Database();
        } catch (SQLException e) {
            Database.printSQLException(e);
            System.exit(0);
        }

        String initialFolder = "pictures/";
        String finalFolder = "preprocessed_pictures/";
        String extension = "jpg";

        try {
            Files.createDirectories(Paths.get(finalFolder));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        File folder = new File(initialFolder);
        File[] listOfFiles = folder.listFiles();
        if (listOfFiles != null) {
            for (File file : listOfFiles) {
                if (file.isFile()) {
                    try {
                        ProcessedImage image = new ProcessedImage(file.getName(), initialFolder, finalFolder, extension);
                        Pair<String, String> pair = image.processImage();
                        if (pair != null) {
                            String gstin = pair.getLeft();
                            String total = pair.getRight();

                            System.out.println("GSTIN:\t" + gstin);
                            System.out.println("TOTAL:\t" + total);

                            if (database.notDuplicate(gstin, total)) {
                                try {
                                    database.insertReceipt(gstin, total, initialFolder + file.getName());
                                } catch (SQLException e) {
                                    Database.printSQLException(e);
                                }
                            } else {
                                System.out.println("This receipt already exists in the database!");
                            }
                        }
                    } catch (TesseractException e) {
                        throw new RuntimeException(e);
                    } catch (SQLException e) {
                        Database.printSQLException(e);
                        System.exit(0);
                    }
                }
            }
        }
    }
}

