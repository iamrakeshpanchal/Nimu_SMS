package com.iamrakeshpanchal.nimusms.data.entities

import androidx.room.*
import java.util.*

@Entity(
    tableName = "groups",
    indices = [Index(value = ["name"], unique = true)]
)
data class GroupEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "name")
    val name: String,
    
    @ColumnInfo(name = "color")
    val color: Int,
    
    @ColumnInfo(name = "rule_type")
    val ruleType: String, // "keyword", "sender", "regex"
    
    @ColumnInfo(name = "rule_value")
    val ruleValue: String,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long = Date().time
)
