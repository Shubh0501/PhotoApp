package pasari.shubham.photoapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.security.spec.AlgorithmParameterSpec;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class ShowImagesActivity extends AppCompatActivity implements View.OnClickListener {

    //Layout variables and adapter to connect ListView
    private ArrayAdapter<String> arrayAdapter;
    private ListView mListView;
    private List<String> fileNames;
    private Button takenewphoto;
    //Folder where encrypted file is present
    private File encryptFolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_images);
        //Function to connect Layout
        connectLayout();
        //Initializing the encrypted folder name
        encryptFolder = getDir("encrypted-images", MODE_PRIVATE);
        //Getting all the files in encrypted folder
        File[] files = encryptFolder.listFiles();

        fileNames = new ArrayList<>();
        //Initializing "fileNames" with the name of files present in encrypt folder
        for (File file: files){
            fileNames.add(file.getName());
        }
        //Initializing arrayAdapter
        arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, fileNames);
        //Setting up adapter for listview and connecting onClick and onLongClick function
        mListView.setAdapter(arrayAdapter);
        mListView.setOnItemClickListener(itemClickListener);
        mListView.setOnItemLongClickListener(itemLongClickListener);
    }

    //Function to connect and get data from previous activity
    private void connectLayout() {
        mListView = findViewById(R.id.listview);
        takenewphoto = findViewById(R.id.takenewphoto);
        //OnClickListeners for buttons
        takenewphoto.setOnClickListener(this);
    }

    //OnClickListener
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.takenewphoto:
                //finishing this activity and redirecting to the first activity to upload new photo
                SharedPreferences preferences = getSharedPreferences("PhotoApp", MODE_PRIVATE);
                preferences.edit().putBoolean("from_image_activity", true).apply();
                startActivity(new Intent(this, MainActivity.class));
                finish();
                break;
        }
    }

    //ListView OnClickListener
    private AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            //Getting the name of file at the position clicked and then getting the location
            String name = fileNames.get(position);
            File file = new File(encryptFolder, name);
            //Function to decrypt and show the image
            decryptFileAndShow(file);
        }
    };

    //ListView OnLongClickListener
    private AdapterView.OnItemLongClickListener itemLongClickListener = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            //Getting the name of file at the position clicked and then getting the location
            String name = fileNames.get(position);
            File file = new File(encryptFolder, name);

            //Deleting the file on which long pressed
            file.delete();

            //Removing file name from listview and notifying the adapter to make those changes
            fileNames.remove(position);
            arrayAdapter.notifyDataSetChanged();

            File encrypt_folder = getDir("encrypted-images", MODE_PRIVATE);
            File[] temp_file = encrypt_folder.listFiles();
            if(temp_file.length==0){
                startActivity(new Intent(ShowImagesActivity.this, MainActivity.class));
                finish();
            }
            return true;
        }
    };

    private final static String ALGORITHM_SECRET_KEY_GENERATOR = "AES";
    private final static int BUFFER_LENGTH = 16;
    //Function to decrypt the file and show
    private void decryptFileAndShow(File file) {

        try {
            //Creating a temporary file to decrypt the image
            File temp = new File(getDir("temp_decrypted", MODE_PRIVATE), "temp.png");
            //Initializing the key
            //Reading the secret key from the file created when app was started for the first time
            File dir = getDir("secret", MODE_PRIVATE);
            File keyFile = new File(dir, "key.txt");
            FileInputStream fileInputStream = new FileInputStream(keyFile);
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            SecretKey secretKey = (SecretKey) objectInputStream.readObject();
            byte[] keyData = secretKey.getEncoded();
            SecretKey key2  = new SecretKeySpec(keyData, 0, keyData.length, ALGORITHM_SECRET_KEY_GENERATOR);
            //Interface to group all parameter
            byte[] buffer1 = new byte[BUFFER_LENGTH];
            for (int i = 0; i< BUFFER_LENGTH; i++){
                buffer1[i] = (byte) ((i*2) % 9);
            }
            AlgorithmParameterSpec paramSpec = new IvParameterSpec(buffer1);

            //Decrypting...
            Encrypter.decrypt(key2, paramSpec, new FileInputStream(file), new FileOutputStream(temp));

            //Function to show the image
            showImage(temp);
        } catch (Exception e) {
            //Showing message as toast in case of an error
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }

    }

    //Function to show the image
    private void showImage(File temp) {

        //Fragment to show the image
        ShowImageFragment fragment = new ShowImageFragment();
        //Sending the file name to the fragment
        Bundle bundle = new Bundle();
        bundle.putSerializable("File_name", temp);
        fragment.setArguments(bundle);
        fragment.show(getSupportFragmentManager(), "Image");
    }

}
