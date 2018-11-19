package psurepository.edu.ph.psurepository;

public class AreaFolders {
    String folder_id;
    String folder_name;
    String timestamp;

    public AreaFolders() {

    }

    public AreaFolders(String folder_id, String folder_name, String timestamp) {
        this.folder_id = folder_id;
        this.folder_name = folder_name;
        this.timestamp = timestamp;
    }

    public String getFolder_id() {
        return folder_id;
    }

    public String getFolder_name() {
        return folder_name;
    }

    public String getTimestamp() {
        return timestamp;
    }
}
