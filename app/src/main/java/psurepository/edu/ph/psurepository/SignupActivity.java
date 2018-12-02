package psurepository.edu.ph.psurepository;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class SignupActivity extends AppCompatActivity {
  public static final String FIRST_NAME="FIRST_NAME";
  public static final String LAST_NAME="LAST_NAME";
  public static final String ACCESS_AREA="ACCESS_AREA";

  // WIDGETS
  private EditText edtEmail, edtPassword, edtFirstName, edtLastName;
  private Spinner areaSpinner;
  private Button btnSignup;
  private TextView txtLogin;
  private ProgressDialog progressDialog;

  /* Firebase Auth */
  private FirebaseAuth auth;

  /* Firebase Database */
  private DatabaseReference databaseUser;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_signup);

    auth = FirebaseAuth.getInstance();
    databaseUser = FirebaseDatabase.getInstance().getReference("users");

    edtEmail = findViewById(R.id.logEmail);
    edtPassword = findViewById(R.id.logPassword);
    edtFirstName = findViewById(R.id.edtFirstName);
    edtLastName = findViewById(R.id.edtLastName);
    btnSignup = findViewById(R.id.btnSignup);
    txtLogin = findViewById(R.id.txtLogin);
    areaSpinner = findViewById(R.id.areaSpinner);
    progressDialog = new ProgressDialog(this);

    createUser();
    loginUser();
  }

  @Override
  protected void onStart() {
    super.onStart();
    Query foldersQuery = FirebaseDatabase.getInstance()
            .getReference("areafolders").orderByChild("folder_name");
    foldersQuery.addValueEventListener(new ValueEventListener() {
      @Override
      public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
        final List<String> areas = new ArrayList<String>();

        for(DataSnapshot areafolderSnapshot : dataSnapshot.getChildren()) {
          String areaName = areafolderSnapshot.child("folder_name").getValue(String.class);
          areas.add(areaName);
        }

        ArrayAdapter<String> areasAdapter = new ArrayAdapter<String>(SignupActivity.this, android.R.layout.simple_spinner_item, areas);
        areasAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        areaSpinner.setAdapter(areasAdapter);
      }
      @Override
      public void onCancelled(@NonNull DatabaseError databaseError) {

      }
    });
  }
  private void createUser() {
    btnSignup.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        final String email, password, firstName, lastName, area;
        email = edtEmail.getText().toString().trim();
        password = edtPassword.getText().toString().trim();
        firstName = edtFirstName.getText().toString().trim();
        lastName = edtLastName.getText().toString().trim();
        area =  areaSpinner.getSelectedItem().toString();

        if (TextUtils.isEmpty(email)) {
          Toast.makeText(SignupActivity.this, "Enter Email Address.", Toast.LENGTH_SHORT).show();
          return;
        }

        if (TextUtils.isEmpty(password)) {
          Toast.makeText(SignupActivity.this, "Enter Password.", Toast.LENGTH_SHORT).show();
          return;
        }

        if (TextUtils.isEmpty(firstName)) {
          Toast.makeText(SignupActivity.this, "Enter First Name.", Toast.LENGTH_SHORT).show();
          return;
        }

        if (TextUtils.isEmpty(lastName)) {
          Toast.makeText(SignupActivity.this, "Enter Last Name.", Toast.LENGTH_SHORT).show();
          return;
        }

        if (password.length() < 6) {
          Toast.makeText(SignupActivity.this, "Password too short, enter minimum 6 characters!", Toast.LENGTH_SHORT).show();
          return;
        }

        progressDialog.show();
        progressDialog.setMessage("Creating user...");

        auth.createUserWithEmailAndPassword(email, password)
          .addOnCompleteListener(SignupActivity.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
              if (task.isSuccessful()) {
                Log.d("USER CREATED", "createUserWithEmail:success");
                try {
                  // Date
                  Long tsLong = System.currentTimeMillis()/1000;
                  String ts = tsLong.toString();

                  // User Id
                  String userId = task.getResult().getUser().getUid();

                  // Creating Object
                  User userClass = new User(email, password, firstName, lastName, area, ts);
                  databaseUser.child(userId).setValue(userClass);
                  progressDialog.dismiss();
                  userIsExists();
                }catch (Exception e) {
                  e.printStackTrace();
                }

              } else {
                // If sign in fails, display a message to the user.
                Log.w("FAILED TO CREATE USER", "createUserWithEmail:failure", task.getException());
                Toast.makeText(SignupActivity.this, "Authentication failed.",
                        Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
              }
            }
          });

      }
    });
  }
  private void loginUser() {
    txtLogin.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        startActivity(new Intent(SignupActivity.this, LoginActivity.class));
        finish();
      }
    });
  }
  private void userIsExists() {
    try {
      if (auth.getCurrentUser() != null) {
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pd.setMessage("Fetching user data. . . ");
        pd.setIndeterminate(true);
        pd.setCancelable(false);
        pd.show();
        final Query usersQuery = FirebaseDatabase.getInstance()
                .getReference("users").child(auth.getCurrentUser().getUid());
        usersQuery.addValueEventListener(new ValueEventListener() {
          @Override
          public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            User userClass = dataSnapshot.getValue(User.class);
            Log.d("FIRST NAME", "onDataChange: " + userClass.getFirstName());
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.putExtra(FIRST_NAME, userClass.getFirstName());
            intent.putExtra(LAST_NAME, userClass.getLastName());
            intent.putExtra(ACCESS_AREA, userClass.getArea());
            startActivity(intent);
            finish();
            pd.dismiss();
          }

          @Override
          public void onCancelled(@NonNull DatabaseError databaseError) {

          }
        });
      }
    }catch (Exception e) {
      e.printStackTrace();
    }
  }
}
