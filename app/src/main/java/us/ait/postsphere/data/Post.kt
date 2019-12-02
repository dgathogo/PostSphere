package us.ait.postsphere.data

data class Post(
    var postId: String = "",
    var postAuthor: String = "",
    var postTitle: String = "",
    var postBody: String = "",
    var imgUrl: String = "",
    var postComments: MutableList<Comment>
)

data class Comment(
    var commentId: String = "",
    var commentAuthour: String = "",
    var commentBody: String = "",
    var comments: MutableList<Comment>
)