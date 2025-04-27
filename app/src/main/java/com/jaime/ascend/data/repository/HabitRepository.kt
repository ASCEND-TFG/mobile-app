package com.jaime.ascend.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.jaime.ascend.data.models.BadHabit
import com.jaime.ascend.data.models.GoodHabit
import kotlinx.coroutines.tasks.await

class HabitRepository(
    private val firestore: FirebaseFirestore
) {
    suspend fun getGoodHabitsByCategory(categoryId: String): List<GoodHabit> {
        return try {
            val categoryRef = firestore.collection("categories").document(categoryId)
            println("Buscando hábitos con referencia: ${categoryRef.path}")
            val result = firestore.collection("ghabit_templates")
                .whereEqualTo("category", categoryRef)
                .get()
                .await()

            println("Documentos encontrados: ${result.size()}")
            result.forEach { println("   - ${it.data["name"]}") }

            result.toObjects(GoodHabit::class.java)
        } catch (e: Exception) {
            println("Error: ${e.message}")
            emptyList()
        }
    }

    suspend fun getBadHabitsByCategory(categoryId: String): List<BadHabit> {
        return firestore.collection("bhabit_templates")
            .whereEqualTo("categoryId", categoryId)
            .get()
            .await()
            .toObjects(BadHabit::class.java)
    }

    suspend fun searchGoodHabits(query: String): List<GoodHabit> {
        return firestore.collection("ghabit_templates")
            .get()
            .await()
            .toObjects(GoodHabit::class.java)
            .filter { it.name.values.any { name -> name.contains(query, ignoreCase = true) } }
    }

    suspend fun searchBadHabits(query: String): List<BadHabit> {
        return firestore.collection("bhabit_templates")
            .get()
            .await()
            .toObjects(BadHabit::class.java)
            .filter { it.name.values.any { name -> name.contains(query, ignoreCase = true) } }
    }
}