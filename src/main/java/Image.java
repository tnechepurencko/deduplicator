public class Image {
    protected final String filename;
    protected final String initialFolder;
    protected final String finalFolder;
    protected final String extension;

    public Image(String filename, String initialFolder, String finalFolder, String extension) {
        this.filename = filename;
        this.initialFolder = initialFolder;
        this.finalFolder = finalFolder;
        this.extension = extension;
    }
}
