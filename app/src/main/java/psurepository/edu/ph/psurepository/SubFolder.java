package psurepository.edu.ph.psurepository;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class SubFolder {
    String sub_folder_id;
    String sub_folder_name;
    String img_url;
    String timestamp;


    public SubFolder() {

    }

    public SubFolder(String sub_folder_id, String sub_folder_name, String img_url, String timestamp) {
        this.sub_folder_id = sub_folder_id;
        this.sub_folder_name = sub_folder_name;
        this.img_url = img_url;
        this.timestamp = timestamp;
    }

    public String getSub_folder_id() {
        return sub_folder_id;
    }

    public String getSub_folder_name() {
        return sub_folder_name;
    }

    public String getImage_upload() {
        return img_url;
    }

    public String getTimestamp() {
        return timestamp;
    }
}
