package cl.duoc.agenciaconnect

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * Regla JUnit 4 que reemplaza el Dispatcher.Main por un dispatcher de prueba
 * antes de cada test y lo restaura al finalizar.
 * Usamos UnconfinedTestDispatcher para que las corrutinas avancen de forma
 * eager sin necesidad de llamar a advanceUntilIdle() en cada prueba.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule : TestWatcher() {

    val testDispatcher = UnconfinedTestDispatcher()

    override fun starting(description: Description) {
        // Reemplazamos el dispatcher principal por el dispatcher de prueba.
        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: Description) {
        // Restauramos el dispatcher original al terminar cada test.
        Dispatchers.resetMain()
    }
}
