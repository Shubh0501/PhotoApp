package pasari.shubham.photoapp;

import android.app.Application;
import android.content.SharedPreferences;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

public class MyApplication extends Application {
    private final static String ALGORITHM_SECRET_KEY_GENERATOR = "AES";
    @Override
    public void onCreate() {
        super.onCreate();

        //Getting the sharedPerferences of the app to check whether the app is run for the first time or not
        SharedPreferences preferences = getSharedPreferences("PhotoApp", MODE_PRIVATE);

        //Checking whether app is running for the first time
        if(preferences.getBoolean("app_first_run", true)){
            try {
                //Generating the secret key for encryption
                SecretKey key = KeyGenerator.getInstance(ALGORITHM_SECRET_KEY_GENERATOR).generateKey();

                //Creating a private file and saving the secret key in it for future decryption
                File dir = getDir("secret", MODE_PRIVATE);
                File keyFile = new File(dir, "key.txt");

                //Output streams to save the data
                FileOutputStream fileOutputStream = new FileOutputStream(keyFile);
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);

                objectOutputStream.writeObject(key);
                objectOutputStream.flush();

                //Changing the "app_first_run" to false
                preferences.edit().putBoolean("app_first_run", false).apply();

            } catch (Exception e) {
                //Toast message in case of any error
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            }

        }
    }
}
