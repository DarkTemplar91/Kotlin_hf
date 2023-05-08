package hu.bme.aut.android.portfoliotracker.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import hu.bme.aut.android.portfoliotracker.databinding.ItemHistoryListBinding
import hu.bme.aut.android.portfoliotracker.stockInfo.model.StockHistory
import java.text.DecimalFormatSymbols
import java.util.*


class HistoryItemAdapter : RecyclerView.Adapter<HistoryItemAdapter.HistoryViewHolder>() {

    private val items = mutableListOf<StockHistory>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder = HistoryViewHolder(
        ItemHistoryListBinding.inflate(LayoutInflater.from(parent.context),parent,false)
    )

    override fun getItemCount(): Int = items.size

    fun addItem(item: StockHistory) {
        items.add(item)
        notifyItemInserted(items.size - 1)

    }
    fun addItem(position: Int, item: StockHistory){
        items.add(position,item)
        notifyItemInserted(position)

    }
    fun removeItem(item : StockHistory){
        val pos = items.indexOf(item)
        items.remove(item)
        notifyItemRemoved(pos)

    }

    inner class HistoryViewHolder(val binding: ItemHistoryListBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val item =items[position]
        holder.binding.strTransactionType.text = if(item.isBuy) "Buy" else "Sell"
        val formattedQuantity = String.format("%.2f", item.quantity, DecimalFormatSymbols(Locale.ENGLISH))
        holder.binding.strAmount.text=formattedQuantity
        holder.binding.strSymbol.text = item.symbol
        val formattedPrice = String.format("%.2f", item.price, DecimalFormatSymbols(Locale.ENGLISH))
        holder.binding.strNetAmount.text = if(item.isBuy) "-$$formattedPrice" else "+$$formattedPrice"
        holder.binding.strNetAmount.setTextColor( if(item.isBuy) Color.RED else Color.GREEN )

    }


}