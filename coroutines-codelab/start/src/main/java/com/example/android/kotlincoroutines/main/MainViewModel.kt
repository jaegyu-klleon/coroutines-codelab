/*
 * Copyright (C) 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.kotlincoroutines.main

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.kotlincoroutines.util.singleArgViewModelFactory
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


/**
 * MainViewModel designed to store and manage UI-related data in a lifecycle conscious way. This
 * allows data to survive configuration changes such as screen rotations. In addition, background
 * work such as fetching network results can continue through configuration changes and deliver
 * results after the new Fragment or Activity is available.
 *
 * @param repository the data source this ViewModel will fetch results from.
 */
class MainViewModel(private val repository: TitleRepository) : ViewModel() {

    companion object {
        /**
         * Factory for creating [MainViewModel]
         *
         * @param arg the repository to pass to [MainViewModel]
         */
        val FACTORY = singleArgViewModelFactory(::MainViewModel)
    }

    /**
     * 스낵바에 문자열 표시 요청
     *
     * 외부에서 참조 불가
     * 내부에서만 값 설정 가능
     */
    private val _snackBar = MutableLiveData<String?>()

    // get _snackBar 불변
    val snackbar: LiveData<String?>
        get() = _snackBar

    // LiveData 타이틀
    val title = repository.title

    // 위와 동일, 스피너 표시 여부
    private val _spinner = MutableLiveData<Boolean>(false)
    val spinner: LiveData<Boolean>
        get() = _spinner

    // 화면 클릭 한 횟수
    private var tapCount = 0

    // 위와 동일, "${탭 횟수} taps"
    private val _taps = MutableLiveData<String>("$tapCount taps")
    val taps: LiveData<String>
        get() = _taps

    /**
     * 화면 클릭 이벤트 핸들링
     * title refresh + tap count 증가
     */
    fun onMainViewClicked() {
//        refreshTitle()
        updateTaps()
    }


    // Dispatcher.MAIN
    private fun tapClick() {
        viewModelScope.launch {
            tapCount++
            delay(1000)
            _taps.postValue("$tapCount taps")
        }
    }

    val TAG = "asdf"

    // 잠깐 delay 후 탭 횟수 증가
    private fun updateTaps() {

////        예시
////        tapCount++
////        BACKGROUND.submit {
////            Log.d("asdf", Thread.currentThread().name) //  pool-3-thread-1 ,  pool-3-thread-2
////            Thread.sleep(1_000)
////            _taps.postValue("$tapCount taps")
////        }
//
////        Log.d("asdf", Thread.currentThread().name) // main
////        Thread.sleep(1_000)
////        _taps.postValue("$tapCount taps")
////        Log.d("asdf", "Thread : ${Thread.currentThread().name}") // main
//
//        viewModelScope.launch {
//            Log.d("asdf", "launch 시작 Thread : ${Thread.currentThread().name}") // main
//            tapCount++
//            Log.d("asdf", "$tapCount") // main
////            Thread.sleep(1_000)
////            delay(1000) 뺀거 한번 넣은거 한번.
//            _taps.postValue("$tapCount taps")
////            Log.d("asdf", "delay 완료")
//        }
//
//        Log.d("asdf", Thread.currentThread().name) // main
////        Thread.sleep(1_000)
//        _taps.postValue("$tapCount taps")
//        Log.d("asdf", "Thread : ${Thread.currentThread().name}") // main

        Log.d(TAG, "화면 클릭 Thread : ${Thread.currentThread().name}") // main
        tapCount++
        _taps.postValue("$tapCount taps")


        viewModelScope.launch() {
//            Log.d(TAG, "1")
//            val answer1 = networkCall() //3초 딜레이
//            Log.d(TAG, "2")
//            val answer2 = networkCall2()// 3초딜레이
//            Log.d(TAG, "3")
//            Log.d(TAG, "Answer1 is $answer1")
//            Log.d(TAG, "Answer2 is $answer2")

//            val time = measureTimeMillis {
                Log.d(TAG, "1 : ${Thread.currentThread().name}") // main
                val answer1 = networkCall() //3초 딜레이
                Log.d(TAG, "2 : ${Thread.currentThread().name}") // main
                val answer2 = networkCall2()// 3초딜레이
                Log.d(TAG, "3 : ${Thread.currentThread().name}") // main
                Log.d(TAG, "Answer1 is $answer1")
                Log.d(TAG, "Answer2 is $answer2")
//            }
//            Log.d(TAG, "Requests took $time ms.") //0 6초


//            val time = measureTimeMillis {
//                var answer1: String? = null
//                var answer2: String? = null
//
//                Log.d(TAG, "1")
//                val job1 = launch { answer1 = networkCall() }
//                Log.d(TAG, "2")
//                val job2 = launch { answer2 = networkCall2() }
//                Log.d(TAG, "3")
//
//                job1.join()
//                job2.join()
//
//                Log.d(TAG, "Answer1 is $answer1")
//                Log.d(TAG, "Answer2 is $answer2")
//            }
//            Log.d(TAG, "Requests took $time ms.") //0 6초

        }


    }

