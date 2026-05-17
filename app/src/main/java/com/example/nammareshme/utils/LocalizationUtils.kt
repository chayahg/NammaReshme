package com.example.nammareshme.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.nammareshme.R

object LocalizationUtils {
    @Composable
    fun getLocalizedBreed(breed: String): String {
        return when (breed) {
            "Bivoltine" -> stringResource(R.string.bivoltine)
            "Multivoltine" -> stringResource(R.string.multivoltine)
            "Cross Breed" -> stringResource(R.string.cross_breed)
            else -> breed
        }
    }

    @Composable
    fun getLocalizedMulberry(type: String): String {
        return when (type) {
            "V1" -> stringResource(R.string.v1)
            "S36" -> stringResource(R.string.s36)
            "G4" -> stringResource(R.string.g4)
            "Local" -> stringResource(R.string.local)
            else -> type
        }
    }

    @Composable
    fun getLocalizedStage(stage: String): String {
        return when (stage) {
            "1st Instar" -> stringResource(R.string.instar_1)
            "2nd Instar" -> stringResource(R.string.instar_2)
            "3rd Instar" -> stringResource(R.string.instar_3)
            "4th Instar" -> stringResource(R.string.instar_4)
            "5th Instar" -> stringResource(R.string.instar_5)
            "Cocooning" -> stringResource(R.string.cocooning)
            else -> stage
        }
    }
}
