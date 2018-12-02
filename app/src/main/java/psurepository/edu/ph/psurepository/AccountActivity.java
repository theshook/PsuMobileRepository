package psurepository.edu.ph.psurepository;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AccountActivity extends AppCompatActivity {
  public static final String FIRST_NAME="FIRST_NAME";
  public static final String LAST_NAME="LAST_NAME";
  public static final String ACCESS_AREA="ACCESS_AREA";
  private String UFN, ULN, access_area;

  // Getting Intents
  private Intent intent;

  // Widgets
  private ListView usersListView;
  private ProgressDialog progressDialog;

  // List Data
  private List<User> userList;

  // Database
  private DatabaseReference databaseUsers;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_account);

    // Get intent
    intent = getIntent();

    // Full name of current user
    UFN = intent.getStringExtra(FIRST_NAME);
    ULN = intent.getStringExtra(LAST_NAME);
    access_area = intent.getStringExtra(ACCESS_AREA);

    // Database
    databaseUsers = FirebaseDatabase.getInstance().getReference("users");

    usersListView = findViewById(R.id.userList);
    progressDialog = new ProgressDialog(this);

    //List Data Initialize
    userList = new ArrayList<>();
  }

  @Override
  protected void onStart() {
    super.onStart();
    progressDialog.show();
    progressDialog.setMessage("Fetching data . . .");

    Query usersQuery = FirebaseDatabase.getInstance()
            .getReference("users").orderByChild("lastName");
    usersQuery.addValueEventListener(new ValueEventListener() {
      @Override
      public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
        userList.clear();
        for(DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
          User user = userSnapshot.getValue(User.class);
          userList.add(user);
        }

        AccountsList accountsListAdapater = new AccountsList(AccountActivity.this, userList);
        usersListView.setAdapter(accountsListAdapater);
        progressDialog.dismiss();
      }
      @Override
      public void onCancelled(@NonNull DatabaseError databaseError) {

      }
    });
  }

  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.accounts_menu, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.createAccount:
        createAccount();
        break;
      case R.id.areaFolders:
        viewAreaFolders();
        break;
      default:
        return super.onOptionsItemSelected(item);
    }
    return false;
  }

  private void viewAreaFolders() {
    intent = new Intent(getApplicationContext(), MainActivity.class);
    intent.putExtra(FIRST_NAME, UFN);
    intent.putExtra(LAST_NAME, ULN);
    intent.putExtra(ACCESS_AREA, access_area);
    startActivity(intent);
    finish();
  }

  private void createAccount() {
    startActivity(new Intent(this, CreateAccountActivity.class));
  }
}
