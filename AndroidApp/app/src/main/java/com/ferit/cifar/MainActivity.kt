package com.ferit.cifar

import android.content.Intent
import android.graphics.Bitmap
import android.media.ThumbnailUtils
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.ferit.cifar.ml.Model
import kotlinx.android.synthetic.main.activity_main.*
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder


@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {

    // Define the button and imageview type variable
    private lateinit var cameraOpenId: Button
    private lateinit var clickImageId: ImageView
    private lateinit var clickCompressedImageId: ImageView
    private lateinit var galleryOpenId: Button
    private val imageSize : Int = 32

    companion object {
        // Define the pic id
        private const val pic_id = 123
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // By ID we can get each component which id is assigned in XML file get Buttons and imageview.
        cameraOpenId = btnCamera
        clickImageId = ivImage
        clickCompressedImageId = ivImageCompressed
        galleryOpenId = btnGallery

        // Camera_open button is for open the camera and add the setOnClickListener in this button
        cameraOpenId.setOnClickListener {
            // Create the camera_intent ACTION_IMAGE_CAPTURE it will open the camera for capture the image
            val cameraIntent =
                Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            // Start the activity with camera_intent, and request pic id
            startActivityForResult(cameraIntent, pic_id)
        }

        galleryOpenId.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, pic_id)

        }
    }

    // This method will help to retrieve the image
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Match the request 'pic id with requestCode
        if (requestCode == pic_id) {
            // BitMap is data structure of image file which store the image in memory
            var photo = data!!.extras!!["data"] as Bitmap?
            val photoNotCompressed = data.extras!!["data"] as Bitmap?
            val dimension = photo!!.width.coerceAtMost(photo.height)
            photo = ThumbnailUtils.extractThumbnail(photo, dimension, dimension)
            photo = Bitmap.createScaledBitmap(photo, imageSize, imageSize, false)
            classifyImage(photo)
            // Set the image in imageview for display
            clickCompressedImageId.setImageBitmap(photo)
            clickImageId.setImageBitmap(photoNotCompressed)
            } else {
                val dat: Uri = data?.extras?.get("data") as Uri
                var photo: Bitmap? = null
            try {
                photo = MediaStore.Images.Media.getBitmap(this.contentResolver, dat)
            } catch (e: IOException) {
                e.printStackTrace() }
            }
    }


    private fun classifyImage(image : Bitmap) {
        try {
            val model = Model.newInstance(applicationContext)

            // Creates inputs for reference.
            val inputFeature0 =
                TensorBuffer.createFixedSize(intArrayOf(1, 32, 32, 3), DataType.FLOAT32)
            val byteBuffer: ByteBuffer = ByteBuffer.allocateDirect(4 * imageSize * imageSize * 3)
            byteBuffer.order(ByteOrder.nativeOrder())
            val intValues = IntArray(imageSize * imageSize)
            image.getPixels(intValues, 0, image.width, 0, 0, image.width, image.height)
            var pixel = 0
            //iterate over each pixel and extract R, G, and B values. Add those values individually to the byte buffer.
            for (i in 0 until imageSize) {
                for (j in 0 until imageSize) {
                    val `val` = intValues[pixel++] // RGB
                    byteBuffer.putFloat((`val` shr 16 and 0xFF) * (1f / 1))
                    byteBuffer.putFloat((`val` shr 8 and 0xFF) * (1f / 1))
                    byteBuffer.putFloat((`val` and 0xFF) * (1f / 1))
                }
            }
            inputFeature0.loadBuffer(byteBuffer)

            // Runs model inference and gets result.
            val outputs = model.process(inputFeature0)
            val outputFeature0 = outputs.outputFeature0AsTensorBuffer
            val confidences = outputFeature0.floatArray
            // find the index of the class with the biggest confidence.
            var maxPos = 0
            var maxConfidence = 0f

            val classes = arrayOf("airplane", "automobile", "bird", "cat", "deer",
                "dog", "frog", "horse", "ship", "truck")

            var output = ""
            for (i in confidences.indices) {
                if (confidences[i] > maxConfidence) {
                    maxConfidence = confidences[i]
                    maxPos = i
                }

                output += classes[i] + ": " + confidences[i] + "\n"

            }

            tvClass.text = classes[maxPos]

            tvClassValues.text = output
//            Toast.makeText(baseContext, output.toString(), Toast.LENGTH_LONG).show()

            // Releases model resources if no longer used.
            model.close()
        } catch (e: IOException) {
            // TODO Handle the exception
        }
    }




}

