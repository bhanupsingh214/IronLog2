package com.bhanu.ironlog.data.model.cloud

sealed class CloudResult<out T> {
    data class Success<T>(val data: T) : CloudResult<T>()
    data class Error(val message: String, val throwable: Throwable? = null) : CloudResult<Nothing>()
}
