package us.ait.postsphere.data

data class Post(
    var postId: String = "",
    var postTitle: String = "",
    var postBody: String = "",
    var postComments: MutableList<Comment>
)

data class Comment(
    var commentId: String = "",
    var commentBody: String = "",
    var comments: MutableList<Comment>
)