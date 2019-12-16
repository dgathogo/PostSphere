package us.ait.postsphere

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_post_detail.*
import kotlinx.android.synthetic.main.post_row.view.*
import us.ait.postsphere.ForumActivity.Companion.KEY_POST
import us.ait.postsphere.data.Comment
import us.ait.postsphere.data.Post
import java.util.*

class PostDetailActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var postReference: DatabaseReference
    private lateinit var commentsReference: DatabaseReference

    private var postListener: ValueEventListener? = null
    private var adapter: CommentAdapter? = null
    private lateinit var user: FirebaseUser
    private var editIndex = -1

    companion object {

        private const val TAG = "PostDetailActivity"
        const val EXTRA_POST_KEY = "post_key"
        const val TAG_COMMENT_DIALOG = "TAG_COMMENT_DIALOG"
        const val TAG_COMMENT_EDIT = "TAG_COMMENT_EDIT"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        user = FirebaseAuth.getInstance().currentUser!!
        setContentView(R.layout.activity_post_detail)

        var postKey = intent.getStringExtra(EXTRA_POST_KEY)
            ?: throw IllegalArgumentException("Must pass EXTRA_POST_KEY")

        postReference = FirebaseDatabase.getInstance().reference
            .child("posts").child(postKey)

        commentsReference = FirebaseDatabase.getInstance().reference
            .child("comments").child(postKey)

        var post = intent.extras?.getSerializable(KEY_POST) as Post
        initView(post)

        btnComment.setOnClickListener(this)
        rvComments.layoutManager = LinearLayoutManager(this)
    }

    private fun initView(post: Post) {
        tvTitle.text = post.title
        tvBody.text = post.body
        tvAuthor.text = post.author
    }

    public override fun onStart() {
        super.onStart()

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                val post = dataSnapshot.getValue(Post::class.java)

                post?.let {
                    tvAuthor.text = it.author
                    tvTitle.text = it.title
                    tvBody.text = it.body
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
                Toast.makeText(
                    baseContext, "Failed to load post.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        postReference.addValueEventListener(postListener)

        this.postListener = postListener

        adapter = CommentAdapter(this, user.uid, commentsReference)
        rvComments.addItemDecoration(ForumActivity.VerticalSpaceItemDecoration(ForumActivity.VERTICAL_ITEM_SPACE))
        rvComments.adapter = adapter
    }

    public override fun onStop() {
        super.onStop()
        postListener?.let {
            postReference.removeEventListener(it)
        }
        adapter?.cleanupListener()
    }

    override fun onClick(v: View) {
        val i = v.id
        if (i == R.id.btnComment) {
//            postComment()
            showCommentDialog()

        }
    }

    private fun showCommentDialog() {
        CommentDialog().show(supportFragmentManager, TAG_COMMENT_DIALOG)
    }

    fun showEditCommentDialog(commentEdit: Comment, position: Int) {
        editIndex = position
        val editDialog = CommentDialog()
        val bundle = Bundle()
        bundle.putSerializable(ForumActivity.KEY_COMMENT, commentEdit)
        editDialog.arguments = bundle

        editDialog.show(supportFragmentManager, TAG_COMMENT_EDIT)
    }

    fun postComment(text: String) {
        val uid = user.uid
        FirebaseDatabase.getInstance().reference.child("users").child(uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val authorName = user.displayName!!
                    val comment = Comment(uid, authorName, text)
                    commentsReference.push().setValue(comment)
                }

                override fun onCancelled(databaseError: DatabaseError) {
                }
            })
    }

    fun removeComment(key: String) {
        commentsReference.child(key).removeValue()
    }

    private class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val btnDelete: Button = itemView.btnDelete
        fun bind(comment: Comment) {
            itemView.tvAuthor.text = comment.author
            itemView.tvBody.text = comment.text
        }
    }

    private class CommentAdapter(
        private val context: Context,
        private val uid: String,
        private val databaseReference: DatabaseReference
    ) : RecyclerView.Adapter<CommentViewHolder>() {

        private val childEventListener: ChildEventListener?

        private val commentIds = ArrayList<String>()
        private val comments = ArrayList<Comment>()

        init {
            val childEventListener = object : ChildEventListener {
                override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {
                    Log.d(TAG, "onChildAdded:" + dataSnapshot.key!!)

                    val comment = dataSnapshot.getValue(Comment::class.java)
                    commentIds.add(dataSnapshot.key!!)
                    comments.add(comment!!)
                    notifyItemInserted(comments.size - 1)
                }

                override fun onChildChanged(
                    dataSnapshot: DataSnapshot,
                    previousChildName: String?
                ) {
                    Log.d(TAG, "onChildChanged: ${dataSnapshot.key}")
                    val newComment = dataSnapshot.getValue(Comment::class.java)
                    val commentKey = dataSnapshot.key

                    val commentIndex = commentIds.indexOf(commentKey)
                    if (commentIndex > -1 && newComment != null) {
                        comments[commentIndex] = newComment

                        notifyItemChanged(commentIndex)
                    } else {
                        Log.w(TAG, "onChildChanged:unknown_child: $commentKey")
                    }
                }

                override fun onChildRemoved(dataSnapshot: DataSnapshot) {
                    Log.d(TAG, "onChildRemoved:" + dataSnapshot.key!!)
                    val commentKey = dataSnapshot.key

                    val commentIndex = commentIds.indexOf(commentKey)
                    if (commentIndex > -1) {
                        commentIds.removeAt(commentIndex)
                        comments.removeAt(commentIndex)

                        notifyItemRemoved(commentIndex)
                    } else {
                        Log.w(TAG, "onChildRemoved:unknown_child:" + commentKey!!)
                    }
                }

                override fun onChildMoved(dataSnapshot: DataSnapshot, previousChildName: String?) {
                    Log.d(TAG, "onChildMoved:" + dataSnapshot.key!!)
//                    val movedComment = dataSnapshot.getValue(Comment::class.java)
//                    val commentKey = dataSnapshot.key
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.w(TAG, "postComments:onCancelled", databaseError.toException())
                    Toast.makeText(
                        context, "Failed to load comments.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            databaseReference.addChildEventListener(childEventListener)
            this.childEventListener = childEventListener
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
            val inflater = LayoutInflater.from(context)
            val view = inflater.inflate(R.layout.comment_row, parent, false)
            return CommentViewHolder(view)
        }

        override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
            holder.bind(comments[position])
            if (comments[position].uid == uid) {
                holder.btnDelete.visibility = View.VISIBLE
                holder.btnDelete.setOnClickListener {
                    (context as PostDetailActivity).removeComment(commentIds[position])
                }
            } else {
                holder.btnDelete.visibility = View.GONE
            }
        }

        override fun getItemCount(): Int = comments.size

        fun cleanupListener() {
            childEventListener?.let {
                databaseReference.removeEventListener(it)
            }
        }
    }
}
