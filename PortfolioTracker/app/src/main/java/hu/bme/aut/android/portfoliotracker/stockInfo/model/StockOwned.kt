package hu.bme.aut.android.portfoliotracker.stockInfo.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "stocksOwned")
class StockOwned(symbol: String, amount: Double) {

    @ColumnInfo(name ="id") @PrimaryKey(autoGenerate = true) var id: Long? = null
    var symbol: String
    var amount: Double
    @Ignore
    var stockHistory = mutableListOf<StockHistory>()

    init {
        this.symbol=symbol
        this.amount=amount
    }

}