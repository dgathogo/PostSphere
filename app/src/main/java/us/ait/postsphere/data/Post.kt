package us.ait.postsphere.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "posts")
data class Post(
    @PrimaryKey(autoGenerate = true) var postId: Long?,
    @ColumnInfo(name = "title") var postTitle: String,
    @ColumnInfo(name = "body") var postBody: String?,
    @ColumnInfo(name = "comments") var postComments: MutableList<Comment>
) : Serializable

@Entity(tableName = "comments")
data class Comment(
    @PrimaryKey(autoGenerate = true) var commentId: Long?,
    @ColumnInfo(name = "text") var commentBody: String,
    @ColumnInfo(name = "comments") var comments: MutableList<Comment>
)