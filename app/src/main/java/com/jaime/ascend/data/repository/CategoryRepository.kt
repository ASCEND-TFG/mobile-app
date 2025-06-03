package com.jaime.ascend.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.jaime.ascend.data.models.Category
import kotlinx.coroutines.tasks.await

/**
 * Repository for categories.
 * @param firestore The Firebase Firestore instance.
 * @return A list of categories.
 * @throws Exception if the retrieval fails.
 * @author Jaime Martínez Fernández
 */
class CategoryRepository(private val firestore: FirebaseFirestore) {
    suspend fun getCategories(): List<Category> {
        return firestore.collection("categories").get().await().documents.mapNotNull { doc ->
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

}