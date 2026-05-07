package com.assetcoach.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.assetcoach.data.db.dao.CategoryDao
import com.assetcoach.data.db.dao.TransactionDao
import com.assetcoach.data.db.entity.CategoryEntity
import com.assetcoach.data.db.entity.TransactionEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@Database(
    entities = [TransactionEntity::class, CategoryEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AssetCoachDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao

    companion object {
        @Volatile private var INSTANCE: AssetCoachDatabase? = null

        fun get(context: Context): AssetCoachDatabase = INSTANCE ?: synchronized(this) {
            INSTANCE ?: build(context).also { INSTANCE = it }
        }

        private fun build(context: Context): AssetCoachDatabase {
            val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
            return Room.databaseBuilder(
                context.applicationContext,
                AssetCoachDatabase::class.java,
                "assetcoach.db"
            )
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        // 카테고리 시드
                        val instance = INSTANCE ?: return
                        scope.launch {
                            instance.categoryDao().insertAll(CategoryEntity.SEED)
                        }
                    }
                })
                .build()
        }
    }
}
