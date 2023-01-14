package com.ferit.cifar

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.provider.MediaStore.Audio.Media
import kotlinx.android.synthetic.main.activity_main.*
import java.util.jar.Manifest

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        button.setOnClickListener {

            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivity(cameraIntent);
        }

        button2.setOnClickListener {
            val mediaIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivity(mediaIntent);
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if(resultCode == RESULT_OK) {
            var image : Bitmap = data.extras.get("data")
        }


        super.onActivityResult(requestCode, resultCode, data)
    }

}