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

public class AreaFoldersList extends ArrayAdapter<AreaFolders> {

    private Activity context;
    private List<AreaFolders> areaFoldersList;

    public AreaFoldersList(Activity context, List<AreaFolders> areaFoldersList) {
        super(context, R.layout.areafolderslistitem, areaFoldersList);
        this.context = context;
        this.areaFoldersList = areaFoldersList;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();

        View listViewItem = inflater.inflate(R.layout.areafolderslistitem, null, true);

        TextView txtFolderName = listViewItem.findViewById(R.id.textViewFolderName);
        ImageView img_default = listViewItem.findViewById(R.id.imageView2);
        TextView txtDate = listViewItem.findViewById(R.id.txtDate);

        AreaFolders areaFolders = areaFoldersList.get(position);
        txtFolderName.setText(areaFolders.getFolder_name());
        Picasso.get().load(R.drawable.ic_folder).into(img_default);

        if (areaFolders.getTimestamp() != null) {
            long timestamp = Long.parseLong(areaFolders.getTimestamp()) * 1000L;
            txtDate.setText(getDate(timestamp));
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
