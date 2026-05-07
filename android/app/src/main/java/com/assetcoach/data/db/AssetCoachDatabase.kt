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
import net.sqlcipher.database.SupportFactory

@Database(
    entities = [TransactionEntity::class, CategoryEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AssetCoachDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao

    companion object {
        private const val DB_NAME = "assetcoach.enc.db"

        @Volatile private var INSTANCE: AssetCoachDatabase? = null

        fun get(context: Context): AssetCoachDatabase = INSTANCE ?: synchronized(this) {
            INSTANCE ?: build(context).also { INSTANCE = it }
        }

        private fun build(context: Context): AssetCoachDatabase {
            // Phase 2.5: 이전 plain SQLite DB 가 있으면 삭제 (마이그레이션 단순화).
            // Phase 2.6: 정식 마이그레이션 — 평문 DB 의 데이터를 암호화 DB 로 복사.
            context.getDatabasePath("assetcoach.db").let { old ->
                if (old.exists()) {
                    old.delete()
                    context.getDatabasePath("assetcoach.db-shm").delete()
                    context.getDatabasePath("assetcoach.db-wal").delete()
                }
            }

            // 패스프레이즈는 EncryptedSharedPreferences 에서 가져옴 (Keystore 보호).
            val passphrase = DatabaseKeyManager(context).getOrCreatePassphrase()
            val factory = SupportFactory(passphrase)

            val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
            return Room.databaseBuilder(
                context.applicationContext,
                AssetCoachDatabase::class.java,
                DB_NAME
            )
                .openHelperFactory(factory)
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
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
