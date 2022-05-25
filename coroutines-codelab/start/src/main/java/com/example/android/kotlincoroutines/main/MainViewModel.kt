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
        refreshTitle()
        updateTaps()
    }

    // 잠깐 delay 후 탭 횟수 증가
    private fun updateTaps() {
//        tapCount++
//        BACKGROUND.submit {
//            Log.d("asdf", Thread.currentThread().name) //  pool-3-thread-1 ,  pool-3-thread-2
//            Thread.sleep(1_000)
//            _taps.postValue("$tapCount taps")
//        }

//        tapCount++
//        Log.d("asdf", Thread.currentThread().name) // main
//        Thread.sleep(1_000)
//        _taps.postValue("$tapCount taps")

        viewModelScope.launch {
            tapCount++
            Log.d("asdf", "$tapCount") // main
            Log.d("asdf", "updateTaps 안 쓰레드 이름 ${Thread.currentThread().name}") // main
            delay(1000)
            _taps.postValue("$tapCount taps")
        }
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
        // TODO: Convert refreshTitle to use coroutines
        viewModelScope.launch {
            try {
                _spinner.value = true
                repository.refreshTitle()
            } catch (error: TitleRefreshError) {
                _snackBar.value = error.message
            } finally {
                _spinner.value = false
            }
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
}
