package com.martin.androlibs3;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.concurrent.Callable;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    Button btnPick;
    Button btnCancel;
    TextView textView;
    Date date;
    Disposable disposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        date = new Date();

        btnPick = findViewById(R.id.pick_image);
        btnPick.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Choose picture"), 1);
        });
        btnCancel = findViewById(R.id.btn_cancel_convert);
        //Попробывал сделать второе задание, но вылетает с ошибкой при нажатии
        btnCancel.setOnClickListener(v -> {
            if (disposable != null && !disposable.isDisposed()) {
                disposable.dispose();
                disposable = null;
            }
        });

        textView = findViewById(R.id.txt_convert_process);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        textView.setText(R.string.start_conv);
        if (resultCode == RESULT_OK) {
            Uri selectedImgUri = data.getData();
            disposable = fromJpgTOPng(this, selectedImgUri)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(s1 -> {
                        if (s1.equals("ok")) {
                            Toast.makeText(getApplicationContext(), "Picture succesfully converted", Toast.LENGTH_LONG).show();
                        }
                        textView.setText(R.string.finish_conv);

                    }, throwable -> Log.d("mytag", throwable.getMessage()));
        }
    }

    Observable<String> fromJpgTOPng(Context context, Uri uri) {
        return Observable.fromCallable(new Callable<String>() {
            @Override
            public String call() throws Exception {
                Thread.sleep(5000);
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);
                File file = new File(getApplicationContext().getFilesDir(), "IMG" + date.getTime() + ".png");
                OutputStream stream = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                stream.flush();
                stream.close();
                Log.d("mytag", "Process in : " + Thread.currentThread().getName());
                return "ok";
            }
        });
    }
}
