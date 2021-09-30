package com.nikuolive.mpda.client.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class MPDConnectionProfile (
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var profileId: Long = 0,
    var name: String,
    var ipAddress: String,
    var password: String,
    var port: Int
)
