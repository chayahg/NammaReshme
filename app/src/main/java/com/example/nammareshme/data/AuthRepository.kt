package com.example.nammareshme.data

import com.example.nammareshme.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    val currentUser: FirebaseUser?
        get() = auth.currentUser

    private fun phoneToEmail(phone: String): String {
        val cleanPhone = phone.replace(Regex("[^0-9]"), "")
        return "$cleanPhone@reshmeapp.com"
    }

    suspend fun register(name: String, phone: String, password: String): Result<User> {
        return try {
            val email = phoneToEmail(phone)
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user ?: throw Exception("User creation failed")
            
            val user = User(
                uid = firebaseUser.uid,
                name = name,
                phone = phone,
                email = email,
                isVerified = false,
                location = "",
                isGoogleUser = false
            )
            
            firestore.collection("users").document(firebaseUser.uid).set(user).await()
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun login(phone: String, password: String): Result<User> {
        return try {
            val email = phoneToEmail(phone)
            auth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = auth.currentUser ?: throw Exception("Login failed")
            
            val snapshot = firestore.collection("users").document(firebaseUser.uid).get().await()
            val user = snapshot.toObject(User::class.java) ?: throw Exception("User data not found")
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun forgotPassword(phone: String): Result<Unit> {
        return try {
            val email = phoneToEmail(phone)
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() {
        auth.signOut()
    }

    suspend fun getUserData(uid: String): Result<User> {
        return try {
            val snapshot = firestore.collection("users").document(uid).get().await()
            val user = snapshot.toObject(User::class.java) ?: throw Exception("User data not found")
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saveUserData(user: User): Result<Unit> {
        return try {
            firestore.collection("users").document(user.uid).set(user).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signInWithGoogle(idToken: String): Result<User> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = auth.signInWithCredential(credential).await()
            val firebaseUser = authResult.user ?: throw Exception("Google Sign-In failed")
            
            val snapshot = firestore.collection("users").document(firebaseUser.uid).get().await()
            val user = if (snapshot.exists()) {
                snapshot.toObject(User::class.java)!!
            } else {
                val newUser = User(
                    uid = firebaseUser.uid,
                    name = firebaseUser.displayName ?: "Google User",
                    phone = "",
                    email = firebaseUser.email ?: "",
                    profileImageUrl = firebaseUser.photoUrl?.toString(),
                    isVerified = true,
                    isGoogleUser = true
                )
                firestore.collection("users").document(firebaseUser.uid).set(newUser).await()
                newUser
            }
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
