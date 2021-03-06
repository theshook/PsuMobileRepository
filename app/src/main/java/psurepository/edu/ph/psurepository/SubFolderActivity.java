package psurepository.edu.ph.psurepository;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class SubFolderActivity extends AppCompatActivity {
  public static final String FOLDER_NAME="FOLDER_NAME";
  public static final String FOLDER_ID="FOLDER_ID";
  public static final String FIRST_NAME="FIRST_NAME";
  public static final String LAST_NAME="LAST_NAME";
  public static final String ACCESS_AREA="ACCESS_AREA";

  private String USER_FIRSTNAME, USER_LASTNAME;

    // Widgets
    TextView breadcrumbs;
    FloatingActionButton fab_plus, fab_camera, fab_folder;
    Animation FabOpen, FabClose, FabClockwise, FabCounterClockwise;
    ListView listViewSubFolders;
    EditText edtSearch;
    Button btnSearch;

    // Used for animation
    boolean isOpen = false;

    // List Data
    List<SubFolder> subFolderList;
    List<Uploads> uploadsList;

    // Database Reference
    DatabaseReference databaseSubFolder, databaseUpload;

    // Storage Reference
    StorageReference mStorage;

    // PROGRESS DIALOG
    private ProgressDialog mProgress;

    // CAMERA REQUEST CODE
    private static final int CAMERA_REQUEST_CODE = 1;
    private static final int RequestPermissionCode = 7;

    String txtfolderName;
    String folder_id;
    String folder_name;
    String imageUrl;

    String currentImagePath = null;
    Uri imageUri;
    private File getImageFile() throws IOException {
      String timeStamp = new SimpleDateFormat("yyyymmdd_HHmmss").format(new Date());
      String imageName = "jpg_"+timeStamp+"_";
      File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

      File imageFile = File.createTempFile(imageName, ".jpg", storageDir);
      currentImagePath = imageFile.getAbsolutePath();

      return imageFile;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub_folder);

        // WIDGETS
        breadcrumbs = (TextView) findViewById(R.id.breadcrumbs);
        listViewSubFolders = (ListView) findViewById(R.id.listViewSubFolder);
        fab_plus = (FloatingActionButton) findViewById(R.id.fab_plus);
        fab_camera = (FloatingActionButton) findViewById(R.id.fab_camera);
        fab_folder = (FloatingActionButton) findViewById(R.id.fab_folder);
        edtSearch = findViewById(R.id.edtSearch);
        btnSearch = findViewById(R.id.btnSearch);

        // FLOATING ACTION BUTTON ANIMATIONS
        FabOpen = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_open);
        FabClose = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_close);
        FabClockwise = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate_clockwise);
        FabCounterClockwise = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate_counter_clockwise);

        // Getting Intents
        Intent intent = getIntent();
        folder_id = intent.getStringExtra(FOLDER_ID);
        folder_name = intent.getStringExtra(FOLDER_NAME);
        USER_FIRSTNAME = intent.getStringExtra(FIRST_NAME);
        USER_LASTNAME = intent.getStringExtra(LAST_NAME);
      Log.d("FULL NAME", "onCreate: " + USER_FIRSTNAME + USER_LASTNAME);
        // Firebase
        databaseSubFolder = FirebaseDatabase.getInstance().getReference("subfolders").child(folder_id);
        databaseUpload = FirebaseDatabase.getInstance().getReference("uploads").child(folder_id);
        mStorage = FirebaseStorage.getInstance().getReference();

        // Progress Dialog
        mProgress = new ProgressDialog(this);

        //List Data Initialize
        subFolderList = new ArrayList<>();
        uploadsList = new ArrayList<>();

        // Set breadcrumbs
        breadcrumbs.setText(folder_name);

        // Button Click Listeners
        fab_plusOnClick();
        fab_folderOnClick();
        listViewFolders_OnClick();
        fab_cameraOnClick();
        listViewFolders_OnLongClick();
        btnSearch_onClick();

      // Permissions
        isPermissionEnabled();

    }

    protected void onStart() {
    super.onStart();

      final Query foldersQuery = FirebaseDatabase.getInstance().getReference("subfolders").child(folder_id).orderByChild("sub_folder_name");
      foldersQuery.addValueEventListener(new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
          subFolderList.clear();
          for(DataSnapshot subfolderSnapshot : dataSnapshot.getChildren()) {
            SubFolder subFolder = subfolderSnapshot.getValue(SubFolder.class);
            subFolderList.add(subFolder);
          }
          SubFolderList areaFoldersListAdapter = new SubFolderList(SubFolderActivity.this, subFolderList);
          listViewSubFolders.setAdapter(areaFoldersListAdapter);

          Query dbUploads = databaseUpload.orderByKey();
          dbUploads.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
              for(DataSnapshot subfolderSnapshot : dataSnapshot.getChildren()) {
                SubFolder subFolder = subfolderSnapshot.getValue(SubFolder.class);
                subFolderList.add(subFolder);
              }
              SubFolderList uploadImageListAdapter = new SubFolderList(SubFolderActivity.this, subFolderList);
              listViewSubFolders.setAdapter(uploadImageListAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
          });
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
      });
  }

    private void listViewFolders_OnClick() {
    listViewSubFolders.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        SubFolder subFolder = subFolderList.get(position);

        String fold_name = subFolder.getSub_folder_name();
        if (fold_name != null) {
          Intent intent = new Intent(getApplicationContext(), SubFolderActivity.class);
          intent.putExtra(FOLDER_NAME, subFolder.getSub_folder_name());
          intent.putExtra(FOLDER_ID, subFolder.getSub_folder_id());
          startActivity(intent);
        } else {
          Toast.makeText(SubFolderActivity.this, "Another Activity!", Toast.LENGTH_SHORT).show();
        }
      }
    });
    }
    private void listViewFolders_OnLongClick() {
      listViewSubFolders.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
          SubFolder subFolder = subFolderList.get(position);

          if (subFolder.getSub_folder_name() != null) {
            folder_onLongPress(subFolder);
          } else {
            file_onLongPress(subFolder);
          }
          return true;
        }
      });
    }
    private void fab_plusOnClick() {
        fab_plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isOpen) {
                    // Start Animation
                    fab_camera.startAnimation(FabClose);
                    fab_folder.startAnimation(FabClose);
                    fab_plus.startAnimation(FabCounterClockwise);

                    // Set Clickable to False
                    fab_camera.setClickable(false);
                    fab_folder.setClickable(false);

                    isOpen = false;
                } else {
                    // Start Animation
                    fab_camera.startAnimation(FabOpen);
                    fab_folder.startAnimation(FabOpen);
                    fab_plus.startAnimation(FabClockwise);

                    // Set Clickable to True
                    fab_camera.setClickable(true);
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
                AlertDialog.Builder mBuilder = new AlertDialog.Builder(SubFolderActivity.this);
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
                            String folderId = databaseSubFolder.push().getKey();

                          Long tsLong = System.currentTimeMillis()/1000;
                          String ts = tsLong.toString();
                          String createdBy = USER_LASTNAME + ", " + USER_FIRSTNAME;
                            // Creating Object
                            SubFolder SubFolderClass = new SubFolder(folderId, txtfolderName, null, ts, createdBy, null);

                            // Saving Object
                            databaseSubFolder.child(folderId).setValue(SubFolderClass);

                            Toast.makeText(SubFolderActivity.this,"Succesfully Added.",Toast.LENGTH_SHORT).show();
                            dialogAreaFolder.dismiss();
                        } else {
                            Toast.makeText(SubFolderActivity.this,"Kindly Fill necessary fields.",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }
    private void fab_cameraOnClick() {
    fab_camera.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (intent.resolveActivity(getPackageManager()) != null) {
          File imageFile = null;

          try {
            imageFile = getImageFile();
          } catch (IOException e) {
            e.printStackTrace();
          }

          if (imageFile!=null) {
            imageUri = FileProvider.getUriForFile(
                    SubFolderActivity.this,
                    "psurepository.edu.ph.psurepository",
                                imageFile);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            startActivityForResult(intent, CAMERA_REQUEST_CODE);
          }
        }
      }
    });
  }
    private void btnSearch_onClick() {
        btnSearch.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            String search = edtSearch.getText().toString();
            if (!search.equals("")) {
              searchData(search);
            } else {
              onStart();
            }
          }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
      if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {
        mProgress.show();
        mProgress.setMessage("Uploading . . .");

        //Firebase storage folder where you want to put the images
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();

        //name of the image file (add time to have different files to avoid rewrite on the same file)
        final StorageReference imagesRef = storageRef.child(folder_name).child(folder_name + new Date().getTime()+".png");
        //send this name to database
        //upload image
        UploadTask uploadTask = imagesRef.putFile(imageUri);
        uploadTask.addOnFailureListener(new OnFailureListener() {
          @Override
          public void onFailure(@NonNull Exception exception) {
            Toast.makeText(SubFolderActivity.this, "Sending failed, Make sure that your GPS is switch on.", Toast.LENGTH_SHORT).show();
          }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
          @Override
          public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
            imagesRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
              @Override
              public void onSuccess(Uri uri) {
                mProgress.dismiss();
                Toast.makeText(SubFolderActivity.this, "Upload Successfully Send...", Toast.LENGTH_SHORT).show();
                imageUrl = uri.toString();

                Long tsLong = System.currentTimeMillis()/1000;
                String ts = tsLong.toString();
                String createdBy = USER_LASTNAME + ", " + USER_FIRSTNAME;

                String uploadID = databaseUpload.push().getKey();

                Uploads uploads = new Uploads(folder_id, null, imageUrl, ts, createdBy, uploadID);

                databaseUpload.child(uploadID).setValue(uploads);

                File file = new File(imageUri.getPath());
                file.delete();
                if(file.exists()){
                  try {
                    file.getCanonicalFile().delete();
                  } catch (IOException e) {
                    e.printStackTrace();
                  }
                  if(file.exists()){
                    getApplicationContext().deleteFile(file.getName());
                  }
                }
              }
            });
          }});
      }
    }

    private void isPermissionEnabled() {
    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
    {
      if(!CheckingPermissionIsEnabledOrNot())
      {
        //Calling method to enable permission.
        RequestMultiplePermission();
      }
    }
  }
    private void RequestMultiplePermission() {
    // Creating String Array with Permissions.
    ActivityCompat.requestPermissions(SubFolderActivity.this, new String[]
            {Manifest.permission.CAMERA,
              Manifest.permission.WRITE_EXTERNAL_STORAGE,
              Manifest.permission.READ_EXTERNAL_STORAGE
            }, RequestPermissionCode);
  }
    // Calling override method.
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
    switch (requestCode) {
      case RequestPermissionCode:
        if (grantResults.length > 0) {
          boolean CameraPermission = grantResults[0] == PackageManager.PERMISSION_GRANTED;
          boolean ReadStorage = grantResults[0] == PackageManager.PERMISSION_GRANTED;
          boolean WriteStorage = grantResults[0] == PackageManager.PERMISSION_GRANTED;
          if (CameraPermission && ReadStorage && WriteStorage) {
            Toast.makeText(SubFolderActivity.this, "Permission Granted", Toast.LENGTH_LONG).show();
          }
          else {
            RequestMultiplePermission();
          }
        }
        break;
    }
  }
    // Checking permission is enabled or not using function starts from here.
    public boolean CheckingPermissionIsEnabledOrNot() {
      int FirstPermissionResult = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA);
      int SecondPermissionResult = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE);
      int ThirdPermissionResult = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
      return FirstPermissionResult == PackageManager.PERMISSION_GRANTED &&
              SecondPermissionResult == PackageManager.PERMISSION_GRANTED &&
              ThirdPermissionResult == PackageManager.PERMISSION_GRANTED;
  }

  // ************************** FUNCTIONS **********************************************

  //File on long Press
  private void file_onLongPress(final SubFolder subFolder) {
    AlertDialog.Builder mBuilder = new AlertDialog.Builder(SubFolderActivity.this);
    View mView = getLayoutInflater().inflate(R.layout.dialog_information, null);
    TextView txtDate, txtUploaded;
    ImageView dialog_image;
    Button btnDownload, btnDelete;

    txtDate = mView.findViewById(R.id.txtDate);
    txtUploaded = mView.findViewById(R.id.txtUploaded);
    dialog_image = mView.findViewById(R.id.dialogImage);
    btnDownload = mView.findViewById(R.id.btnDownload);
    btnDelete = mView.findViewById(R.id.btnDelete);

    if (subFolder.getTimestamp() != null) {
      long timestamp = Long.parseLong(subFolder.getTimestamp()) * 1000L;
      txtDate.setText("Date uploaded: " + getDate(timestamp));
    }

    if (subFolder.getCreatedBy() != null) {
      txtUploaded.setText(subFolder.getCreatedBy().toString());
    }
    Picasso.get()
            .load(subFolder.getImg_url())
            .into(dialog_image);

    // Show Dialog
    mBuilder.setView(mView);
    final AlertDialog dialogAreaFolder;
    dialogAreaFolder = mBuilder.create();
    dialogAreaFolder.show();

    btnDownload.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        mStorage.child(subFolder.getImg_url());
        FirebaseStorage storage = FirebaseStorage.getInstance();
        // Create a storage reference from our app
        StorageReference storageRef = storage.getReferenceFromUrl(subFolder.getImg_url());
        downloadToLocalFile(storageRef);
      }
    });

    btnDelete.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        fileDelete(subFolder.getImg_id());
        dialogAreaFolder.dismiss();
      }
    });
  }

  // Folder on long Press
  private void folder_onLongPress(final SubFolder subFolder) {
    AlertDialog.Builder mBuilder = new AlertDialog.Builder(SubFolderActivity.this);
    View mView = getLayoutInflater().inflate(R.layout.dialog_folder_information, null);
    TextView txtDate, txtUploaded;
    Button btnDelete;

    txtDate = mView.findViewById(R.id.txtDate);
    txtUploaded = mView.findViewById(R.id.txtCreated);
    btnDelete = mView.findViewById(R.id.btnDelete);

    // Show Dialog
    mBuilder.setView(mView);
    final AlertDialog dialogAreaFolder;
    dialogAreaFolder = mBuilder.create();
    dialogAreaFolder.show();

    if (subFolder.getTimestamp() != null) {
      long timestamp = Long.parseLong(subFolder.getTimestamp()) * 1000L;
      txtDate.setText("Date Created: " + getDate(timestamp));
    }

    if (subFolder.getCreatedBy() != null) {
      txtUploaded.setText(subFolder.getCreatedBy());
    }

    btnDelete.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        folderDelete(subFolder.getSub_folder_id());
        dialogAreaFolder.dismiss();
      }
    });
  }

  private void fileDelete(String FOLDER_ID) {
    DatabaseReference drSubFolders = FirebaseDatabase
            .getInstance().getReference("uploads")
            .child(folder_id)
            .child(FOLDER_ID);

    drSubFolders.removeValue();

    Toast.makeText(this, "File successfully deleted."
            , Toast.LENGTH_LONG).show();
  }

  private void folderDelete(String FOLDER_ID) {
      DatabaseReference drSubFolders = FirebaseDatabase
              .getInstance().getReference("subfolders").child(folder_id).child(FOLDER_ID);

      drSubFolders.removeValue();

      Toast.makeText(this, "Folder successfully deleted."
              , Toast.LENGTH_LONG).show();
  }


  //Download file
  private void downloadToLocalFile(StorageReference fileRef) {
    if (fileRef != null) {
      mProgress.setTitle("Downloading...");
      mProgress.setMessage(null);
      mProgress.show();

      try {
        final File localFile = File.createTempFile("images", "jpg");

        String myfolder=Environment.getExternalStoragePublicDirectory
                (Environment.DIRECTORY_PICTURES).getPath()+"/PSUREPOSITORY/";
        File f = new File(myfolder);
        File fileNameOnDevice = null;
        if(!f.exists())
          f.mkdir();
        else
          fileNameOnDevice = new File(myfolder +
                  folder_name + new Date().getTime()+".jpg");

        fileRef.getFile(fileNameOnDevice).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
          @Override
          public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
            mProgress.dismiss();
          }
        }).addOnFailureListener(new OnFailureListener() {
          @Override
          public void onFailure(@NonNull Exception exception) {
            mProgress.dismiss();
            Toast.makeText(SubFolderActivity.this, exception.getMessage(), Toast.LENGTH_LONG).show();
          }
        }).addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
          @Override
          public void onProgress(FileDownloadTask.TaskSnapshot taskSnapshot) {
            // progress percentage
            double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();

            // percentage in progress dialog
            mProgress.setMessage("Downloaded " + ((int) progress) + "%...");
          }
        });
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  // Convert Date
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

  private void searchData(String keyword) {
    final Query foldersQuery = FirebaseDatabase.getInstance()
            .getReference("subfolders")
            .child(folder_id)
            .orderByChild("sub_folder_name")
            .equalTo(keyword);
    foldersQuery.addValueEventListener(new ValueEventListener() {
      @Override
      public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
        subFolderList.clear();
        for(DataSnapshot subfolderSnapshot : dataSnapshot.getChildren()) {
          SubFolder subFolder = subfolderSnapshot.getValue(SubFolder.class);
          subFolderList.add(subFolder);
        }
        SubFolderList areaFoldersListAdapter = new SubFolderList(SubFolderActivity.this, subFolderList);
        listViewSubFolders.setAdapter(areaFoldersListAdapter);

        Query dbUploads = databaseUpload.orderByKey();
        dbUploads.addListenerForSingleValueEvent(new ValueEventListener() {
          @Override
          public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            for(DataSnapshot subfolderSnapshot : dataSnapshot.getChildren()) {
              SubFolder subFolder = subfolderSnapshot.getValue(SubFolder.class);
              subFolderList.add(subFolder);
            }
            SubFolderList uploadImageListAdapter = new SubFolderList(SubFolderActivity.this, subFolderList);
            listViewSubFolders.setAdapter(uploadImageListAdapter);
          }

          @Override
          public void onCancelled(@NonNull DatabaseError databaseError) {

          }
        });
      }

      @Override
      public void onCancelled(@NonNull DatabaseError databaseError) {

      }
    });
  }
}



