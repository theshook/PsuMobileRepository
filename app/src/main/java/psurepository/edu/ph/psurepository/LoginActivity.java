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
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {
  public static final String FIRST_NAME="FIRST_NAME";
  public static final String LAST_NAME="LAST_NAME";
  public static final String ACCESS_AREA="ACCESS_AREA";

  // WIDGETS
  private EditText edtEmail, edtPassword;
  private Button btnLogin;
  private TextView txtSignup;
  private ProgressDialog progressDialog;

  /* Firebase Auth */
  private FirebaseAuth auth;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_login);

    auth = FirebaseAuth.getInstance();
    progressDialog = new ProgressDialog(this);

    // Check if user Exists
    userIsExists();

    edtEmail = findViewById(R.id.logEmail);
    edtPassword = findViewById(R.id.logPassword);
    btnLogin = findViewById(R.id.btnLogin);
    txtSignup = findViewById(R.id.txtSignup);

    loginUser();
    createUser();
  }

  private void loginUser() {
    btnLogin.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        final String email, password;
        email = edtEmail.getText().toString().trim();
        password = edtPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
          Toast.makeText(LoginActivity.this, "Enter Email Address.", Toast.LENGTH_SHORT).show();
          return;
        }

        if (TextUtils.isEmpty(password)) {
          Toast.makeText(LoginActivity.this, "Enter Password.", Toast.LENGTH_SHORT).show();
          return;
        }

        progressDialog.show();
        progressDialog.setMessage("Logging in. . .");

        auth.signInWithEmailAndPassword(email, password)
          .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
              if (task.isSuccessful()) {
                progressDialog.dismiss();
                userIsExists();
              } else {
                progressDialog.dismiss();
                Log.w("FAILED TO CREATE USER", "createUserWithEmail:failure", task.getException());
                Toast.makeText(LoginActivity.this, "Authentication failed.",
                        Toast.LENGTH_SHORT).show();
              }
            }
          });
      }
    });
  }
  private void createUser() {
    txtSignup.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        startActivity(new Intent(LoginActivity.this, SignupActivity.class));
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
