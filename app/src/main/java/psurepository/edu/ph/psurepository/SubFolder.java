package psurepository.edu.ph.psurepository;

public class SubFolder {
    String sub_folder_id;
    String sub_folder_name;
    String img_url;
    String timestamp;
    String createdBy;
    String img_id;

    public SubFolder() {

    }

    public SubFolder(String sub_folder_id, String sub_folder_name, String img_url, String timestamp, String createdBy, String img_id) {
        this.sub_folder_id = sub_folder_id;
        this.sub_folder_name = sub_folder_name;
        this.img_url = img_url;
        this.timestamp = timestamp;
        this.createdBy  = createdBy;
        this.img_id = img_id;
    }

    public String getSub_folder_id() {
        return sub_folder_id;
    }

    public String getSub_folder_name() {
        return sub_folder_name;
    }

    public String getImg_url() {
        return img_url;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public String getImg_id() {
        return img_id;
    }
}
