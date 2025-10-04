package fm.mrc.resumebuilder.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import fm.mrc.resumebuilder.data.converters.Converters
import fm.mrc.resumebuilder.data.dao.ResumeDao
import fm.mrc.resumebuilder.data.entity.ResumeEntity

/**
 * Room Database for Resume Builder app
 */
@Database(
    entities = [ResumeEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class ResumeDatabase : RoomDatabase() {
    
    abstract fun resumeDao(): ResumeDao
    
    companion object {
        @Volatile
        private var INSTANCE: ResumeDatabase? = null
        
        private const val DATABASE_NAME = "resume_database"
        
        /**
         * Get database instance using singleton pattern
         */
        fun getDatabase(context: Context): ResumeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ResumeDatabase::class.java,
                    DATABASE_NAME
                )
                    .fallbackToDestructiveMigration() // For development - remove in production
                    .build()
                INSTANCE = instance
                instance
            }
        }
        
        /**
         * Close database instance (useful for testing)
         */
        fun closeDatabase() {
            INSTANCE?.close()
            INSTANCE = null
        }
    }
}
