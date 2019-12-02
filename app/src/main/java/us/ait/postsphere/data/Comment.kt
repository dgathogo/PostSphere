package us.ait.postsphere.data

data class Comment(
    var commentId: String = "",
    var commentAuthour: String = "",
    var commentBody: String = "",
    var comments: MutableList<Comment>
)