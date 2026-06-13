package com.fittrack.ui.auth

import com.fittrack.data.model.AuthResponse
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.fittrack.data.repository.AuthRepository
import com.fittrack.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    @get:Rule val instantTask = InstantTaskExecutorRule()
    private val testDispatcher = UnconfinedTestDispatcher()
    private val authRepo: AuthRepository = mock()
    private lateinit var vm: AuthViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        vm = AuthViewModel(authRepo)
    }

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `login sukces ustawia authState na Success`() = runTest {
        val fakeResponse = AuthResponse(
            accessToken = "fake_token",
            userId = 1L,
            email = "test@test.com"
        )
        whenever(authRepo.login("test@test.com", "pass123"))
            .thenReturn(Resource.Success(fakeResponse))

        vm.login("test@test.com", "pass123")

        assertTrue(vm.authState.value is Resource.Success)
    }

    @Test
    fun `login blad ustawia authState na Error`() = runTest {
        whenever(authRepo.login(any(), any()))
            .thenReturn(Resource.Error("Nieprawidlowe dane"))

        vm.login("zly@email.com", "zlehaslo")

        assertTrue(vm.authState.value is Resource.Error)
    }

    @Test
    fun `login z pustym emailem nie wywoluje repo`() = runTest {
        vm.login("", "haslo123")
        verify(authRepo, never()).login(any(), any())
    }

    @Test
    fun `login z pustym haslem nie wywoluje repo`() = runTest {
        vm.login("test@test.com", "")
        verify(authRepo, never()).login(any(), any())
    }
}