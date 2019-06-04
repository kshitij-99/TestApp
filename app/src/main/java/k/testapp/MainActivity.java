package k.testapp;

import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.camerakit.CameraKitView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mukesh.OnOtpCompletionListener;
import com.mukesh.OtpView;

import java.io.File;
import java.io.FileOutputStream;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;
import info.hoang8f.widget.FButton;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {
    RelativeLayout layout1,layout3,layout4;
    LinearLayout layout2;
    CircleImageView imageView;
    TextInputEditText phone;
    File compressedImage;
    ImageView click;
    Button sendotp;
    OtpView otpView;
    Button validate,signout;
    CameraKitView cameraKitView;


    private static String uniqueIdentifier = null;
    private static final String UNIQUE_ID = "UNIQUE_ID";
    private static final long ONE_HOUR_MILLI = 60*60*1000;

    private static final String TAG = "FirebasePhoneNumAuth";
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private FirebaseAuth mAuth;

    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        cameraKitView=findViewById(R.id.camera);
        mAuth = FirebaseAuth.getInstance();
        layout1=findViewById(R.id.layout1);
        layout2=findViewById(R.id.layout2);
        layout3=findViewById(R.id.layout3);
        layout4=findViewById(R.id.layout4);
        imageView=findViewById(R.id.image);
        phone=findViewById(R.id.phone);
        sendotp=findViewById(R.id.sendotp);
        otpView=findViewById(R.id.otp_view);
        click=findViewById(R.id.click);
        validate=findViewById(R.id.validate_button);
        layout1.setVisibility(View.VISIBLE);
        signout=findViewById(R.id.signout);
        signout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuth.signOut();
                layout4.setVisibility(View.GONE);
                layout1.setVisibility(View.VISIBLE);
            }
        });
        sendotp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String number=phone.getText().toString();
                if (TextUtils.isEmpty(number)) {
                    phone.setError("Enter a Phone Number");
                    phone.requestFocus();
                }
                else if(!isValidMobile(number))
                {
                    phone.setError("Please enter a valid phone");
                    phone.requestFocus();
                }
                else
                {
                    PhoneAuthProvider.getInstance().verifyPhoneNumber(
                            "+91"+number,        // Phone number to verify
                            15,                 // Timeout duration
                            TimeUnit.SECONDS,   // Unit of timeout
                            MainActivity.this,               // Activity (for callback binding)
                            mCallbacks);

                    layout1.setVisibility(View.GONE);
                    layout2.setVisibility(View.VISIBLE);
                }

            }
        });
        mCallbacks =new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                String code=phoneAuthCredential.getSmsCode();
                otpView.setText(code);
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {

            }
            @Override
            public void onCodeSent(String verificationId,
                                   PhoneAuthProvider.ForceResendingToken token) {

                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                mResendToken = token;

                // ...
            }
        };
        otpView.setOtpCompletionListener(new OnOtpCompletionListener() {
            @Override
            public void onOtpCompleted(String otp) {
                String verificationCode = otpView.getText().toString();
                if(verificationCode.isEmpty()){
                    Toast.makeText(MainActivity.this,"Enter verification code",Toast.LENGTH_SHORT).show();
                }else {

                    try {
                        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, verificationCode);
                        signInWithPhoneAuthCredential(credential);
                    }catch (Exception e){
                        Toast toast = Toast.makeText(getApplicationContext(), "Verification Code is wrong", Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER,0,0);
                        toast.show();
                        layout4.setVisibility(View.VISIBLE);
                        layout2.setVisibility(View.GONE);
                    }

                }


        }
        });
        validate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String verificationCode = otpView.getText().toString();
                if(verificationCode.isEmpty()){
                    Toast.makeText(MainActivity.this,"Enter verification code",Toast.LENGTH_SHORT).show();
                }else {

                    try {
                        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, verificationCode);
                        signInWithPhoneAuthCredential(credential);
                    }catch (Exception e){
                        Toast toast = Toast.makeText(getApplicationContext(), "Verification Code is wrong", Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER,0,0);
                        toast.show();
                        layout4.setVisibility(View.VISIBLE);
                        layout2.setVisibility(View.GONE);
                    }

                }

                }



        });
        click.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cameraKitView.captureImage(new CameraKitView.ImageCallback() {
                    @Override
                    public void onImage(CameraKitView cameraKitView, final byte[] capturedImage) {
                        File savedPhoto = new File(Environment.getExternalStorageDirectory(), "photo.jpg");

                        try {
                            FileOutputStream outputStream = new FileOutputStream(savedPhoto.getPath());
                            outputStream.write(capturedImage);
                            outputStream.close();
                        } catch (java.io.IOException e) {
                            e.printStackTrace();
                        }
                        Bitmap bmp = BitmapFactory.decodeByteArray(capturedImage, 0, capturedImage.length);
                        imageView.setImageBitmap(bmp);
                        new Compressor(getApplicationContext())
                                .compressToFileAsFlowable(savedPhoto)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Consumer<File>() {
                                    @Override
                                    public void accept(File file) {
                                        compressedImage = file;
                                    }
                                }, new Consumer<Throwable>() {
                                    @Override
                                    public void accept(Throwable throwable) {
                                        throwable.printStackTrace();
                                    }
                                });

                    }

                });
                layout3.setVisibility(View.GONE);
                layout1.setVisibility(View.VISIBLE);
            }
        });



    }
    private boolean isValidMobile(String phone) {
        Pattern p = Pattern.compile("(0/91)?[7-9][0-9]{9}");

        Matcher m = p.matcher(phone);
        return (m.find() && m.group().equals(phone));
    }
    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            layout4.setVisibility(View.VISIBLE);
                            layout2.setVisibility(View.GONE);

                        } else {

                            Toast.makeText(MainActivity.this,"Something wrong",Toast.LENGTH_SHORT).show();
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                layout4.setVisibility(View.VISIBLE);
                                layout2.setVisibility(View.GONE);
                            }
                        }
                    }
                });
    }
    @Override
    protected void onStart() {
        super.onStart();
        cameraKitView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cameraKitView.onResume();
    }

    @Override
    protected void onPause() {
        cameraKitView.onPause();
        super.onPause();
    }

    @Override
    protected void onStop() {
        cameraKitView.onStop();
        super.onStop();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        cameraKitView.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
