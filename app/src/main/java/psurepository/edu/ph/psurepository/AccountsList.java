package psurepository.edu.ph.psurepository;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class AccountsList extends ArrayAdapter<User> {
  private Activity context;
  private List<User> userList;

  public AccountsList(Activity context, List<User> userList) {
    super(context, R.layout.user_list_item, userList);
    this.context = context;
    this.userList = userList;
  }

  @NonNull
  @Override
  public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
    LayoutInflater inflater = context.getLayoutInflater();

    View listViewItem = inflater.inflate(R.layout.user_list_item, null, true);

    TextView userFullname, userEmail, userArea, userTimeStamp;
    userFullname = listViewItem.findViewById(R.id.userFullName);
    userEmail = listViewItem.findViewById(R.id.userEmail);
    userArea = listViewItem.findViewById(R.id.userArea);
    userTimeStamp = listViewItem.findViewById(R.id.userTimeStamp);

    User user = userList.get(position);

    userFullname.setText("Name: " + user.getLastName() + ", " + user.getFirstName());
    userEmail.setText("Email: " + user.getEmail());
    userArea.setText("Access: " + user.getArea());
    if (user.getTimestamp() != null) {
      long timestamp = Long.parseLong(user.getTimestamp()) * 1000L;
      userTimeStamp.setText("Registered Date: " + getDate(timestamp));
    }

    return listViewItem;
  }

  private String getDate(long timeStamp){

    try{
      SimpleDateFormat sdf = new SimpleDateFormat("MMM-dd-yyyy");
      sdf.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
      Date netDate = (new Date(timeStamp));
      return sdf.format(netDate);
    }
    catch(Exception ex){
      return "xx";
    }
  }
}
