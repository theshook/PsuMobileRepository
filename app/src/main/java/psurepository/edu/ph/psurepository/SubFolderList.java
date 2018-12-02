package psurepository.edu.ph.psurepository;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class SubFolderList extends ArrayAdapter<SubFolder> {
  private Activity context;
  private List<SubFolder> subFolderList;

  public SubFolderList(Activity context, List<SubFolder> subFolderList) {
    super(context, R.layout.areafolderslistitem, subFolderList);
    this.context = context;
    this.subFolderList = subFolderList;
  }

  @NonNull
  @Override
  public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
    LayoutInflater inflater = context.getLayoutInflater();

    View listViewItem = inflater.inflate(R.layout.areafolderslistitem, null, true);

    TextView txtFolderName = listViewItem.findViewById(R.id.textViewFolderName);
    ImageView img_url = listViewItem.findViewById(R.id.img_upload_image);
    ImageView img_default = listViewItem.findViewById(R.id.imageView2);
    TextView txtDate = listViewItem.findViewById(R.id.txtDate);
    TextView txtUpload = listViewItem.findViewById(R.id.txtUpload);

    SubFolder subFolder = subFolderList.get(position);

    txtFolderName.setText(subFolder.getSub_folder_name());

    if (subFolder.getCreatedBy() != null) {
      txtUpload.setText(subFolder.getCreatedBy());
    }

    if (subFolder.getTimestamp() != null) {
      long timestamp = Long.parseLong(subFolder.getTimestamp()) * 1000L;
      txtDate.setText(getDate(timestamp));
    }

    if (!txtFolderName.getText().toString().isEmpty()) {
      Picasso.get().load(R.drawable.ic_folder).into(img_default);
    } else {
      Picasso.get()
              .load(subFolder.getImage_upload())
              .resize(350, 350)
              .centerCrop()
              .into(img_url);
    }
    return listViewItem;
  }

  private String getDate(long timeStamp){

    try{
      SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy hh:mm:ssa");
      sdf.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
      Date netDate = (new Date(timeStamp));
      return sdf.format(netDate);
    }
    catch(Exception ex){
      return "xx";
    }
  }
}
