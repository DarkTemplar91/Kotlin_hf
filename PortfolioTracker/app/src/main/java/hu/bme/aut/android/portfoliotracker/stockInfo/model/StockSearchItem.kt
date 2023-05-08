package hu.bme.aut.android.portfoliotracker.stockInfo.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "favoriteItem")
data class StockSearchItem(

    @ColumnInfo(name ="id") @PrimaryKey(autoGenerate = true) var id: Long? = null,

    @ColumnInfo(name = "symbol")
    @SerializedName("1. symbol")
    var symbol: String?,
    @ColumnInfo(name = "name")
    @SerializedName("2. name")
    var name: String?,
    @ColumnInfo(name = "type")
    @SerializedName("3. type")
    var type: String?
){
    @Ignore
    constructor(symbol: String?, name: String?, type: String?) : this(id = null, symbol,name,type)
}
