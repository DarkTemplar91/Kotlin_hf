package hu.bme.aut.android.portfoliotracker.fragments

import android.content.ContentValues
import android.graphics.Color
import android.icu.text.DecimalFormatSymbols
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import hu.bme.aut.android.portfoliotracker.R
import hu.bme.aut.android.portfoliotracker.User
import hu.bme.aut.android.portfoliotracker.adapter.HistoryItemAdapter
import hu.bme.aut.android.portfoliotracker.databinding.FragmentStockBinding
import hu.bme.aut.android.portfoliotracker.stockInfo.model.GlobalQuote
import hu.bme.aut.android.portfoliotracker.stockInfo.model.StockCurrentState
import hu.bme.aut.android.portfoliotracker.stockInfo.model.StockOwned
import hu.bme.aut.android.portfoliotracker.stockInfo.network.NetworkManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.math.absoluteValue

class StockFragment : Fragment() {

    private lateinit var stockOwned: StockOwned
    private lateinit var binding : FragmentStockBinding
    private lateinit var callback: CallbackClass
    private lateinit var adapter: HistoryItemAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val id = arguments?.getInt("stockOwned")!!
        stockOwned = User.stocksOwned[id]
        binding = FragmentStockBinding.inflate(inflater)
        callback = CallbackClass()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.symbol.text = stockOwned.symbol
        binding.tvAmountOwned.text = stockOwned.amount.toString()
        binding.tvTotalMoneySpent.text = stockOwned.stockHistory.sumOf { x->x.price }.toString()
        initRecyclerView()

        binding.symbol.setOnClickListener{
            val bundle = Bundle()
            bundle.putString("symbol", stockOwned.symbol)
            findNavController().navigate(R.id.action_stockFragment_to_infoFragment,bundle)
        }

        val stockInfo = NetworkManager.getStockCurrentState(stockOwned.symbol)
        stockInfo.enqueue(callback)


    }

    private fun fillValue(stock: StockCurrentState?){
        binding.tvPrice.text = stock?.price.toString()
        val currentValue = stock?.price?.toDouble()!! * stockOwned.amount
        binding.tvCurrentValue.text = String.format("%.2f",currentValue,
            DecimalFormatSymbols(Locale.ENGLISH))
        var changePercent: Double = (currentValue.toString().toDouble() / binding.tvTotalMoneySpent.text.toString().toDouble()) * 100
        changePercent -=100.0

        val change: Double = currentValue - binding.tvTotalMoneySpent.text.toString().toDouble()
        val sign: String = if(change < 0){
            "-"
        } else{
            "+"
        }
        val stringChange = resources.getString(R.string.change_with_variable, change.absoluteValue, changePercent.absoluteValue,sign)
        binding.tvChange.text = stringChange
        binding.tvChange.setTextColor(if(change < 0) Color.RED else Color.GREEN)
    }

    private fun initRecyclerView() {
        adapter = HistoryItemAdapter()
        binding.rvHistory.layoutManager = LinearLayoutManager(this.context)
        binding.rvHistory.adapter = adapter
        loadStockHistory()
    }
    private fun loadStockHistory(){
        activity?.runOnUiThread{
            for(item in stockOwned.stockHistory){
                adapter.addItem(item)
            }
        }
    }

    private inner class CallbackClass : Callback<GlobalQuote?> {
        override fun onResponse(
            call: Call<GlobalQuote?>,
            response: Response<GlobalQuote?>
        ) {
            if (response.isSuccessful) {
                val responseBody = response.body()
                if (responseBody == null || responseBody.stockCurrentState == null) {
                    Log.d(ContentValues.TAG, "Response was null: " + response.message())
                    if(responseBody?.Note != null)
                    {
                        Snackbar.make(binding.root,"Error: Could not fetch data. Free API Calls may have been used up",
                            Snackbar.LENGTH_LONG).show()
                    }

                } else {
                    fillValue(response.body()?.stockCurrentState)

                }
            } else {
                Log.d(ContentValues.TAG, "Error: " + response.message())
                Snackbar.make(binding.root,"Error: Something went wrong", Snackbar.LENGTH_LONG).show()
            }
        }

        override fun onFailure(
            call: Call<GlobalQuote?>,
            throwable: Throwable
        ) {
            throwable.printStackTrace()
            Snackbar.make(binding.root,"Network request error occurred, check LOG", Snackbar.LENGTH_LONG).show()
        }
    }

}