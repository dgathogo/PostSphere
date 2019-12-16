package us.ait.postsphere

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_create_post.*
import us.ait.postsphere.ForumActivity.Companion.KEY_KEY
import us.ait.postsphere.data.Post
import java.io.ByteArrayOutputStream
import java.net.URLEncoder
import java.util.*

class CreatePostActivity : AppCompatActivity() {

    var uploadBitmap: Bitmap? = null

    companion object {
        private const val PERMISSION_REQUEST_CODE = 101
        private const val CAMERA_REQUEST_CODE = 102
    }

    var editMode = false
    private lateinit var postsCollection: CollectionReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_post)
        postsCollection = FirebaseFirestore.getInstance().collection("posts")

        var post = intent.extras?.getSerializable(ForumActivity.KEY_POST) as Post?
        var postKey = intent.getStringExtra(KEY_KEY)

        if (post != null) {
            editMode = true
            etBody.setText(post.body)
            etTitle.setText(post.title)
        }
        btnSend.setOnClickListener {
            if (uploadBitmap != null) {
                try {
                    uploadPostWithImage()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else {
                if (editMode) {
                    editPost(postKey!!)
                } else {
                    uploadPost()
                }
            }
        }

        btnAttach.setOnClickListener {
            startActivityForResult(
                Intent(MediaStore.ACTION_IMAGE_CAPTURE),
                CAMERA_REQUEST_CODE
            )
        }

        requestNeededPermission()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            uploadBitmap = data!!.extras!!.get("data") as Bitmap
            imgAttach.setImageBitmap(uploadBitmap)
            imgAttach.visibility = View.VISIBLE

            data.let {
                uploadBitmap = it.extras!!.get("data") as Bitmap
                imgAttach.setImageBitmap(uploadBitmap)
                imgAttach.visibility = View.VISIBLE
            }
        }
    }

    private fun requestNeededPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    android.Manifest.permission.CAMERA
                )
            ) {
                Toast.makeText(
                    this,
                    getString(R.string.permissions_explanation), Toast.LENGTH_SHORT
                ).show()
            }

            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.CAMERA),
                PERMISSION_REQUEST_CODE
            )
        } else {
            // we already have permission
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, getString(R.string.perm_granted), Toast.LENGTH_SHORT)
                        .show()
                } else {
                    Toast.makeText(this, getString(R.string.perm_not_granted), Toast.LENGTH_SHORT)
                        .show()
                    btnAttach.visibility = View.GONE
                }
            }
        }
    }

    private fun uploadPost(imageUrl: String = "") {
        var post = Post(
            FirebaseAuth.getInstance().currentUser!!.uid,
            FirebaseAuth.getInstance().currentUser!!.displayName!!,
            etTitle.text.toString(),
            etBody.text.toString(),
            imageUrl,
            mutableListOf()
        )

        postsCollection.add(post).addOnSuccessListener {
            Toast.makeText(
                this@CreatePostActivity,
                getString(R.string.upload_ok),
                Toast.LENGTH_LONG
            ).show()
            finish()
        }.addOnFailureListener {
            Toast.makeText(
                this@CreatePostActivity,
                getString(R.string.upload_failed, it.message),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    @Throws(Exception::class)
    private fun uploadPostWithImage() {
        val baos = ByteArrayOutputStream()
        uploadBitmap?.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val imageInBytes = baos.toByteArray()

        val storageRef = FirebaseStorage.getInstance().reference
        val newImage = URLEncoder.encode(UUID.randomUUID().toString(), "UTF-8") + ".jpg"
        val newImagesRef = storageRef.child("images/$newImage")


        newImagesRef.putBytes(imageInBytes)
            .addOnFailureListener {
                Toast.makeText(this@CreatePostActivity, it.message, Toast.LENGTH_SHORT).show()
            }.addOnSuccessListener {
                newImagesRef.downloadUrl.addOnCompleteListener { task -> uploadPost(task.result.toString()) }
            }
    }

    private fun editPost(postKey: String) {
        postsCollection.document(postKey)
            .update("title", etTitle.text.toString(), "body", etBody.text.toString())
        finish()
    }
}
