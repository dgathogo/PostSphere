package us.ait.postsphere.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
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
    private val clickListener: ItemClickListener
) : RecyclerView.Adapter<PostAdapter.ViewHolder>() {

    private var postsList = mutableListOf<Post>()
    private var postKeys = mutableListOf<String>()

    private var lastIndex = -1

    interface ItemClickListener {
        fun onItemClicked(post: Post, key: String)
    }

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
        holder.tvAuthor.text = post.author
        holder.tvTitle.text = post.title
        holder.tvBody.text = post.body
        holder.bind(post, clickListener, postKeys[position])
        setAnimation(holder.itemView, position)

        if (post.imgUrl.isEmpty()) {
            holder.ivPhoto.visibility = View.GONE
        } else {
            holder.ivPhoto.visibility = View.VISIBLE
            Glide.with(context).load(post.imgUrl).into(holder.ivPhoto)
        }

        if (post.uid == uid) {
            holder.btnDelete.visibility = View.VISIBLE
            holder.btnEdit.visibility = View.VISIBLE
            holder.btnDelete.setOnClickListener {
                removePost(holder.adapterPosition)
            }
            holder.btnEdit.setOnClickListener {
                (context as ForumActivity).editPost(post, postKeys[holder.adapterPosition])
            }
        } else {
            holder.btnDelete.visibility = View.GONE
            holder.btnEdit.visibility = View.GONE
        }
    }

    fun addPost(post: Post, key: String) {
        postsList.add(post)
        postKeys.add(key)
        notifyDataSetChanged()
    }

    private fun removePost(position: Int) {
        FirebaseFirestore.getInstance().collection(context.getString(R.string.db_posts)).document(
            postKeys[position]
        ).delete()

        postsList.removeAt(position)
        postKeys.removeAt(position)
        notifyItemRemoved(position)
    }

    fun removePostByKey(key: String) {
        val index = postKeys.indexOf(key)
        if (index != -1) {
            postsList.removeAt(index)
            postKeys.removeAt(index)
            notifyItemRemoved(index)
        }
    }

    fun updatePost(post: Post, key: String) {
        var position = postKeys.indexOf(key)
        postsList[position] = post
        notifyItemChanged(position)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.tvTitle
        val tvBody: TextView = itemView.tvBody
        val btnDelete: Button = itemView.btnDelete
        val ivPhoto: ImageView = itemView.ivPhoto
        val tvAuthor: TextView = itemView.tvAuthor
        val btnEdit: Button = itemView.btnEdit

        fun bind(post: Post, clickListener: ItemClickListener, key: String) {
            itemView.setOnClickListener {
                clickListener.onItemClicked(post, key)
            }
        }
    }
}