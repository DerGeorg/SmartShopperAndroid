package at.smartshopper.smartshopper.activitys;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import at.smartshopper.smartshopper.R;
import at.smartshopper.smartshopper.db.Database;
import at.smartshopper.smartshopper.shoppinglist.Member;

public class EditUser extends Activity {

    private EditText editname;
    private ImageView userbild;
    private Button finish, chooseImg;
    private Bitmap userBitmap;
    private Database db;
    private final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_user);
        userbild = (ImageView) findViewById(R.id.userImage);
        editname = (EditText) findViewById(R.id.editName);
        finish = (Button) findViewById(R.id.editFinish);
        chooseImg = (Button) findViewById(R.id.chooseImg);

        db = new Database();

        Member member = null;
        try {
            member = db.getUser(FirebaseAuth.getInstance().getUid());
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String name = member.getName();
        String photoUrl = member.getPic();


            userbild.setImageDrawable(LoadImageFromWebOperations(photoUrl));
            editname.setText(name);




        chooseImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
                getIntent.setType("image/*");

                Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                pickIntent.setType("image/*");

                Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{pickIntent});

                startActivityForResult(chooserIntent, 1);
            }
        });

        finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList arlist = new ArrayList();
                arlist.add(userBitmap);
                arlist.add(FirebaseAuth.getInstance().getUid());
                Object[] objArr = arlist.toArray();
                ImgSaver imgSaver = new ImgSaver();
                String uri = null;
                String name = editname.getText().toString();
                try {
                    uri = imgSaver.execute(objArr).get();


                    Log.d("SmartShopper", uri + " " + name);
                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                            .setDisplayName(name)
                            .setPhotoUri(Uri.parse(uri))
                            .build();
                    try {
                        Member member = db.getUser(FirebaseAuth.getInstance().getUid());
                        db.updateUser(FirebaseAuth.getInstance().getUid(), member.getMsid(), name, FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl().toString(), member.getEmail());
                    } catch (SQLException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    user.updateProfile(profileUpdates)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Log.d("SmartShopper", "User profile updated.");
                                        doRestart(EditUser.this);
                                    }
                                }
                            });

                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    public static void doRestart(Context c) {
        try {
            //check if the context is given
            if (c != null) {
                //fetch the packagemanager so we can get the default launch activity
                // (you can replace this intent with any other activity if you want
                PackageManager pm = c.getPackageManager();
                //check if we got the PackageManager
                if (pm != null) {
                    //create the intent with the default start activity for your application
                    Intent mStartActivity = pm.getLaunchIntentForPackage(
                            c.getPackageName()
                    );
                    if (mStartActivity != null) {
                        mStartActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        //create a pending intent so the application is restarted after System.exit(0) was called.
                        // We use an AlarmManager to call this intent in 100ms
                        int mPendingIntentId = 223344;
                        PendingIntent mPendingIntent = PendingIntent
                                .getActivity(c, mPendingIntentId, mStartActivity,
                                        PendingIntent.FLAG_CANCEL_CURRENT);
                        AlarmManager mgr = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
                        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
                        //kill the application
                        System.exit(0);
                    } else {
                        Log.e("SmartShopper", "Was not able to restart application, mStartActivity null");
                    }
                } else {
                    Log.e("SmartShopper", "Was not able to restart application, PM null");
                }
            } else {
                Log.e("SmartShopper", "Was not able to restart application, Context null");
            }
        } catch (Exception ex) {
            Log.e("SmartShopper", "Was not able to restart application");
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            try {
                final Uri imageUri = data.getData();
                final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                userBitmap = selectedImage;
                userbild.setImageBitmap(selectedImage);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private Drawable LoadImageFromWebOperations(String url) {
        try {
            InputStream is = (InputStream) new URL(url).getContent();
            Drawable d = Drawable.createFromStream(is, "src name");
            return d;
        } catch (Exception e) {
            return null;
        }
    }

    public class ImgSaver extends AsyncTask<Object, String, String> {

        private FirebaseStorage storage = FirebaseStorage.getInstance();
        private String downloadUriFinal = "";

        @Override
        protected String doInBackground(Object... objects) {
            final StorageReference storageRef = storage.getReference("/" + objects[1]);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Bitmap bitmap = (Bitmap) objects[0];
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] data = baos.toByteArray();

            UploadTask uploadTask = storageRef.putBytes(data);
            Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    // Continue with the task to get the download URL
                    return storageRef.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        Log.d("SmartShopper", downloadUri.toString());
                        downloadUriFinal = downloadUri.toString();
                    } else {
                        System.out.println(task.getException().getMessage());
                    }
                }
            });
            return downloadUriFinal;
        }
    }
}
