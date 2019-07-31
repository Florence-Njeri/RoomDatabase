/*
 * Copyright 2018, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.trackmysleepquality.sleeptracker

import android.app.Application
import android.content.res.Resources
import android.provider.SyncStateContract.Helpers.insert
import android.provider.SyncStateContract.Helpers.update
import android.text.method.TextKeyListener.clear
import androidx.lifecycle.*
import com.example.android.trackmysleepquality.database.SleepDatabaseDao
import com.example.android.trackmysleepquality.database.SleepNight
import com.example.android.trackmysleepquality.formatNights
import kotlinx.coroutines.*

/**
 * ViewModel for SleepTrackerFragment. Will access data in the db
 */
class SleepTrackerViewModel(
        val database: SleepDatabaseDao,
        application: Application) : AndroidViewModel(application) {
//Implement click handler for start button and use coroutines

    private var viewModelJob = Job()

    override fun onCleared() {
        super.onCleared()
//        Cancel all coroutine
        viewModelJob.cancel()
    }


    /**
     * Determine scope the couroutines will run on --
     * the thread the Coroutine  will run on
     */

    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
    private var tonight = MutableLiveData<SleepNight>()
    val nights = database.getAllNights()
    val nightString=Transformations.map(nights){
        nights ->
        formatNights(nights, application.resources)
    }
    private val _navigateToSleepQuality = MutableLiveData<SleepNight>()

    val navigateToSleepQuality: LiveData<SleepNight>
        get() = _navigateToSleepQuality
    /**
     * If tonight has not been set, then the START button should be visible.
     */
    val startButtonVisible = Transformations.map(tonight) {
        null == it
    }

    /**
     * If tonight has been set, then the STOP button should be visible.
     */
    val stopButtonVisible = Transformations.map(tonight) {
        null != it
    }

    /**
     * If there are any nights in the database, show the CLEAR button.
     */
    val clearButtonVisible = Transformations.map(nights) {
        it?.isNotEmpty()
    }
    fun doneNavigating() {
        _navigateToSleepQuality.value = null
    }
    init {
        initializeTonight()
    }

    private fun initializeTonight() {
uiScope.launch {
    tonight.value=getTonightFromDatabase()
}
    }

    private suspend fun getTonightFromDatabase(): SleepNight? {
return withContext(Dispatchers.IO){
    var night=database.getTonight()
 if(night?.endTimeMilli!=night?.startTimeMilli)   {
   night=null
 }
    night
}
    }
    fun onStartTracking(){
        uiScope.launch {
            val newNight=SleepNight()
            insert(newNight)
            tonight.value=getTonightFromDatabase()
        }
    }


    private suspend fun insert(night:SleepNight){
        withContext(Dispatchers.IO){
            database.insert(night)
        }
    }
    fun onStopTracking(){
        uiScope.launch {
            val oldNight=tonight.value?:return@launch
            oldNight.endTimeMilli=System.currentTimeMillis()

            update(oldNight)
            _navigateToSleepQuality.value = oldNight
        }
    }
    private suspend fun update(night:SleepNight){
        withContext(Dispatchers.IO){
            database.update(night)
        }
    }
    fun onClear(){
        uiScope.launch {
            clear()
            tonight.value=null
        }

    }
    suspend fun clear(){
        withContext(Dispatchers.IO){
            database.clear()
        }
    }
//    fun someWorkNeedsToBeDone(){
//        uiScope.launch {
//            suspendFunction()
//        }
//    }

//    private fun suspendFunction() {
//        withContext(Dispatchers.IO){
//            longRunningWork()
//        }
//    }
//
//    private fun longRunningWork(): Any {
//
//    }
}

