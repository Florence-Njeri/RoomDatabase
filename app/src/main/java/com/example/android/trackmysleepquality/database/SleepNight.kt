package com.example.android.trackmysleepquality.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
//Define sleep data entity that will create a table daily_sleep_quality_table
@Entity(tableName="daily_sleep_quality_table")
data class SleepNight(
        @PrimaryKey(autoGenerate = true)
        var nightId:Long=0L,
        @ColumnInfo(name="startTimeMilli")
        var startTimeMilli:Long=System.currentTimeMillis(),
        @ColumnInfo(name="endTimeMilli")
        var endTimeMilli:Long=startTimeMilli,
        @ColumnInfo(name="sleepQuality")
        var sleepQuality:Int=-1)