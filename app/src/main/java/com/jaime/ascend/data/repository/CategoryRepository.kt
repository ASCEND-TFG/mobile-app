package com.jaime.ascend.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.jaime.ascend.data.models.Category
import com.jaime.ascend.data.models.GoodHabit
import kotlinx.coroutines.tasks.await

class CategoryRepository(private val firestore: FirebaseFirestore) {
    suspend fun getCategories(): List<Category> {
        return firestore.collection("categories")
            .get()
            .await()
            .documents
            .mapNotNull { doc ->
                try {
                    val data = doc.data ?: emptyMap()
                    Category(
                        id = doc.id,
                        name = (data["name"] as? Map<String, String>) ?: emptyMap(),
                        description = (data["description"] as? Map<String, String>) ?: emptyMap(),
                        icon = data["icon"] as? String ?: ""
                    )
                } catch (e: Exception) {
                    null
                }
            }
    }

    suspend fun searchCategories(query: String): List<Category> {
        return firestore.collection("categories").get().await()
            .toObjects(Category::class.java)
            .filter { habit ->
                habit.name.any { (_, value) ->
                    value.contains(query, ignoreCase = true)
                } ||
                        habit.description.any { (_, value) ->
                            value.contains(query, ignoreCase = true)
                        }
            }
    }

}