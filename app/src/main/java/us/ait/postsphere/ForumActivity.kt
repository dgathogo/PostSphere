package us.ait.postsphere

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import kotlinx.android.synthetic.main.activity_forum.*
import us.ait.postsphere.adapter.PostAdapter
import us.ait.postsphere.data.Comment
import us.ait.postsphere.data.Post

class ForumActivity : AppCompatActivity(), CommentDialog.CommentHandler {
    private lateinit var postsAdapter: PostAdapter

    companion object {
        const val KEY_POST = "KEY_TODO"
        const val KEY_COMMENT = "KEY_COMMENT"
        const val KEY_STARTED = "KEY_STARTED"
        const val TAG_COMMENT_DIALOG = "TAG_COMMENT_DIALOG"
        const val TAG_COMMENT_EDIT = "TAG_COMMENT_EDIT"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forum)
        setSupportActionBar(toolbar)
        fab.setOnClickListener {
            startActivity(Intent(this@ForumActivity, CreatePostActivity::class.java))
        }


        postsAdapter =
            PostAdapter(this, FirebaseAuth.getInstance().currentUser!!.uid, View.OnClickListener {})

        var linLayoutManager = LinearLayoutManager(this)
        linLayoutManager.reverseLayout = true
        linLayoutManager.stackFromEnd = true

        recyclerPosts.layoutManager = linLayoutManager

        recyclerPosts.adapter = postsAdapter
        addPosts()
//        queryPosts()

    }

    fun addPosts() {

        var post = Post("aljsdf", "Daniel", "Test Post", "This is just a test", "")
        post.postComments = mutableListOf(
            Comment("laskdjdf", "Daniel", "No Comment really"),
            Comment(
                "zxnvzcxmnsdf",
                "Toudo",
                "Another one",
                mutableListOf(
                    Comment("laskdjdf", "Daniel", "No Comment really"),
                    Comment("laskdjdf", "Daniel", "No Comment really")
                )
            )
        )
        postsAdapter.addPost(post, "aldkhfasdf")
    }

    fun showCommentDialog() {
        CommentDialog().show(supportFragmentManager,TAG_COMMENT_DIALOG)
    }

    override fun commentCreated(item: Comment) {

    }

    override fun commentUpdated(item: Comment) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
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
