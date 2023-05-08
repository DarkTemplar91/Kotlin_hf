package hu.bme.aut.android.portfoliotracker.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import co.dift.ui.SwipeToAction
import hu.bme.aut.android.portfoliotracker.R
import hu.bme.aut.android.portfoliotracker.databinding.ItemSearchListBinding
import hu.bme.aut.android.portfoliotracker.stockInfo.model.StockSearchItem


class SearchItemAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    private val items = mutableListOf<StockSearchItem>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder = SearchViewHolder(
        ItemSearchListBinding.inflate(LayoutInflater.from(parent.context),parent,false)
    )


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val searchItem = items[position]
        val vh: SearchViewHolder = holder as SearchViewHolder
        vh.nameView?.text = searchItem.name!!
        vh.symbolView?.text = searchItem.symbol!!
        vh.data = searchItem

        if(position % 2 ==1){
            holder.cardView?.setCardBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.item_background_alternate))
        } else{
            holder.cardView?.setCardBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.item_background_primary))

        }


    }
    override fun getItemCount(): Int = items.size

    interface SearchItemClickListener {
        fun onItemChanged(item: StockSearchItem)
    }

    fun addItem(item: StockSearchItem) {
        items.add(item)
        notifyItemInserted(items.size - 1)
    }
    fun addItem(position: Int, item: StockSearchItem){
        items.add(position,item)
        notifyItemInserted(position)
    }
    fun removeItem(item : StockSearchItem){
        val pos = items.indexOf(item)
        items.remove(item)
        notifyItemRemoved(pos)
    }

    fun getIndex(item: StockSearchItem): Int {
        return items.indexOf(item)
    }

    private inner class SearchViewHolder(binding: ItemSearchListBinding) : SwipeToAction.ViewHolder<StockSearchItem>(binding.root)
    {
        var symbolView: TextView? = null
        var nameView: TextView? = null
        var cardView: CardView? = null
        init {
            symbolView = binding.strSymbol
            nameView = binding.strPrice
            cardView = binding.cardView

        }
    }

}