package com.jaime.ascend.data.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.jaime.ascend.viewmodel.GoodHabitsViewModel
import com.jaime.ascend.viewmodel.RewardsViewModel

class RewardsViewModelFactory(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val habitsViewModel: GoodHabitsViewModel
    // ghrepo
    // bhrepo
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RewardsViewModel::class.java)) {
            return RewardsViewModel(auth, firestore) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }





    // todo
    /*
    * - Arreglar las cosas que están en los viewmodels que deberían estar en el repository
    * - Implementar los BadHabits diferencia con los otros, tienen un contador y
    *   en la dificultad a parte de calcular xp y coins, tmb tiene lifeloss
    *
    *   Arreglar los vm para que no sean tan largos y para el rewardsvmF
    *   RVMF:
    *       -
    * */
}