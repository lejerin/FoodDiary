package lej.happy.fooddiary.data.Model

import lej.happy.fooddiary.data.db.entity.Post

data class ReviewRank (
    var post: Post,
    var num: Int = 0,
    var best: Int = 0,
    var good: Int = 0,
    var bad: Int = 0

)
