package com.jaime.ascend.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.jaime.ascend.data.models.HabitTemplate
import kotlinx.coroutines.tasks.await

class TemplateRepository(private val firestore: FirebaseFirestore) {
    suspend fun getTemplatesByCategory(categoryId: String): List<HabitTemplate> {
        return try {
            // Get the category reference first
            val categoryRef = firestore.collection("categories").document(categoryId)

            firestore.collection("ghabit_templates")
                .whereEqualTo("category", categoryRef)
                .get()
                .await()
                .documents
                .mapNotNull { doc ->
                    doc.toObject(HabitTemplate::class.java)?.copy(id = doc.id)
                }
        } catch (e: Exception) {
            Log.e(
                "TemplateRepository",
                "getTemplatesByCategory: Error getting templates by category",
                e
            )
            emptyList()
        }
    }

    suspend fun getTemplateById(templateId: String): HabitTemplate? {
        return try {
            firestore.collection("ghabit_templates")
                .document(templateId)
                .get()
                .await()
                .toObject(HabitTemplate::class.java)
        } catch (e: Exception) {
            Log.e("TemplateRepository", "getTemplateById: Error getting template by id", e)
            null
        }

    }
}