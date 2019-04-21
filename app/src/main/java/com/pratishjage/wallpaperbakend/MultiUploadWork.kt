package com.pratishjage.wallpaperbakend

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.OnProgressListener
import com.google.firebase.storage.StorageReference
import com.pratishjage.wallpaperbakend.Cnstants.IMAGES_ARRAY
import java.io.File
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.math.log
import id.zelory.compressor.Compressor
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import java.nio.file.Files

class MultiUploadWork(val appContext: Context, workparam: WorkerParameters) : Worker(appContext, workparam) {

    lateinit var mFiles: ArrayList<Uri>
    lateinit var db: FirebaseFirestore
    lateinit var storageRef: StorageReference
    override fun doWork(): Result {

        db = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference();

        val imgs = inputData.getStringArray(IMAGES_ARRAY)
        val keyValueMap = inputData.keyValueMap

        imgs!!.forEach {
            val uri = Uri.fromFile(File(it))
            mFiles.add(uri)
        }
        mFiles.forEachIndexed { index, uri ->
            runBlocking {
                val compressedImage = compressImage(uri = uri, appContext = appContext)
                compressedImage
            }
        }
        mFiles.forEach {
            runBlocking {
                val compressedImage = compressImage(uri = it, appContext = appContext)
                compressedImage

            }
        }

        return Result.success()

    }

    suspend fun startUpload(mFiles: ArrayList<Uri>) = coroutineScope {


    }

    private suspend fun compressImage(uri: Uri, appContext: Context): File? {
        var actualImage: File? = null
        var compressedImageFile: File? = null
        try {
            actualImage = FileUtil.from(appContext, uri)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        try {
            compressedImageFile = Compressor(appContext).compressToFile(actualImage!!)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        try {
            Thread.sleep(2000)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

        return if (actualImage != null && compressedImageFile != null) {
            compressedImageFile
        } else {
            null
        }
    }

    private suspend fun UploadComprees(filepath: Uri, compressedImagePath: Uri, position: Int) {

        /*Tasks.await(storageRef.child("testing_compress_wallpaper/" + UUID.randomUUID().toString()).putFile(compressedImagePath))

        storageRef.child("testing_compress_wallpaper/" + UUID.randomUUID().toString()).putFile(compressedImagePath).continueWithTask { uploadTask->
            if (!uploadTask.isSuccessful){
                throw uploadTask.exception!!
            }

            return@continueWithTask storageRef.downloadUrl
        }*/
        storageRef.child("testing_compress_wallpaper/" + UUID.randomUUID().toString()).putFile(compressedImagePath).addOnSuccessListener {

        }.addOnFailureListener(OnFailureListener {
            false
        }).addOnProgressListener(OnProgressListener {
            val progress = 100.0 * it.getBytesTransferred() / it
                    .getTotalByteCount()

            Log.d("MultiUploadWork", "Progress_Compressed postion " + position + " :" + progress)

        })
    }

}