    suspend fun networkCall(): String {
        delay(3000L)
        return "Answer 1"
    }

    suspend fun networkCall2(): String {
        delay(3000L)
        return "Answer 2"
    }

    // UI에 스낵바가 표시된 직후에 호출. 스낵바 내림
    fun onSnackbarShown() {
        _snackBar.value = null
    }

    /**
     * Title 새로고침
     * title refresh callback 정의 / interface TitleRefreshCallback object 구현
     * onCompleted - 성공 시 스피너 숨김
     * onError - 오류 발생 시 스피너 숨김 + 스낵바(error 메세지가 포함 된) 표시
     * Refresh the title, showing a loading spinner while it refreshes and errors via snackbar.
     */
    fun refreshTitle() {
        launchDataLoad {
            repository.refreshTitle()
        }


//        _spinner.value = true
//        repository.refreshTitleWithCallbacks(object : TitleRefreshCallback {
//            override fun onCompleted() {
//                _spinner.postValue(false)
//            }
//
//            override fun onError(cause: Throwable) {
//                _snackBar.postValue(cause.message)
//                _spinner.postValue(false)
//            }
//        })
    }


    private fun launchDataLoad(suspendFuction: suspend () -> Unit): Job {
//        return viewModelScope.launch {
//            try {
//                _spinner.value = true
//                suspendFuction()
//            } catch (error: TitleRefreshError) {
//                _snackBar.value = error.message
//            } finally {
//                _spinner.value = false
//            }
//        }

        return viewModelScope.launch {
            try {
                _spinner.value = true
                repository.refreshTitle()
            } catch (error: TitleRefreshError) {
                _snackBar.value = error.message
            } finally {
                _spinner.value = false
            }
        }
    }
}

/**
 * 서버 api 콜
 * 먼저 오는 순서대로
 *
 * IO 스레드가 같으면 나머지 대기 하냐
 * 디스패처
 *
 */

//fun startWithoutJoin() {
//    runBlocking {
//        println("runBlocking start")
//        val job1 = launch(Dispatcher.IO) {
//            // thread name
//            delay(1000)
//            println("1 Launch start")
//        }
//        val job2 = launch(Dispatcher.IO) {
//            println("2 Launch start")
//        }
//        val job3 = launch {
//            println("3 Launch start")
//        }
//
//        java.lang.Thread.sleep(1000)
//        println("runBlocking end")
//    }
//}
//
//fun startWithYield() {
//    runBlocking {
//        println("runBlocking start")
//        launch(Dispatchers.IO) {
//            repeat(3) {
//                println("Launch start")
//                // yield()   // This is not needed anymore
//            }
//        }
//        yield()
//        println("runBlocking end")
//    }
//}

