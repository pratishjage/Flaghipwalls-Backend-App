package com.pratishjage.wallpaperbakend

import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.android.gms.tasks.OnFailureListener
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.OnProgressListener
import com.google.firebase.storage.StorageReference
import com.pratishjage.wallpaperbakend.Cnstants.IMAGES_ARRAY
import java.io.File
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList
import id.zelory.compressor.Compressor
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.time.LocalDate
import kotlin.collections.HashMap

class MultiUploadWork(val appContext: Context, workparam: WorkerParameters) : Worker(appContext, workparam) {

    lateinit var mFiles: ArrayList<Uri>
    lateinit var db: FirebaseFirestore
    lateinit var storageRef: StorageReference
    lateinit var mapData: MutableMap<String, Any>
    lateinit var Wallpapername:String
    override fun doWork(): Result {

        db = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference();

        val imgs = inputData.getStringArray(IMAGES_ARRAY)

        Wallpapername= String()
        mapData = HashMap()



        mapData.putAll(inputData.keyValueMap)
        mapData.remove(IMAGES_ARRAY)
        mapData.put("created_at", FieldValue.serverTimestamp())
        Wallpapername=mapData.get("name").toString()
        val formatter = SimpleDateFormat("EEE MMM dd HH:mm:ss zzzz yyyy", Locale.US)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val temp = mapData.get("device_release_date").toString()
            val deviceDate = formatter.parse(temp)
                mapData.replace("device_release_date", Timestamp(deviceDate))


            val temp1 = mapData.get("os_release_date").toString()
            val osDate = formatter.parse(temp1)

                mapData.replace("os_release_date", Timestamp(osDate))



            val temp2 = mapData.get("release_date").toString()
            val wallDate = formatter.parse(temp2)

                mapData.replace("release_date", Timestamp(wallDate))
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }


        mFiles = ArrayList();
        imgs!!.forEach {
            val uri = Uri.fromFile(File(it))
            mFiles.add(uri)
        }
        runBlocking {
            mFiles.forEachIndexed { index, uri ->

                Log.d("upload_status", "compressing : " + index + " image");


                val compressedImage = compressImage(uri = uri, appContext = appContext)
                val file = Uri.fromFile(compressedImage)
                Log.d("upload_status", "compressing : " + index + " image");


                val compressStorageRef = storageRef.child("testing_compress_wallpaper/" + UUID.randomUUID().toString())

                val taskSnapshot = compressStorageRef.putFile(file).await()
                val progress = 100.0 * taskSnapshot.bytesTransferred / taskSnapshot
                        .totalByteCount
                Log.d("upload_status", "uploading : " + index + " image Progress :" + progress);
                val compressDownloadURI = taskSnapshot.storage.downloadUrl.await()
                val compressWallpaperUrl = compressDownloadURI.toString()
                Log.d("upload_status", "uploadeedCompress : " + index + " image " + compressWallpaperUrl)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    mapData.replace("compressed_imgurl", compressWallpaperUrl)
                }


                val OrignalWallStorageRef = storageRef.child("testing_debug_wallpaper/" + UUID.randomUUID().toString())

                val taskSnapshotDebuig = OrignalWallStorageRef.putFile(mFiles.get(index)).await()
                val debugprogress = 100.0 * taskSnapshotDebuig.bytesTransferred / taskSnapshotDebuig
                        .totalByteCount
                Log.d("upload_status", "uploadingOrignal : " + index + " image Progress :" + debugprogress);
                val OrignalDownloadURl = taskSnapshotDebuig.storage.downloadUrl.await()
                val OrignalWallpaperUrl = OrignalDownloadURl.toString()



                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    mapData.replace("imgurl", OrignalWallpaperUrl)
                    mapData.replace("name",Wallpapername+"_"+(index+1))

                }


                val addWallpaper = db.collection("testing_walls").add(mapData).await()


            }
        }
        return Result.success()

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


}
