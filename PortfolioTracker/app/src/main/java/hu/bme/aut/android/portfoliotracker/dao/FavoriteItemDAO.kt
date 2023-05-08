package hu.bme.aut.android.portfoliotracker.dao

import androidx.room.*
import hu.bme.aut.android.portfoliotracker.stockInfo.model.StockSearchItem

@Dao
interface FavoriteItemDAO {
    @Query("SELECT * FROM favoriteItem")
    fun getAll(): List<StockSearchItem>

    @Insert
    fun insert(favorite: StockSearchItem): Long

    @Update
    fun update(favorite: StockSearchItem)

    @Delete
    fun deleteItem(favorite: StockSearchItem)
}