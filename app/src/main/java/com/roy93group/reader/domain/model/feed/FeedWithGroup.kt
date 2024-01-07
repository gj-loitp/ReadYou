package com.roy93group.reader.domain.model.feed

import androidx.annotation.Keep
import androidx.room.Embedded
import androidx.room.Relation
import com.roy93group.reader.domain.model.group.Group

/**
 * A [feed] contains a [group].
 */

@Keep
data class FeedWithGroup(
    @Embedded
    var feed: Feed,
    @Relation(parentColumn = "groupId", entityColumn = "id")
    var group: Group,
)
