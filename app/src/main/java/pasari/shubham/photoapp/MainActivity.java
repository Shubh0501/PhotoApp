package pasari.shubham.photoapp;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.iceteck.silicompressorr.PathUtil;
import com.iceteck.silicompressorr.SiliCompressor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.net.URISyntaxException;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    //Private Request Codes for different processes
    private static final int CAPTURE_IMAGE_REQUEST = 982;
    private static final int PERMISSION_REQUEST = 767;
    private static final int PICK_IMAGE_REQUEST = 100;
    public boolean permissionGranted = false;

    //Global Variables
    private String path_for_image;
    private String file_name;
    private EditText file_name_et;

    //Views from the layout
    private Button camerabutton;
    private Button uploadbutton;

    //SharedPreferences and some values
    private SharedPreferences preferences;
    private boolean first_time_request;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Connecting Views
        connect_views();
        preferences = getSharedPreferences("PhotoApp", MODE_PRIVATE);
        first_time_request = preferences.getBoolean("permission_request_first_time", true);
        check_if_from_show_image_activity();
        check_permission();
    }

    //Function to check if coming from showImages activity or starting for the first time
    private void check_if_from_show_image_activity() {
        File encryptFolder = getDir("encrypted-images", MODE_PRIVATE);
        //Getting all the files in encrypted folder
        File[] files = encryptFolder.listFiles();
        boolean from_image_activity = preferences.getBoolean("from_image_activity", false);
        preferences.edit().putBoolean("from_image_activity", false).apply();
        if (files.length != 0) {
            if(!from_image_activity){
                startActivity(new Intent(MainActivity.this, ShowImagesActivity.class));
                finish();
            }
        }
    }

    //Function to check permission
    private void check_permission() {
        //Checking if permission for external storage is provided or not
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            //If permission has been asked before or not
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                new AlertDialog.Builder(this).setTitle("Permission necessary !")
                        .setMessage("Please provide this permission to serve you well !")
                        .setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}
                                        , PERMISSION_REQUEST);
                            }
                        }).setNegativeButton("No thanks, Close App", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                }).show();
            }

            //Checking if permission is asked for the first time or user has blocked the permission
            else {
                if (first_time_request) {
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}
                            , PERMISSION_REQUEST);
                    preferences.edit().putBoolean("permission_request_first_time", false).apply();
                } else {
                    new AlertDialog.Builder(this).setTitle("Permission denied !")
                            .setMessage("We are sorry. It seems that you have blocked the permission." +
                                    "Please manually go to settings->apps->PhotoApp and allow the " +
                                    "permission to read External storage")
                            .setCancelable(false)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    finish();
                                }
                            }).show();
                }
            }
        }

        //Permission granted
        else {
            permissionGranted = true;
        }
    }


    //Function to connect the layout
    private void connect_views() {
        camerabutton = findViewById(R.id.camerabutton);
        uploadbutton = findViewById(R.id.uploadbutton);
        //OnClickListeners
        camerabutton.setOnClickListener(this);
        uploadbutton.setOnClickListener(this);
    }

    //OnActivityResult
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //Getting the directory to work on in "target"
        File root = Environment.getExternalStorageDirectory();
        File directory = new File(root, "PhotoApp");
        File target = new File(directory, ".temp_storage");

        //If we get the data from camera:
        if (requestCode == CAPTURE_IMAGE_REQUEST && resultCode == RESULT_OK) {
            //Original file saved by camera address
            File original = new File(directory, ".image.jpg");
            //Compressed file saved in "target"
            path_for_image = SiliCompressor.with(this).compress(original.getAbsolutePath(), target);
            //Deleting original file if only came from camera
            original.delete();

            //Dialog to input the name of the file
            final Dialog dialog = new Dialog(this);
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.setContentView(R.layout.file_name_input_view);
            file_name_et = dialog.findViewById(R.id.filenameedittext);
            Button done = dialog.findViewById(R.id.donebutton);
            dialog.setCancelable(false);
            dialog.show();
            //OnClickListener for Save button in Dialog box
            done.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    file_name = file_name_et.getText().toString();
                    encryptImage(new File(path_for_image));
                    dialog.dismiss();
                }
            });

        }
        //If we get the data from file explorer
        else if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK) {
            //creating a file to save the address of the image specified
            File file = null;
            //Trying to get data from specified path
            try {
                file = new File(PathUtil.getPath(this, data.getData()));
            } catch (URISyntaxException e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            }
            //saving campressed image in "target"
            path_for_image = SiliCompressor.with(this).compress(file.getAbsolutePath(), target);

            //Dialog to take the file name by the user
            final Dialog dialog = new Dialog(this);
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.setContentView(R.layout.file_name_input_view);
            file_name_et = dialog.findViewById(R.id.filenameedittext);
            Button done = dialog.findViewById(R.id.donebutton);
            dialog.setCancelable(false);
            dialog.show();
            //OnClickListener for save button on dialog box
            done.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    file_name = file_name_et.getText().toString();
                    encryptImage(new File(path_for_image));
                    dialog.dismiss();
                }
            });

        }
    }

    //OnClickListener Function
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.camerabutton:
                //Getting the location of external Storage in root
                File root = Environment.getExternalStorageDirectory();

                //Creating an own directory for the app if not exists
                File directory = new File(root, "PhotoApp");

                //Creating an image file in the app directory to save the image from camera
                File outputImage = new File(directory, ".image.jpg");

                //Using file provider to get Uri
                final Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".provider", outputImage);

                //Intent for Camera
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                startActivityForResult(intent, CAPTURE_IMAGE_REQUEST);
                break;

            case R.id.uploadbutton:
                //Intent for file explorer to search the image file
                Intent intent1 = new Intent();
                intent1.setType("image/*");
                intent1.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent1, PICK_IMAGE_REQUEST);
                break;
        }
    }

    //Encryption variables
    private final static int BUFFER_LENGTH = 16;

    //Function to encrypt Image
    private void encryptImage(File image) {

        try {
            File encryptfile;
            //Creating a new File to save the encrypted image in private directory.
            if (file_name.equals("")) {
                encryptfile = new File(getDir("encrypted-images", MODE_PRIVATE), System.currentTimeMillis() + "-image.enc");
            } else {
                encryptfile = new File(getDir("encrypted-images", MODE_PRIVATE), file_name + ".enc");
            }

            //Reading the secret key from the file created when app was started for the first time
            File dir = getDir("secret", MODE_PRIVATE);
            File keyFile = new File(dir, "key.txt");
            FileInputStream fileInputStream = new FileInputStream(keyFile);
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            SecretKey secretKey = (SecretKey) objectInputStream.readObject();

            //Creating a initialization_vector of length 16 and initializing fixed values
            byte[] initialization_vector = new byte[BUFFER_LENGTH];
            for (int i = 0; i < BUFFER_LENGTH; i++) {
                initialization_vector[i] = (byte) ((i * 2) % 9);
            }
            //Interface to group all parameter
            AlgorithmParameterSpec paramSpec = new IvParameterSpec(initialization_vector);

            //Encrypting...
            Encrypter.encrypt(secretKey, paramSpec,
                    new FileInputStream(image), new FileOutputStream(encryptfile));
            //Deleting the image
            image.delete();

            //Starting the next Activity
            preferences.edit().putBoolean("from_image_activity", true).apply();
            Intent intent = new Intent(this, ShowImagesActivity.class);
            startActivity(intent);
            finish();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    //Function which returns the result of permissions asked
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        //Changing the permissionGranted value according to the input by user
        if (requestCode == PERMISSION_REQUEST && grantResults[0] == RESULT_OK) {
            permissionGranted = true;
        } else {
            permissionGranted = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        preferences.edit().putBoolean("started_now", true).apply();
    }
}
