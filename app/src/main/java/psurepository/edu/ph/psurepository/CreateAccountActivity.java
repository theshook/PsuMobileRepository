package psurepository.edu.ph.psurepository;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class CreateAccountActivity extends AppCompatActivity {

  // WIDGETS
  private EditText edtEmail, edtPassword, edtFirstName, edtLastName;
  private Spinner areaSpinner;
  private Button btnSignup;
  private TextView txtLogin;
  private ProgressDialog progressDialog;

  /* Firebase Auth */
  private FirebaseAuth auth, register_auth;

  /* Firebase Database */
  private DatabaseReference databaseUser;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_signup);

    auth = FirebaseAuth.getInstance();
    register_auth = FirebaseAuth.getInstance();
    databaseUser = FirebaseDatabase.getInstance().getReference("users");

    edtEmail = findViewById(R.id.logEmail);
    edtPassword = findViewById(R.id.logPassword);
    edtFirstName = findViewById(R.id.edtFirstName);
    edtLastName = findViewById(R.id.edtLastName);
    btnSignup = findViewById(R.id.btnSignup);
    txtLogin = findViewById(R.id.txtLogin);
    areaSpinner = findViewById(R.id.areaSpinner);

    // Hide Widgets
    areaSpinner.setVisibility(View.INVISIBLE);
    txtLogin.setVisibility(View.INVISIBLE);

    progressDialog = new ProgressDialog(this);

    // Create User Account
    createUserAccount();
  }

  private void createUserAccount() {
    btnSignup.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        final String email, password, firstName, lastName;
        email = edtEmail.getText().toString().trim();
        password = edtPassword.getText().toString().trim();
        firstName = edtFirstName.getText().toString().trim();
        lastName = edtLastName.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
          Toast.makeText(CreateAccountActivity.this, "Enter Email Address.", Toast.LENGTH_SHORT).show();
          return;
        }

        if (TextUtils.isEmpty(password)) {
          Toast.makeText(CreateAccountActivity.this, "Enter Password.", Toast.LENGTH_SHORT).show();
          return;
        }

        if (TextUtils.isEmpty(firstName)) {
          Toast.makeText(CreateAccountActivity.this, "Enter First Name.", Toast.LENGTH_SHORT).show();
          return;
        }

        if (TextUtils.isEmpty(lastName)) {
          Toast.makeText(CreateAccountActivity.this, "Enter Last Name.", Toast.LENGTH_SHORT).show();
          return;
        }

        if (password.length() < 6) {
          Toast.makeText(CreateAccountActivity.this, "Password too short, enter minimum 6 characters!", Toast.LENGTH_SHORT).show();
          return;
        }

        progressDialog.show();
        progressDialog.setMessage("Creating user...");

        saveUserAccount(email, password, firstName, lastName);
      }
    });
  }

  private void saveUserAccount(final String email,
                               final String password,
                               final String firstName,
                               final String lastName) {
    register_auth.createUserWithEmailAndPassword(email, password)
      .addOnCompleteListener(CreateAccountActivity.this,
        new OnCompleteListener<AuthResult>() {

          @Override
          public void onComplete(@NonNull Task<AuthResult> task) {
            try {
              // Date
              Long tsLong = System.currentTimeMillis()/1000;
              String ts = tsLong.toString();

              // User Id
              String userId = task.getResult().getUser().getUid();

              // Creating Object
              User userClass = new User(email, password, firstName, lastName, "Admin", ts);
              databaseUser.child(userId).setValue(userClass);
              register_auth.signOut();
              progressDialog.dismiss();
              startActivity(new Intent(CreateAccountActivity.this, AccountActivity.class));
              finish();
            }catch (Exception e) {
              e.printStackTrace();
            }
          }
      });
  }
}
