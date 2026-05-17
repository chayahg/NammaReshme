package com.example.nammareshme.data

import com.example.nammareshme.ui.screens.BatchHistoryItem
import com.example.nammareshme.ui.screens.ClimateLog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class BatchRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val userId: String?
        get() = auth.currentUser?.uid

    suspend fun addBatch(batch: BatchHistoryItem): Result<Unit> {
        val uid = userId ?: return Result.failure(Exception("User not logged in"))
        return try {
            firestore.collection("users").document(uid)
                .collection("batches").document(batch.id).set(batch).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getBatches(): Flow<List<BatchHistoryItem>> = callbackFlow {
        val uid = userId
        if (uid == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val subscription = firestore.collection("users").document(uid)
            .collection("batches")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val batches = snapshot?.toObjects(BatchHistoryItem::class.java) ?: emptyList()
                trySend(batches)
            }
        awaitClose { subscription.remove() }
    }

    suspend fun addClimateLog(log: ClimateLog): Result<Unit> {
        val uid = userId ?: return Result.failure(Exception("User not logged in"))
        return try {
            val docRef = firestore.collection("users").document(uid)
                .collection("climate_logs").document()
            val finalLog = log.copy(id = docRef.id)
            docRef.set(finalLog).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getClimateLogs(): Flow<List<ClimateLog>> = callbackFlow {
        val uid = userId
        if (uid == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val subscription = firestore.collection("users").document(uid)
            .collection("climate_logs")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val logs = snapshot?.toObjects(ClimateLog::class.java) ?: emptyList()
                trySend(logs)
            }
        awaitClose { subscription.remove() }
    }
}
