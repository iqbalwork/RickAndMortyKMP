package com.rickandmorty.app.core.util

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ResultTest {

    @Test
    fun success_map_transformsDataCorrectly() {
        val result: Result<Int, DataError.Network> = Result.Success(10)
        val mapped = result.map { it * 2 }

        assertTrue(mapped is Result.Success)
        assertEquals(20, mapped.data)
    }

    @Test
    fun success_onSuccess_invokesCallback() {
        val result: Result<String, DataError.Network> = Result.Success("Rick")
        var receivedData: String? = null
        var errorInvoked = false

        result
            .onSuccess { receivedData = it }
            .onError { errorInvoked = true }

        assertEquals("Rick", receivedData)
        assertFalse(errorInvoked)
    }

    @Test
    fun success_asEmptyDataResult_returnsSuccessUnit() {
        val result: Result<String, DataError.Local> = Result.Success("Morty")
        val emptyResult: EmptyResult<DataError.Local> = result.asEmptyDataResult()

        assertTrue(emptyResult is Result.Success)
        assertEquals(Unit, emptyResult.data)
    }

    @Test
    fun error_map_propagatesErrorWithoutTransformation() {
        val result: Result<Int, DataError.Network> = Result.Error(DataError.Network.SERVER_ERROR)
        var transformCalled = false
        val mapped = result.map {
            transformCalled = true
            it * 2
        }

        assertTrue(mapped is Result.Error)
        assertEquals(DataError.Network.SERVER_ERROR, mapped.error)
        assertFalse(transformCalled)
    }

    @Test
    fun error_onError_invokesCallback() {
        val result: Result<String, DataError.Local> = Result.Error(DataError.Local.DISK_FULL)
        var receivedError: DataError.Local? = null
        var successInvoked = false

        result
            .onSuccess { successInvoked = true }
            .onError { receivedError = it }

        assertEquals(DataError.Local.DISK_FULL, receivedError)
        assertFalse(successInvoked)
    }

    @Test
    fun error_asEmptyDataResult_returnsError() {
        val result: Result<String, DataError.Network> = Result.Error(DataError.Network.NO_INTERNET)
        val emptyResult: EmptyResult<DataError.Network> = result.asEmptyDataResult()

        assertTrue(emptyResult is Result.Error)
        assertEquals(DataError.Network.NO_INTERNET, emptyResult.error)
    }
}
