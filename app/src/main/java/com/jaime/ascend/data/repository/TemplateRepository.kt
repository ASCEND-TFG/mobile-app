package com.jaime.ascend.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.jaime.ascend.data.models.HabitTemplate
import kotlinx.coroutines.tasks.await

/**
 * Repository for templates.
 * @param firestore The Firebase Firestore instance.
 */
class TemplateRepository(private val firestore: FirebaseFirestore) {

    /**
     * Gets all bad habit templates.
     * @return A list of bad habit templates.
     * @throws Exception if the templates could not be retrieved.
     */
    suspend fun getAllBadHabitTemplates(): List<HabitTemplate> {
        return try {
            firestore.collection("bhabit_templates")
                .get()
                .await()
                .documents
                .mapNotNull { doc ->
                    doc.toObject(HabitTemplate::class.java)?.copy(id = doc.id)
                }
        } catch (
            e: Exception
        ) {
            Log.e("TemplateRepository", "getAllTemplates: Error getting templates", e)
            emptyList()
        }
    }

    /**
     * Gets all good habit templates.
     * @return A list of good habit templates.
     * @throws Exception if the templates could not be retrieved.
     */
    suspend fun getAllGoodHabitTemplates(): List<HabitTemplate> {
        return try {
            firestore.collection("ghabit_templates")
                .get()
                .await()
                .documents
                .mapNotNull { doc ->
                    doc.toObject(HabitTemplate::class.java)?.copy(id = doc.id)
                }
        } catch (
            e: Exception
        ) {
            Log.e("TemplateRepository", "getAllTemplates: Error getting templates", e)
            emptyList()
        }
       }

    /**
     * Gets bad habit templates by category.
     * @param categoryId The ID of the category.
     * @return A list of bad habit templates.
     * @throws Exception if the templates could not be retrieved.
     */
    suspend fun getBadHabitTemplatesByCategory(categoryId: String): List<HabitTemplate> {
        return try {
            val categoryRef = firestore.collection("categories").document(categoryId)

            firestore.collection("bhabit_templates")
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

    /**
     * Gets good habit templates by category.
     * @param categoryId The ID of the category.
     * @return A list of good habit templates.
     * @throws Exception if the templates could not be retrieved.
     */
    suspend fun getGoodHabitTemplatesByCategory(categoryId: String): List<HabitTemplate> {
        return try {
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

    /**
     * Gets a bad habit template by ID.
     * @param templateId The ID of the template.
     * @return A bad habit template.
     * @throws Exception if the template could not be retrieved.
     */
    suspend fun getBadHabitTemplateById(templateId: String): HabitTemplate? {
        return try {
            firestore.collection("bhabit_templates")
                .document(templateId)
                .get()
                .await()
                .toObject(HabitTemplate::class.java)
        } catch (e: Exception) {
            Log.e("TemplateRepository", "getTemplateById: Error getting template by id", e)
            null
        }

    }

    /**
     * Gets a good habit template by ID.
     * @param templateId The ID of the template.
     * @return A good habit template.
     * @throws Exception if the template could not be retrieved.
     */
    suspend fun getGoodHabitTemplateById(templateId: String): HabitTemplate? {
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