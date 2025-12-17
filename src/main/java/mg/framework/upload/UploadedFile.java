package mg.framework.upload;

public class UploadedFile {
    private final String fieldName;
    private final String submittedFileName;
    private final String contentType;
    private final long size;
    private final byte[] bytes;

    public UploadedFile(String fieldName, String submittedFileName, String contentType, long size, byte[] bytes) {
        this.fieldName = fieldName;
        this.submittedFileName = submittedFileName;
        this.contentType = contentType;
        this.size = size;
        this.bytes = bytes;
    }

    public String getFieldName() { return fieldName; }
    public String getSubmittedFileName() { return submittedFileName; }
    public String getContentType() { return contentType; }
    public long getSize() { return size; }
    public byte[] getBytes() { return bytes; }
}
