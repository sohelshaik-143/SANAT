package com.civicguard.data.local

import androidx.room.*
import com.civicguard.data.local.entity.ComplaintEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ComplaintDao {
    @Query("SELECT * FROM complaints ORDER BY createdAt DESC")
    fun getAllComplaints(): Flow<List<ComplaintEntity>>

    @Query("SELECT * FROM complaints WHERE id = :id")
    suspend fun getComplaintById(id: String): ComplaintEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComplaints(complaints: List<ComplaintEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComplaint(complaint: ComplaintEntity)

    @Query("DELETE FROM complaints")
    suspend fun clearAll()
}

@Database(entities = [ComplaintEntity::class], version = 1, exportSchema = false)
abstract class CivicGuardDatabase : RoomDatabase() {
    abstract fun complaintDao(): ComplaintDao
}
