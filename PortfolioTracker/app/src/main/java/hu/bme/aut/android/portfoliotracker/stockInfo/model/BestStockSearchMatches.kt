package hu.bme.aut.android.portfoliotracker.stockInfo.model

data class BestStockSearchMatches(

    var bestMatches: Array<StockSearchItem>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BestStockSearchMatches

        if (!bestMatches.contentEquals(other.bestMatches)) return false

        return true
    }

    override fun hashCode(): Int {
        return bestMatches.contentHashCode()
    }
}
