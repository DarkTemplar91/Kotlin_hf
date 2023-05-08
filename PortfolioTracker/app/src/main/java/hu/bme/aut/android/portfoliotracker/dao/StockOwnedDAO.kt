package hu.bme.aut.android.portfoliotracker.dao

import androidx.room.*
import hu.bme.aut.android.portfoliotracker.stockInfo.model.StockOwned

@Dao
interface StockOwnedDAO {

    @Query("SELECT * FROM stocksOwned WHERE id = :s_id")
    fun getStockWithID(s_id: Long?): StockOwned?

    @Query("SELECT * FROM stocksOwned")
    fun getAll(): List<StockOwned>

    @Insert
    fun insert(stockOwned: StockOwned): Long

    @Update
    fun update(stockOwned: StockOwned)

    @Delete
    fun deleteItem(stockOwned: StockOwned)
}