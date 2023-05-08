package hu.bme.aut.android.portfoliotracker.adapter

import android.icu.text.DecimalFormatSymbols
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import hu.bme.aut.android.portfoliotracker.R
import hu.bme.aut.android.portfoliotracker.User
import hu.bme.aut.android.portfoliotracker.databinding.ItemStockListBinding
import hu.bme.aut.android.portfoliotracker.stockInfo.model.StockOwned
import java.util.*

class StockItemAdapter(parent: Fragment) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val items = mutableListOf<StockOwned>()
    private val parent: Fragment

    init{
        this.parent=parent
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder = StockViewHolder(
        ItemStockListBinding.inflate(LayoutInflater.from(parent.context),parent,false)
    )

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val stockItem = items[position]
        val vh: StockItemAdapter.StockViewHolder = holder as StockItemAdapter.StockViewHolder
        vh.binding.idShareAmount.text = String.format("%.3f",stockItem.amount,
            DecimalFormatSymbols(Locale.ENGLISH)
        )
        vh.binding.idSymbol.text=stockItem.symbol
        vh.apply {
            with(vh.itemView){
                itemView.setOnClickListener{
                    val navController = Navigation.findNavController(itemView)
                    val bundle = Bundle()
                    bundle.putInt("stockOwned", User.stocksOwned.indexOf(stockItem))
                    navController.navigate(R.id.action_mainPageFragment_to_stockFragment,bundle)
                }
            }
        }
    }

    fun addItem(item: StockOwned) {
        items.add(item)
        notifyItemInserted(items.size - 1)

    }

    override fun getItemCount(): Int = items.count()

    private inner class StockViewHolder(val binding: ItemStockListBinding) : RecyclerView.ViewHolder(binding.root)

}