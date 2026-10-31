package com.example.carlog

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.carlog.ui.cars.CarDetailScreen
import com.example.carlog.ui.cars.CarListScreen
import com.example.carlog.ui.work.WorkFormScreen

@Composable
fun MainNavigation() {
  val backStack = rememberNavBackStack(CarList)

  NavDisplay(
    backStack = backStack,
    onBack = { backStack.removeLastOrNull() },
    entryProvider =
      entryProvider {
        entry<CarList> {
          CarListScreen(onOpenCar = { id -> backStack.add(CarDetail(id)) })
        }
        entry<CarDetail> { key ->
          CarDetailScreen(
            carId = key.carId,
            onBack = { backStack.removeLastOrNull() },
            onAddWork = { backStack.add(WorkForm(key.carId)) },
            onOpenWork = { entryId -> backStack.add(WorkForm(key.carId, entryId)) },
          )
        }
        entry<WorkForm> { key ->
          WorkFormScreen(
            carId = key.carId,
            entryId = key.entryId,
            onDone = { backStack.removeLastOrNull() },
          )
        }
      },
  )
}
