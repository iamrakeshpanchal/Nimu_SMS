package com.iamrakeshpanchal.nimusms.data.dao

import androidx.room.*
import com.iamrakeshpanchal.nimusms.data.entities.GroupEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GroupDao {
    @Query("SELECT * FROM groups ORDER BY name ASC")
    fun getAllGroups(): Flow<List<GroupEntity>>
    
    @Query("SELECT * FROM groups WHERE id = :id")
    suspend fun getGroupById(id: Long): GroupEntity?
    
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertGroup(group: GroupEntity): Long
    
    @Update
    suspend fun updateGroup(group: GroupEntity)
    
    @Delete
    suspend fun deleteGroup(group: GroupEntity)
    
    @Query("DELETE FROM groups WHERE id = :id")
    suspend fun deleteGroupById(id: Long)
}
