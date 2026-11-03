package fr.spamoff.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "personal")
data class Personal(@PrimaryKey val number: String, val allowed: Boolean)
@Entity(tableName = "rules", primaryKeys = ["value", "kind"])
data class Rule(val value: String, val kind: String)
@Entity(tableName = "metadata")
data class Metadata(@PrimaryKey val id: Int = 1, val version: Long, val updated: Long, val source: String)
@Entity(tableName = "stats")
data class Stats(@PrimaryKey val id: Int = 1, val rejected: Long = 0)
@Entity(tableName = "reports")
data class LocalReport(@PrimaryKey val number: String, val category: String, val comment: String)
data class Counts(val exact: Int, val prefixes: Int)
@Dao
interface SpamDao {
 @Query("SELECT * FROM personal ORDER BY allowed DESC, number") fun people(): Flow<List<Personal>>
 @Query("SELECT * FROM personal WHERE number=:number") suspend fun person(number: String): Personal?
 @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun put(person: Personal)
 @Query("DELETE FROM personal WHERE number=:number") suspend fun remove(number: String)
 @Query("SELECT EXISTS(SELECT 1 FROM rules WHERE kind='exact' AND value=:number)") suspend fun exact(number: String): Boolean
 @Query("SELECT EXISTS(SELECT 1 FROM rules WHERE kind='prefix' AND value IN (:prefixes))") suspend fun prefix(prefixes: List<String>): Boolean
 @Query("SELECT COALESCE(SUM(CASE WHEN kind='exact' THEN 1 ELSE 0 END),0) AS exact, COALESCE(SUM(CASE WHEN kind='prefix' THEN 1 ELSE 0 END),0) AS prefixes FROM rules") fun counts(): Flow<Counts?>
 @Query("SELECT * FROM metadata WHERE id=1") fun metadataFlow(): Flow<Metadata?>
 @Query("SELECT * FROM metadata WHERE id=1") suspend fun metadata(): Metadata?
 @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun putMetadata(metadata: Metadata)
 @Query("DELETE FROM rules") suspend fun clearRules()
 @Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun rules(rules: List<Rule>)
 @Query("SELECT rejected FROM stats WHERE id=1") fun rejected(): Flow<Long?>
 @Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun ensureStats(stats: Stats)
 @Query("UPDATE stats SET rejected=rejected+1 WHERE id=1") suspend fun incrementValue()
 @Transaction suspend fun increment() { ensureStats(Stats()); incrementValue() }
 @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun report(report: LocalReport)
 @Query("DELETE FROM reports WHERE number=:number") suspend fun removeReport(number: String)
}
@Database(entities=[Personal::class, Rule::class, Metadata::class, Stats::class, LocalReport::class], version=1, exportSchema=true)
abstract class SpamDatabase: RoomDatabase() { abstract fun dao(): SpamDao }
