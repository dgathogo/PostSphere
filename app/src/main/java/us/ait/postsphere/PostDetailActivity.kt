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
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_post_detail.*
import kotlinx.android.synthetic.main.comment_row.view.*
import kotlinx.android.synthetic.main.post_row.view.btnDelete
import kotlinx.android.synthetic.main.post_row.view.tvAuthor
import kotlinx.android.synthetic.main.post_row.view.tvBody
import us.ait.postsphere.ForumActivity.Companion.KEY_KEY
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

    companion object {

        private const val TAG = "PostDetailActivity"
        const val EXTRA_POST_KEY = "POST_KEY"
        const val TAG_COMMENT_DIALOG = "TAG_COMMENT_DIALOG"
        const val TAG_COMMENT_EDIT = "TAG_COMMENT_EDIT"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        user = FirebaseAuth.getInstance().currentUser!!
        setContentView(R.layout.activity_post_detail)

        var postKey = intent.getStringExtra(EXTRA_POST_KEY)
            ?: throw IllegalArgumentException(getString(R.string.error_key_missing))

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
        if (post.imgUrl.isEmpty()) {
            ivPhoto.visibility = View.GONE
        } else {
            ivPhoto.visibility = View.VISIBLE
            Glide.with(this).load(post.imgUrl).into(ivPhoto)
        }
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
                Log.w(TAG, getString(R.string.db_cancelled), databaseError.toException())
                Toast.makeText(
                    baseContext, getString(R.string.error_load_post),
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
            showCommentDialog()

        }
    }

    private fun showCommentDialog() {
        CommentDialog().show(supportFragmentManager, TAG_COMMENT_DIALOG)
    }

    fun showEditCommentDialog(commentEdit: Comment, key: String) {
        val editDialog = CommentDialog()
        val bundle = Bundle()
        bundle.putSerializable(ForumActivity.KEY_COMMENT, commentEdit)
        bundle.putString(KEY_KEY, key)
        editDialog.arguments = bundle
        editDialog.show(supportFragmentManager, TAG_COMMENT_EDIT)

    }

    fun postComment(text: String) {
        val uid = user.uid
        FirebaseDatabase.getInstance().reference.child(getString(R.string.users)).child(uid)
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

    fun updateComment(text: String, key: String) {
        commentsReference.child(key).child(getString(R.string.text)).setValue(text)
    }

    fun removeComment(key: String) {
        commentsReference.child(key).removeValue()
    }

    private class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val btnDelete: Button = itemView.btnDelete
        val btnEdit: Button = itemView.btnEditComment
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
                    val comment = dataSnapshot.getValue(Comment::class.java)
                    commentIds.add(dataSnapshot.key!!)
                    comments.add(comment!!)
                    notifyItemInserted(comments.size - 1)
                }

                override fun onChildChanged(
                    dataSnapshot: DataSnapshot,
                    previousChildName: String?
                ) {
                    val newComment = dataSnapshot.getValue(Comment::class.java)
                    val commentKey = dataSnapshot.key

                    val commentIndex = commentIds.indexOf(commentKey)
                    if (commentIndex > -1 && newComment != null) {
                        comments[commentIndex] = newComment

                        notifyItemChanged(commentIndex)
                    } else {
                    }
                }

                override fun onChildRemoved(dataSnapshot: DataSnapshot) {
                    val commentKey = dataSnapshot.key

                    val commentIndex = commentIds.indexOf(commentKey)
                    if (commentIndex > -1) {
                        commentIds.removeAt(commentIndex)
                        comments.removeAt(commentIndex)

                        notifyItemRemoved(commentIndex)
                    } else {
                    }
                }

                override fun onChildMoved(dataSnapshot: DataSnapshot, previousChildName: String?) {}

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.w(
                        TAG,
                        context.getString(R.string.db_cancelled),
                        databaseError.toException()
                    )
                    Toast.makeText(
                        context, context.getString(R.string.error_load_comments),
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

                holder.btnEdit.visibility = View.VISIBLE
                holder.btnEdit.setOnClickListener {
                    (context as PostDetailActivity).showEditCommentDialog(
                        comments[position],
                        commentIds[position]
                    )
                }
            } else {
                holder.btnDelete.visibility = View.GONE
                holder.btnEdit.visibility = View.GONE
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
