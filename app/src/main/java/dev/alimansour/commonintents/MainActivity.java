package dev.alimansour.commonintents;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Date;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import dev.alimansour.commonintents.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private static final int REQUIRED_PERMISSIONS_CODE = 1;
    private static final int REQUEST_SELECT_CONTACT = 2;
    static final int REQUEST_IMAGE_CAPTURE = 3;
    private static Uri locationForPhotos;
    private String mNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.dialButton.setOnClickListener(v -> dialPhone("123456789"));
        binding.callButton.setOnClickListener(v -> callPhone("123456789"));
        binding.composeButton.setOnClickListener(v ->
                composeEmail(new String[]{"marimshabana2510@gmail.com"},
                        "abdelmonemanwr7777@gmail.com",
                        "Test Message",
                        "Welcome to Android Course!"));
        binding.pickContactButton.setOnClickListener(v -> selectContact());
        binding.captureButton.setOnClickListener(v -> capturePhoto());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        try {
            if (requestCode == REQUIRED_PERMISSIONS_CODE) {
                if (grantResults.length > 0) {
                    for (String permission : permissions) {
                        if (permission.equals(Manifest.permission.CALL_PHONE)) {
                            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                                callPhone(mNumber);
                            } else {
                                Toast.makeText(this, getString(R.string.call_phone_permission),
                                        Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                }
            } else if (requestCode == REQUEST_SELECT_CONTACT) {
                if (grantResults.length > 0) {
                    for (String permission : permissions) {
                        if (permission.equals(Manifest.permission.READ_CONTACTS)) {
                            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                                selectContact();
                            } else {
                                Toast.makeText(this, getString(R.string.read_contacts_permission),
                                        Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_SELECT_CONTACT && resultCode == RESULT_OK) {
            Uri contactUri = data.getData();
            String[] projection = new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER};
            assert contactUri != null;
            Cursor cursor = getContentResolver().query(contactUri, projection,
                    null, null, null);
            // If the cursor returned is valid, get the phone number
            if (cursor != null && cursor.moveToFirst()) {
                int numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                String number = cursor.getString(numberIndex);
                callPhone(number);
            }
            if (cursor != null) cursor.close();
        } else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bitmap thumbnail = data.getParcelableExtra("data");
            // Do other work with full size photo saved in locationForPhotos
            binding.imageView.setImageBitmap(thumbnail);
        }
    }

    private void callPhone(String phoneNumber) {
        try {
            mNumber = phoneNumber;
            // Check for required permissions
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.CALL_PHONE
                ) == PackageManager.PERMISSION_DENIED
                ) {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.CALL_PHONE},
                            REQUIRED_PERMISSIONS_CODE);
                    return;
                }
            }
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setData(Uri.parse("tel:" + phoneNumber));
            startActivity(callIntent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void dialPhone(String phoneNumber) {
        Intent callIntent = new Intent(Intent.ACTION_DIAL);
        callIntent.setData(Uri.parse("tel:" + phoneNumber));
        startActivity(callIntent);
    }

    public void composeEmail(String[] addresses, String carbonCopy, String subject, String message) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:")); // only email apps should handle this
        intent.putExtra(Intent.EXTRA_EMAIL, addresses);
        intent.putExtra(Intent.EXTRA_CC, carbonCopy);
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, message);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Toast.makeText(this, "You need to install Email client first!", Toast.LENGTH_LONG).show();
        }
    }

    public void selectContact() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(
                    Manifest.permission.READ_CONTACTS
            ) == PackageManager.PERMISSION_DENIED
            ) {
                requestPermissions(
                        new String[]{Manifest.permission.READ_CONTACTS},
                        REQUEST_SELECT_CONTACT);
                return;
            }
        }
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_SELECT_CONTACT);
        } else {
            Toast.makeText(this, "Please install Contacts application!", Toast.LENGTH_LONG).show();
        }
    }

    public void capturePhoto() {
        Date currentTime = Calendar.getInstance().getTime();
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT,
                Uri.withAppendedPath(locationForPhotos, currentTime.toString()));
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
        }
    }

}