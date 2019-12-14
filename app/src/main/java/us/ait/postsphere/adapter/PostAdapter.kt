package us.ait.postsphere.adapter

//import com.bumptech.glide.Glide
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.post_row.view.*
import us.ait.postsphere.ForumActivity
import us.ait.postsphere.R
import us.ait.postsphere.data.Post

class PostAdapter(
    private val context: Context,
    private val uid: String,
    private val clickListener: View.OnClickListener
) : RecyclerView.Adapter<PostAdapter.ViewHolder>() {

    private var postsList = mutableListOf<Post>()
    private var postKeys = mutableListOf<String>()

    private var lastIndex = -1
    private val viewPool = RecyclerView.RecycledViewPool()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view = LayoutInflater.from(parent.context).inflate(R.layout.post_row, parent, false)
        return ViewHolder(view)

    }

    override fun getItemCount(): Int = postsList.size

    private fun setAnimation(viewToAnimate: View, position: Int) {
        if (position > lastIndex) {
            val animation = AnimationUtils.loadAnimation(
                context,
                android.R.anim.slide_in_left
            )
            viewToAnimate.startAnimation(animation)
            lastIndex = position
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val post = postsList[position]
        holder.tvAuthor.text = post.postAuthor
        holder.tvTitle.text = post.postTitle
        holder.tvBody.text = post.postBody
        holder.itemView.setOnClickListener(clickListener)
        setAnimation(holder.itemView, position)

        if (post.imgUrl.isEmpty()) {
            holder.ivPhoto.visibility = View.GONE
        } else {
            holder.ivPhoto.visibility = View.VISIBLE
            Glide.with(context).load(post.imgUrl).into(holder.ivPhoto)
        }
        // if this is my post message
        if (post.postId == uid) {
            holder.btnDelete.visibility = View.VISIBLE
            holder.btnDelete.setOnClickListener {
                removePost(holder.adapterPosition)
            }
        } else {
            holder.btnDelete.visibility = View.GONE
        }
        val childLayoutManager =
            LinearLayoutManager(holder.rvPostComments.context, RecyclerView.VERTICAL, false)
        val childAdapter = CommentAdapter(context, uid, post, null)
        childAdapter.addAll(post.postComments)

        holder.rvPostComments.apply {
            layoutManager = childLayoutManager
            adapter = childAdapter
            setRecycledViewPool(viewPool)
        }
        holder.btnComment.setOnClickListener {
            (context as ForumActivity).showCommentDialog()
        }
    }

    fun addPost(post: Post, key: String) {
        postsList.add(post)
        postKeys.add(key)
        notifyDataSetChanged()
    }

    private fun removePost(index: Int) {
        FirebaseFirestore.getInstance().collection("posts").document(
            postKeys[index]
        ).delete()

        postsList.removeAt(index)
        postKeys.removeAt(index)
        notifyItemRemoved(index)
    }

    fun removePostByKey(key: String) {
        val index = postKeys.indexOf(key)
        if (index != -1) {
            postsList.removeAt(index)
            postKeys.removeAt(index)
            notifyItemRemoved(index)
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.tvTitle
        val tvBody: TextView = itemView.tvBody
        val btnDelete: Button = itemView.btnDelete
        val ivPhoto: ImageView = itemView.ivPhoto
        val tvAuthor: TextView = itemView.tvAuthor
        val rvPostComments: RecyclerView = itemView.rvComments
        val btnComment: Button = itemView.btnComment
    }
}