package us.ait.postsphere

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import kotlinx.android.synthetic.main.activity_forum.*
import us.ait.postsphere.adapter.PostAdapter
import us.ait.postsphere.data.Post

class ForumActivity : AppCompatActivity() {
    private lateinit var postsAdapter: PostAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forum)
        setSupportActionBar(toolbar)
        fab.setOnClickListener {
            startActivity(Intent(this@ForumActivity, CreatePostActivity::class.java))
        }

        postsAdapter = PostAdapter(this, FirebaseAuth.getInstance().currentUser!!.uid)

        var linLayoutManager = LinearLayoutManager(this)
        linLayoutManager.reverseLayout = true
        linLayoutManager.stackFromEnd = true

        recyclerPosts.layoutManager = linLayoutManager

        recyclerPosts.adapter = postsAdapter

        queryPosts()
    }


    private fun queryPosts() {
        val db = FirebaseFirestore.getInstance()
        val query = db.collection("posts")

        query.addSnapshotListener(
            object : EventListener<QuerySnapshot> {
                override fun onEvent(
                    querySnapshot: QuerySnapshot?,
                    e: FirebaseFirestoreException?
                ) {

                    if (e != null) {
                        Toast.makeText(
                            this@ForumActivity,
                            "listen error: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                        return
                    }

                    for (dc in querySnapshot!!.documentChanges) {
                        when (dc.type) {
                            DocumentChange.Type.ADDED -> {
                                val post = dc.document.toObject(Post::class.java)
                                postsAdapter.addPost(post, dc.document.id)
                            }
                            DocumentChange.Type.MODIFIED -> {
                                Toast.makeText(
                                    this@ForumActivity,
                                    "update: ${dc.document.id}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                            DocumentChange.Type.REMOVED -> {
                                postsAdapter.removePostByKey(dc.document.id)
                            }
                        }
                    }
                }
            })
    }
}
