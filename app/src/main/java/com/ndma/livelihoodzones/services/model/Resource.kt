package com.ndma.livelihoodzones.services.model

data class Resource<out T>(val status: Status, val data: T?, val message: String?) {
    companion object {
        fun <T> success(data: T): Resource<T> {
            return Resource(Status.SUCCESS, data, null)
        }

        fun <T> error(message: String?, data: T): Resource<T> {
            return Resource(Status.ERROR, data, message)
        }

        fun <T> unauthorised(message: String?, data: T): Resource<T> {
            return Resource(Status.UNAUTHORISED, data, message)
        }

        fun <T> unprocessableEntity(message: String?, data: T): Resource<T> {
            return Resource(Status.UNPROCESSABLE_ENTITY, data, message)
        }

        fun <T> loading(data: T?): Resource<T> {
            return Resource(Status.LOADING, data, null)
        }
    }
}