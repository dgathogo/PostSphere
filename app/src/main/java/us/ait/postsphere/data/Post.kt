package us.ait.postsphere.data

import java.io.Serializable

data class Post(
    var postId: String = "",
    var postAuthor: String = "",
    var postTitle: String = "",
    var postBody: String = "",
    var imgUrl: String = "",
    var postComments: MutableList<Comment> = ArrayList()
) :Serializable

data class Comment(
    var commentId: String = "",
    var commentAuthor: String = "",
    var commentBody: String = "",
    var comments: MutableList<Comment> = ArrayList()
) :Serializable