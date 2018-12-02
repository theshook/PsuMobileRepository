package psurepository.edu.ph.psurepository;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import android.view.Menu;

public class MainActivity extends AppCompatActivity {
    public static final String FOLDER_NAME="FOLDER_NAME";
    public static final String FOLDER_ID="FOLDER_ID";
    public static final String FIRST_NAME="FIRST_NAME";
    public static final String LAST_NAME="LAST_NAME";
    public static final String ACCESS_AREA="ACCESS_AREA";
    private String UFN, ULN, access_area;
    // Getting Intents
    Intent intent;

    String txtfolderName;

    // Widgets
    FloatingActionButton fab_plus, fab_folder;
    Animation FabOpen, FabClose, FabClockwise, FabCounterClockwise;
    ListView listViewFolders;
    private ProgressDialog progressDialog;

    // List Data
    List<AreaFolders> areaFoldersList;

    // Used for animation
    boolean isOpen = false;

    DatabaseReference databaseAreaFolder;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Get intent
        intent = getIntent();
        UFN = intent.getStringExtra(FIRST_NAME);
        ULN = intent.getStringExtra(LAST_NAME);
        access_area = intent.getStringExtra(ACCESS_AREA);

        // Firebase
        auth = FirebaseAuth.getInstance();
        databaseAreaFolder = FirebaseDatabase.getInstance().getReference("areafolders");

        //get current user
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    // user auth state is changed - user is null
                    // launch login activity
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    finish();
                }
            }
        };

        // WIDGETS
        listViewFolders = (ListView) findViewById(R.id.listViewFolder);
        fab_plus = (FloatingActionButton) findViewById(R.id.fab_plus);
        fab_folder = (FloatingActionButton) findViewById(R.id.fab_folder);
        progressDialog = new ProgressDialog(this);

        //List Data Initialize
        areaFoldersList = new ArrayList<>();

        // FLOATING ACTION BUTTON ANIMATIONS
        FabOpen = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_open);
        FabClose = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_close);
        FabClockwise = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate_clockwise);
        FabCounterClockwise = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate_counter_clockwise);

        //OnClickListener
        fab_plusOnClick();
        fab_folderOnClick();
        listViewFolders_OnClick();
    }

    @Override
    protected void onStart() {
        super.onStart();
        progressDialog.show();
        progressDialog.setMessage("Loading . . .");
        Query foldersQuery = null;
        if (access_area.equals("Admin")) {
            foldersQuery = FirebaseDatabase.getInstance()
                    .getReference("areafolders")
                    .orderByChild("folder_name");
        } else {
            foldersQuery = FirebaseDatabase.getInstance()
                    .getReference("areafolders")
                    .orderByChild("folder_name")
                    .equalTo(access_area);
        }

        foldersQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                areaFoldersList.clear();
                for(DataSnapshot areafolderSnapshot : dataSnapshot.getChildren()) {
                    AreaFolders areaFolders = areafolderSnapshot.getValue(AreaFolders.class);
                    areaFoldersList.add(areaFolders);
                }

                AreaFoldersList areaFoldersListAdapter = new AreaFoldersList(MainActivity.this, areaFoldersList);
                listViewFolders.setAdapter(areaFoldersListAdapter);
                progressDialog.dismiss();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void listViewFolders_OnClick() {
        listViewFolders.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                AreaFolders areaFolders = areaFoldersList.get(position);
                Intent intent = new Intent(getApplicationContext(), SubFolderActivity.class);
                intent.putExtra(FOLDER_ID, areaFolders.getFolder_id());
                intent.putExtra(FOLDER_NAME, areaFolders.getFolder_name());
                intent.putExtra(FIRST_NAME, UFN);
                intent.putExtra(LAST_NAME, ULN);
                intent.putExtra(ACCESS_AREA, intent.getStringExtra(ACCESS_AREA));
                startActivity(intent);
            }
        });
    }
    private void fab_plusOnClick() {
        fab_plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isOpen) {
                    // Start Animation
                    fab_folder.startAnimation(FabClose);
                    fab_plus.startAnimation(FabCounterClockwise);

                    // Set Clickable to False
                    fab_folder.setClickable(false);

                    isOpen = false;
                } else {
                    // Start Animation
                    fab_folder.startAnimation(FabOpen);
                    fab_plus.startAnimation(FabClockwise);

                    // Set Clickable to True
                    fab_folder.setClickable(true);

                    isOpen = true;
                }
            }
        });
    }
    private void fab_folderOnClick() {
        fab_folder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder mBuilder = new AlertDialog.Builder(MainActivity.this);
                View mView = getLayoutInflater().inflate(R.layout.dialog_area_folder, null);
                final EditText mFolderName = (EditText) mView.findViewById(R.id.folderName);
                Button mCreateFolder = (Button) mView.findViewById(R.id.createFolder);

                // Show Dialog
                mBuilder.setView(mView);
                final AlertDialog dialogAreaFolder;
                dialogAreaFolder = mBuilder.create();
                dialogAreaFolder.show();

                // Button OnClickListener
                mCreateFolder.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!mFolderName.getText().toString().trim().isEmpty()) {
                            txtfolderName = mFolderName.getText().toString();

                            // Getting unique ID and will use as primary key for folders
                            String folderId = databaseAreaFolder.push().getKey();

                            Long tsLong = System.currentTimeMillis()/1000;
                            String ts = tsLong.toString();
                            String createdBy = intent.getStringExtra(LAST_NAME) + ", " + intent.getStringExtra(FIRST_NAME);
                            // Creating Object
                            AreaFolders AreaFoldersClass = new AreaFolders(folderId, txtfolderName, createdBy, ts);

                            // Saving Object
                            databaseAreaFolder.child(folderId).setValue(AreaFoldersClass);

                            Toast.makeText(MainActivity.this,
                                    "Succesfully Added.",
                                    Toast.LENGTH_SHORT).show();
                            dialogAreaFolder.dismiss();
                        } else {
                            Toast.makeText(MainActivity.this,
                                    "Kindly Fill necessary fields.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }

    // this listener will be called when there is change in firebase user session
    FirebaseAuth.AuthStateListener authListener = new FirebaseAuth.AuthStateListener() {
        @SuppressLint("SetTextI18n")
        @Override
        public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user == null) {
                // user auth state is changed - user is null
                // launch login activity
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                finish();
            }
        }


    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout:
              logout();
              break;
            case R.id.accounts:
                if (access_area.equals("Admin")) {
                    accounts();
                } else {
                    Toast.makeText(MainActivity.this,
                            "You don't have privileged to access this  area!",
                            Toast.LENGTH_SHORT).show();
                }
              break;
            default:
              return super.onOptionsItemSelected(item);
        }
      return false;
    }

    public void logout() {
        auth.signOut();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
    private void accounts() {
        intent = new Intent(getApplicationContext(), AccountActivity.class);
        intent.putExtra(FIRST_NAME, UFN);
        intent.putExtra(LAST_NAME, ULN);
        intent.putExtra(ACCESS_AREA, access_area);
        startActivity(intent);
    }
}
