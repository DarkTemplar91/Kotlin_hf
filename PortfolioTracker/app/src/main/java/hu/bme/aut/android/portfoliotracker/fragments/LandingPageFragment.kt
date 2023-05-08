package hu.bme.aut.android.portfoliotracker.fragments

import android.app.AlertDialog
import android.app.SearchManager
import android.content.ContentValues
import android.content.DialogInterface
import android.database.Cursor
import android.database.MatrixCursor
import android.icu.text.DecimalFormatSymbols
import android.os.Bundle
import android.provider.BaseColumns
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.cursoradapter.widget.CursorAdapter
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import hu.bme.aut.android.portfoliotracker.R
import hu.bme.aut.android.portfoliotracker.User
import hu.bme.aut.android.portfoliotracker.adapter.StockItemAdapter
import hu.bme.aut.android.portfoliotracker.databinding.FragmentLandingPageBinding
import hu.bme.aut.android.portfoliotracker.stockInfo.model.BestStockSearchMatches
import hu.bme.aut.android.portfoliotracker.stockInfo.network.NetworkManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception
import java.util.*

class LandingPageFragment : Fragment() {
    private lateinit var binding : FragmentLandingPageBinding
    private lateinit var adapter : StockItemAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLandingPageBinding.inflate(inflater, container, false)
        initRecyclerView()
        return binding.root
    }

    private fun initRecyclerView() {
        adapter = StockItemAdapter(this)
        binding.rvStocksOwned.layoutManager = LinearLayoutManager(this.context)
        binding.rvStocksOwned.adapter = adapter
        loadStocksOwned()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?){
        binding.balanceText.text = String.format("%.2f",User.getBalance(),
            DecimalFormatSymbols(Locale.ENGLISH)
        )
        binding.buttonAddBalance.setOnClickListener {
            val alertDialog = AlertDialog.Builder(binding.root.context)

            alertDialog.setTitle("Add money to you account?")
            alertDialog.setMessage("Please select the amount you want to transfer to your account!\nYour current balance: ${User.getBalance()}")
            val viewDialog = layoutInflater.inflate(R.layout.dialog_transfer_money_activity, null, false)
            alertDialog.setView(viewDialog)
            val textView = viewDialog.findViewById<TextView>(R.id.buttonAddAmount)

            alertDialog.setPositiveButton("Ok") { _, _ ->
                run {
                    val stringValue = textView.text.toString()
                    if(stringValue.isEmpty())
                        return@run
                    val amount = textView.text.toString().toDouble()
                    User.addToBalance(amount)
                    binding.balanceText.text = User.getBalance().toString()

                }
            }
            alertDialog.setNegativeButton(
                "Cancel"
            ) { _, _ -> }

            alertDialog.show()
        }
        binding.buttonDeductBalance.setOnClickListener {
            val alertDialog = AlertDialog.Builder(binding.root.context)

            alertDialog.setTitle("Deduct money from you account?")
            alertDialog.setMessage("Please select the amount you want to deduct from your account!\nYour current balance: ${User.getBalance()}")
            val viewDialog = layoutInflater.inflate(R.layout.dialog_transfer_money_activity, null, false)
            alertDialog.setView(viewDialog)
            val textView = viewDialog.findViewById<TextView>(R.id.buttonAddAmount)
            alertDialog.setPositiveButton("Ok") { _, _ -> }
            alertDialog.setNegativeButton("Cancel") { _, _ -> }

            val dialog = alertDialog.create()

            dialog.setOnShowListener(object : DialogInterface.OnShowListener{
                override fun onShow(p0: DialogInterface?) {
                    val okButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE)
                    okButton.setOnClickListener {
                        val stringValue = textView.text.toString()
                        if(stringValue.isEmpty()) {
                            dialog.dismiss()
                            return@setOnClickListener
                        }

                        val amount = textView.text.toString().toDouble()
                        try{
                            User.deductFromBalance(amount)
                            binding.balanceText.text = User.getBalance().toString()
                            dialog.dismiss()
                        }
                        catch (e: Exception){
                            textView.error = "Amount exceeds your balance!"
                        }
                    }
                }

            })
            dialog.show()
        }


        binding.searchView.suggestionsAdapter =
            android.widget.SimpleCursorAdapter(
                context,
                android.R.layout.simple_list_item_1,
                null,
                arrayOf(SearchManager.SUGGEST_COLUMN_TEXT_1),
                intArrayOf(
                    android.R.id.text1
                ), CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER
            )

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
            android.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query!!.length >= 2) {
                    return true
                }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText?.length!! >= 2) {
                    loadStockSearchItems(newText)

                } else {
                    activity?.runOnUiThread{
                        binding.searchView.suggestionsAdapter.changeCursor(null)
                    }
                }
                return true
            }

        })

        binding.searchView.setOnSuggestionListener(object :
            android.widget.SearchView.OnSuggestionListener {
            override fun onSuggestionSelect(position: Int): Boolean {

                val cursor = binding.searchView.suggestionsAdapter.getItem(position) as Cursor
                val searchName = cursor.getString(1)
                val bundle = Bundle()
                val symbol = searchName.substringBefore("-").trim()
                bundle.putString("symbol", symbol)
                findNavController().navigate(R.id.action_mainPageFragment_to_infoFragment,bundle)

                return true
            }

            override fun onSuggestionClick(position: Int): Boolean {
                return onSuggestionSelect(position)
            }

        })

        initRecyclerView()

    }

    private fun loadStocksOwned() {
        activity?.runOnUiThread{
            for (item in User.stocksOwned){
                adapter.addItem(item)
            }
        }
    }

    private fun loadStockSearchItems(keyword: String) {
        NetworkManager.getStockSearchItem(keyword)
            .enqueue(object : Callback<BestStockSearchMatches?> {
                override fun onResponse(
                    call: Call<BestStockSearchMatches?>,
                    response: Response<BestStockSearchMatches?>
                ) {
                    Log.d(ContentValues.TAG, "onResponse: " + response.code())
                    if (response.isSuccessful) {
                        val cursor: MatrixCursor
                        val sAutocompleteColNames = arrayOf(
                            BaseColumns._ID,
                            SearchManager.SUGGEST_COLUMN_TEXT_1
                        )
                        cursor = MatrixCursor(sAutocompleteColNames)

                        val matches = response.body()?.bestMatches

                        if (matches != null) {
                            var counter=0
                            for (index in matches.indices) {
                                if(matches[index].type!! != "Equity")
                                    continue
                                if (counter >= 4)
                                    break

                                counter++

                                val name: String = matches[index].name!!
                                val symbol: String = matches[index].symbol!!
                                val searchItem = "$symbol - $name"
                                val row = arrayOf<Any>(index, searchItem)
                                cursor.addRow(row)
                            }

                            activity?.runOnUiThread{
                                binding.searchView.suggestionsAdapter.changeCursor(cursor)
                            }
                        } else {
                            activity?.runOnUiThread{
                                binding.searchView.suggestionsAdapter.changeCursor(null)
                            }
                        }
                    } else {
                        Snackbar.make(binding.root,"Error: " + response.message(), Snackbar.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(
                    call: Call<BestStockSearchMatches?>,
                    throwable: Throwable
                ) {
                    throwable.printStackTrace()
                    Snackbar.make(binding.root,"Network request error occurred, check LOG",
                        Snackbar.LENGTH_LONG).show()
                }
            })
    }
}