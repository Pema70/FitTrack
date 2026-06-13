package com.fittrack.ui.profile

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.fittrack.data.model.ProfileResponse
import com.fittrack.data.model.ProfileUpdateRequest
import com.fittrack.data.repository.AuthRepository
import com.fittrack.data.repository.ProfileRepository
import com.fittrack.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    @get:Rule val instantTask = InstantTaskExecutorRule()

    private val testDispatcher = UnconfinedTestDispatcher()
    private val profileRepo: ProfileRepository = mock()
    private val authRepo: AuthRepository = mock()
    private lateinit var vm: ProfileViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        vm = ProfileViewModel(profileRepo, authRepo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun fakeProfile() = ProfileResponse(
        userId = 1L,
        displayName = "Jan",
        email = "jan@test.com",
        weightKg = 80.0,
        heightCm = 180.0,
        birthDate = "1990-01-01",
        gender = "MALE",
        activityLevel = "MODERATE",
        goal = "MAINTAIN",
        dailyKcalGoal = 2000
    )

    @Test
    fun `loadProfile emits Success gdy repo zwraca dane`() = runTest {
        whenever(profileRepo.get()).thenReturn(Resource.Success(fakeProfile()))

        vm.loadProfile()

        val state = vm.profile.value
        assertTrue(state is Resource.Success)
        assertEquals("Jan", (state as Resource.Success).data.displayName)
    }

    @Test
    fun `loadProfile emits Error gdy repo rzuca blad`() = runTest {
        whenever(profileRepo.get()).thenReturn(Resource.Error("Brak polaczenia"))

        vm.loadProfile()

        val state = vm.profile.value
        assertTrue(state is Resource.Error)
        assertEquals("Brak polaczenia", (state as Resource.Error).message)
    }

    @Test
    fun `logout wywoluje authRepo logout`() = runTest {
        vm.logout()
        verify(authRepo).logout()
    }

    @Test
    fun `updateProfile wywoluje repo z poprawnymi danymi`() = runTest {
        val request = ProfileUpdateRequest(
            displayName = "Anna",
            weightKg = null,
            heightCm = null,
            birthDate = null,
            gender = null,
            activityLevel = null,
            goal = null
        )
        whenever(profileRepo.update(request)).thenReturn(Resource.Success(fakeProfile()))
        whenever(profileRepo.get()).thenReturn(Resource.Success(fakeProfile()))

        vm.updateProfile(request)

        verify(profileRepo).update(request)
    }
}