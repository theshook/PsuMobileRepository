package psurepository.edu.ph.psurepository;

import android.content.Intent;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
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

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    public static final String FOLDER_NAME="FOLDER_NAME";
    public static final String FOLDER_ID="FOLDER_ID";

    String txtfolderName;

    // Widgets
    FloatingActionButton fab_plus, fab_folder;
    Animation FabOpen, FabClose, FabClockwise, FabCounterClockwise;
    ListView listViewFolders;

    // List Data
    List<AreaFolders> areaFoldersList;

    // Used for animation
    boolean isOpen = false;

    DatabaseReference databaseAreaFolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Firebase
        databaseAreaFolder = FirebaseDatabase.getInstance().getReference("areafolders");

        // WIDGETS
        listViewFolders = (ListView) findViewById(R.id.listViewFolder);
        fab_plus = (FloatingActionButton) findViewById(R.id.fab_plus);
        fab_folder = (FloatingActionButton) findViewById(R.id.fab_folder);

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

        Query foldersQuery = FirebaseDatabase.getInstance()
                .getReference("areafolders").orderByChild("folder_name");
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

                            // Creating Object
                            AreaFolders AreaFoldersClass = new AreaFolders(folderId, txtfolderName, ts);

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

}
