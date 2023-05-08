package hu.bme.aut.android.portfoliotracker.stockInfo.model



import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "stockHistory")
class StockHistory(symbol: String, quantity: Double, price: Double, date: LocalDate, isBuy: Boolean) {

    @ColumnInfo(name ="id") @PrimaryKey(autoGenerate = true) var id: Long? = null
    @ColumnInfo(name = "isBuy") var isBuy: Boolean
    @ColumnInfo(name = "symbol") var symbol : String
    @ColumnInfo(name = "quantity") var quantity: Double
    @ColumnInfo(name = "price") var price: Double
    @ColumnInfo(name = "date") var date: LocalDate

    init {
        this.isBuy=isBuy
        this.quantity=quantity
        this.symbol=symbol
        this.date=date
        this.price=price
    }

}