package psurepository.edu.ph.psurepository;

public class Uploads {
  private String sub_folder_id;
  private String sub_folder_name;
  private String img_url;
  private String timestamp;

  public Uploads() {

  }

  public Uploads(String sub_folder_id, String sub_folder_name, String img_url, String timestamp) {
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

  public String getImg_url() {
    return img_url;
  }

  public void setSub_folder_id(String sub_folder_id) {
    this.sub_folder_id = sub_folder_id;
  }

  public void setSub_folder_name(String sub_folder_name) {
    this.sub_folder_name = sub_folder_name;
  }

  public void setImg_url(String img_url) {
    this.img_url = img_url;
  }

  public void setTimestamp(String timestamp) {
    this.timestamp = timestamp;
  }

  public String getTimestamp() {

    return timestamp;
  }
}
