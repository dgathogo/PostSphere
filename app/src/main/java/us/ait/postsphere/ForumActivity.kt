package us.ait.postsphere

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import kotlinx.android.synthetic.main.activity_forum.*
import us.ait.postsphere.PostDetailActivity.Companion.EXTRA_POST_KEY
import us.ait.postsphere.adapter.PostAdapter
import us.ait.postsphere.adapter.PostAdapter.ItemClickListener
import us.ait.postsphere.data.Post


class ForumActivity : AppCompatActivity() {
    private lateinit var postsAdapter: PostAdapter

    companion object {
        const val KEY_POST = "KEY_TODO"
        const val KEY_COMMENT = "KEY_COMMENT"
        const val KEY_KEY = "KEY_KEY"
        const val VERTICAL_ITEM_SPACE = 8
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forum)
        setSupportActionBar(toolbar)

        fab.setOnClickListener {
            startActivity(Intent(this@ForumActivity, CreatePostActivity::class.java))
        }


        var clickListener = object : ItemClickListener {
            override fun onItemClicked(post: Post, key: String) {
                var intent = Intent(this@ForumActivity, PostDetailActivity::class.java)
                val bundle = Bundle()
                bundle.putSerializable(KEY_POST, post)
                intent.putExtras(bundle)

                intent.putExtra(EXTRA_POST_KEY, key)
                startActivity(intent)
            }
        }
        postsAdapter =
            PostAdapter(this, FirebaseAuth.getInstance().currentUser!!.uid, clickListener)

        var linLayoutManager = LinearLayoutManager(this)
        linLayoutManager.reverseLayout = true
        linLayoutManager.stackFromEnd = true

        rvPosts.layoutManager = linLayoutManager

        rvPosts.addItemDecoration(VerticalSpaceItemDecoration(VERTICAL_ITEM_SPACE))

        rvPosts.adapter = postsAdapter
        queryPosts()

    }

    fun editPost(post: Post, key: String) {

        var intent = Intent(this@ForumActivity, CreatePostActivity::class.java)
        val bundle = Bundle()
        bundle.putSerializable(KEY_POST, post)
        intent.putExtras(bundle)
        intent.putExtra(KEY_KEY, key)
        startActivity(intent)
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
                            getString(R.string.listen_error, e.message),
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
                                val post = dc.document.toObject(Post::class.java)
                                postsAdapter.updatePost(post, dc.document.id)
                            }
                            DocumentChange.Type.REMOVED -> {
                                postsAdapter.removePostByKey(dc.document.id)
                            }
                        }
                    }
                }
            })
    }

    class VerticalSpaceItemDecoration(private val verticalSpaceHeight: Int) :
        ItemDecoration() {
        override fun getItemOffsets(outRect: Rect, itemPosition: Int, parent: RecyclerView) {
            outRect.bottom = verticalSpaceHeight
            if (itemPosition != parent.adapter?.itemCount?.minus(1)) {
                outRect.bottom = verticalSpaceHeight
            }
        }

    }
}
