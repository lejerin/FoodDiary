package lej.happy.fooddiary.data.model

import lej.happy.fooddiary.data.local.db.entity.Post

data class ReviewRank (
    var post: Post,
    var num: Int = 0,
    var best: Int = 0,
    var good: Int = 0,
    var bad: Int = 0

)
